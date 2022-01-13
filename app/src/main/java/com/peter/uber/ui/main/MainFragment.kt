package com.peter.uber.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.peter.uber.databinding.FragmentMainBinding
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.common.ConnectionResult
import com.peter.uber.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.gms.common.api.GoogleApiClient
import android.view.inputmethod.EditorInfo

import android.widget.TextView.OnEditorActionListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import android.text.Editable
import androidx.lifecycle.map
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import android.graphics.Bitmap
import android.graphics.Canvas

import androidx.core.content.ContextCompat

import android.graphics.drawable.Drawable
import android.net.Uri

import com.google.android.gms.maps.model.BitmapDescriptor
import kotlin.properties.Delegates
import android.widget.Toast
import android.widget.AdapterView

import android.widget.AdapterView.OnItemClickListener

class MainFragment : Fragment(), OnMapReadyCallback {
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by viewModel()
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isPermissionGranted by Delegates.notNull<Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        /** setup Map **/
        isPermissionGranted = isLocationPermissionGranted()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context as Activity)

        setupDrawer()

        viewModel.sourceLocations.observe(viewLifecycleOwner, Observer { list ->
            binding.sourceLocationEdt.setAdapter(
                ArrayAdapter(
                    context as Activity,
                    R.layout.support_simple_spinner_dropdown_item,
                    list.map { it.name })
            )

        })

        binding.sourceLocationEdt.setOnItemClickListener(OnItemClickListener { arg0, view, position, arg3 ->
            viewModel.setSource()
        })


        binding.destinationEdt.setOnItemClickListener(OnItemClickListener { arg0, view, position, arg3 ->
            viewModel.setDestination(viewModel.predictedLocations.value!![position])
        })

        viewModel.currentLocation.observe(viewLifecycleOwner, {
            if (it != null) {
                Log.i("CURRENT_LOCATION", it.latitude.toString() + " " + it.longitude.toString())
                updateMarkers()
            }
        })

        viewModel.sourceLocation.observe(viewLifecycleOwner, {
            if (it != null) {
                updateMarkers()
            }
        })


        viewModel.destinationLocation.observe(viewLifecycleOwner, {
            if (it != null)
                updateMarkers()
        })

        viewModel.predictedLocations.observe(viewLifecycleOwner, Observer { list ->
            binding.destinationEdt.setAdapter(
                ArrayAdapter(
                    context as Activity,
                    R.layout.support_simple_spinner_dropdown_item,
                    list.map { it.name })
            )

            viewModel.drivers.observe(viewLifecycleOwner, {
                if (it.isNotEmpty())
                    updateMarkers()
            })
        })

        binding.destinationEdt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    viewModel.predict(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        viewModel.bestDrivers.observe(viewLifecycleOwner, {list ->
            if (list.isNotEmpty()) {
                var driverNames:String = ""
                list.forEach{
                    driverNames.plus(it!!.name)
                }

                for (d in list) {
                    driverNames.plus(d!!.name)
                }
                Toast.makeText(context, driverNames, Toast.LENGTH_SHORT).show()
            }
        })
        return binding.root
    }

    private fun updateMarkers() {
        mMap.clear()
        /**DRAW CURRENT LOCATION**/
        if (viewModel.currentLocation.value != null)
            drawMarker(
                LatLng(
                    viewModel.currentLocation.value!!.latitude,
                    viewModel.currentLocation.value!!.longitude
                ), viewModel.currentLocation.value!!.name,
                false
            )

        /**DRAW SOURCE LOCATION**/
        if (viewModel.sourceLocation.value != null)
            drawMarker(
                LatLng(
                    viewModel.sourceLocation.value!!.latitude,
                    viewModel.sourceLocation.value!!.longitude
                ), viewModel.sourceLocation.value!!.name,
                false
            )

        /**DRAW DESTINATION LOCATION**/
        if (viewModel.destinationLocation.value != null)
            drawMarker(
                LatLng(
                    viewModel.destinationLocation.value!!.latitude,
                    viewModel.destinationLocation.value!!.longitude
                ), viewModel.destinationLocation.value!!.name,
                false
            )

        if (viewModel.drivers.value != null)
            for (d in viewModel.drivers.value!!) {
                drawMarker(
                    LatLng(
                        d.latitude,
                        d.longitude
                    ), d.name,
                    true
                )
            }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        if (isPermissionGranted) {
            map.isMyLocationEnabled = true
        }
        getLastKnownLocation()


    }

    private fun isLocationPermissionGranted(): Boolean {

        return if (ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1000

            )
            binding.mapView.onResume()
            binding.mapView.getMapAsync(this)
            false

        } else {
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setupDrawer() {

        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(
                context as Activity?,
                binding.myDrawerLayout,
                R.string.nav_open,
                R.string.nav_close
            )
        binding.myDrawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()
        (activity as AppCompatActivity?)!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity?)!!.supportActionBar!!.setHomeButtonEnabled(true)

        binding.toolbar.setNavigationOnClickListener(View.OnClickListener {
            if (binding.myDrawerLayout.isDrawerOpen(GravityCompat.START))
                binding.myDrawerLayout.closeDrawer(GravityCompat.START)
            else binding.myDrawerLayout.openDrawer(GravityCompat.START)
        })
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        drawMarker(latLng, title, false)
    }

    private fun drawMarker(latLng: LatLng, title: String, isTaxi: Boolean) {
        if (isTaxi)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(context?.let { BitmapFromVector(it, R.drawable.ic_car) })
                    .title(title)
            )
        else
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title)
            )
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        if (isPermissionGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        moveCamera(
                            LatLng(location.latitude, location.longitude),
                            10.0f,
                            "My Location"
                        )
                        viewModel.setCurrent(location.latitude, location.longitude)
                    }

                }
        }

    }

    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        // below line is use to generate a drawable.
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

        // below line is use to set bounds to our vector drawable.
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        // below line is use to create a bitmap for our
        // drawable which we have added.
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // below line is use to add bitmap in our canvas.
        val canvas = Canvas(bitmap)

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas)

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}


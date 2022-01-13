package com.peter.uber.service

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.peter.uber.BuildConfig
import com.peter.uber.pojo.Location
import java.util.*
import kotlin.collections.ArrayList

const val COLLECTION = "Source"
const val DRIVERS = "Drivers"
class FirestoreUber(private val application: Application) {

    private val db = Firebase.firestore

    fun fetchSourceLocation(liveData: MutableLiveData<List<Location>>) {
        db.collection(COLLECTION).get().addOnSuccessListener { result ->
            var array = ArrayList<Location>()
            for (document in result)
                if (document.exists()) {
                    val location: Location by lazy { Location("", 0.0, 0.0) }
                    location.longitude = document.get("longitude") as Double
                    location.latitude = document.get("latitude") as Double
                    location.name = document.get("name") as String
                    array.add(location)
                }
            liveData.postValue(array)
            Log.i("SIZE_DB", liveData.value!!.size.toString())

        }.addOnFailureListener { e ->
            Log.e("ERROR_FIRESTORE", e.toString())
        }

    }
    fun fetchDrivers(liveData: MutableLiveData<List<Location>>) {
        db.collection(DRIVERS).get().addOnSuccessListener { result ->
            var array = ArrayList<Location>()
            for (document in result)
                if (document.exists()) {
                    val location: Location by lazy { Location("", 0.0, 0.0) }
                    location.longitude = document.get("longitude") as Double
                    location.latitude = document.get("latitude") as Double
                    location.name = document.get("name") as String
                    array.add(location)
                }
            liveData.postValue(array)
            Log.i("SIZE_DB", liveData.value!!.size.toString())

        }.addOnFailureListener { e ->
            Log.e("ERROR_FIRESTORE", e.toString())
        }

    }

    fun addLocation(location: Location): Boolean {
        return try {
            db.collection(COLLECTION).add(location.hash())
                .addOnSuccessListener { documentReference ->
                    Log.d("ADD", documentReference.id)
                }
            true
        } catch (e: Exception) {
            false
        }

    }
    fun addDriver(location: Location): Boolean {
        return try {
            db.collection(DRIVERS).add(location.hash())
                .addOnSuccessListener { documentReference ->
                    Log.d("ADD", documentReference.id)
                }
            true
        } catch (e: Exception) {
            false
        }

    }

    fun predict(query: String, liveData: MutableLiveData<List<Location>>) {
        var tempList = ArrayList<Location>()
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.

        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setCountries("EG")
                .setOrigin(LatLng(30.085804770919268, 31.250889792780978))
                .setSessionToken(token)
                .setQuery(query)
                .build()

        if (!Places.isInitialized()) {
            Places.initialize(application, BuildConfig.MAPS_API_KEY, Locale.US);
        }
        val placesClient = Places.createClient(application)
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    Log.i("PLACE_ID", prediction.placeId)
                    Log.i("PLACE_TEXT", prediction.getPrimaryText(null).toString())
                    val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

// Construct a request object, passing the place ID and fields array.
                    val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                    if (!Places.isInitialized()) {
                        Places.initialize(application, BuildConfig.MAPS_API_KEY, Locale.US);
                    }
                    val placesClient = Places.createClient(application)

                    placesClient.fetchPlace(request)
                        .addOnSuccessListener { response: FetchPlaceResponse ->
                            val place = response.place
                            tempList.add(
                                Location(
                                    place.name!!,
                                    place.latLng!!.latitude,
                                    place.latLng!!.longitude
                                )
                            )
                            liveData.postValue(tempList)
                            Log.i("PREDICT_FIND_PLACE", "Place found: ${place.name}")
                        }.addOnFailureListener { exception: Exception ->
                            if (exception is ApiException) {
                                Log.e("ERROR_FIND_PLACE", "Place not found: ${exception.message}")
                            }
                        }
                }
                Log.i("PREDICT_SERVICE_SIZE", tempList.size.toString())
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e("PLACE_ERROR", "Place not found: " + exception.statusCode)
                }
            }
    }
}
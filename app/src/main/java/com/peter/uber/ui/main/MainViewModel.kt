package com.peter.uber.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.peter.uber.pojo.Location
import com.peter.uber.pojo.LocationDiff
import com.peter.uber.service.FirestoreUber
import kotlinx.coroutines.*
import kotlin.collections.ArrayList


class MainViewModel(application: Application, private val fireStoreUber: FirestoreUber) :
    AndroidViewModel(application) {
    private val _sourceLocations = MutableLiveData<List<Location>>()
    val sourceLocations: LiveData<List<Location>>
        get() = _sourceLocations

    var selectedSourceName = MutableLiveData<String>()

    private val _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?>
        get() = _currentLocation

    private val _sourceLocation = MutableLiveData<Location?>()
    val sourceLocation: LiveData<Location?>
        get() = _sourceLocation

    private val _destinationLocation = MutableLiveData<Location?>()
    val destinationLocation: LiveData<Location?>
        get() = _destinationLocation


    private val _predictedLocations = MutableLiveData<List<Location>>()
    val predictedLocations: LiveData<List<Location>>
        get() = _predictedLocations


    private val _drivers = MutableLiveData<List<Location>>()
    val drivers: LiveData<List<Location>>
        get() = _drivers

    private val _bestDrivers = MutableLiveData<List<LocationDiff?>>()
    val bestDrivers: LiveData<List<LocationDiff?>>
        get() = _bestDrivers


    fun setCurrent(lat: Double, lng: Double) {
        _currentLocation.value = Location("My location", lat, lng)
    }

    fun setSource() {

        for (l in _sourceLocations.value!!) {
            if (l.name == selectedSourceName.value)
                _sourceLocation.value = l
        }
        CoroutineScope(Dispatchers.IO).launch {
            getDrivers()
        }
    }

    fun setDestination(location: Location) {
        _destinationLocation.value = location
    }


    private suspend fun getLocations() = withContext(Dispatchers.IO) {
        launch { fireStoreUber.fetchSourceLocation(_sourceLocations) }
    }

    private suspend fun getDrivers() = withContext(Dispatchers.IO) {
        launch { fireStoreUber.fetchDrivers(_drivers) }
    }


    private suspend fun getPredictions(s: String) = withContext(Dispatchers.IO) {
        launch { fireStoreUber.predict(s, _predictedLocations) }
        Log.i("PREDICT_SIZE_MVM", _predictedLocations.value!!.size.toString())
    }

    fun predict(s: String) {
        CoroutineScope(Dispatchers.IO).launch {
            getPredictions(s)
        }
    }

    fun chooseTaxi() {
        if (_drivers.value!!.isNotEmpty() && _sourceLocation.value != null) {
            var listDiff = ArrayList<LocationDiff>()
            for (t in _drivers.value!!) {
                var diff = LocationDiff(t.name, t.latitude, t.longitude)
                diff.calc(_sourceLocation.value!!)
                listDiff.add(diff)
                Log.i("DIFF", t.name + ">>" + diff.diff)
            }
            listDiff.sortBy { it.diff }
            val filtered3 = listDiff.take(3)
            Log.i("FILTERED", filtered3.size.toString())
            _bestDrivers.postValue(filtered3)
            _drivers.postValue(filtered3)
        }
    }

    init {
        _bestDrivers.value = ArrayList()
        _sourceLocations.value = ArrayList()
        _predictedLocations.value = ArrayList()
        _drivers.value = ArrayList()
        _currentLocation.value = null
        _sourceLocation.value = null
        _destinationLocation.value = null
        CoroutineScope(Dispatchers.IO).launch {
            getLocations()
        }

        CoroutineScope(Dispatchers.IO).launch {
            getDrivers()
        }
        /**RUN THEM ONE TIME ONLY**/

        /**
        fireStoreUber.addLocation(Location("The Pyramids of Giza, Al Haram",29.977491346677553, 31.132495497005177))
        fireStoreUber.addLocation(Location("Smart Villages Development and Management Company",30.07863586807445, 31.013916816044066))
        fireStoreUber.addLocation(Location("The Egyptian Museum",30.047939386276184, 31.233862960220034))
        fireStoreUber.addLocation(Location("Luxor",25.68998689307632, 32.63972513472881))
        fireStoreUber.addLocation(Location("Aswan",24.091001580638963, 32.89976268519015))
        fireStoreUber.addLocation(Location("Al Ahly Club",30.04477533978394, 31.22119083933605))
        fireStoreUber.addLocation(Location("Helwan University",29.867783330990186, 31.31516321277406))
        fireStoreUber.addLocation(Location("Hurghada",27.280140365011473, 33.805008154727155))
        fireStoreUber.addLocation(Location("Siwa Oasis",29.20610861990626, 25.519970657585095))
        fireStoreUber.addLocation(Location("Alexandria",31.214052322887685, 29.912988936097552))
        fireStoreUber.addLocation(Location("The new administrative capital",30.004488621977952, 31.706541776223226))
         **/
        /**
        fireStoreUber.addDriver(Location("Driver 1", 29.898800813505137, 31.383640710707805))
        fireStoreUber.addDriver(Location("Driver 2", 29.80828055814414, 31.03894468830429))
        fireStoreUber.addDriver(Location("Driver 3", 30.11973108989483, 30.826626876583735))
        fireStoreUber.addDriver(Location("Driver 4", 30.25738658691177, 31.94723395507994))
        fireStoreUber.addDriver(Location("Driver 5", 30.190261599581724, 31.370422339285533))
        fireStoreUber.addDriver(Location("Driver 6", 29.565503870199276, 31.661741337161494))
        fireStoreUber.addDriver(Location("Driver 7", 30.50311439259819, 31.355380245960962))
        fireStoreUber.addDriver(Location("Driver 8", 29.353448579574746, 30.306185978565402))
        fireStoreUber.addDriver(Location("Driver 9", 30.232964520812104, 32.322177057801895))
        fireStoreUber.addDriver(Location("Driver 10", 29.525667339006326, 32.06399836373074))
        fireStoreUber.addDriver(Location("Driver 11", 29.580620034261038, 31.838778651881423))
        fireStoreUber.addDriver(Location("Driver 12", 31.142301849993252, 30.70169376738075))
        fireStoreUber.addDriver(Location("Driver 13", 30.095233913313265, 31.303195192929515))
        fireStoreUber.addDriver(Location("Driver 14", 30.055686971690324, 31.220753041649044))
         **/
    }


}
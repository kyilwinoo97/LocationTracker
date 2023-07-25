package com.locationtracker.mm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.locationtracker.mm.data.LocationRepository
import java.util.concurrent.Executors

class LocationUpdateViewModel(application: Application) : AndroidViewModel(application) {

        private val locationRepository = LocationRepository.getInstance(
            application.applicationContext,
            Executors.newSingleThreadExecutor()
        )
        val locationListLiveData = locationRepository.getLocations()

        fun startLocationUpdates() = locationRepository.startLocationUpdates()
        fun stopLocationUpdates() = locationRepository.stopLocationUpdates()



}
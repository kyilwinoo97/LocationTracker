package com.locationtracker.mm.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.locationtracker.mm.LocationService
import com.locationtracker.mm.hasPermission

class LocationTrackerManager private constructor(private  val context: Context){
    private  val  TAG = "LocationManager"

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val INTERVAL_3_MINUTES : Long = 3 * 60 * 1000

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates(){
        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            _receivingLocationUpdates.value = true
            startLocationService()

        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false

            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }


    @MainThread
    fun startLocationService(){
        ContextCompat.startForegroundService(context, Intent(context, LocationService::class.java))

    }
    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.value = false
    }



    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LocationTrackerManager? = null

        fun getInstance(context: Context): LocationTrackerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationTrackerManager(context).also { INSTANCE = it }
            }
        }
    }

}
package com.locationtracker.mm.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.locationtracker.mm.LocationUpdateBroadcastReceiver
import com.locationtracker.mm.hasPermission
import java.util.concurrent.TimeUnit

class LocationTrackerManager private constructor(private  val context: Context){
    private  val  TAG = "LocationManager"

    private val _receivingLocationUpdates: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val receivingLocationUpdates: LiveData<Boolean>
        get() = _receivingLocationUpdates
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest: LocationRequest = LocationRequest().apply {
        interval = TimeUnit.MINUTES.toMillis(3)
        fastestInterval = TimeUnit.MINUTES.toMillis(1)
        maxWaitTime = TimeUnit.MINUTES.toMillis(3)
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

    }

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
        intent.action = LocationUpdateBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
    }
    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates(){
        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            Log.d(TAG,"Location updates()")
            _receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)

        } catch (permissionRevoked: SecurityException) {
            _receivingLocationUpdates.value = false

            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }
    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        _receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
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
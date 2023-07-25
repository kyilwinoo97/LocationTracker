package com.locationtracker.mm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.provider.CallLog.Locations
import android.util.Log
import androidx.core.content.ContextCompat


class Restarter : BroadcastReceiver() {
    private val TAG = "Restarter"
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!LocationService().isRunning){
            Log.d(TAG,"Restart broadcast receive")
            ContextCompat.startForegroundService(context!!,Intent(context,LocationService::class.java))
        }

    }
}
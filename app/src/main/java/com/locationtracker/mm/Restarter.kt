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
        Log.d(TAG,"Restart broadcast receive")
        if (!LocationService().isRunning){
            Log.d(TAG,"Restart broadcast receive")
//            context!!.startService(Intent(context, LocationService::class.java))
            ContextCompat.startForegroundService(context!!,Intent(context,LocationService::class.java))
        }

//        if (!LocationService().isRunning){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context!!.startForegroundService(Intent(context, LocationService::class.java))
//            } else {
//                context!!.startService(Intent(context, LocationService::class.java))
//            }
//        }
    }
}
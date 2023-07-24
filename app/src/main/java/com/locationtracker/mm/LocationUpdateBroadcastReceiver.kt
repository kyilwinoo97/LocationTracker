//package com.locationtracker.mm
//
//import android.Manifest
//import android.app.ActivityManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Geocoder
//import android.location.Location
//import android.util.Log
//import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.tasks.CancellationToken
//import com.google.android.gms.tasks.CancellationTokenSource
//import com.google.android.gms.tasks.OnTokenCanceledListener
//import com.locationtracker.mm.data.LocationRepository
//import com.locationtracker.mm.data.db.LocationEntity
//import java.io.IOException
//import java.util.Date
//import java.util.Locale
//import java.util.concurrent.Executors
//
//class LocationUpdateBroadcastReceiver : BroadcastReceiver() {
//    private val TAG = "LocationBroadcast"
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        Log.d(TAG, "onReceive() context:$context, intent:$intent")
//        if (intent?.action == ACTION_PROCESS_UPDATES){
//
//            // getCurrentLocation
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,object :CancellationToken(){
//                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
//
//                override fun isCancellationRequested() = false
//            }).addOnSuccessListener { location: Location? ->
//                if (location == null)
//
//                else {
//                    var locationEntity = LocationEntity(
//                        latitude = location.latitude,
//                        longitude = location.longitude,
//                        foreground = isAppInForeground(context),
//                        date = Date(location.time),
//                        address = getAddress(location.latitude,location.longitude, context)
//                    )
//                    LocationRepository.getInstance(context!!, Executors.newSingleThreadExecutor())
//                        .addLocation(locationEntity)
//
//                }
//
//            }
//
//
//            //
////            LocationResult.extractResult(intent)?.let { locationResult ->
////                val locations = locationResult.locations.map { location ->
////                    Log.d(TAG,"Address ${getAddress(location.latitude,location.longitude,context!!)}  ")
////                    LocationEntity(
////                        latitude = location.latitude,
////                        longitude = location.longitude,
////                        foreground = isAppInForeground(context),
////                        date = Date(location.time),
////                        address = getAddress(location.latitude,location.longitude, context)
////                    )
////                }
////                if (locations.isNotEmpty()) {
////                    LocationRepository.getInstance(context!!, Executors.newSingleThreadExecutor())
////                        .addLocations(locations)
////                }
////            }
//
//        }
//
//    }
//
//
//    //only for debugging purpose
//    private fun isAppInForeground(context: Context): Boolean {
//        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses = activityManager.runningAppProcesses ?: return false
//
//        appProcesses.forEach { appProcess ->
//            if (appProcess.importance ==
//                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
//                appProcess.processName == context.packageName) {
//                return true
//            }
//        }
//        return false
//    }
//
//    private fun getAddress(latitude: Double, longitude: Double, context: Context): String {
//        val geocoder = Geocoder(context, Locale.getDefault())
//        var addr = ""
//        try {
//            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//            val obj = addresses?.get(0)!!
//             addr = obj.getAddressLine(0)
//
//            addr = addr + " , " +
//                    (obj.countryName ?: "")
//        }catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return  addr
//    }
//
//
//    companion object {
//        const val ACTION_PROCESS_UPDATES =
//            "com.locationtracker.mm.action." +
//                    "PROCESS_UPDATES"
//    }
//
//}
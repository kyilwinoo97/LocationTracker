package com.locationtracker.mm

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.locationtracker.mm.data.LocationRepository
import com.locationtracker.mm.data.db.LocationEntity
import com.locationtracker.mm.ui.MainActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.AbstractMap
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class LocationService: Service(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    @Volatile
    private var isRunning = false

    private var currentLocation: Location? = null
    private var speed = ""

    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private val PERMISSION_NOT_GRANTED_NOTIFICATION_ID = 75
    private var CHANNEL_ID = "com.locationtracker.mm"
    private var wakelock: PowerManager.WakeLock? = null
    internal var requestID = 10001
    private val fusedLocationProviderApi = LocationServices.FusedLocationApi
    private var thread: ContinousThread? = null

    private val SERVICE_INTERVAL: Long = 20000
    private val SERVICE_FASTEST: Long = 14000

    private val TAG = "LocationService"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "tracker:wakelock")

        thread = ContinousThread()

        init()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun init() {
        wakelock!!.acquire()

        if (!isRunning) {
            isRunning = true
            thread!!.start()
        }

        locationRequest = LocationRequest.create()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setInterval(SERVICE_INTERVAL)
        locationRequest!!.setFastestInterval(SERVICE_FASTEST)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build()
        try {
            googleApiClient!!.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun startForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelName = "LocationTracker"
        val chan = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Location Tracker")
            .setContentTitle("App is running in background")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            Log.v(TAG, location.accuracy.toString())
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProviderApi!!.requestLocationUpdates(googleApiClient!!, locationRequest!!, locationListener)
        } else {
            showNotification(
                applicationContext, PERMISSION_NOT_GRANTED_NOTIFICATION_ID,
                "Location is off",
                "Could you please enable location permission for this app?"
            )
        }
    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private fun showNotification(context: Context, notificationId: Int, title: String, content: String) {
        var mBuilder: NotificationCompat.Builder? = null
        val managerCompat = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = NotificationCompat.Builder(context)
        mBuilder.setContentTitle(title)
        mBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(content))
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        mBuilder.setContentText(content)
        mBuilder.setChannelId(CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "LocationTracker", NotificationManager.IMPORTANCE_HIGH)
            try {
                managerCompat!!.createNotificationChannel(channel)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        mBuilder.setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
        mBuilder.setLights(Color.WHITE, 2000, 3000)
        mBuilder.setDefaults(
            Notification.DEFAULT_LIGHTS
                    or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND
        )

        if (notificationId == PERMISSION_NOT_GRANTED_NOTIFICATION_ID) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            val pendingIntent = PendingIntent.getActivity(
                context, requestID,
                intent, PendingIntent.FLAG_IMMUTABLE
            )

            mBuilder.setContentIntent(pendingIntent)
        }

        managerCompat.notify(9, mBuilder.build())
    }

    internal inner class ContinousThread : Thread() {
        override fun run() {
            while (isRunning) {
                try {
                    sleep(1 * 60 * 1000)
                    saveLocation()

                } catch (e: InterruptedException) {
                    isRunning = false
                }

            }
        }

        private fun saveLocation() {
           var location = getLastKnownLocation()
            var context = applicationContext
            location?.let {
                var locationEntity = LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    foreground = isAppInForeground(context),
                    date = Date(),
                    address = getAddress(location.latitude,location.longitude, context)
                )
                LocationRepository.getInstance(context, Executors.newSingleThreadExecutor())
                    .addLocation(locationEntity)
            }

        }
        private fun isAppInForeground(context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false

            appProcesses.forEach { appProcess ->
                if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName == context.packageName) {
                    return true
                }
            }
            return false
        }

        private fun getAddress(latitude: Double, longitude: Double, context: Context): String {
            val geocoder = Geocoder(context, Locale.getDefault())
            var addr = ""
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val obj = addresses?.get(0)!!
                addr = obj.getAddressLine(0)

                addr = addr + " , " +
                        (obj.countryName ?: "")
            }catch (e: IOException) {
                e.printStackTrace()
            }
            return  addr
        }

        @SuppressLint("MissingPermission")
        private fun getLastKnownLocation(): Location? {
            val providers = locationManager!!.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val location = locationManager!!.getLastKnownLocation(provider) ?: continue

                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
            return bestLocation
        }

    }
}
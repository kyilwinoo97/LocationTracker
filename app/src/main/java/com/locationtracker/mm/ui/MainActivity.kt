package com.locationtracker.mm.ui

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.locationtracker.mm.LocationService
import com.locationtracker.mm.R
import com.locationtracker.mm.Restarter
import com.locationtracker.mm.data.db.LocationEntity
import com.locationtracker.mm.databinding.ActivityMainBinding
import com.locationtracker.mm.hasPermission
import com.locationtracker.mm.requestPermissionWithRationale
import com.locationtracker.mm.viewmodel.LocationUpdateViewModel


class MainActivity : AppCompatActivity() {

    private val locationUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(LocationUpdateViewModel::class.java)
    }
    private lateinit var binding: ActivityMainBinding
    private val TAG = "View"
    private val INTERVAL_3_MINUTES: Long = 3 * 60 * 1000

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
            "ACCESS FINE LOCATION",
            Snackbar.LENGTH_LONG
        )
            .setAction("OK") {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
            "Background access permission",
            Snackbar.LENGTH_LONG
        )
            .setAction("OK") {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }
    private lateinit var locationAdapter: LocationAdapter
    private var locationList = ArrayList<LocationEntity>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)

        setUpRecyclerView()

        //request permission
        requestFineLocationPermission()
        checkLocationEnable(context = this)

        locationUpdateViewModel.locationListLiveData.observe(
            this
        ) { locations ->
            locations?.let {
                if (locations.isEmpty()) {
                    binding.circularProgress.visibility = View.VISIBLE
                } else {
                    binding.circularProgress.visibility = View.GONE
                    locationList.clear()
                    locationList.addAll(locations)
                    locationAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Main Activity Destroy")
        LocationService().isRunning = false
//        val broadcastIntent = Intent(this, Restarter::class.java)
//        broadcastIntent.action = "com.locationtracker.mm.restart"
//        this.sendBroadcast(broadcastIntent)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this,Restarter::class.java)
        intent.action = "com.locationtracker.mm.restart";
        var pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + INTERVAL_3_MINUTES,
            pendingIntent
        )
        super.onDestroy()

    }

    private fun setUpRecyclerView() {
        locationAdapter = LocationAdapter(locationList, context = this)
        binding.apply {
            binding.recyclerViewLocation.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = locationAdapter
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.recyclerViewLocation.visibility = View.VISIBLE
                    requestBackgroundLocationPermission()
                    return
                }

            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.recyclerViewLocation.visibility = View.VISIBLE
                    locationUpdateViewModel.startLocationUpdates()
                    return
                }

        }
    }

    private fun requestFineLocationPermission() {
        val permissionApproved =
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            locationUpdateViewModel.startLocationUpdates()
            binding.recyclerViewLocation.visibility = View.VISIBLE
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar
            )
        }
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved =
            hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        if (permissionApproved) {
            locationUpdateViewModel.startLocationUpdates()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar
            )
        }
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

    }

    private fun checkLocationEnable(context: Context): Boolean {
        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
        var gps_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled) {
            AlertDialog.Builder(context)
                .setMessage(getString(R.string.turn_on_device_location))
                .setPositiveButton(
                    getString(R.string.ok)
                ) { paramDialogInterface, paramInt ->
                    context.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .setNegativeButton(getString(R.string.no_thanks), null)
                .show()
        }
        return gps_enabled
    }
}
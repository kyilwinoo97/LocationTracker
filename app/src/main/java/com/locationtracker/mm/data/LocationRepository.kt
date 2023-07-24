package com.locationtracker.mm.data

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.locationtracker.mm.data.db.LocationEntity
import com.locationtracker.mm.data.db.LocationTrackerDb
import java.util.concurrent.ExecutorService

class LocationRepository private constructor(
    private val locationTrackerDb: LocationTrackerDb,
    private val myLocationManager: LocationTrackerManager,
    private val executor: ExecutorService
) {

    val receivingLocationUpdates: LiveData<Boolean> = myLocationManager.receivingLocationUpdates


    private val locationDao = locationTrackerDb.locationTrackerDao()

    fun getLocations(): LiveData<List<LocationEntity>> = locationDao.getLocations()

    fun updateLocation(locationEntity: LocationEntity) {
        executor.execute {
            locationDao.updateLocation(locationEntity)
        }
    }

    fun  addLocation(locationEntity: LocationEntity){
        executor.execute{
            locationDao.addLocation(locationEntity)
        }
    }
    fun  addLocations(locations :List<LocationEntity>){
        executor.execute{
            locationDao.addLocations(locations)
        }
    }

    @MainThread
    fun startLocationUpdates() = myLocationManager.startLocationUpdates()

    @MainThread
    fun startForegroundLocationUpdate() = myLocationManager.startForegroundLocationUpdates()
    @MainThread
    fun  stopLocationUpdates() = myLocationManager.stopLocationUpdates()
    fun stopForegroundLocationUpdate()  = myLocationManager.stopForegroundLocationUpdate()

    companion object{
        @Volatile private var INSTANCE: LocationRepository? = null

        fun  getInstance(context: Context,executor: ExecutorService) : LocationRepository {
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: LocationRepository(
                    LocationTrackerDb.getInstance(context),
                    LocationTrackerManager.getInstance(context),
                    executor
                ).also { INSTANCE = it }
            }
        }
    }


}
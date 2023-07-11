package com.locationtracker.mm.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.UUID

@Dao
interface LocationTrackerDao {
    @Query("SELECT * FROM my_location_table ORDER BY date DESC")
    fun getLocations() : LiveData<List<LocationEntity>>

    @Query("SELECT * FROM my_location_table WHERE id=(:id)")
    fun getLocation(vararg id:UUID) : LiveData<LocationEntity>

    @Update
    fun updateLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocation(myLocationEntity: LocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<LocationEntity>)
}

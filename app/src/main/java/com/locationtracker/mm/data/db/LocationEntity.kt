package com.locationtracker.mm.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.util.Date
import java.util.UUID

@Entity(tableName = "my_location_table")
data class LocationEntity (
        @PrimaryKey var id: UUID = UUID.randomUUID(),
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var foreground: Boolean = true,
        var date: Date = Date(),
        var address: String = ""
        ){
        override fun toString(): String {
                val appState = if (foreground) {
                        "in app"
                } else {
                        "in BG"
                }

                return "$latitude, $longitude $appState on " +
                        "${DateFormat.getDateTimeInstance().format(date)}.\n"
        }
}
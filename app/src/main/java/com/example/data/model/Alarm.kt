package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey val id: String,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDaysString: String, // e.g. "1,2,3,4,5,6,7" where 1=Mon, ..., 7=Sun. Empty means a one-time alarm.
    val isEnabled: Boolean,
    val soundUri: String, // Ringtone or sound identifier
    val snoozeDuration: Int, // in minutes
    val vibrationEnabled: Boolean,
    val isSnoozed: Boolean = false,
    val snoozedUntilMillis: Long = 0L
) : Serializable

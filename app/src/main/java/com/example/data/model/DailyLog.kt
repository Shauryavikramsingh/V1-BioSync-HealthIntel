package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val glucose: Double? = null, // mg/dL
    val bpSystolic: Int? = null, // mmHg
    val bpDiastolic: Int? = null, // mmHg
    val spo2: Double? = null, // %
    val narrative: String? = null, // Voice transcript or text
    val audioPath: String? = null, // Local audio recording file path
    val photoPath: String? = null, // Local photo path / URI for notebook attachments
    val symptoms: String = "", // Comma-separated list of extracted symptoms (e.g., "nausea, dizziness")
    val driftScore: Double? = null, // Biophysiological Drift Score (0-100)
    val analysisResult: String? = null, // Detailed clinical summaries or notes
    val isVerified: Boolean = false
) : Serializable

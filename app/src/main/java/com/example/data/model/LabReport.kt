package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "lab_reports")
data class LabReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null, // Path to local captured image or predefined mockup sample
    val title: String = "Laboratory Document",
    val extractedText: String? = null, // Raw OCR text
    val parsedVitalsCode: String? = null, // Semi-structured: "Glucose: XX; BP: Sys/Dia; SpO2: YY"
    val status: String = "Captured", // "Captured", "Processing", "Extracted", "Failed"
    val notes: String? = null
) : Serializable

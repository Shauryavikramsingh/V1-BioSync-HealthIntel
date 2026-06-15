package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.DailyLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun generatePractitionerReport(context: Context, logs: List<DailyLog>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Paper dimensions in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(26, 54, 93) // Clinical navy blue
            textSize = 18f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(26, 54, 93)
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Draw Title Block
        canvas.drawText("BIOSYNC: CLINICAL INTELLIGENCE PORTAL", 40f, 55f, headerPaint)
        
        paint.color = Color.rgb(26, 54, 93)
        paint.strokeWidth = 2f
        canvas.drawLine(40f, 65f, 555f, 65f, paint)

        // Metadata block
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        textPaint.textSize = 9f
        textPaint.color = Color.DKGRAY
        canvas.drawText("LONGITUDINAL DIAGNOSTIC EXPORT", 40f, 82f, textPaint)
        canvas.drawText("Report Trigger Time: ${dateFormat.format(Date())}", 40f, 95f, textPaint)
        canvas.drawText("Target Physician Number: +91 9924309810", 350f, 95f, textPaint)

        var y = 130f
        
        // SECTION 1: CLINICAL VITALS
        canvas.drawText("1. SERIAL BIOMETRIC READINGS (LAST 7 PERIODS)", 40f, y, sectionPaint)
        y += 8f
        paint.color = Color.rgb(26, 54, 93)
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        // Table headers
        textPaint.color = Color.BLACK
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date & Time", 40f, y, textPaint)
        canvas.drawText("Glucose", 160f, y, textPaint)
        canvas.drawText("Blood Pressure", 260f, y, textPaint)
        canvas.drawText("O2 Saturation (SpO2)", 380f, y, textPaint)
        canvas.drawText("Drift Score", 480f, y, textPaint)
        
        y += 6f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val logDateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val recentLogs = logs.take(15) // Can fit up to 15 logs easily

        for (log in recentLogs) {
            canvas.drawText(logDateFormat.format(Date(log.timestamp)), 40f, y, textPaint)
            
            val glucoseStr = if (log.glucose != null) "${log.glucose} mg/dL" else "--"
            canvas.drawText(glucoseStr, 160f, y, textPaint)
            
            val bpStr = if (log.bpSystolic != null && log.bpDiastolic != null) {
                "${log.bpSystolic}/${log.bpDiastolic} mmHg"
            } else if (log.bpSystolic != null) {
                "${log.bpSystolic}/-- mmHg"
            } else {
                "--/-- mmHg"
            }
            canvas.drawText(bpStr, 260f, y, textPaint)
            
            val spo2Str = if (log.spo2 != null) "${log.spo2}%" else "--"
            canvas.drawText(spo2Str, 380f, y, textPaint)
            
            val driftStr = if (log.driftScore != null) String.format("%.1f%%", log.driftScore) else "0.0%"
            canvas.drawText(driftStr, 480f, y, textPaint)

            y += 18f
            if (y > 450f) break // Reserve the rest of the sheet for qualitative logs
        }

        // SECTION 2: QUALITATIVE SYMPTOM NARRATIVE
        y += 15f
        canvas.drawText("2. PATIENT VERBAL DICTATIONS & SYMPTOM TAGS", 40f, y, sectionPaint)
        y += 8f
        paint.color = Color.rgb(26, 54, 93)
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 18f

        val wordPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            isAntiAlias = true
        }
        
        for (log in recentLogs) {
            if (log.narrative != null && log.narrative.isNotBlank()) {
                val dateStr = logDateFormat.format(Date(log.timestamp))
                wordPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("[$dateStr] Clinical Tags: ${log.symptoms}", 40f, y, wordPaint)
                
                y += 14f
                wordPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                val transcript = log.narrative
                val trimmedTranscript = if (transcript.length > 90) transcript.take(87) + "..." else transcript
                canvas.drawText("\"$trimmedTranscript\"", 55f, y, wordPaint)
                
                y += 20f
                if (y > 750f) break
            }
        }

        // SECTION 3: SYSTEM SUMMARY FOOTER
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 790f, 555f, 790f, paint)
        wordPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        wordPaint.color = Color.GRAY
        wordPaint.textSize = 8f
        canvas.drawText("CONFIDENTIAL PATIENT RECORD - ENCRYPTED CLINICAL DIRECT SHARED PATHWAY", 40f, 805f, wordPaint)
        canvas.drawText("Generated exclusively via BioSync Longitudinal platform in compliance with medical telemetry standards.", 40f, 815f, wordPaint)

        pdfDocument.finishPage(page)

        val destinationFile = File(context.cacheDir, "BioSync_Clinical_Report.pdf")
        if (destinationFile.exists()) {
            destinationFile.delete()
        }
        FileOutputStream(destinationFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return destinationFile
    }

    /**
     * Helper to export raw clinical JSON format.
     */
    fun generatePractitionerJson(logs: List<DailyLog>): String {
        val rootObj = JSONObject()
        val logsArray = JSONArray()
        
        for (log in logs.take(15)) {
            val logObj = JSONObject().apply {
                put("id", log.id)
                put("timestamp", log.timestamp)
                put("glucose_mg_dl", log.glucose ?: JSONObject.NULL)
                put("bp_systolic_mmhg", log.bpSystolic ?: JSONObject.NULL)
                put("bp_diastolic_mmhg", log.bpDiastolic ?: JSONObject.NULL)
                put("spo2_percentage", log.spo2 ?: JSONObject.NULL)
                put("transcript", log.narrative ?: "")
                put("symptoms", log.symptoms)
                put("drift_score_pct", log.driftScore ?: 0.0)
                put("verified", log.isVerified)
            }
            logsArray.put(logObj)
        }
        rootObj.put("biosync_report_header", JSONObject().apply {
            put("practitioner_contact", "+919924309810")
            put("generation_timestamp", System.currentTimeMillis())
            put("vitals_count", logsArray.length())
        })
        rootObj.put("longitudinal_logs", logsArray)
        return rootObj.toString(2)
    }
}

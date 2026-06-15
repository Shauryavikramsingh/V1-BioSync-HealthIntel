package com.example.data.repository

import com.example.data.local.HealthDao
import com.example.data.model.DailyLog
import com.example.data.model.LabReport
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {
    val allLogs: Flow<List<DailyLog>> = healthDao.getAllLogs()
    val allReports: Flow<List<LabReport>> = healthDao.getAllReports()

    suspend fun getLogById(id: Int): DailyLog? = healthDao.getLogById(id)
    suspend fun getLast7DaysLogs(): List<DailyLog> = healthDao.getLast7DaysLogs()

    suspend fun insertLog(log: DailyLog): Long {
        val calculatedDrift = calculateDriftScore(
            glucose = log.glucose,
            bpSystolic = log.bpSystolic,
            bpDiastolic = log.bpDiastolic,
            spo2 = log.spo2
        )
        val logWithDrift = if (log.glucose == null && log.bpSystolic == null && log.spo2 == null) {
            log.copy(driftScore = null)
        } else {
            log.copy(driftScore = calculatedDrift)
        }
        return healthDao.insertLog(logWithDrift)
    }

    suspend fun deleteLog(log: DailyLog) = healthDao.deleteLog(log)
    suspend fun deleteLogById(id: Int) = healthDao.deleteLogById(id)

    suspend fun insertReport(report: LabReport): Long = healthDao.insertReport(report)
    suspend fun deleteReport(report: LabReport) = healthDao.deleteReport(report)
    suspend fun deleteReportById(id: Int) = healthDao.deleteReportById(id)

    fun calculateDriftScore(
        glucose: Double?,
        bpSystolic: Int?,
        bpDiastolic: Int?,
        spo2: Double?
    ): Double {
        var totalVariance = 0.0
        var parametersCount = 0

        if (glucose != null) {
            // Target: 95 mg/dL. Deviation limit: 45 mg/dL (70-140)
            val target = 95.0
            val maxAcceptableDeviation = 45.0
            val deviation = Math.abs(glucose - target)
            val score = (deviation / maxAcceptableDeviation) * 100.0
            totalVariance += Math.min(score, 100.0)
            parametersCount++
        }

        if (bpSystolic != null && bpDiastolic != null) {
            // Systolic target: 120 mmHg. Diastolic target: 80 mmHg.
            val targetSys = 120.0
            val maxDevSys = 30.0
            val devSys = Math.abs(bpSystolic - targetSys)
            val scoreSys = (devSys / maxDevSys) * 100.0

            val targetDia = 80.0
            val maxDevDia = 15.0
            val devDia = Math.abs(bpDiastolic - targetDia)
            val scoreDia = (devDia / maxDevDia) * 100.0

            val bpScore = (scoreSys + scoreDia) / 2.0
            totalVariance += Math.min(bpScore, 100.0)
            parametersCount++
        } else if (bpSystolic != null) {
            val targetSys = 120.0
            val maxDevSys = 30.0
            val devSys = Math.abs(bpSystolic - targetSys)
            val scoreSys = (devSys / maxDevSys) * 100.0
            totalVariance += Math.min(scoreSys, 100.0)
            parametersCount++
        }

        if (spo2 != null) {
            // SpO2 Target: 98%. Below 95% triggers heavy clinical drift scoring.
            val deviation = if (spo2 >= 95.0) {
                (98.0 - spo2) * 10.0 // 95% SpO2 results in 30% drift
            } else {
                30.0 + (95.0 - spo2) * 14.0 // 90% SpO2 results in 100% drift
            }
            totalVariance += Math.max(0.0, Math.min(deviation, 100.0))
            parametersCount++
        }

        if (parametersCount == 0) return 0.0
        return totalVariance / parametersCount
    }
}

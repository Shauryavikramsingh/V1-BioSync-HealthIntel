package com.example.data.local

import androidx.room.*
import com.example.data.model.DailyLog
import com.example.data.model.LabReport
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Int): DailyLog?

    @Query("SELECT * FROM daily_logs ORDER BY timestamp DESC LIMIT 7")
    suspend fun getLast7DaysLogs(): List<DailyLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog): Long

    @Delete
    suspend fun deleteLog(log: DailyLog)

    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    // --- Lab Reports ---
    @Query("SELECT * FROM lab_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<LabReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: LabReport): Long

    @Delete
    suspend fun deleteReport(report: LabReport)

    @Query("DELETE FROM lab_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)
}

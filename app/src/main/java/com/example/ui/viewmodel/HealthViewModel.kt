package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HealthDatabase
import com.example.data.model.DailyLog
import com.example.data.model.LabReport
import com.example.data.repository.HealthRepository
import com.example.data.gemini.GeminiManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthRepository
    val hasApiKey: Boolean

    // --- Persistent App UI & Accessibility Settings ---
    private val prefs = application.getSharedPreferences("notepad_settings", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode = _themeMode.asStateFlow()

    private val _fontScale = MutableStateFlow(prefs.getFloat("font_scale", 1.0f))
    val fontScale = _fontScale.asStateFlow()

    private val _contrastMode = MutableStateFlow(prefs.getString("contrast_mode", "normal") ?: "normal")
    val contrastMode = _contrastMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "en") ?: "en")
    val appLanguage = _appLanguage.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
        prefs.edit().putFloat("font_scale", scale).apply()
    }

    fun setContrastMode(mode: String) {
        _contrastMode.value = mode
        prefs.edit().putString("contrast_mode", mode).apply()
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", lang).apply()
    }

    init {
        val database = HealthDatabase.getDatabase(application)
        repository = HealthRepository(database.healthDao())
        hasApiKey = GeminiManager.hasValidApiKey()
        startTickerIfNeeded()
    }

    // --- Data Streams ---
    val logs: StateFlow<List<DailyLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<LabReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Biometric Input HUD Scratchpad State ---
    private val _glucoseInput = MutableStateFlow("")
    val glucoseInput = _glucoseInput.asStateFlow()

    private val _bpSystolicInput = MutableStateFlow("")
    val bpSystolicInput = _bpSystolicInput.asStateFlow()

    private val _bpDiastolicInput = MutableStateFlow("")
    val bpDiastolicInput = _bpDiastolicInput.asStateFlow()

    private val _spo2Input = MutableStateFlow("")
    val spo2Input = _spo2Input.asStateFlow()

    private val _narrativeInput = MutableStateFlow("")
    val narrativeInput = _narrativeInput.asStateFlow()

    // --- Search & Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredLogs: StateFlow<List<DailyLog>> = combine(logs, searchQuery) { logList, query ->
        if (query.isBlank()) {
            logList
        } else {
            logList.filter { log ->
                val symMatch = log.symptoms.contains(query, ignoreCase = true)
                val notesMatch = (log.narrative ?: "").contains(query, ignoreCase = true)
                val analysisMatch = (log.analysisResult ?: "").contains(query, ignoreCase = true)
                symMatch || notesMatch || analysisMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic UI States ---
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec = _recordingDurationSec.asStateFlow()

    private val _apiLoading = MutableStateFlow(false)
    val apiLoading = _apiLoading.asStateFlow()

    private val _ocrLoading = MutableStateFlow(false)
    val ocrLoading = _ocrLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    // --- Input Update Handlers ---
    fun setGlucose(value: String) { _glucoseInput.value = value }
    fun setBpSystolic(value: String) { _bpSystolicInput.value = value }
    fun setBpDiastolic(value: String) { _bpDiastolicInput.value = value }
    fun setSpo2(value: String) { _spo2Input.value = value }
    fun setNarrative(value: String) { _narrativeInput.value = value }
    fun setSearchQuery(value: String) { _searchQuery.value = value }

    fun clearInputs() {
        _glucoseInput.value = ""
        _bpSystolicInput.value = ""
        _bpDiastolicInput.value = ""
        _spo2Input.value = ""
        _narrativeInput.value = ""
        _statusMessage.value = null
    }

    fun dismissStatus() {
        _statusMessage.value = null
    }

    // --- Voice Recording State Handlers ---
    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
        if (_isRecording.value) {
            _recordingDurationSec.value = 0
            // Increment timer or record actual audio
        }
    }

    fun setRecordingDuration(sec: Int) {
        _recordingDurationSec.value = sec
    }

    // --- AI Operations ---
    
    /**
     * Sends the text in narrativeInput to Gemini to extract clinical symptoms and numbers
     */
    fun parseActiveNarrative() {
        val textToParse = _narrativeInput.value
        if (textToParse.isBlank()) {
            _statusMessage.value = "Narrative is empty. Speak or type something first."
            return
        }

        viewModelScope.launch {
            _apiLoading.value = true
            try {
                val result = GeminiManager.parseNarrative(textToParse)
                
                // Set the parsed vitals into the input fields if extracted
                result.glucose?.let { _glucoseInput.value = it.toString() }
                result.bpSystolic?.let { _bpSystolicInput.value = it.toString() }
                result.bpDiastolic?.let { _bpDiastolicInput.value = it.toString() }
                result.spo2?.let { _spo2Input.value = it.toString() }

                // Compose the symptoms into comma-separated text
                val processedSymptoms = result.symptoms.joinToString(", ")
                _statusMessage.value = "AI Extracted: ${result.symptoms.size} symptoms tagged."
                
                // Auto-save the parsed daily log
                val log = DailyLog(
                    glucose = result.glucose,
                    bpSystolic = result.bpSystolic,
                    bpDiastolic = result.bpDiastolic,
                    spo2 = result.spo2,
                    narrative = textToParse,
                    symptoms = processedSymptoms,
                    analysisResult = result.clinicalNotes
                )
                repository.insertLog(log)
                _narrativeInput.value = ""
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Narrative analysis failed: ${e.message}")
                _statusMessage.value = "Analysis failed: ${e.localizedMessage}"
            } finally {
                _apiLoading.value = false
            }
        }
    }

    /**
     * Takes custom recorded audio or voice and parses it
     */
    fun processVoiceDictation(transcript: String, localAudioPath: String? = null) {
        viewModelScope.launch {
            _apiLoading.value = true
            try {
                val result = GeminiManager.parseNarrative(transcript)
                val processedSymptoms = result.symptoms.joinToString(", ")

                val log = DailyLog(
                    glucose = result.glucose,
                    bpSystolic = result.bpSystolic,
                    bpDiastolic = result.bpDiastolic,
                    spo2 = result.spo2,
                    narrative = transcript,
                    audioPath = localAudioPath,
                    symptoms = processedSymptoms,
                    analysisResult = result.clinicalNotes
                )
                repository.insertLog(log)
                _statusMessage.value = "Voice Narrative captured and clinical logs extracted successfully."
            } catch (e: Exception) {
                _statusMessage.value = "Voice extraction breakdown: ${e.localizedMessage}"
            } finally {
                _apiLoading.value = false
            }
        }
    }

    /**
     * Inserts manual clinical metrics entered through the biometric input HUD.
     */
    fun saveManualLogs() {
        val gluc = _glucoseInput.value.toDoubleOrNull()
        val sys = _bpSystolicInput.value.toIntOrNull()
        val dia = _bpDiastolicInput.value.toIntOrNull()
        val o2 = _spo2Input.value.toDoubleOrNull()
        val text = _narrativeInput.value.ifBlank { "Manual biometric HUD update." }

        if (gluc == null && sys == null && dia == null && o2 == null) {
            _statusMessage.value = "Please enter at least one clinical metric before saving."
            return
        }

        viewModelScope.launch {
            _apiLoading.value = true
            try {
                // If there's an active narrative, parse with Gemini.
                // Otherwise, save instantly or run offline parsing.
                val processedSymptoms: String
                val clinicalNotes: String
                
                if (_narrativeInput.value.isNotBlank()) {
                    val parsed = GeminiManager.parseNarrative(_narrativeInput.value)
                    processedSymptoms = parsed.symptoms.joinToString(", ")
                    clinicalNotes = parsed.clinicalNotes
                } else {
                    processedSymptoms = "Biometrics Logged"
                    clinicalNotes = "Objective vital measurements registered manually."
                }

                val log = DailyLog(
                    glucose = gluc,
                    bpSystolic = sys,
                    bpDiastolic = dia,
                    spo2 = o2,
                    narrative = text,
                    symptoms = processedSymptoms,
                    analysisResult = clinicalNotes,
                    isVerified = true
                )
                repository.insertLog(log)
                _statusMessage.value = "Daily biometrics registered and verified."
                clearInputs()
            } catch (e: Exception) {
                _statusMessage.value = "Saving error: ${e.localizedMessage}"
            } finally {
                _apiLoading.value = false
            }
        }
    }

    /**
     * Conducts OCR reading and extraction on physical reports or photo testing strips.
     */
    fun processLabReportOCR(bitmap: Bitmap, imageUriStr: String? = null) {
        viewModelScope.launch {
            _ocrLoading.value = true
            try {
                val ocrResult = GeminiManager.parseLabImage(bitmap)
                
                // Register report in database
                val report = LabReport(
                    imageUrl = imageUriStr,
                    title = ocrResult.title,
                    extractedText = ocrResult.rawSnippet,
                    parsedVitalsCode = "Glucose: ${ocrResult.glucose ?: "N/A"} | BP: ${ocrResult.bpSystolic ?: "N/A"}/${ocrResult.bpDiastolic ?: "N/A"} | SpO2: ${ocrResult.spo2 ?: "N/A"}",
                    status = "Extracted",
                    notes = ocrResult.findings
                )
                repository.insertReport(report)

                // Also automatically append to Daily Vitals Logs for clinical continuity!
                if (ocrResult.glucose != null || ocrResult.bpSystolic != null || ocrResult.spo2 != null) {
                    val log = DailyLog(
                        glucose = ocrResult.glucose,
                        bpSystolic = ocrResult.bpSystolic,
                        bpDiastolic = ocrResult.bpDiastolic,
                        spo2 = ocrResult.spo2,
                        narrative = "Extracted automatically from photo lab document: ${ocrResult.title}",
                        symptoms = "Document Parsing",
                        analysisResult = ocrResult.findings,
                        isVerified = true
                    )
                    repository.insertLog(log)
                }

                _statusMessage.value = "OCR parsing success: ${ocrResult.title} added to health history."
            } catch (e: Exception) {
                _statusMessage.value = "OCR processing failed: ${e.localizedMessage}"
            } finally {
                _ocrLoading.value = false
            }
        }
    }

    fun saveDailyNoteText(
        text: String,
        idToUpdate: Int? = null,
        customTimestamp: Long? = null,
        photoPath: String? = null,
        audioPath: String? = null
    ) {
        viewModelScope.launch {
            _apiLoading.value = true
            try {
                val existingLog = idToUpdate?.let { repository.getLogById(it) }
                val finalTimestamp = customTimestamp ?: existingLog?.timestamp ?: System.currentTimeMillis()
                val finalPhoto = photoPath ?: existingLog?.photoPath
                val finalAudio = audioPath ?: existingLog?.audioPath

                val log = DailyLog(
                    id = idToUpdate ?: 0,
                    timestamp = finalTimestamp,
                    narrative = text,
                    photoPath = finalPhoto,
                    audioPath = finalAudio,
                    symptoms = "",
                    isVerified = true
                )
                repository.insertLog(log)
                _statusMessage.value = if (idToUpdate != null) {
                    "Notepad entry updated successfully!"
                } else {
                    "Notepad entry saved successfully!"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Failed to save note: ${e.localizedMessage}"
            } finally {
                _apiLoading.value = false
            }
        }
    }

    fun deleteLog(log: DailyLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun deleteReport(report: LabReport) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }

    // --- Dynamic Medication Alarms & Timers State Engine ---
    private var tickerJob: kotlinx.coroutines.Job? = null

    private val _activeTimers = MutableStateFlow<List<MedicationTimer>>(emptyList())
    val activeTimers = _activeTimers.asStateFlow()

    private val _ringingTimer = MutableStateFlow<MedicationTimer?>(null)
    val ringingTimer = _ringingTimer.asStateFlow()

    // --- Advanced Persistent SQLite Alarms Flow & States ---
    val alarms: StateFlow<List<com.example.data.model.Alarm>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _ringingAlarm = MutableStateFlow<com.example.data.model.Alarm?>(null)
    val ringingAlarm = _ringingAlarm.asStateFlow()

    private val triggeredAlarmsForMinute = mutableMapOf<String, Int>() // alarmId -> TriggeredMinute

    fun startTickerIfNeeded() {
        if (tickerJob != null && tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                
                // 1. Check & decrements legacy simulated countdown timers
                val currentList = _activeTimers.value
                if (currentList.isNotEmpty()) {
                    val updatedList = currentList.map { timer ->
                        if (timer.remainingSeconds > 0) {
                            val nextSec = timer.remainingSeconds - 1
                            if (nextSec == 0) {
                                val ringingTimerCopy = timer.copy(remainingSeconds = 0, isRinging = true)
                                _ringingTimer.value = ringingTimerCopy
                                ringingTimerCopy
                            } else {
                                timer.copy(remainingSeconds = nextSec)
                            }
                        } else {
                            timer
                        }
                    }
                    _activeTimers.value = updatedList
                }

                // 2. Checking advanced SQLite/Room-based persistent alarms
                checkAndTriggerAlarms()
            }
        }
    }

    private var activeRingtone: android.media.Ringtone? = null
    private var activeVibrator: android.os.Vibrator? = null

    private fun startRingAndVibration(alarm: com.example.data.model.Alarm) {
        stopRingAndVibration()

        try {
            val context = getApplication<Application>()
            val soundUri: Uri = when (alarm.soundUri) {
                "Chirp Tune" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "Zen Chimes" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }
                ringtone.play()
                activeRingtone = ringtone
            }
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to play ringtone", e)
        }

        if (alarm.vibrationEnabled) {
            try {
                val context = getApplication<Application>()
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = longArrayOf(0, 800, 800)
                        val effect = VibrationEffect.createWaveform(pattern, 0)
                        vibrator.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 800, 800), 0)
                    }
                    activeVibrator = vibrator
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to vibrate", e)
            }
        }
    }

    private fun stopRingAndVibration() {
        try {
            activeRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            activeRingtone = null
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to stop ringtone", e)
        }

        try {
            activeVibrator?.let {
                it.cancel()
            }
            activeVibrator = null
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to cancel vibration", e)
        }
    }

    private fun checkAndTriggerAlarms() {
        val calendar = java.util.Calendar.getInstance()
        val currentDayOfWeek = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> 1
            java.util.Calendar.TUESDAY -> 2
            java.util.Calendar.WEDNESDAY -> 3
            java.util.Calendar.THURSDAY -> 4
            java.util.Calendar.FRIDAY -> 5
            java.util.Calendar.SATURDAY -> 6
            java.util.Calendar.SUNDAY -> 7
            else -> 1
        }
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeMillis = System.currentTimeMillis()

        alarms.value.forEach { alarm ->
            if (alarm.isEnabled) {
                if (alarm.isSnoozed) {
                    if (currentTimeMillis >= alarm.snoozedUntilMillis) {
                        // Rescheduled snooze elapsed! Ring again!
                        viewModelScope.launch {
                            val updated = alarm.copy(isSnoozed = false, snoozedUntilMillis = 0L)
                            repository.insertAlarm(updated)
                        }
                        triggerRingingAlarm(alarm)
                    }
                } else {
                    // Check standard match
                    if (alarm.hour == currentHour && alarm.minute == currentMinute) {
                        val repeats = alarm.repeatDaysString.split(",")
                            .filter { it.isNotBlank() }
                            .mapNotNull { it.toIntOrNull() }
                        
                        val matchesDay = repeats.isEmpty() || repeats.contains(currentDayOfWeek)
                        if (matchesDay) {
                            val lastTriggeredMin = triggeredAlarmsForMinute[alarm.id]
                            if (lastTriggeredMin != currentMinute) {
                                triggeredAlarmsForMinute[alarm.id] = currentMinute
                                triggerRingingAlarm(alarm)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerRingingAlarm(alarm: com.example.data.model.Alarm) {
        if (_ringingAlarm.value?.id != alarm.id) {
            _ringingAlarm.value = alarm
            startRingAndVibration(alarm)
        }
    }

    fun insertAlarm(hour: Int, minute: Int, label: String, repeatDays: List<Int>, soundUri: String, snoozeDuration: Int, vibrationEnabled: Boolean) {
        viewModelScope.launch {
            val id = java.util.UUID.randomUUID().toString()
            val repeatDaysString = repeatDays.joinToString(",")
            val newAlarm = com.example.data.model.Alarm(
                id = id,
                hour = hour,
                minute = minute,
                label = label,
                repeatDaysString = repeatDaysString,
                isEnabled = true,
                soundUri = soundUri,
                snoozeDuration = snoozeDuration,
                vibrationEnabled = vibrationEnabled
            )
            repository.insertAlarm(newAlarm)
            startTickerIfNeeded()
            _statusMessage.value = "Registered new medication alarm: $label"
        }
    }

    fun toggleAlarmEnabled(alarm: com.example.data.model.Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled, isSnoozed = false)
            repository.insertAlarm(updated)
            if (!updated.isEnabled && _ringingAlarm.value?.id == alarm.id) {
                _ringingAlarm.value = null
                stopRingAndVibration()
            }
            _statusMessage.value = if (updated.isEnabled) "Alarm activated" else "Alarm deactivated"
        }
    }

    fun updateAlarm(alarm: com.example.data.model.Alarm) {
        viewModelScope.launch {
            repository.insertAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: com.example.data.model.Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            if (_ringingAlarm.value?.id == alarm.id) {
                _ringingAlarm.value = null
                stopRingAndVibration()
            }
            _statusMessage.value = "Alarm deleted successfully"
        }
    }

    fun snoozeAlarm(alarm: com.example.data.model.Alarm) {
        viewModelScope.launch {
            val addMillis = alarm.snoozeDuration * 60 * 1000L
            val updated = alarm.copy(
                isSnoozed = true,
                snoozedUntilMillis = System.currentTimeMillis() + addMillis
            )
            repository.insertAlarm(updated)
            _ringingAlarm.value = null // Dismiss ringing state
            stopRingAndVibration()
            _statusMessage.value = "Alarm '${alarm.label}' snoozed for ${alarm.snoozeDuration} minutes"
        }
    }

    fun dismissAlarm(alarm: com.example.data.model.Alarm) {
        viewModelScope.launch {
            // Check if alarm repeats. If it doesn't repeat (empty string), we disable it.
            val shouldDisable = alarm.repeatDaysString.isBlank()
            val updated = alarm.copy(
                isEnabled = !shouldDisable,
                isSnoozed = false,
                snoozedUntilMillis = 0L
            )
            repository.insertAlarm(updated)
            _ringingAlarm.value = null // Dismiss ringing state
            stopRingAndVibration()

            // Write medication / event compliance log to local Database securely
            val timeText = "%02d:%02d".format(alarm.hour, alarm.minute)
            val logText = "💊 ADVANCED ALARM COMPLETED: ${alarm.label}\nScheduled Time: $timeText\nSettings: Sound='${alarm.soundUri}', Vibration=${alarm.vibrationEnabled}\nCompliance Status: Cleared on time!"
            val log = DailyLog(
                narrative = logText,
                symptoms = "Medication Alarm Compliance",
                analysisResult = "Alarm '${alarm.label}' successfully taken & dismissed. Auto-logged securely.",
                isVerified = true
            )
            repository.insertLog(log)
            _statusMessage.value = "Logged compliance: '${alarm.label}' was taken successfully!"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRingAndVibration()
    }

    // --- Backward Compatible Simulated Countdowns ---
    fun addMedicationTimer(name: String, seconds: Int, note: String) {
        val id = java.util.UUID.randomUUID().toString()
        val totalSec = if (seconds <= 0) 10 else seconds
        val formatted = "%02d:%02d".format(totalSec / 60, totalSec % 60)
        val newTimer = MedicationTimer(
            id = id,
            medicationName = name,
            durationSeconds = totalSec,
            remainingSeconds = totalSec,
            note = note,
            totalDurationFormatted = formatted
        )
        _activeTimers.value = _activeTimers.value + newTimer
        startTickerIfNeeded()
    }

    fun deleteMedicationTimer(id: String) {
        _activeTimers.value = _activeTimers.value.filter { it.id != id }
    }

    fun clearTimerHistory() {
        _activeTimers.value = emptyList()
    }

    fun dismissRingingAlarm() {
        _ringingTimer.value = null
    }

    fun acknowledgeAndLogMedication(timer: MedicationTimer) {
        viewModelScope.launch {
            _ringingTimer.value = null
            _activeTimers.value = _activeTimers.value.filter { it.id != timer.id }
            
            val logText = "💊 MEDICATION TAKEN CHECK: ${timer.medicationName}\nInstructions/Notes: ${timer.note.ifBlank { "Taken on schedule" }}"
            val log = DailyLog(
                narrative = logText,
                symptoms = "Medication Intake",
                analysisResult = "Active physical compliance logged. Medication registered on schedule.",
                isVerified = true
            )
            repository.insertLog(log)
            _statusMessage.value = "Registered medication compliance log: ${timer.medicationName}!"
        }
    }
}

// Representing dynamic medication countdown alarms (Legacy / Backwards Compatible)
data class MedicationTimer(
    val id: String,
    val medicationName: String,
    val durationSeconds: Int,
    val remainingSeconds: Int,
    val note: String,
    val isRinging: Boolean = false,
    val totalDurationFormatted: String = ""
)


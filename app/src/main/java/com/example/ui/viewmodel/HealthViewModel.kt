package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
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

    init {
        val database = HealthDatabase.getDatabase(application)
        repository = HealthRepository(database.healthDao())
        hasApiKey = GeminiManager.hasValidApiKey()
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
}

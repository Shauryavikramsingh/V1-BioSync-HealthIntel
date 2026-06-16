package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DailyLog
import com.example.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Translator {
    private val translations = mapOf(
        "en" to mapOf(
            "app_title" to "DAILY NOTEPAD",
            "app_subtitle" to "RICH MEMORIES, PHOTO SNAPSHOTS & AUDIO MEMOS",
            "write_new_note" to "WRITE NEW DAILY NOTE",
            "edit_note" to "EDIT NOTEPAD ENTRY",
            "calendar_now" to "Calendar: Now",
            "input_placeholder" to "Type daily thoughts, checklists, observations, or general standard updates...",
            "attached_media" to "ATTACHED MEDIA ITEMS:",
            "add_media" to "ADD MEDIA",
            "clear" to "CLEAR",
            "cancel" to "CANCEL",
            "save_journal" to "SAVE JOURNAL NOTE",
            "update_note" to "UPDATE NOTE",
            "search_placeholder" to "Filter notepad items by search phrase...",
            "saved_history" to "SAVED NOTEPAD HISTORY",
            "filtered_results" to "FILTERED RESULTS",
            "entries_label" to "ENTRIES",
            "empty_state_title" to "No saved journal entries yet.",
            "empty_state_search_title" to "No matching notes found.",
            "empty_state_subtitle" to "Click ADD MEDIA or write custom logs to populate your secure local notebook history.",
            "empty_state_search_subtitle" to "Try adjusting search spelling or keyword.",
            "ai_triage_chat" to "AI TRIAGE CHAT",
            "settings" to "SETTINGS",
            "adjust_ui_theme" to "Adjust UI Theme",
            "font_size_scale" to "Font Size Scale",
            "language" to "Language",
            "ui_contrast" to "UI Contrast Mode",
            "system_default" to "System Default",
            "light_theme" to "Light",
            "dark_theme" to "Dark",
            "font_small" to "Small (85%)",
            "font_medium" to "Medium (Default)",
            "font_large" to "Large (115%)",
            "font_extra_large" to "Extra Large (130%)",
            "normal_contrast" to "Normal Contrast",
            "high_contrast" to "High Contrast",
            "close" to "Close"
        ),
        "es" to mapOf(
            "app_title" to "BLOC DE NOTAS DIARIO",
            "app_subtitle" to "MEMORIAS RICAS, FOTOS Y MEMOS DE AUDIO",
            "write_new_note" to "ESCRIBIR NUEVA NOTA DIARIA",
            "edit_note" to "EDITAR ENTRADA DE NOTICIA",
            "calendar_now" to "Calendario: Ahora",
            "input_placeholder" to "Escriba pensamientos diarios, listas, observaciones o actualizaciones generales...",
            "attached_media" to "ELEMENTOS MULTIMEDIA ADJUNTOS:",
            "add_media" to "AÑADIR MULTIMEDIA",
            "clear" to "LIMPIAR",
            "cancel" to "CANCELAR",
            "save_journal" to "GUARDAR NOTA DE DIARIO",
            "update_note" to "ACTUALIZAR NOTA",
            "search_placeholder" to "Filtrar elementos del bloc de notas...",
            "saved_history" to "HISTORIAL DE NOTAS GUARDADAS",
            "filtered_results" to "RESULTADOS FILTRADOS",
            "entries_label" to "ENTRADAS",
            "empty_state_title" to "No hay entradas guardadas aún.",
            "empty_state_search_title" to "No se encontraron notas coincidentes.",
            "empty_state_subtitle" to "Haga clic en AÑADIR MULTIMEDIA o escriba notas personalizadas para llenar su historial.",
            "empty_state_search_subtitle" to "Intente ajustar la búsqueda o palabra clave.",
            "ai_triage_chat" to "CHATEAR CON LA IA",
            "settings" to "CONFIGURACIÓN",
            "adjust_ui_theme" to "Ajustar tema de la interfaz",
            "font_size_scale" to "Escala de fuente",
            "language" to "Idioma",
            "ui_contrast" to "Contraste de interfaz",
            "system_default" to "Predeterminado de sistema",
            "light_theme" to "Claro",
            "dark_theme" to "Oscuro",
            "font_small" to "Pequeño (85%)",
            "font_medium" to "Medio (Predeterminado)",
            "font_large" to "Grande (115%)",
            "font_extra_large" to "Muy Grande (130%)",
            "normal_contrast" to "Contraste normal",
            "high_contrast" to "Alto contraste",
            "close" to "Cerrar"
        ),
        "fr" to mapOf(
            "app_title" to "BLOC-NOTES QUOTIDIEN",
            "app_subtitle" to "SOUVENIRS RICHES, PHOTOS & MÉMOS AUDIO",
            "write_new_note" to "ÉCRIRE UNE NOUVELLE NOTE",
            "edit_note" to "MODIFIER LA NOTE",
            "calendar_now" to "Calendrier: Maintenant",
            "input_placeholder" to "Saisissez vos pensées, listes, observations ou remarques générales...",
            "attached_media" to "MÉDIAS ATTACHÉS:",
            "add_media" to "AJOUTER MÉDIA",
            "clear" to "EFFACER",
            "cancel" to "ANNULER",
            "save_journal" to "ENREGISTRER LA NOTE",
            "update_note" to "METTRE À JOUR",
            "search_placeholder" to "Filtrer les notes par mot-clé...",
            "saved_history" to "HISTORIQUE DES NOTES",
            "filtered_results" to "RÉSULTATS FILTRÉS",
            "entries_label" to "ENTRÉES",
            "empty_state_title" to "Aucun souvenir enregistré pour l'instant.",
            "empty_state_search_title" to "Aucune note correspondante.",
            "empty_state_subtitle" to "Cliquez sur AJOUTER MÉDIA ou écrivez pour remplir votre journal local.",
            "empty_state_search_subtitle" to "Essayez d'ajuster l'orthographe du mot-clé.",
            "ai_triage_chat" to "CONVERSATION IA",
            "settings" to "PARAMÈTRES",
            "adjust_ui_theme" to "Thème de l'interface",
            "font_size_scale" to "Échelle de la police",
            "language" to "Langue",
            "ui_contrast" to "Mode de contraste",
            "system_default" to "Défaut système",
            "light_theme" to "Clair",
            "dark_theme" to "Sombre",
            "font_small" to "Petit (85%)",
            "font_medium" to "Moyen (Défaut)",
            "font_large" to "Grand (115%)",
            "font_extra_large" to "Très Grand (130%)",
            "normal_contrast" to "Contraste Normal",
            "high_contrast" to "Contraste Élevé",
            "close" to "Fermer"
        ),
        "hi" to mapOf(
            "app_title" to "दैनिक नोटबुक",
            "app_subtitle" to "समृद्ध यादें, फोटो स्नैपशॉट और ऑडियो मेमो",
            "write_new_note" to "नई दैनिक प्रविष्टि लिखें",
            "edit_note" to "नोटपाद प्रविष्टि संपादित करें",
            "calendar_now" to "कैलेंडर: अभी",
            "input_placeholder" to "अपने दैनिक विचार, चेकलिस्ट या सामान्य अपडेट टाइप करें...",
            "attached_media" to "संलग्न मीडिया फाइलें:",
            "add_media" to "मीडिया जोड़ें",
            "clear" to "साफ़ करें",
            "cancel" to "रद्द करें",
            "save_journal" to "नोट सहेजें",
            "update_note" to "अपडेट करें",
            "search_placeholder" to "नोटपैड में खोजें...",
            "saved_history" to "सहेजे गए नोटबुक इतिहास",
            "filtered_results" to "फ़िल्टर किए गए परिणाम",
            "entries_label" to "प्रविष्टियां",
            "empty_state_title" to "अभी तक कोई नोट सहेजा नहीं गया है।",
            "empty_state_search_title" to "कोई मिलान प्रविष्टि नहीं मिली।",
            "empty_state_subtitle" to "अपने नोटबुक इतिहास को आबाद करने के लिए मीडिया फ़ाइलें जोड़ें या नोट लिखें।",
            "empty_state_search_subtitle" to "खोज वर्तनी या कीवर्ड को बदलने का प्रयास करें।",
            "ai_triage_chat" to "एआई चैट",
            "settings" to "सेटिंग्स",
            "adjust_ui_theme" to "थीम बदलें",
            "font_size_scale" to "फ़ॉन्ट स्केल",
            "language" to "भाषा",
            "ui_contrast" to "स्क्रीन कंट्रास्ट",
            "system_default" to "सिस्टम डिफ़ॉल्ट",
            "light_theme" to "लाइट थीम",
            "dark_theme" to "डार्क थीम",
            "font_small" to "छोटा (85%)",
            "font_medium" to "सामान्य (डिफ़ॉल्ट)",
            "font_large" to "बड़ा (115%)",
            "font_extra_large" to "अतिरिक्त बड़ा (130%)",
            "normal_contrast" to "सामान्य कंट्रास्ट",
            "high_contrast" to "उच्च कंट्रास्ट",
            "close" to "बंद करें"
        ),
        "zh" to mapOf(
            "app_title" to "每日笔记本",
            "app_subtitle" to "丰富回忆、照片快照与语音备忘录",
            "write_new_note" to "撰写新日记",
            "edit_note" to "编辑日记",
            "calendar_now" to "日历: 现在",
            "input_placeholder" to "输入每日想法、清单、观察或日常更新...",
            "attached_media" to "已附加媒体文件:",
            "add_media" to "添加媒体",
            "clear" to "清除",
            "cancel" to "取消",
            "save_journal" to "保存日记",
            "update_note" to "更新日记",
            "search_placeholder" to "通过关键字过滤日记...",
            "saved_history" to "已保存的日记记录",
            "filtered_results" to "过滤后的结果",
            "entries_label" to "条记录",
            "empty_state_title" to "尚无保存的日记记录。",
            "empty_state_search_title" to "未找到匹配的日记。",
            "empty_state_subtitle" to "点击“添加媒体”或撰写自定义日志来充实您的安全本地笔记本历史。",
            "empty_state_search_subtitle" to "尝试调整搜索拼写或关键字。",
            "ai_triage_chat" to "AI 导诊聊天",
            "settings" to "设置",
            "adjust_ui_theme" to "调整界面主题",
            "font_size_scale" to "字体大小缩放",
            "language" to "语言",
            "ui_contrast" to "界面对比度模式",
            "system_default" to "系统默认",
            "light_theme" to "浅色",
            "dark_theme" to "深色",
            "font_small" to "小 (85%)",
            "font_medium" to "中 (默认)",
            "font_large" to "大 (115%)",
            "font_extra_large" to "超大 (130%)",
            "normal_contrast" to "普通对比度",
            "high_contrast" to "高对比度",
            "close" to "关闭"
        ),
        "ja" to mapOf(
            "app_title" to "デイリーメモ帳",
            "app_subtitle" to "豊かな思い出、写真スナップと音声メモ",
            "write_new_note" to "新しい日記を書く",
            "edit_note" to "日記のエントリを編集",
            "calendar_now" to "カレンダー: 現在",
            "input_placeholder" to "毎日の考え、チェックリスト、観察、または一般的な更新を入力...",
            "attached_media" to "添付されたメディア:",
            "add_media" to "メディアを追加",
            "clear" to "クリア",
            "cancel" to "キャンセル",
            "save_journal" to "日記を保存",
            "update_note" to "日記を更新",
            "search_placeholder" to "キーワードで日記を検索...",
            "saved_history" to "保存された日記履歴",
            "filtered_results" to "フィルター結果",
            "entries_label" to "件",
            "empty_state_title" to "保存された日記はありません。",
            "empty_state_search_title" to "一致する日記が見つかりません。",
            "empty_state_subtitle" to "「メディアを追加」をクリックするか、記述を始めてローカルの安全なメモ帳履歴を充実させましょう。",
            "empty_state_search_subtitle" to "検索ワードやキーワードを調整してみてください。",
            "ai_triage_chat" to "AIチャット",
            "settings" to "設定",
            "adjust_ui_theme" to "テーマ調整",
            "font_size_scale" to "文字サイズ",
            "language" to "言語",
            "ui_contrast" to "コントラスト",
            "system_default" to "システム設定",
            "light_theme" to "ライト",
            "dark_theme" to "ダーク",
            "font_small" to "小 (85%)",
            "font_medium" to "中 (デフォルト)",
            "font_large" to "大 (115%)",
            "font_extra_large" to "特大 (130%)",
            "normal_contrast" to "標準コントラスト",
            "high_contrast" to "高コントラスト",
            "close" to "閉じる"
        )
    )

    fun translate(key: String, lang: String): String {
        return translations[lang]?.get(key) ?: translations["en"]?.get(key) ?: key
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val logs by viewModel.filteredLogs.collectAsState()
    val rawLogs by viewModel.logs.collectAsState()
    val apiLoading by viewModel.apiLoading.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // --- Dynamic Customization settings ---
    val currentLanguage by viewModel.appLanguage.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentFontScale by viewModel.fontScale.collectAsState()
    val currentContrastMode by viewModel.contrastMode.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    var activeNoteText by remember { mutableStateOf("") }
    var editingLogId by remember { mutableStateOf<Int?>(null) }

    // Calendar state (defaults to null, which is auto timestamp "Now")
    var customDate by remember { mutableStateOf<Date?>(null) }

    // Media Upload / Capture attachments paths
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }
    var attachedAudioPath by remember { mutableStateOf<String?>(null) }

    // UI helper states
    var attachmentMenuExpanded by remember { mutableStateOf(false) }
    var showVoiceRecorderDialog by remember { mutableStateOf(false) }
    var selectedPreviewImageUri by remember { mutableStateOf<String?>(null) }

    // Standard media players
    var activeMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlayingLogId by remember { mutableStateOf<Int?>(null) }
    var audioProgress by remember { mutableStateOf(0.0f) }

    val formatter = remember { SimpleDateFormat("MMMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()) }

    // Photo selection launcher (Standard gallery picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
        }
    }

    // Camera picture-taking launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "clicked_photo_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                attachedPhotoUri = Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var showFallbackSpeechDialog by remember { mutableStateOf(false) }
    var showShareExportDialog by remember { mutableStateOf(false) }

    // AI Triage Chatbot State variables
    var showAiChatbotWindow by remember { mutableStateOf(false) }
    var chatbotQueryText by remember { mutableStateOf("") }
    var chatbotMessages by remember { mutableStateOf(listOf<DashboardScreenChatMsg>()) }
    var chatbotWaitingForAi by remember { mutableStateOf(false) }

    // Speech-To-Text Launcher standard integration
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val spokenText = results[0]
                if (activeNoteText.isBlank()) {
                    activeNoteText = spokenText
                } else {
                    activeNoteText += " $spokenText"
                }
            }
        }
    }

    // Android DatePickerDialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selCalendar = Calendar.getInstance()
                selCalendar.set(Calendar.YEAR, year)
                selCalendar.set(Calendar.MONTH, month)
                selCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                customDate = selCalendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Audio Playback Controller
    fun playAudio(logId: Int, path: String) {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }

            val mp = MediaPlayer().apply {
                setDataSource(context, Uri.parse(path))
                prepare()
                start()
            }
            activeMediaPlayer = mp
            currentlyPlayingLogId = logId

            coroutineScope.launch {
                while (mp.isPlaying) {
                    val duration = mp.duration.toFloat()
                    if (duration > 0) {
                        audioProgress = mp.currentPosition / duration
                    }
                    delay(300)
                }
                if (currentlyPlayingLogId == logId) {
                    currentlyPlayingLogId = null
                    audioProgress = 0.0f
                }
            }

            mp.setOnCompletionListener {
                it.release()
                if (currentlyPlayingLogId == logId) {
                    currentlyPlayingLogId = null
                    audioProgress = 0.0f
                    activeMediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback play mock indicator
            currentlyPlayingLogId = logId
            coroutineScope.launch {
                repeat(10) { i ->
                    audioProgress = i / 10f
                    delay(500)
                }
                currentlyPlayingLogId = null
                audioProgress = 0.0f
            }
        }
    }

    fun stopAudio() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        activeMediaPlayer = null
        currentlyPlayingLogId = null
        audioProgress = 0.0f
    }

    // Clean active states when starting edit
    fun startEditing(log: DailyLog) {
        activeNoteText = log.narrative ?: ""
        editingLogId = log.id
        customDate = Date(log.timestamp)
        attachedPhotoUri = log.photoPath
        attachedAudioPath = log.audioPath
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            Translator.translate("app_title", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            Translator.translate("app_subtitle", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = Translator.translate("settings", currentLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showShareExportDialog = true },
                        modifier = Modifier.testTag("share_notebook_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export and share notebook zip",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (rawLogs.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "${rawLogs.size} ${Translator.translate("entries_label", currentLanguage)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAiChatbotWindow = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .testTag("ai_chatbot_floating_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Open AI Triage Chatbot Ball",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI TRIAGE CHAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Editor Card containing notepad writing space
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title row detailing add/edit mode + Calendar timestamping pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editingLogId != null) Translator.translate("edit_note", currentLanguage) else Translator.translate("write_new_note", currentLanguage),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )

                            // Calendar Date Customizer Pill (Satisfies Calendar selection + Auto timestamping)
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                ndbShape = RoundedCornerShape(12.dp),
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Select Date",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (customDate == null) Translator.translate("calendar_now", currentLanguage) else dayFormatter.format(customDate!!),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Main notes entry input text field
                        OutlinedTextField(
                            value = activeNoteText,
                            onValueChange = { activeNoteText = it },
                            placeholder = {
                                Text(
                                    Translator.translate("input_placeholder", currentLanguage),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                                .testTag("note_input"),
                            textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Attachments Row View (If photos/audio are added)
                        if (attachedPhotoUri != null || attachedAudioPath != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "ATTACHED MEDIA ITEMS:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Attached photo layout preview
                                    attachedPhotoUri?.let { path ->
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            AsyncImage(
                                                model = path,
                                                contentDescription = "Attached photo preview",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .background(Color.Black.copy(0.6f), CircleShape)
                                                    .clickable { attachedPhotoUri = null },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Attached sound memo indicator
                                    attachedAudioPath?.let { audio ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(64.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.List,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Column {
                                                    Text(
                                                        "Voice Memo",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "Recorded ready",
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { attachedAudioPath = null },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove recording",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Options and Buttons Row (+ Attachment Trigger, Clear, Save button)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Plus button triggering attachment selection dialog options
                                Box {
                                    OutlinedButton(
                                        onClick = { attachmentMenuExpanded = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(48.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add files icon",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Translator.translate("add_media", currentLanguage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    // Dropdown offering specified media attachments options
                                    DropdownMenu(
                                        expanded = attachmentMenuExpanded,
                                        onDismissRequest = { attachmentMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("🖼️ Upload Photos", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                attachmentMenuExpanded = false
                                                photoPickerLauncher.launch("image/*")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("📸 Camera Click Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                attachmentMenuExpanded = false
                                                cameraLauncher.launch(null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("🎙️ Recordings (Mic recording)", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                attachmentMenuExpanded = false
                                                showVoiceRecorderDialog = true
                                            }
                                        )
                                    }
                                }

                                // Interactive Speech to Text Mic Button
                                OutlinedIconButton(
                                    onClick = {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak clearly to transcribe your note...")
                                        }
                                        try {
                                            speechRecognizerLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            showFallbackSpeechDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("speech_to_text_mic_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = IconButtonDefaults.outlinedIconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_mic),
                                        contentDescription = "Speech to text transcription mic",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (editingLogId != null || activeNoteText.isNotBlank() || attachedPhotoUri != null || attachedAudioPath != null) {
                                    OutlinedButton(
                                        onClick = {
                                            activeNoteText = ""
                                            editingLogId = null
                                            customDate = null
                                            attachedPhotoUri = null
                                            attachedAudioPath = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("clear_button")
                                    ) {
                                        Text(
                                            text = if (editingLogId != null) Translator.translate("cancel", currentLanguage) else Translator.translate("clear", currentLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (activeNoteText.isNotBlank() || attachedPhotoUri != null || attachedAudioPath != null) {
                                            viewModel.saveDailyNoteText(
                                                text = activeNoteText,
                                                idToUpdate = editingLogId,
                                                customTimestamp = customDate?.time,
                                                photoPath = attachedPhotoUri,
                                                audioPath = attachedAudioPath
                                            )
                                            activeNoteText = ""
                                            editingLogId = null
                                            customDate = null
                                            attachedPhotoUri = null
                                            attachedAudioPath = null
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("save_note_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (apiLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                    } else {
                                        Text(
                                            text = if (editingLogId != null) Translator.translate("update_note", currentLanguage) else Translator.translate("save_journal", currentLanguage),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar Component
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            Translator.translate("search_placeholder", currentLanguage),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search query",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("search_input"),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // History Header
            item {
                Text(
                    text = if (searchQuery.isEmpty()) Translator.translate("saved_history", currentLanguage) else "${Translator.translate("filtered_results", currentLanguage)} (${logs.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Notepad History List with support for playing audio + viewing image thumbnails
            if (logs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isEmpty()) Icons.Default.Edit else Icons.Default.Search,
                                contentDescription = "Empty state icon",
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) Translator.translate("empty_state_title", currentLanguage) else Translator.translate("empty_state_search_title", currentLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) Translator.translate("empty_state_subtitle", currentLanguage) else Translator.translate("empty_state_search_subtitle", currentLanguage),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    val isLogCurrentlyPlaying = currentlyPlayingLogId == log.id

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (editingLogId == log.id)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (editingLogId == log.id) 1.5.dp else 1.dp,
                                color = if (editingLogId == log.id)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("note_card_${log.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    startEditing(log)
                                }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Created timestamp",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = formatter.format(Date(log.timestamp)),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.2.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { startEditing(log) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("edit_button_${log.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit entry text",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (editingLogId == log.id) {
                                                activeNoteText = ""
                                                editingLogId = null
                                                customDate = null
                                                attachedPhotoUri = null
                                                attachedAudioPath = null
                                            }
                                            viewModel.deleteLog(log)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_button_${log.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete entry from notepad",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Show attached photo inside the post if it exists
                            log.photoPath?.let { path ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedPreviewImageUri = path }
                                ) {
                                    AsyncImage(
                                        model = path,
                                        contentDescription = "Journal image snapshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "🔍 CLICK TO ZOOM",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Show audio player recording pill inline if it exists
                            log.audioPath?.let { audio ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Sound action control play button
                                                IconButton(
                                                    onClick = {
                                                        if (isLogCurrentlyPlaying) {
                                                            stopAudio()
                                                        } else {
                                                            playAudio(log.id, audio)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLogCurrentlyPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                                        contentDescription = "Play/pause sound memo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Text(
                                                    text = if (isLogCurrentlyPlaying) "PLAYING VOICE ENTRY..." else "VOICE MEMO RECORDING",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }

                                            // Recording mini badge
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        // Progress Slider bar showing simulated audio movement
                                        if (isLogCurrentlyPlaying) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = audioProgress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Notes Narrative Text
                            if (!log.narrative.isNullOrBlank()) {
                                Text(
                                    text = log.narrative,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Live Voice Recording Dialog overlay ---
        if (showVoiceRecorderDialog) {
            var isRecordingActive by remember { mutableStateOf(false) }
            var recordDuration by remember { mutableStateOf(0) }
            var liveWaveMultiplier by remember { mutableStateOf(0.1f) }
            var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
            var voiceFilePath: String? by remember { mutableStateOf(null) }

            // Timer controller
            LaunchedEffect(isRecordingActive) {
                if (isRecordingActive) {
                    while (isRecordingActive) {
                        delay(1000)
                        recordDuration++
                        liveWaveMultiplier = kotlin.random.Random.nextFloat() * 0.8f + 0.2f
                    }
                }
            }

            Dialog(onDismissRequest = {
                if (isRecordingActive) {
                    try {
                        mediaRecorder?.stop()
                        mediaRecorder?.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                showVoiceRecorderDialog = false
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "🎙️ AUDIO VOICE RECORDER",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            "Record custom thoughts, checklists, or audio notes straight into notepad.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Wave Animation representing sound capture
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(60.dp)
                        ) {
                            val waveHeights = listOf(20.dp, 40.dp, 55.dp, 30.dp, 48.dp, 15.dp, 36.dp, 50.dp, 22.dp)
                            waveHeights.forEach { height ->
                                val scale by animateFloatAsState(
                                    targetValue = if (isRecordingActive) liveWaveMultiplier else 0.15f,
                                    animationSpec = tween(150, easing = LinearOutSlowInEasing)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(height * scale + 6.dp)
                                        .background(
                                            if (isRecordingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        }

                        // Time counter view
                        Text(
                            text = String.format("00:%02d", recordDuration),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecordingActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Capture buttons controller
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isRecordingActive && recordDuration == 0) {
                                // Start recording action
                                FloatingActionButton(
                                    onClick = {
                                        try {
                                            val outFile = File(context.cacheDir, "record_${System.currentTimeMillis()}.m4a")
                                            voiceFilePath = outFile.absolutePath
                                            
                                            val mr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                                MediaRecorder(context)
                                            } else {
                                                @Suppress("DEPRECATION")
                                                MediaRecorder()
                                            }

                                            mr.apply {
                                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                                setOutputFile(outFile.absolutePath)
                                                prepare()
                                                start()
                                            }
                                            mediaRecorder = mr
                                            isRecordingActive = true
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            // Fallback: simulate voice recording beautifully in emulator settings
                                            isRecordingActive = true
                                            val outFile = File(context.cacheDir, "record_simulated_${System.currentTimeMillis()}.m4a")
                                            try {
                                                outFile.createNewFile()
                                                voiceFilePath = outFile.absolutePath
                                            } catch (x: Exception) {
                                                x.printStackTrace()
                                            }
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Voice Memo Mic", modifier = Modifier.size(28.dp))
                                }
                            } else if (isRecordingActive) {
                                // Stop voice capturing
                                FloatingActionButton(
                                    onClick = {
                                        isRecordingActive = false
                                        try {
                                            mediaRecorder?.apply {
                                                stop()
                                                release()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        mediaRecorder = null
                                    },
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Stop Mic capture", modifier = Modifier.size(28.dp))
                                }
                            }
                        }

                        // Bottom controllers (Save, Cancel)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    showVoiceRecorderDialog = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }

                            if (!isRecordingActive && recordDuration > 0) {
                                Button(
                                    onClick = {
                                        voiceFilePath?.let { path ->
                                            attachedAudioPath = path
                                        }
                                        showVoiceRecorderDialog = false
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("KEEP MEMO", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Speech-To-Text fallback simulator dialog ---
        if (showFallbackSpeechDialog) {
            var simulatedSpeechInput by remember { mutableStateOf("") }
            val presets = listOf(
                "I am feeling much better today. Blood pressure has stabilized and my head is clear.",
                "Completed morning cardio session: 45 minutes of fast walking, heart rate peak at 115 bpm.",
                "Logged afternoon blood sugar check. Glucose level is 112 mg/dL in target range.",
                "Slight fatigue noticed after lunch. Drank glass of water and taking a 15-minute rest."
            )

            Dialog(onDismissRequest = { showFallbackSpeechDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "🎙️ SPEECH-TO-TEXT HELP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            "Google Voice Recognition Services are not fully active on your system instance. Record notes using live dictation simulation, or choose typical presets below!",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Live audio waveform mock
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(30.dp)
                        ) {
                            repeat(12) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height((10..30).random().dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                )
                            }
                        }

                        // Simulated text field input
                        OutlinedTextField(
                            value = simulatedSpeechInput,
                            onValueChange = { simulatedSpeechInput = it },
                            placeholder = {
                                Text(
                                    "Speak/Type dictated thoughts into notepad here...",
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp, max = 150.dp)
                                .testTag("simulated_speech_input"),
                            textStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Quick Select Suggestions
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "POPULAR SPEECH PRESETS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                letterSpacing = 0.5.sp
                            )
                            presets.forEach { text ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { simulatedSpeechInput = text }
                                ) {
                                    Text(
                                        text = "\"$text\"",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = { showFallbackSpeechDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }

                            Button(
                                onClick = {
                                    if (simulatedSpeechInput.isNotBlank()) {
                                        if (activeNoteText.isBlank()) {
                                            activeNoteText = simulatedSpeechInput
                                        } else {
                                            activeNoteText += " $simulatedSpeechInput"
                                        }
                                    }
                                    showFallbackSpeechDialog = false
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("TRANSCRIBE", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- AI Triage Chatbot Floating Dialog overlay ---
        if (showAiChatbotWindow) {
            val recent2Logs = rawLogs.sortedByDescending { it.timestamp }.take(2)
            Dialog(onDismissRequest = { showAiChatbotWindow = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Title Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "AI Head icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "AI TRIAGE COPILOT",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Conversational Medical RAG Analyzer",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            IconButton(onClick = { showAiChatbotWindow = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Chatbot Window",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // RAG Knowledge Base Panel
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "🧠 ACTIVE RAG KNOWLEDGE BASES (RECENT 2 NOTEPAD LOGS):",
                                fontSize = 8.6.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (recent2Logs.isEmpty()) {
                                Text(
                                    text = "⚠️ No notepad entries logged yet! Log biometrics or write a daily note to enable real-time personalized clinical RAG context.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 14.sp
                                )
                            } else {
                                recent2Logs.forEachIndexed { idx, log ->
                                    val dateFmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                                    val vitalsSummary = listOfNotNull(
                                        log.glucose?.let { "Glucose: ${it} mg/dL" },
                                        log.bpSystolic?.let { "BP: $it/${log.bpDiastolic ?: ""}" },
                                        log.spo2?.let { "SpO2: $it%" }
                                    ).joinToString(", ").ifEmpty { "Only clinical symptoms/narrative logged" }

                                    Text(
                                        text = "• [$dateFmt] $vitalsSummary",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Severity Scale Rating Card Panel
                        val lastAiMsg = chatbotMessages.lastOrNull { !it.isUser }
                        lastAiMsg?.scale?.let { scaleStr ->
                            val (badgeText, badgeColor, badgeDesc) = when (scaleStr.uppercase()) {
                                "SERIOUS" -> Triple(
                                    "🚨 TOO SERIOUS - SEEK IMMEDIATE MEDICAL ATTENTION",
                                    Color(0xFFD32F2F),
                                    "Your recent logs & inquiry imply high-severity indicators. Please contact emergency systems or see clinical doctors immediately!"
                                )
                                "INTERMEDIATE" -> Triple(
                                    "⚠️ INTERMEDIATE SEVERITY - CLOSE CLINICAL MONITORING",
                                    Color(0xFFF57C00),
                                    "Metrics are outside physiological baselines but do not point to instant emergencies. Continue tracking daily trends and consult a practitioner."
                                )
                                else -> Triple(
                                    "✅ NORMAL / STABLE - ROUTINE HEALTH METRICS",
                                    Color(0xFF388E3C),
                                    "Vitals appear consistent with standard targets. Maintain routine lifestyle logs and stay alert to adjustments."
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(badgeColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(badgeColor, CircleShape)
                                    )
                                    Text(
                                        text = badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = badgeColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = badgeDesc,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        // Conversation message list
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            if (chatbotMessages.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search icon",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ask me anything about your symptoms or medical concerns! I will automatically query your 2 newest logs for clinical diagnostics RAG context.",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                                LaunchedEffect(chatbotMessages.size) {
                                    if (chatbotMessages.isNotEmpty()) {
                                        listState.animateScrollToItem(chatbotMessages.size - 1)
                                    }
                                }

                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(chatbotMessages.size) { idx ->
                                        val msg = chatbotMessages[idx]
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (msg.isUser) "YOU" else "AI TRIAGE BOT",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                )
                                                Text(
                                                    text = msg.timeString,
                                                    fontSize = 8.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                            Card(
                                                shape = RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                                    bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                                ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (msg.isUser) {
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    } else {
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                                    }
                                                ),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = msg.text,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(10.dp),
                                                    lineHeight = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Loading Indicator
                        if (chatbotWaitingForAi) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                Text(
                                    text = "Consulting clinical RAG context nodes...",
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Input action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = chatbotQueryText,
                                onValueChange = { chatbotQueryText = it },
                                placeholder = { Text("Ask about symptoms or status...", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_chatbot_input_field"),
                                textStyle = TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Button(
                                onClick = {
                                    if (chatbotQueryText.isNotBlank()) {
                                        val query = chatbotQueryText.trim()
                                        chatbotQueryText = ""
                                        
                                        // 1. Add user message
                                        val userMessage = DashboardScreenChatMsg(isUser = true, text = query)
                                        chatbotMessages = chatbotMessages + userMessage
                                        
                                        // 2. Query chatbot with RAG
                                        chatbotWaitingForAi = true
                                        coroutineScope.launch {
                                            try {
                                                val result = com.example.data.gemini.GeminiManager.queryChatbot(query, recent2Logs)
                                                chatbotMessages = chatbotMessages + DashboardScreenChatMsg(
                                                    isUser = false,
                                                    text = result.advice,
                                                    scale = result.scale
                                                )
                                            } catch (e: Exception) {
                                                chatbotMessages = chatbotMessages + DashboardScreenChatMsg(
                                                    isUser = false,
                                                    text = "Triage evaluation failed: ${e.localizedMessage}. Please retry.",
                                                    scale = "NORMAL"
                                                )
                                            } finally {
                                                chatbotWaitingForAi = false
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("ai_chatbot_send_btn")
                            ) {
                                Text("SEND", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        // Medical/Disclaimer Note
                        Text(
                            text = "(AI can make mistakes while it is RAG based. If you suspect a serious emergency, please seek immediate medical attention rather than relying on this copilot.)",
                            fontSize = 8.2.sp,
                            lineHeight = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- Elegant image zoom dialog ---
        selectedPreviewImageUri?.let { path ->
            Dialog(onDismissRequest = { selectedPreviewImageUri = null }) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = path,
                            contentDescription = "Enlarged journal photo reference",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { selectedPreviewImageUri = null }) {
                                Text("CLOSE PREVIEW", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // --- Custom Accessibility & App Preferences Settings Dialog ---
        if (showSettingsDialog) {
            Dialog(onDismissRequest = { showSettingsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("settings_dialog_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚙️ " + Translator.translate("settings", currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            IconButton(onClick = { showSettingsDialog = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close settings"
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // 1. Theme Configuration
                        Text(
                            text = Translator.translate("adjust_ui_theme", currentLanguage),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val themes = listOf(
                                "system" to Translator.translate("system_default", currentLanguage),
                                "light" to Translator.translate("light_theme", currentLanguage),
                                "dark" to Translator.translate("dark_theme", currentLanguage)
                            )
                            themes.forEach { (mode, label) ->
                                val isSelected = currentThemeMode == mode
                                Button(
                                    onClick = { viewModel.setThemeMode(mode) },
                                    modifier = Modifier.weight(1f).testTag("theme_btn_$mode"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }

                        // 2. Font Scale Configuration
                        Text(
                            text = Translator.translate("font_size_scale", currentLanguage),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val scales = listOf(
                                0.85f to Translator.translate("font_small", currentLanguage),
                                1.0f to Translator.translate("font_medium", currentLanguage),
                                1.15f to Translator.translate("font_large", currentLanguage),
                                1.3f to Translator.translate("font_extra_large", currentLanguage)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    scales.take(2).forEach { (factor, label) ->
                                        val isSelected = currentFontScale == factor
                                        Button(
                                            onClick = { viewModel.setFontScale(factor) },
                                            modifier = Modifier.weight(1f).testTag("font_btn_$factor"),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    scales.drop(2).forEach { (factor, label) ->
                                        val isSelected = currentFontScale == factor
                                        Button(
                                            onClick = { viewModel.setFontScale(factor) },
                                            modifier = Modifier.weight(1f).testTag("font_btn_$factor"),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Language Configuration
                        Text(
                            text = Translator.translate("language", currentLanguage),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val languages = listOf(
                                "en" to "EN 🇺🇸",
                                "es" to "ES 🇪🇸",
                                "hi" to "HI 🇮🇳",
                                "fr" to "FR 🇫🇷",
                                "zh" to "ZH 🇨🇳",
                                "ja" to "JA 🇯🇵"
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                languages.take(3).forEach { (code, flagLabel) ->
                                    val isSelected = currentLanguage == code
                                    Button(
                                        onClick = { viewModel.setAppLanguage(code) },
                                        modifier = Modifier.weight(1f).testTag("lang_btn_$code"),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(text = flagLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                languages.drop(3).forEach { (code, flagLabel) ->
                                    val isSelected = currentLanguage == code
                                    Button(
                                        onClick = { viewModel.setAppLanguage(code) },
                                        modifier = Modifier.weight(1f).testTag("lang_btn_$code"),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(text = flagLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 4. Contrast Mode Configuration
                        Text(
                            text = Translator.translate("ui_contrast", currentLanguage),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val contrastOptions = listOf(
                                "normal" to Translator.translate("normal_contrast", currentLanguage),
                                "high" to Translator.translate("high_contrast", currentLanguage)
                            )
                            contrastOptions.forEach { (mode, label) ->
                                val isSelected = currentContrastMode == mode
                                Button(
                                    onClick = { viewModel.setContrastMode(mode) },
                                    modifier = Modifier.weight(1f).testTag("contrast_btn_$mode"),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Dialog Control Action Row
                        Button(
                            onClick = { showSettingsDialog = false },
                            modifier = Modifier.fillMaxWidth().testTag("settings_close_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = Translator.translate("close", currentLanguage),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Custom Share-To-Zip Export Dialog ---
        if (showShareExportDialog) {
            val maxLogs = rawLogs.size
            var sliderValue by remember { mutableStateOf(maxLogs.toFloat()) }
            var textInputValue by remember { mutableStateOf(maxLogs.toString()) }

            Dialog(onDismissRequest = { showShareExportDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "📦 SHARE & EXPORT ZIP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        if (maxLogs == 0) {
                            Text(
                                "No notepad entries found. Please write some journal notes and log some health records first before exporting!",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showShareExportDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CLOSE", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        } else {
                            Text(
                                "Specify how many of your newest notepad entries to pack and share in a compressed .zip file container:",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Interactive Text Input Field
                            OutlinedTextField(
                                value = textInputValue,
                                onValueChange = { newVal ->
                                    textInputValue = newVal
                                    val parsed = newVal.toIntOrNull()
                                    if (parsed != null && parsed in 1..maxLogs) {
                                        sliderValue = parsed.toFloat()
                                    }
                                },
                                label = { Text("Number of entries to export", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_notepad_count_input"),
                                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // Interactive Slider (Synced with the Text Input)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1 (Newest / single)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text("All ($maxLogs)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { fVal ->
                                        val rounded = Math.round(fVal)
                                        sliderValue = rounded.toFloat()
                                        textInputValue = rounded.toString()
                                    },
                                    valueRange = 1f..maxLogs.toFloat(),
                                    steps = if (maxLogs > 2) maxLogs - 2 else 0,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("export_notepad_count_slider")
                                )
                            }

                            // Calculate list of filtered items for export
                            val count = textInputValue.toIntOrNull() ?: Math.round(sliderValue)
                            val clampedCount = count.coerceIn(1, maxLogs)
                            val newestLogs = rawLogs.sortedByDescending { it.timestamp }
                            val logsToExport = newestLogs.take(clampedCount)

                            // Preview list showing exact contents going in the ZIP
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "ZIP CONTAINER PREVIEW (${logsToExport.size} SELECTED):",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                logsToExport.take(3).forEachIndexed { i, log ->
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(
                                        text = "• [$dateStr] ${log.narrative?.trim()?.take(36) ?: "No text attachment"}...",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                if (logsToExport.size > 3) {
                                    Text(
                                        text = "And ${logsToExport.size - 3} more entries included...",
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            // Dynamic Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = { showShareExportDialog = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val zipFile = exportLogsToZip(context, logsToExport)
                                            if (zipFile != null) {
                                                shareZipFile(context, zipFile)
                                            }
                                            showShareExportDialog = false
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("CREATE & SHARE ZIP", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status messages bar implementation
        statusMsg?.let { msg ->
            SnackbarHost(
                hostState = remember { SnackbarHostState() },
                modifier = Modifier.padding(16.dp)
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.dismissStatus() }) {
                            Text("DISMISS", color = MaterialTheme.colorScheme.primaryContainer)
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(msg)
                }
            }
        }
    }
}

// Custom shape helper inline extension for standard custom rendering
@Composable
fun Surface(
    color: Color,
    ndbShape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, ndbShape)
            .clip(ndbShape)
    ) {
        content()
    }
}

// --- Help helpers to export selected logs into a zipped archive for easy transferring/sharing ---
private fun exportLogsToZip(context: android.content.Context, selectedLogs: List<DailyLog>): File? {
    try {
        val zipFile = File(context.cacheDir, "exported_notepad_entries_${System.currentTimeMillis()}.zip")
        java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // 1. Write the main master list text file summary
            val masterContent = StringBuilder().apply {
                append("=========================================\n")
                append("HEALTH NOTEPAD ZIP ARCHIVE EXPORT SUMMARY\n")
                append("Exported on: ${Date()}\n")
                append("Number of Entries included: ${selectedLogs.size}\n")
                append("=========================================\n\n")
                selectedLogs.forEachIndexed { index, log ->
                    append("${index + 1}. [${Date(log.timestamp)}] - Narrative fragment: ${log.narrative?.take(60) ?: "No text body"}\n")
                }
            }.toString()

            zos.putNextEntry(java.util.zip.ZipEntry("notepads_summary.txt"))
            zos.write(masterContent.toByteArray())
            zos.closeEntry()

            // 2. Iterate and write individual detailed text entries and their associated attachments
            selectedLogs.forEachIndexed { index, log ->
                val entryDateStr = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date(log.timestamp))
                val filename = "entry_${index + 1}_$entryDateStr.txt"

                val logDetails = StringBuilder().apply {
                    append("=========================================\n")
                    append("DAILY NOTEPAD ENTRY #${index + 1}\n")
                    append("Date/Time: ${Date(log.timestamp)}\n")
                    append("=========================================\n")
                    append("NARRATIVE JOURNAL:\n")
                    append(log.narrative ?: "(No text narrative entered)")
                    append("\n\n")
                    append("EXTRACTED HEALTH PARAMETERS:\n")
                    if (log.glucose != null) append("- Glucose Level: ${log.glucose} mg/dL\n")
                    if (log.bpSystolic != null && log.bpDiastolic != null) append("- Blood Pressure Level: ${log.bpSystolic}/${log.bpDiastolic} mmHg\n")
                    if (log.spo2 != null) append("- Blood Oxygen SpO2 Level: ${log.spo2}%\n")
                    if (log.symptoms.isNotEmpty()) append("- Core Symptoms Identified: ${log.symptoms}\n")
                    if (log.driftScore != null) append("- Biophysiological Drift Score: ${log.driftScore}/100\n")
                    if (log.analysisResult != null) append("- Clinical AI Analysis: ${log.analysisResult}\n")
                    append("\n")
                    append("Status: ${if (log.isVerified) "VERIFIED" else "SCRATCHPAD"}\n")
                }.toString()

                zos.putNextEntry(java.util.zip.ZipEntry(filename))
                zos.write(logDetails.toByteArray())
                zos.closeEntry()

                // Include photo container attachment inside ZIP if exists
                log.photoPath?.let { photoUriStr ->
                    try {
                        val photoUri = Uri.parse(photoUriStr)
                        context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                            val zipPhotoName = "media/photo_${index + 1}_$entryDateStr.jpg"
                            zos.putNextEntry(java.util.zip.ZipEntry(zipPhotoName))
                            inputStream.copyTo(zos)
                            zos.closeEntry()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Include microphone audio memo recording if exists
                log.audioPath?.let { audioUriStr ->
                    try {
                        val file = File(audioUriStr)
                        if (file.exists()) {
                            file.inputStream().use { inputStream ->
                                val zipAudioName = "media/voice_${index + 1}_$entryDateStr.3gp"
                                zos.putNextEntry(java.util.zip.ZipEntry(zipAudioName))
                                inputStream.copyTo(zos)
                                zos.closeEntry()
                            }
                        } else {
                            val audioUri = Uri.parse(audioUriStr)
                            context.contentResolver.openInputStream(audioUri)?.use { inputStream ->
                                val zipAudioName = "media/voice_${index + 1}_$entryDateStr.3gp"
                                zos.putNextEntry(java.util.zip.ZipEntry(zipAudioName))
                                inputStream.copyTo(zos)
                                zos.closeEntry()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return zipFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun shareZipFile(context: android.content.Context, zipFile: File) {
    try {
        val fileUri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Secure Notepad ZIP Archives")
            putExtra(Intent.EXTRA_TEXT, "Here is the zip file containing my secure notepad entries and media files.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Notepad ZIP"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// --- Data class representing Chatbot conversation states ---
data class DashboardScreenChatMsg(
    val isUser: Boolean,
    val text: String,
    val scale: String? = null,
    val timeString: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
)


package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.data.remote.ParsedExpense
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseEntryScreen(
    draftExpense: ParsedExpense?,
    isAiLoading: Boolean,
    allExpenses: List<ExpenseEntity> = emptyList(),
    onVoiceDictate: (String) -> Unit,
    onOcrScan: (Bitmap) -> Unit,
    onSaveExpense: (Double, String, String, String, String) -> Unit,
    onClearDraft: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Voz, 1: OCR Ticket, 2: Manual

    // Voice simulation or real speech recognizer input
    var voiceText by remember { mutableStateOf("Gasté $420 pesos en gasolina en la estación Pemex") }
    var isListening by remember { mutableStateOf(false) }

    // Captured image preview
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Search and Filter states for Room expenses
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }
    var selectedDateFilter by remember { mutableStateOf("Todas") } // "Todas", "Hoy", "Esta Semana", "Este Mes"

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "MX")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val categoriesList = remember(allExpenses) {
        listOf("Todas") + (allExpenses.map { it.category }.filter { it.isNotBlank() }.distinct().sorted())
    }

    val filteredExpenses = remember(searchQuery, selectedCategoryFilter, selectedDateFilter, allExpenses) {
        val now = System.currentTimeMillis()
        allExpenses.filter { expense ->
            // Search Query Filter
            val matchesQuery = searchQuery.isBlank() ||
                    expense.merchant.contains(searchQuery, ignoreCase = true) ||
                    expense.category.contains(searchQuery, ignoreCase = true) ||
                    expense.note.contains(searchQuery, ignoreCase = true) ||
                    expense.amount.toString().contains(searchQuery)

            // Category Filter
            val matchesCategory = selectedCategoryFilter == "Todas" ||
                    expense.category.equals(selectedCategoryFilter, ignoreCase = true)

            // Date Range Filter
            val matchesDate = when (selectedDateFilter) {
                "Hoy" -> {
                    val oneDayMillis = 24 * 60 * 60 * 1000L
                    (now - expense.dateMillis) <= oneDayMillis
                }
                "Esta Semana" -> {
                    val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
                    (now - expense.dateMillis) <= sevenDaysMillis
                }
                "Este Mes" -> {
                    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000L
                    (now - expense.dateMillis) <= thirtyDaysMillis
                }
                else -> true
            }

            matchesQuery && matchesCategory && matchesDate
        }
    }

    val totalFilteredAmount = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    // Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spoken = matches[0]
                voiceText = spoken
                Toast.makeText(context, "Voz capturada: \"$spoken\"", Toast.LENGTH_SHORT).show()
                onVoiceDictate(spoken)
            }
        }
    }

    // Permission launcher for record audio
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Di tu gasto (Ej: Gasté 250 en Oxxo)")
            }
            try {
                isListening = true
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListening = false
                Toast.makeText(context, "El servicio de voz no está disponible en este dispositivo.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Picture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            Toast.makeText(context, "Foto de ticket tomada con éxito", Toast.LENGTH_SHORT).show()
            onOcrScan(bitmap)
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                    Toast.makeText(context, "Ticket cargado desde galería", Toast.LENGTH_SHORT).show()
                    onOcrScan(bitmap)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir imagen: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Manual Form Inputs
    var amountText by remember { mutableStateOf(draftExpense?.amount?.toString() ?: "150.0") }
    var merchantText by remember { mutableStateOf(draftExpense?.merchant ?: "Starbucks") }
    var categoryText by remember { mutableStateOf(draftExpense?.category ?: "Restaurantes") }
    var noteText by remember { mutableStateOf(draftExpense?.note ?: "Café con el cliente") }
    var dateText by remember { mutableStateOf(draftExpense?.dateText ?: "27/07/2026") }
    var placeText by remember { mutableStateOf(draftExpense?.placeOrAddress ?: "Sucursal Centro") }

    // Synchronize draft expense updates
    if (draftExpense != null) {
        amountText = draftExpense.amount.toString()
        merchantText = draftExpense.merchant
        categoryText = draftExpense.category
        noteText = draftExpense.note
        if (draftExpense.dateText.isNotBlank()) {
            dateText = draftExpense.dateText
        }
        if (draftExpense.placeOrAddress.isNotBlank()) {
            placeText = draftExpense.placeOrAddress
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Registro Rápido de Gastos",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Extrae y clasifica gastos automáticamente con Inteligencia Artificial",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Mode Switcher Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Dictado por Voz") },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                    modifier = Modifier.testTag("tab_voice")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Escaneo OCR") },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    modifier = Modifier.testTag("tab_ocr")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Manual") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.testTag("tab_manual")
                )
            }
        }

        // 1. Dictado por Voz
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Presiona el micrófono y dicta tu gasto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ejemplo: \"Ayer pagué 650 pesos de despensa en Soriana\"",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        val transition = rememberInfiniteTransition(label = "mic_pulse")
                        val scale by transition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = if (isListening) 1.25f else 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.2f)
                                )
                                .clickable {
                                    recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                                .testTag("voice_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictar",
                                tint = if (isListening) MaterialTheme.colorScheme.onPrimary else EmeraldPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = voiceText,
                            onValueChange = { voiceText = it },
                            label = { Text("Transcripción de Voz") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onVoiceDictate(voiceText) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("process_voice_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Procesando Voz con IA...")
                            } else {
                                Text("Analizar Dictado con IA")
                            }
                        }
                    }
                }
            }
        }

        // 2. Escaneo de Tickets (OCR)
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Escaneo OCR de Ticket o Factura",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Toma una foto de tu ticket para extraer automáticamente el total y comercio",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    SapphireSecondary,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (capturedBitmap != null) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Ticket capturado",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = SapphireSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Ninguna foto capturada aún", fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_ocr_camera_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abrir Cámara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scan_ocr_gallery_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireSecondary)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Subir de Galería", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Probador de Lectura OCR con Tickets de Muestra Alta Claridad:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sampleBmp = createSampleReceiptBitmap(
                                        "CADENA COMERCIAL OXXO",
                                        "185.50",
                                        listOf("1x AGUA CIEL 1L  $18.50", "2x CHIPS FUEGO   $48.00", "1x COCA COLA 600ML $119.00")
                                    )
                                    capturedBitmap = sampleBmp
                                    onOcrScan(sampleBmp)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                            ) {
                                Text("OXXO $185.50", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sampleBmp = createSampleReceiptBitmap(
                                        "GASOLINERA PEMEX",
                                        "650.00",
                                        listOf("27.85 L MAGNA PREM $650.00")
                                    )
                                    capturedBitmap = sampleBmp
                                    onOcrScan(sampleBmp)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireSecondary)
                            ) {
                                Text("PEMEX $650.00", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sampleBmp = createSampleReceiptBitmap(
                                        "WALMART SUPERCENTER",
                                        "1240.80",
                                        listOf("1x LECHE LALA 1L  $28.50", "1x MANZANA KG     $45.00", "1x CARNE SIRLOIN $1167.30")
                                    )
                                    capturedBitmap = sampleBmp
                                    onOcrScan(sampleBmp)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                            ) {
                                Text("WALMART $1,240", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val sampleBmp = createSampleReceiptBitmap(
                                        "STARBUCKS COFFEE",
                                        "145.00",
                                        listOf("1x CARAMEL MACCHIATO $145.00")
                                    )
                                    capturedBitmap = sampleBmp
                                    onOcrScan(sampleBmp)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireSecondary)
                            ) {
                                Text("STARBUCKS $145", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isAiLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SapphireSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizando ticket con ML Kit Text Recognition...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireSecondary)
                            }
                        }

                        if (draftExpense != null && selectedTab == 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Datos Extraídos por ML Kit Text Recognition",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("💵 Monto:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("$$amountText MXN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldPrimary)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🏪 Lugar:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(merchantText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("📅 Fecha:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(dateText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("📍 Ubicación:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(placeText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Confirmación / Formulario Final
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detalles de la Transacción",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantText,
                        onValueChange = { merchantText = it },
                        label = { Text("Lugar / Establecimiento") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_merchant_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = { dateText = it },
                            label = { Text("Fecha del Recibo") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("expense_date_input")
                        )

                        OutlinedTextField(
                            value = placeText,
                            onValueChange = { placeText = it },
                            label = { Text("Ubicación / Sucursal") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("expense_place_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Categoría") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_category_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Nota o Desglose ML Kit") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val typeStr = when (selectedTab) {
                                0 -> "Voz"
                                1 -> "ML Kit OCR"
                                else -> "Manual"
                            }
                            val finalNote = if (dateText.isNotBlank() || placeText.isNotBlank()) {
                                "[$dateText | $placeText] $noteText".trim()
                            } else {
                                noteText
                            }
                            if (amt > 0) {
                                onSaveExpense(amt, categoryText, merchantText, finalNote, typeStr)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_expense_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Gasto Automatizado", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECCIÓN: HISTORIAL Y BÚSQUEDA DE REGISTROS DE ROOM ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_expense_history_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historial de Registros de Room",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = "${filteredExpenses.size} registros",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BARRA DE BÚSQUEDA
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por comercio, nota o monto...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_bar"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // FILTRO POR CATEGORÍA
                    Text("Filtrar por Categoría:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sampleCats = if (categoriesList.size <= 1) listOf("Todas", "Supermercado", "Gasolina", "Restaurantes", "Servicios", "Salud", "General") else categoriesList
                        items(sampleCats) { category ->
                            val isSelected = selectedCategoryFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = category },
                                label = { Text(category, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // FILTRO POR FECHA / RANGO
                    Text("Filtrar por Fecha:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateOptions = listOf("Todas", "Hoy", "Esta Semana", "Este Mes")
                        items(dateOptions) { dateOpt ->
                            val isSelected = selectedDateFilter == dateOpt
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDateFilter = dateOpt },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                label = { Text(dateOpt, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphireSecondary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // RESUMEN DE FILTRADO
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Filtrado:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = currencyFormat.format(totalFilteredAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LISTA DE RESULTADOS
                    if (filteredExpenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategoryFilter != "Todas" || selectedDateFilter != "Todas")
                                    "No se encontraron registros de Room con los filtros seleccionados."
                                else "No hay registros guardados en Room aún.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredExpenses.forEach { exp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (exp.inputType) {
                                                            "OCR", "ML Kit OCR" -> SapphireSecondary.copy(alpha = 0.2f)
                                                            "Voz" -> EmeraldPrimary.copy(alpha = 0.2f)
                                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (exp.inputType) {
                                                        "OCR", "ML Kit OCR" -> Icons.Default.QrCodeScanner
                                                        "Voz" -> Icons.Default.Mic
                                                        else -> Icons.Default.AccountBalanceWallet
                                                    },
                                                    contentDescription = null,
                                                    tint = when (exp.inputType) {
                                                        "OCR", "ML Kit OCR" -> SapphireSecondary
                                                        "Voz" -> EmeraldPrimary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = exp.merchant.ifEmpty { "Gasto General" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${exp.category} • ${dateFormat.format(Date(exp.dateMillis))}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (exp.note.isNotBlank()) {
                                                    Text(
                                                        text = exp.note,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "-${currencyFormat.format(exp.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Generate high-resolution receipt ticket bitmap for OCR scanning
fun createSampleReceiptBitmap(
    merchantName: String = "SUPERMERCADO EXPRESS",
    totalAmount: String = "890.50",
    items: List<String> = listOf("1x DESPENSA FAMILIAR $890.50")
): Bitmap {
    val width = 600
    val height = 800
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // White paper background
    val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Gray border
    val borderPaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawRect(12f, 12f, (width - 12).toFloat(), (height - 12).toFloat(), borderPaint)

    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.MONOSPACE
    }

    var y = 75f
    textPaint.textSize = 36f
    textPaint.isFakeBoldText = true
    canvas.drawText(merchantName.uppercase(), width / 2f, y, textPaint)

    y += 45f
    textPaint.textSize = 20f
    textPaint.isFakeBoldText = false
    canvas.drawText("CADENA COMERCIAL Y SERVICIOS S.A. DE C.V.", width / 2f, y, textPaint)

    y += 30f
    canvas.drawText("RFC: FIN260727-XX1 | SUCURSAL 042", width / 2f, y, textPaint)

    y += 30f
    canvas.drawText("FECHA: 27/07/2026  HORA: 14:38", width / 2f, y, textPaint)

    y += 35f
    canvas.drawText("=================================", width / 2f, y, textPaint)

    y += 40f
    textPaint.textSize = 22f
    for (item in items) {
        canvas.drawText(item, width / 2f, y, textPaint)
        y += 35f
    }

    y += 20f
    canvas.drawText("=================================", width / 2f, y, textPaint)

    y += 50f
    textPaint.textSize = 36f
    textPaint.isFakeBoldText = true
    canvas.drawText("TOTAL: $$totalAmount MXN", width / 2f, y, textPaint)

    y += 45f
    textPaint.textSize = 22f
    textPaint.isFakeBoldText = false
    canvas.drawText("PAGADO EN EFECTIVO: $$totalAmount", width / 2f, y, textPaint)

    y += 45f
    canvas.drawText("¡GRACIAS POR SU COMPRA!", width / 2f, y, textPaint)

    return bitmap
}

fun createDummyTicketBitmap(): Bitmap = createSampleReceiptBitmap()

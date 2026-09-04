package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StatementImportEntity
import com.example.data.remote.ParsedStatementItem
import com.example.data.remote.ParsedStatementResult
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatementImportScreen(
    statementImports: List<StatementImportEntity> = emptyList(),
    parsingResult: ParsedStatementResult? = null,
    isParsing: Boolean = false,
    onParseDocument: (fileName: String, fileBytes: ByteArray?, mimeType: String, textContent: String) -> Unit = { _, _, _, _ -> },
    onConfirmImport: (result: ParsedStatementResult, fileName: String) -> Unit = { _, _ -> },
    onClearResult: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "US"))
    var selectedInstitution by remember { mutableStateOf("GBM+ Casa de Bolsa") }
    var inputMode by remember { mutableStateOf("archivo") } // "archivo" or "texto"
    var pasteText by remember { mutableStateOf("") }
    var currentFileName by remember { mutableStateOf("Estado_Cuenta_GBM_Junio2026.pdf") }

    // System File Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri) ?: "Estado_Cuenta_Importado.pdf"
            currentFileName = fileName
            val mimeType = context.contentResolver.getType(uri) ?: "*/*"
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                Toast.makeText(context, "Archivo cargado: $fileName", Toast.LENGTH_SHORT).show()
                onParseDocument(fileName, bytes, mimeType, "")
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer archivo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val institutions = listOf(
        "GBM+ Casa de Bolsa",
        "BBVA México (TC / Débito)",
        "Citibanamex",
        "Capital.com (Apalancamiento)",
        "Plata / Inversiones / Otro"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_from_import_btn")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Importar Estados de Cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "PDF, Excel (.xlsx), Imágenes de comprobantes y Estados Mensuales GBM / Bancos",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Institution Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Selecciona Institución o Empresa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        institutions.forEach { inst ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedInstitution == inst) SapphireSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .border(
                                        width = if (selectedInstitution == inst) 1.5.dp else 0.dp,
                                        color = if (selectedInstitution == inst) SapphireSecondary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedInstitution = inst }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(inst, fontSize = 13.sp, fontWeight = if (selectedInstitution == inst) FontWeight.Bold else FontWeight.Medium)
                                    if (selectedInstitution == inst) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Document Source Upload Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Cargar Estado de Cuenta (PDF / Excel / Imagen)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real System File Picker Button
                    Button(
                        onClick = { documentPickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("select_file_system_btn")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📁 Seleccionar Archivo de tu Dispositivo (PDF / Excel / Foto)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("O selecciona una muestra predeterminada:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // PDF Button
                        Button(
                            onClick = {
                                val fileName = if (selectedInstitution.contains("GBM")) "Estado_Cuenta_GBM_Junio2026.pdf" else "Estado_Cuenta_BBVA_Junio2026.pdf"
                                currentFileName = fileName
                                onParseDocument(fileName, null, "application/pdf", "")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("upload_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Subir PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }

                        // Excel Button
                        Button(
                            onClick = {
                                val fileName = "Reporte_Movimientos_Excel.xlsx"
                                currentFileName = fileName
                                onParseDocument(fileName, null, "application/vnd.ms-excel", "")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("upload_excel_btn")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel (.xlsx)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }

                        // Image / Screenshot Button
                        Button(
                            onClick = {
                                val fileName = "Foto_Estado_Cuenta.jpg"
                                currentFileName = fileName
                                onParseDocument(fileName, null, "image/jpeg", "")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("upload_image_btn")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto/Imagen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("O pega el texto copiado de tu estado de cuenta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        placeholder = { Text("Ej: Compra acciones NVDA $$12,800.00 en GBM+...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("statement_text_paste_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val name = "Texto_Pegado_${selectedInstitution.take(4)}.txt"
                            currentFileName = name
                            onParseDocument(name, null, "text/plain", pasteText)
                        },
                        enabled = pasteText.isNotEmpty() && !isParsing,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("parse_pasted_text_btn")
                    ) {
                        Text("Procesar Texto Pegado con Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Parsing Indicator
        if (isParsing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gemini AI analizando documento $currentFileName...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Extrayendo transacciones, precios de compra/venta y categorías de $selectedInstitution", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Extracted Result Preview
        parsingResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FileDownloadDone, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Resultado de Extracción Gemini AI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${result.institution} • ${result.statementPeriod}", fontSize = 11.sp, color = SapphireSecondary, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            TextButton(onClick = onClearResult) {
                                Text("Limpiar", fontSize = 11.sp, color = ExpenseRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tipo: ${result.statementType}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("Transacciones encontradas: ${result.items.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Monto Total / Saldo", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currencyFormat.format(result.totalAmountOrBalance), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EmeraldPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Movimientos listos para importar:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            result.items.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(item.description, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("${item.dateText} • Categoría/Activo: ${item.categoryOrAsset}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Text(currencyFormat.format(item.amount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (item.type == "EXPENSE") ExpenseRed else EmeraldPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onConfirmImport(result, currentFileName) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("confirm_import_to_db_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar e Importar ${result.items.size} Registros", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Historical Import Log
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Historial de Estados de Cuenta Importados", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (statementImports.isEmpty()) {
                    Text("No se han importado archivos previamente.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        statementImports.forEach { imp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(imp.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${imp.institution} • ${imp.statementPeriod}", fontSize = 11.sp, color = SapphireSecondary)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${imp.parsedTransactionsCount} registros", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                        Text(currencyFormat.format(imp.totalAmount), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

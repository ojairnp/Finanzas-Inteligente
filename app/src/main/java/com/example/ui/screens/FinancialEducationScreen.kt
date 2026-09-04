package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.BackupFrequency
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SapphireSecondary
import com.example.ui.viewmodel.BackupUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinancialEducationScreen(
    userEmail: String,
    is2FAEnabled: Boolean,
    backupUiState: BackupUiState,
    isDarkTheme: Boolean = true,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onToggle2FA: (Boolean) -> Unit,
    onPerformBackupNow: (android.content.Context) -> Unit = {},
    onPerformLocalBackupOnly: (android.content.Context) -> Unit = {},
    onSetBackupFrequency: (BackupFrequency) -> Unit,
    onRestoreFromBackup: () -> Unit,
    onRestoreFromLocalJson: (String) -> Unit = {},
    onRestoreFromLocalCsv: (String) -> Unit = {},
    onClearSampleData: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showLocalRestoreDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var pastedBackupContent by remember { mutableStateOf("") }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Vaciar Datos de Prueba", fontWeight = FontWeight.Bold, color = ExpenseRed) },
            text = {
                Text(
                    "Esta acción eliminará de forma permanente todas las transacciones, metas y posiciones de demostración para dejar la aplicación lista para tu uso real. ¿Deseas continuar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearSampleData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Sí, Vaciar Base de Datos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restaurar desde Nube Google Drive", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Esta acción descargará tu información guardada en Google Drive y Google Sheets y la restaurará en este dispositivo. ¿Deseas continuar?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        onRestoreFromBackup()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Restaurar desde Nube")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLocalRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showLocalRestoreDialog = false },
            title = { Text("Restauración Manual (JSON / CSV)", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Pega el contenido del archivo JSON o CSV de tu respaldo local o presiona restaurar si usaste el contenido más reciente:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pastedBackupContent,
                        onValueChange = { pastedBackupContent = it },
                        label = { Text("Contenido JSON o CSV de Respaldo") },
                        placeholder = { Text("Pega aquí el texto del archivo...") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val contentToRestore = pastedBackupContent.ifEmpty { backupUiState.lastLocalJsonContent }
                            showLocalRestoreDialog = false
                            if (contentToRestore.startsWith("[") || contentToRestore.startsWith("{")) {
                                onRestoreFromLocalJson(contentToRestore)
                            } else {
                                onRestoreFromLocalCsv(contentToRestore)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary)
                    ) {
                        Text("Restaurar Datos", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocalRestoreDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
                text = "Ajustes & Educación Financiera",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Respaldo automático local en CSV/JSON y en Google Drive/Sheets para máxima seguridad de tus datos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // --- LOCAL BACKUP (CSV + JSON) & GOOGLE DRIVE/SHEETS BACKUP CARD ---
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sistema de Respaldos (CSV, JSON & Google Drive)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Copia local diaria y sincronización con tu nube personal",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connected Google Account Status Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cuenta de Google Conectada", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = backupUiState.connectedEmail.ifEmpty { userEmail },
                                        fontSize = 12.sp,
                                        color = SapphireSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Sheets Database Link Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SapphireSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .background(SapphireSecondary.copy(alpha = 0.05f))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(backupUiState.spreadsheetUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) { /* Fallback */ }
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = SapphireSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Base de Datos en Google Sheets (5 Pestañas)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SapphireSecondary
                                    )
                                    Text(
                                        text = "Gastos, Metas, Tarjetas, Portafolio Inversiones, Activos y Pasivos",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir Sheets",
                                tint = SapphireSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Local Backup Status Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderZip, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Archivos de Respaldo Local (CSV + JSON)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = EmeraldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Ruta JSON: ${backupUiState.localJsonPath}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Ruta CSV: ${backupUiState.localCsvPath}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "• Frecuencia local: Generación automática diaria en el equipo.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Frequency selector for Cloud Upload (WhatsApp style: Diario, Semanal, Mensual, Manual)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Frecuencia de Subida a Google Drive / Sheets:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BackupFrequency.values().forEach { freq ->
                            val isSelected = backupUiState.backupFrequency == freq
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetBackupFrequency(freq) },
                                label = {
                                    Text(
                                        text = when (freq) {
                                            BackupFrequency.DAILY -> "Diario"
                                            BackupFrequency.WEEKLY -> "Semanal"
                                            BackupFrequency.MONTHLY -> "Mensual"
                                            BackupFrequency.MANUAL -> "Manual"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("freq_chip_${freq.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Backup metadata
                    val lastDateStr = if (backupUiState.lastBackupTimeMillis > 0) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(backupUiState.lastBackupTimeMillis))
                    } else "Nunca"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(10.dp)
                    ) {
                        Text("• Último respaldo: $lastDateStr", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("• Carpeta Drive: ${backupUiState.driveFolderPath}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Hoja Sheets: ${backupUiState.driveSheetName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Estado: ${backupUiState.statusMessage}", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onPerformBackupNow(context) },
                            enabled = !backupUiState.isBackupLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("backup_now_button")
                        ) {
                            if (backupUiState.isBackupLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generando Respaldo y Sincronizando...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Respaldar Ahora (Local CSV/JSON + Google Drive)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // MANUAL EXPORT & SHARE BUTTONS SECTION
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onPerformLocalBackupOnly(context) },
                                enabled = !backupUiState.isBackupLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireSecondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("local_backup_only_btn")
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar CSV/JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    shareBackupFiles(context, backupUiState.localJsonPath, backupUiState.localCsvPath)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_backup_files_btn")
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Compartir Archivos",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRestoreConfirmDialog = true },
                                enabled = !backupUiState.isBackupLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_cloud_backup_button")
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar Nube", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { showLocalRestoreDialog = true },
                                enabled = !backupUiState.isBackupLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_local_backup_button")
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cargar Local", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = { com.example.worker.LocalBackupWorker.triggerImmediateBackup(context) },
                            enabled = !backupUiState.isBackupLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_workmanager_export_button")
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar Tarea WorkManager (Notificación Toast / Exportación CSV/JSON)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Clear Sample Data for Real Mode Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Iniciar Prueba Real (Vaciar Datos de Ejemplo)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Limpia la base de datos de demostración para registrar únicamente tus finanzas reales.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_sample_data_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🧹 Vaciar Datos de Prueba y Empezar de Cero", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Theme & Appearance Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_preference_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Apariencia y Tema",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDarkTheme) SapphireSecondary.copy(alpha = 0.2f)
                                        else EmeraldPrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) SapphireSecondary else EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isDarkTheme) "Modo Oscuro (Activado)" else "Modo Claro (Activado)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkTheme) "Ideal para reducir fatiga visual con poca luz" else "Mayor contraste para entornos de luz intensa",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SapphireSecondary,
                                checkedTrackColor = SapphireSecondary.copy(alpha = 0.5f),
                                uncheckedThumbColor = EmeraldPrimary,
                                uncheckedTrackColor = EmeraldPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("toggle_dark_theme_switch")
                        )
                    }
                }
            }
        }

        // Security Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seguridad de la Cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Usuario Activo: $userEmail", fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Autenticación de Doble Factor (2FA)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Solicita código SMS/Correo antes de ingresar al panel", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Switch(
                            checked = is2FAEnabled,
                            onCheckedChange = onToggle2FA,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("toggle_2fa_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button")
                    ) {
                        Text("Cerrar Sesión Segura", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Educational Articles / Modules
        item {
            Text(
                text = "Módulos de Educación Financiera",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        item {
            EducationTipCard(
                title = "1. Regla del 50/30/20",
                description = "Destina el 50% de tus ingresos a necesidades primarias, 30% a gustos y 20% obligatoriamente al ahorro e inversión."
            )
        }

        item {
            EducationTipCard(
                title = "2. Interés Compuesto",
                description = "Reinvertir tus rendimientos permite que tus activos generen ganancias sobre ganancias, acelerando tu patrimonio exponencialmente."
            )
        }

        item {
            EducationTipCard(
                title = "3. Fondo de Emergencia Primario",
                description = "Mantén de 3 a 6 meses de tus gastos fijos en instrumentos de alta liquidez sin riesgo (ej. Cetes/Débito) antes de invertir en bolsa."
            )
        }
    }
}

@Composable
fun EducationTipCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Book, contentDescription = null, tint = SapphireSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
        }
    }
}

private fun shareBackupFiles(
    context: android.content.Context,
    jsonPath: String,
    csvPath: String
) {
    try {
        val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "FinanzaInteligente/Backups")
        val jsonFile = if (jsonPath.isNotBlank()) File(jsonPath) else File(backupDir, "FinanzaInteligente_Backup.json")
        val csvFile = if (csvPath.isNotBlank()) File(csvPath) else File(backupDir, "FinanzaInteligente_Respaldo_Completo.csv")

        val uris = ArrayList<Uri>()

        if (jsonFile.exists()) {
            val jsonUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                jsonFile
            )
            uris.add(jsonUri)
        }

        if (csvFile.exists()) {
            val csvUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            uris.add(csvUri)
        }

        if (uris.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Archivos de Respaldo FinanzaInteligente (CSV/JSON)")
                putExtra(Intent.EXTRA_TEXT, "Adjunto los archivos de respaldo CSV y JSON exportados desde FinanzaInteligente.")
            }
            context.startActivity(Intent.createChooser(intent, "Compartir archivos de respaldo"))
        } else {
            Toast.makeText(context, "No se encontraron archivos de respaldo. Presiona 'Exportar CSV/JSON' primero.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir archivos: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

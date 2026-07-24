package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    onToggle2FA: (Boolean) -> Unit,
    onPerformBackupNow: () -> Unit,
    onSetBackupFrequency: (BackupFrequency) -> Unit,
    onRestoreFromBackup: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restaurar Copia de Seguridad", fontWeight = FontWeight.Bold) },
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
                    Text("Restaurar Ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
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
                text = "Respaldo automático en Google Drive y Sheets para evitar pérdida de datos si desinstalas la app",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // --- GOOGLE DRIVE & SHEETS BACKUP CARD (WhatsApp Style) ---
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
                                    text = "Respaldo en Google Drive & Sheets",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Copia de seguridad anti-desinstalación",
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
                                        text = "Base de Datos en Google Sheets",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = SapphireSecondary
                                    )
                                    Text(
                                        text = "Hoja activa: 'FinanzaInteligente DB' (Gastos, Metas, Tarjetas)",
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

                    // Frequency selector (WhatsApp style: Diario, Semanal, Mensual, Manual)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Frecuencia de Respaldo Automático:",
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
                        Text("• Archivo Drive: FinanzaInteligente_Backup.json", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Estado: ${backupUiState.statusMessage}", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPerformBackupNow,
                            enabled = !backupUiState.isBackupLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("backup_now_button")
                        ) {
                            if (backupUiState.isBackupLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardando...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Respaldar Ahora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showRestoreConfirmDialog = true },
                            enabled = !backupUiState.isBackupLoading,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_backup_button")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
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

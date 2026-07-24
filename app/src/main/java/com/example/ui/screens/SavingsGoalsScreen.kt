package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SapphireSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoalEntity>,
    onAddDeposit: (SavingsGoalEntity, Double) -> Unit,
    onAddNewGoal: (String, Double, String, String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var depositText by remember { mutableStateOf("1000") }

    // Dialog state for new goal
    var newGoalTitle by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalCategory by remember { mutableStateOf("Viajes") }
    var newGoalDate by remember { mutableStateOf("15 Diciembre 2027") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Metas de Ahorro Programado",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Visualiza tu progreso y acelera tus objetivos de vida",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Featured Illustration for Japan Goal
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_savings_japan_1784842067887),
                            contentDescription = "Viaje a Japón",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Objetivo Destacado: Viaje a Japón 2027",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Fondo de Ahorro Automatizado",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = EmeraldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            items(goals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    currencyFormat = currencyFormat,
                    onDepositClick = { selectedGoalForDeposit = goal }
                )
            }
        }

        // FAB to add new goal
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_savings_goal_fab"),
            containerColor = EmeraldPrimary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Meta")
        }
    }

    // Deposit Dialog
    if (selectedGoalForDeposit != null) {
        val targetGoal = selectedGoalForDeposit!!
        AlertDialog(
            onDismissRequest = { selectedGoalForDeposit = null },
            title = { Text("Abonar a ${targetGoal.title}") },
            text = {
                Column {
                    Text("Ingresa el monto a transferir a este fondo:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text("Monto ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_amount_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAddDeposit(targetGoal, amt)
                        }
                        selectedGoalForDeposit = null
                    },
                    modifier = Modifier.testTag("confirm_deposit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Confirmar Abono")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalForDeposit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Add Goal Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Meta de Ahorro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newGoalTitle,
                        onValueChange = { newGoalTitle = it },
                        label = { Text("Título de la Meta") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_title_input")
                    )
                    OutlinedTextField(
                        value = newGoalTarget,
                        onValueChange = { newGoalTarget = it },
                        label = { Text("Monto Objetivo ($ MXN)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_goal_target_input")
                    )
                    OutlinedTextField(
                        value = newGoalCategory,
                        onValueChange = { newGoalCategory = it },
                        label = { Text("Categoría (Ej. Viajes, Casa)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGoalDate,
                        onValueChange = { newGoalDate = it },
                        label = { Text("Fecha Objetivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = newGoalTarget.toDoubleOrNull() ?: 50000.0
                        if (newGoalTitle.isNotEmpty()) {
                            onAddNewGoal(newGoalTitle, target, newGoalCategory, newGoalDate)
                        }
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("save_new_goal_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Crear Meta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    currencyFormat: NumberFormat,
    onDepositClick: () -> Unit
) {
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (goal.category) {
                                "Viajes" -> Icons.Default.Flight
                                "Seguridad" -> Icons.Default.Shield
                                "Patrimonio" -> Icons.Default.Home
                                else -> Icons.Default.Savings
                            },
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fecha objetivo: ${goal.targetDate}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "$percentage%",
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ahorrado: ${currencyFormat.format(goal.currentAmount)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Meta: ${currencyFormat.format(goal.targetAmount)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("deposit_goal_btn_${goal.id}")
                ) {
                    Text(
                        text = "+ Abonar",
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

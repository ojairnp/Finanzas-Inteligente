package com.example

import com.example.data.local.AssetLiabilityEntity
import com.example.data.local.CreditCardEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.StockPositionEntity
import com.example.data.remote.BackupPayload
import com.example.util.BackupConverterUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BackupConverterUtilsTest {

    @Test
    fun testJsonRoundTripSerialization() {
        val payload = BackupPayload(
            timestampMillis = 1700000000000L,
            userEmail = "test@example.com",
            expenses = listOf(
                ExpenseEntity(id = 1L, amount = 150.50, category = "Comida", merchant = "Supermercado", note = "Compra semanal")
            ),
            savingsGoals = listOf(
                SavingsGoalEntity(id = 1L, title = "Viaje", targetAmount = 5000.0, currentAmount = 1200.0, category = "Viajes", targetDate = "2026-12-31")
            ),
            creditCards = listOf(
                CreditCardEntity(id = 1L, name = "Oro", bank = "BBVA", creditLimit = 30000.0, currentBalance = 4500.0, cutOffDay = 15, paymentDueDay = 5)
            ),
            stockPositions = listOf(
                StockPositionEntity(id = 1L, symbol = "AAPL", companyName = "Apple Inc.", shares = 10.0, avgBuyPrice = 175.0, currentPrice = 190.0, targetSupportPrice = 160.0)
            ),
            assetsLiabilities = listOf(
                AssetLiabilityEntity(id = 1L, title = "Auto", amount = 120000.0, type = "ASSET", category = "Vehículo")
            )
        )

        val jsonString = BackupConverterUtils.toJson(payload)
        assertNotNull(jsonString)

        val parsedPayload = BackupConverterUtils.fromJson(jsonString)
        assertNotNull(parsedPayload)
        assertEquals("test@example.com", parsedPayload?.userEmail)
        assertEquals(1, parsedPayload?.expenses?.size)
        assertEquals(150.50, parsedPayload?.expenses?.get(0)?.amount ?: 0.0, 0.01)
        assertEquals("Comida", parsedPayload?.expenses?.get(0)?.category)
        assertEquals(1, parsedPayload?.savingsGoals?.size)
        assertEquals(1, parsedPayload?.creditCards?.size)
        assertEquals(1, parsedPayload?.stockPositions?.size)
        assertEquals(1, parsedPayload?.assetsLiabilities?.size)
    }

    @Test
    fun testCsvRoundTripSerialization() {
        val payload = BackupPayload(
            timestampMillis = 1700000000000L,
            userEmail = "test@example.com",
            expenses = listOf(
                ExpenseEntity(id = 1L, amount = 99.99, category = "Entretenimiento", merchant = "Cine", note = "Boletos de pelicula")
            ),
            savingsGoals = listOf(
                SavingsGoalEntity(id = 2L, title = "Fondo de Emergencia", targetAmount = 10000.0, currentAmount = 2500.0, category = "Ahorro", targetDate = "2027-01-01")
            ),
            creditCards = listOf(
                CreditCardEntity(id = 3L, name = "Platinum", bank = "Santander", creditLimit = 50000.0, currentBalance = 8000.0, cutOffDay = 12, paymentDueDay = 2)
            ),
            stockPositions = listOf(
                StockPositionEntity(id = 4L, symbol = "NVDA", companyName = "NVIDIA Corp", shares = 5.0, avgBuyPrice = 450.0, currentPrice = 1200.0, targetSupportPrice = 400.0)
            ),
            assetsLiabilities = listOf(
                AssetLiabilityEntity(id = 5L, title = "Hipoteca", amount = 850000.0, type = "LIABILITY", category = "Bienes Raíces")
            )
        )

        val csvString = BackupConverterUtils.toCombinedCsv(payload)
        assertNotNull(csvString)

        val parsedPayload = BackupConverterUtils.fromCsv(csvString)
        assertNotNull(parsedPayload)
        assertEquals(1, parsedPayload.expenses.size)
        assertEquals(99.99, parsedPayload.expenses[0].amount, 0.01)
        assertEquals("Entretenimiento", parsedPayload.expenses[0].category)
        assertEquals("Cine", parsedPayload.expenses[0].merchant)

        assertEquals(1, parsedPayload.savingsGoals.size)
        assertEquals("Fondo de Emergencia", parsedPayload.savingsGoals[0].title)

        assertEquals(1, parsedPayload.creditCards.size)
        assertEquals("Platinum", parsedPayload.creditCards[0].name)

        assertEquals(1, parsedPayload.stockPositions.size)
        assertEquals("NVDA", parsedPayload.stockPositions[0].symbol)

        assertEquals(1, parsedPayload.assetsLiabilities.size)
        assertEquals("Hipoteca", parsedPayload.assetsLiabilities[0].title)
    }
}

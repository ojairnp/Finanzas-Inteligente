package com.example.util

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Resultado del procesamiento de recibos con ML Kit Text Recognition
 */
data class ReceiptOcrResult(
    val amount: Double,
    val merchant: String,
    val dateText: String,
    val placeOrAddress: String,
    val category: String,
    val fullRawText: String,
    val detectedItems: List<String> = emptyList(),
    val confidenceScore: Float = 0.95f
)

/**
 * Utilidad procesadora de recibos usando Google ML Kit Text Recognition.
 * Extrae automáticamente:
 * 1. Monto total de la compra
 * 2. Fecha del recibo
 * 3. Lugar / Comercio / Establecimiento / Sucursal
 */
object ReceiptOcrProcessor {

    /**
     * Procesa una imagen de recibo bitmap mediante ML Kit Text Recognition de forma asíncrona suspendida.
     */
    suspend fun processReceiptImage(bitmap: Bitmap): ReceiptOcrResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val extractedText = try {
            val visionText = Tasks.await(recognizer.process(inputImage))
            visionText.text.trim()
        } catch (e: Exception) {
            ""
        }

        if (extractedText.isBlank()) {
            val defaultDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            return ReceiptOcrResult(
                amount = 0.0,
                merchant = "Sin texto detectado",
                dateText = defaultDate,
                placeOrAddress = "Sin ubicación",
                category = "General",
                fullRawText = "No se logró extraer texto claro del recibo. Intente con mejor iluminación."
            )
        }

        return parseExtractedText(extractedText)
    }

    /**
     * Analiza el texto plano extraído por ML Kit para identificar Monto, Fecha, Lugar y Categoría.
     */
    fun parseExtractedText(text: String): ReceiptOcrResult {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val textLower = text.lowercase(Locale.getDefault())

        // 1. EXTRAER MONTO TOTAL ($)
        val extractedAmount = extractTotalAmount(lines, textLower)

        // 2. EXTRAER FECHA (DD/MM/YYYY, YYYY-MM-DD, etc.)
        val extractedDate = extractReceiptDate(lines, text)

        // 3. EXTRAER COMERCIO Y LUGAR / SUCURSAL
        val (merchant, place) = extractMerchantAndPlace(lines, textLower)

        // 4. CATEGORIA SUGERIDA
        val category = determineCategory(textLower, merchant)

        // 5. DETECTAR ITEMS / COMPRAS INDIVIDUALES
        val items = extractLineItems(lines)

        return ReceiptOcrResult(
            amount = extractedAmount,
            merchant = merchant,
            dateText = extractedDate,
            placeOrAddress = place,
            category = category,
            fullRawText = text,
            detectedItems = items
        )
    }

    private fun extractTotalAmount(lines: List<String>, textLower: String): Double {
        var total = 0.0

        // Prioridad A: Líneas con palabras clave de Total
        val totalKeywords = listOf("total", "gran total", "monto", "neto", "importe", "pagado", "efectivo", "tarjeta", "total a pagar", "saldo")
        val totalLines = lines.filter { line ->
            val l = line.lowercase(Locale.getDefault())
            totalKeywords.any { kw -> l.contains(kw) }
        }

        val numberRegex = """\$?\s*(\d{1,3}(?:[,\s]\d{3})*(?:[\.,]\d{1,2})?|\d+[\.,]\d{1,2}|\d+)""".toRegex()

        for (line in totalLines) {
            val matches = numberRegex.findAll(line).toList()
            for (match in matches) {
                val numStr = match.groupValues[1].replace(" ", "").replace(",", ".")
                val cleanNumStr = if (numStr.count { it == '.' } > 1) {
                    val lastDot = numStr.lastIndexOf('.')
                    numStr.substring(0, lastDot).replace(".", "") + numStr.substring(lastDot)
                } else {
                    numStr
                }
                val value = cleanNumStr.toDoubleOrNull() ?: 0.0
                if (value in 1.0..199999.0) {
                    total = value
                    break
                }
            }
            if (total > 0.0) break
        }

        // Prioridad B: Buscar la cantidad mayor en todo el recibo
        if (total == 0.0) {
            val allAmounts = numberRegex.findAll(textLower).mapNotNull { match ->
                val numStr = match.groupValues[1].replace(" ", "").replace(",", ".")
                val cleanNumStr = if (numStr.count { it == '.' } > 1) {
                    val lastDot = numStr.lastIndexOf('.')
                    numStr.substring(0, lastDot).replace(".", "") + numStr.substring(lastDot)
                } else {
                    numStr
                }
                cleanNumStr.toDoubleOrNull()
            }.filter { it in 1.0..199999.0 }.toList()

            total = allAmounts.maxOrNull() ?: 0.0
        }

        return total
    }

    private fun extractReceiptDate(lines: List<String>, fullText: String): String {
        // Regex 1: DD/MM/YYYY or DD-MM-YYYY or DD.MM.YYYY or YYYY/MM/DD
        val dateRegex1 = """\b(\d{1,2})[-/. ](\d{1,2})[-/. ](20\d{2}|\d{2})\b""".toRegex()
        val dateRegex2 = """\b(20\d{2})[-/. ](\d{1,2})[-/. ](\d{1,2})\b""".toRegex()
        // Regex 3: 27 de Julio de 2026
        val dateRegex3 = """\b(\d{1,2})\s+(?:de\s+)?(ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)[a-z]*\s+(?:de\s+)?(20\d{2})\b""".toRegex(RegexOption.IGNORE_CASE)

        // Buscar primero en líneas especificas de FECHA
        val fechaLine = lines.firstOrNull { it.uppercase(Locale.getDefault()).contains("FECHA") || it.uppercase(Locale.getDefault()).contains("DATE") }
        if (fechaLine != null) {
            dateRegex1.find(fechaLine)?.let { return formatNormalizedDate(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
            dateRegex2.find(fechaLine)?.let { return formatNormalizedDate(it.groupValues[3], it.groupValues[2], it.groupValues[1]) }
        }

        // Buscar en todo el texto
        dateRegex1.find(fullText)?.let {
            return formatNormalizedDate(it.groupValues[1], it.groupValues[2], it.groupValues[3])
        }

        dateRegex2.find(fullText)?.let {
            return formatNormalizedDate(it.groupValues[3], it.groupValues[2], it.groupValues[1])
        }

        dateRegex3.find(fullText)?.let { match ->
            val day = match.groupValues[1]
            val monthStr = match.groupValues[2]
            val year = match.groupValues[3]
            return "$day ${monthStr.lowercase().capitalize()} $year"
        }

        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun formatNormalizedDate(dayOrYear: String, month: String, yearOrDay: String): String {
        val day = if (dayOrYear.length <= 2) dayOrYear.padStart(2, '0') else yearOrDay.padStart(2, '0')
        val monthPadded = month.padStart(2, '0')
        val year = if (dayOrYear.length == 4) dayOrYear else if (yearOrDay.length == 2) "20$yearOrDay" else yearOrDay
        return "$day/$monthPadded/$year"
    }

    private fun extractMerchantAndPlace(lines: List<String>, textLower: String): Pair<String, String> {
        var merchant = ""
        var place = ""

        val knownMerchants = mapOf(
            "oxxo" to ("CADENA COMERCIAL OXXO" to "Sucursal Local"),
            "walmart" to ("Walmart Supercenter" to "Tienda Departamental"),
            "pemex" to ("Gasolinera Pemex" to "Estación de Servicio"),
            "soriana" to ("Soriana Híper" to "Plaza Comercial"),
            "chedraui" to ("Chedraui" to "Supermercado"),
            "starbucks" to ("Starbucks Coffee" to "Sucursal Zona Centro"),
            "costco" to ("Costco Wholesale" to "Club de Precios"),
            "sam's" to ("Sam's Club" to "Club de Precios"),
            "sams" to ("Sam's Club" to "Club de Precios"),
            "aurrera" to ("Bodega Aurrera" to "Tienda de Autoservicio"),
            "uber" to ("Uber Trip" to "Movilidad Urbana"),
            "cfe" to ("CFE Suministrador" to "Oficina de Servicios"),
            "telmex" to ("Telmex" to "Telecomunicaciones"),
            "shell" to ("Gasolinera Shell" to "Estación de Servicio"),
            "bp" to ("Gasolinera BP" to "Estación de Servicio"),
            "farmacias guadalajara" to ("Farmacias Guadalajara" to "Sucursal 24 Horas"),
            "7-eleven" to ("7-Eleven" to "Tienda de Conveniencia"),
            "seven eleven" to ("7-Eleven" to "Tienda de Conveniencia")
        )

        for ((key, value) in knownMerchants) {
            if (textLower.contains(key)) {
                merchant = value.first
                place = value.second
                break
            }
        }

        // Si no coincide con un comercio conocido, tomar las primeras líneas encabezado
        if (merchant.isBlank()) {
            val candidateLine = lines.take(5).firstOrNull { line ->
                val l = line.uppercase(Locale.getDefault())
                !l.contains("TOTAL") && !l.contains("TICKET") && !l.contains("RFC") &&
                        !l.contains("FOLIO") && !l.contains("FECHA") && !l.contains("SUCURSAL") &&
                        !l.contains("=================") &&
                        line.count { it.isDigit() } < 4 && line.length in 3..40
            }

            merchant = candidateLine?.replace("S.A. DE C.V.", "", ignoreCase = true)
                ?.replace("S DE RL DE CV", "", ignoreCase = true)
                ?.trim()
                ?: "Establecimiento Detectado"
        }

        // Extraer dirección / sucursal si existe
        val sucursalLine = lines.firstOrNull {
            val l = it.uppercase(Locale.getDefault())
            l.contains("SUCURSAL") || l.contains("SUC.") || l.contains("AV.") || l.contains("CALLE") || l.contains("COL.") || l.contains("PLAZA")
        }

        if (sucursalLine != null) {
            place = sucursalLine.trim()
        } else if (place.isBlank()) {
            place = "Dirección en Ticket ML Kit"
        }

        return Pair(merchant, place)
    }

    private fun determineCategory(textLower: String, merchant: String): String {
        return when {
            textLower.contains("gasolina") || textLower.contains("pemex") || textLower.contains("magna") || textLower.contains("premium") || textLower.contains("litros") || merchant.contains("Pemex", true) || merchant.contains("Shell", true) -> "Gasolina"
            textLower.contains("walmart") || textLower.contains("oxxo") || textLower.contains("soriana") || textLower.contains("chedraui") || textLower.contains("despensa") || textLower.contains("super") || textLower.contains("aurrera") -> "Supermercado"
            textLower.contains("restaurante") || textLower.contains("starbucks") || textLower.contains("cafe") || textLower.contains("tacos") || textLower.contains("comida") || textLower.contains("burger") -> "Restaurantes"
            textLower.contains("cfe") || textLower.contains("telmex") || textLower.contains("agua") || textLower.contains("luz") || textLower.contains("izzi") -> "Servicios"
            textLower.contains("farmacia") || textLower.contains("medicina") || textLower.contains("doctor") -> "Salud"
            else -> "General"
        }
    }

    private fun extractLineItems(lines: List<String>): List<String> {
        return lines.filter { line ->
            line.contains("$") || line.matches(""".*\d+x.*""".toRegex(RegexOption.IGNORE_CASE))
        }.take(6)
    }

    private fun String.capitalize(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

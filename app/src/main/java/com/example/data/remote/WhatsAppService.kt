package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

data class WhatsAppResult(
    val success: Boolean,
    val mode: String, // "API" or "DEEP_LINK"
    val message: String,
    val deepLinkUrl: String? = null
)

class WhatsAppService {

    private val TAG = "WhatsAppService"

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    private fun formatMoney(amount: Double): String {
        return currencyFormat.format(amount)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date(timestamp))
    }

    fun cleanPhoneNumber(phone: String, defaultCountryCode: String = "91"): String {
        val digits = phone.filter { it.isDigit() }
        val clean10 = if (digits.length >= 10) digits.takeLast(10) else digits
        return "$defaultCountryCode$clean10"
    }

    fun buildBookingMessage(
        customerName: String,
        projectName: String,
        plotNumber: String,
        bookingAmount: Double,
        totalPrice: Double,
        bookingDate: Long = System.currentTimeMillis()
    ): String {
        return """
            Hello *$customerName*,

            Thank you for booking with us! We are delighted to confirm your plot booking. Here are your official booking details:

            📌 *Project Name:* $projectName
            🏡 *Plot Number:* Plot #$plotNumber
            💰 *Total Agreement Price:* ${formatMoney(totalPrice)}
            💳 *Booking Amount Paid:* ${formatMoney(bookingAmount)}
            📅 *Booking Date:* ${formatDate(bookingDate)}

            Your booking confirmation & payment receipt have been generated. Our operations team will reach out to you shortly with agreement documentation and payment schedule details.

            Thank you for trusting us with your dream property!

            Warm regards,
            Sales & Operations Team · Terra Plots
        """.trimIndent()
    }

    fun buildEmiStatementMessage(
        customerName: String,
        projectDetails: String,
        totalContractPrice: Double,
        totalAmountPaid: Double,
        remainingBalance: Double,
        installmentNumber: Int,
        totalInstallments: Int,
        nextDueDate: String,
        nextDueAmount: Double
    ): String {
        return """
            Dear *$customerName*,

            Here is your updated EMI payment statement and ledger summary for your property reservation:

            🏡 *Property:* $projectDetails
            💰 *Total Contract Price:* ${formatMoney(totalContractPrice)}
            ✅ *Total Amount Received:* ${formatMoney(totalAmountPaid)}
            ⏳ *Remaining Balance:* ${formatMoney(remainingBalance)}

            📈 *EMI Progress:*
            • Milestone: $installmentNumber of $totalInstallments
            • Next Due Date: $nextDueDate
            • Next Due Amount: ${formatMoney(nextDueAmount)}

            Payment receipts and ledger entries have been recorded in your customer account.
            For any queries, please reach out to our treasury desk.

            Best regards,
            Treasury & Accounts · Terra Plots
        """.trimIndent()
    }

    fun getWhatsAppDeepLink(phone: String, messageText: String): String {
        val cleanPhone = cleanPhoneNumber(phone)
        val encodedText = URLEncoder.encode(messageText, "UTF-8")
        return "https://wa.me/$cleanPhone?text=$encodedText"
    }

    fun launchWhatsAppIntent(context: Context, phone: String, messageText: String): Boolean {
        return try {
            val deepLink = getWhatsAppDeepLink(phone, messageText)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp intent: ${e.message}")
            false
        }
    }

    /**
     * Sends booking confirmation via Meta Cloud API using the plot_booking_confirmation template.
     */
    suspend fun sendBookingConfirmationApi(
        customerName: String,
        customerPhone: String,
        projectName: String,
        plotNumber: String,
        bookingAmount: Double,
        totalPrice: Double,
        bookingDate: Long = System.currentTimeMillis()
    ): WhatsAppResult = withContext(Dispatchers.IO) {
        val phoneWithCode = cleanPhoneNumber(customerPhone)
        val formattedMsg = buildBookingMessage(
            customerName = customerName,
            projectName = projectName,
            plotNumber = plotNumber,
            bookingAmount = bookingAmount,
            totalPrice = totalPrice,
            bookingDate = bookingDate
        )
        val deepLink = getWhatsAppDeepLink(customerPhone, formattedMsg)

        val token = AppConfig.WHATSAPP_API_TOKEN
        val phoneId = AppConfig.WHATSAPP_PHONE_NUMBER_ID
        val template = AppConfig.WHATSAPP_TEMPLATE_BOOKING

        if (token.isBlank() || phoneId.isBlank()) {
            return@withContext WhatsAppResult(
                success = false,
                mode = "DEEP_LINK",
                message = "Meta WhatsApp credentials not set. 1-tap WhatsApp ready.",
                deepLinkUrl = deepLink
            )
        }

        try {
            val endpoint = "https://graph.facebook.com/v19.0/$phoneId/messages"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            conn.doOutput = true
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")

            // Meta Cloud API template body parameters
            val params = JSONArray().apply {
                put(JSONObject().apply { put("type", "text"); put("text", customerName) })
                put(JSONObject().apply { put("type", "text"); put("text", projectName) })
                put(JSONObject().apply { put("type", "text"); put("text", plotNumber) })
                put(JSONObject().apply { put("type", "text"); put("text", formatMoney(totalPrice)) })
                put(JSONObject().apply { put("type", "text"); put("text", formatMoney(bookingAmount)) })
                put(JSONObject().apply { put("type", "text"); put("text", formatDate(bookingDate)) })
            }

            val body = JSONObject().apply {
                put("messaging_product", "whatsapp")
                put("to", phoneWithCode)
                put("type", "template")
                put("template", JSONObject().apply {
                    put("name", template)
                    put("language", JSONObject().apply { put("code", "en") })
                    put("components", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "body")
                            put("parameters", params)
                        })
                    })
                })
            }

            conn.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val code = conn.responseCode
            if (code in 200..299) {
                val resp = conn.inputStream.bufferedReader().use { it.readText() }
                Log.i(TAG, "Meta WhatsApp API Success: $resp")
                WhatsAppResult(
                    success = true,
                    mode = "API",
                    message = "Official WhatsApp confirmation sent to +$phoneWithCode!",
                    deepLinkUrl = deepLink
                )
            } else {
                val errResp = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
                Log.w(TAG, "Meta WhatsApp API Error ($code): $errResp")
                WhatsAppResult(
                    success = false,
                    mode = "API_FALLBACK",
                    message = "Meta API: HTTP $code. 1-tap WhatsApp dispatch ready.",
                    deepLinkUrl = deepLink
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp API exception", e)
            WhatsAppResult(
                success = false,
                mode = "DEEP_LINK",
                message = "Network dispatch: ${e.localizedMessage}. 1-tap WhatsApp link prepared.",
                deepLinkUrl = deepLink
            )
        }
    }
}

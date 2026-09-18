package com.example.data.remote

import com.example.BuildConfig

/**
 * Centralized credentials and integration config.
 * Accesses credentials defined in .env via BuildConfig, with clean string parsing
 * and fallback safeguards.
 */
object AppConfig {

    private fun clean(value: String?, fallback: String): String {
        if (value.isNullOrBlank()) return fallback
        return value.trim().trim('\"', '\'')
    }

    val SUPABASE_PROJECT_ID: String
        get() = clean(
            try { BuildConfig.SUPABASE_PROJECT_ID } catch (_: Throwable) { null },
            "zolbuckwnjsxfgqqkcjj"
        )

    val SUPABASE_URL: String
        get() = clean(
            try { BuildConfig.SUPABASE_URL } catch (_: Throwable) { null },
            "https://zolbuckwnjsxfgqqkcjj.supabase.co"
        )

    val SUPABASE_PUBLISHABLE_KEY: String
        get() = clean(
            try { BuildConfig.SUPABASE_PUBLISHABLE_KEY } catch (_: Throwable) { null },
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpvbGJ1Y2t3bmpzeGZncXFrY2pqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQwMTA2NTgsImV4cCI6MjA5OTU4NjY1OH0.IZbcoldTi0xNDr0WvHZ3CkyWcxSmwksaMkRk8QYwsB8"
        )

    val WHATSAPP_API_TOKEN: String
        get() = clean(
            try { BuildConfig.VITE_WHATSAPP_API_TOKEN } catch (_: Throwable) { null },
            "EAAONO18mvZBYBSSgXtZCJ9Ya6ZC0ZCZAuZBs1MoeP0zjrKLXLstjaIeeYRrZAtZBkEsiTXyAPQK17sI5bXfKwSlyucnVCu12q9bKvhhAydhEFn9HgpYb2JWtO5EbR9RBPQ1mWHiAgL4ZCNVD5sl90aHzZB7T5hW0kB1mO2EvQQZCxZA2g9a5nJEz0XD3Mp1p8TT5tBzX7xD7UXehIZAutmQYmUGVPVhksxHBaBhQZAZBaiZBQHZCjrsNhafT8ff6CZAzMK7oVbMJNDdzSc7pukDZCikbZBixUGjipl3N8QZDZD"
        )

    val WHATSAPP_PHONE_NUMBER_ID: String
        get() = clean(
            try { BuildConfig.VITE_WHATSAPP_PHONE_NUMBER_ID } catch (_: Throwable) { null },
            "1126770290524197"
        )

    val WHATSAPP_TEMPLATE_BOOKING: String
        get() = clean(
            try { BuildConfig.VITE_WHATSAPP_TEMPLATE_NAME } catch (_: Throwable) { null },
            "plot_booking_confirmation"
        )

    val WHATSAPP_TEMPLATE_EMI: String
        get() = clean(
            try { BuildConfig.VITE_WHATSAPP_EMI_TEMPLATE_NAME } catch (_: Throwable) { null },
            "customer_emi_statement_v2"
        )
}

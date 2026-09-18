package com.example.data.model

enum class PlotStatus(val label: String) {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    BOOKED("Booked"),
    SOLD("Sold")
}

enum class LeadStage(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    MEETING_SCHEDULED("Meeting Scheduled"),
    NEGOTIATING("Negotiating"),
    CONVERTED("Converted"),
    DROPPED("Dropped")
}

enum class BookingStatus(val label: String) {
    PENDING_CRM("Pending CRM Approval"),
    PENDING_ACCOUNTS("Pending Accounts"),
    PENDING_MANAGEMENT("Pending Management"),
    APPROVED("Approved"),
    CANCELLED("Cancelled")
}

enum class InstallmentStatus(val label: String) {
    UPCOMING("Upcoming"),
    PAID("Paid"),
    OVERDUE("Overdue")
}

enum class PaymentMethod(val label: String) {
    RTGS_NEFT("RTGS / NEFT"),
    CHEQUE("Cheque"),
    UPI("UPI"),
    BANK_TRANSFER("Net Banking"),
    CASH("Cash / Demand Draft")
}

data class BankAccount(
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val branch: String,
    val balance: Double
)

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BookingStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.LeadStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PlotStatus

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String,
    val totalPlots: Int,
    val totalAreaSqft: Double,
    val baseRatePerSqft: Double,
    val status: String,
    val description: String,
    val bankName: String,
    val bankAccountNo: String,
    val ifscCode: String,
    val currentBalance: Double
)

@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val plotNumber: String,
    val areaSqft: Double,
    val ratePerSqft: Double,
    val premiumCharge: Double,
    val facing: String,
    val status: PlotStatus,
    val purchaserName: String? = null,
    val purchaserPhone: String? = null,
    val purchaserEmail: String? = null,
    val reservedLeadId: Long? = null
)

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val plotId: Long? = null,
    val name: String,
    val phone: String,
    val email: String,
    val budget: Double,
    val source: String,
    val stage: LeadStage,
    val notes: String,
    val meetingDate: String? = null,
    val siteVisitProofUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plotId: Long,
    val projectId: Long,
    val leadId: Long? = null,
    val plotNumber: String,
    val projectName: String,
    val buyerName: String,
    val buyerPhone: String,
    val buyerEmail: String,
    val buyerPan: String,
    val agreementValue: Double,
    val govtValue: Double,
    val downPayment: Double,
    val bookingDate: String,
    val status: BookingStatus,
    val approvedByCrm: Boolean = false,
    val approvedByAccounts: Boolean = false,
    val approvedByManagement: Boolean = false,
    val paymentMethod: PaymentMethod = PaymentMethod.RTGS_NEFT,
    val paymentRef: String,
    val cancellationReason: String? = null,
    val refundAmount: Double? = null
)

@Entity(tableName = "installments")
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val stageName: String,
    val percentage: Double,
    val amount: Double,
    val dueDate: String,
    val paidDate: String? = null,
    val status: InstallmentStatus,
    val receiptNumber: String? = null,
    val paymentRef: String? = null
)

@Entity(tableName = "treasury_transfers")
data class TreasuryTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromProjectId: Long,
    val fromProjectName: String,
    val toProjectId: Long,
    val toProjectName: String,
    val amount: Double,
    val transferDate: String,
    val narration: String,
    val referenceNumber: String,
    val tallyVoucherNo: String? = null,
    val isSyncedToTally: Boolean = false
)

@Entity(tableName = "incentives")
data class IncentiveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val plotNumber: String,
    val recipientName: String,
    val recipientRole: String, // "Sales Manager", "BDO Partner"
    val percentage: Double,
    val totalAmount: Double,
    val disbursedAmount: Double,
    val isDisbursed: Boolean = false,
    val disbursalDate: String? = null,
    val paymentRef: String? = null
)

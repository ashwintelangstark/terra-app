package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.BookingStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.LeadStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PlotStatus
import kotlinx.coroutines.flow.Flow

class TerraRepository(private val dao: TerraDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allPlots: Flow<List<PlotEntity>> = dao.getAllPlots()
    val allLeads: Flow<List<LeadEntity>> = dao.getAllLeads()
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()
    val allInstallments: Flow<List<InstallmentEntity>> = dao.getAllInstallments()
    val allTreasuryTransfers: Flow<List<TreasuryTransferEntity>> = dao.getAllTreasuryTransfers()
    val allIncentives: Flow<List<IncentiveEntity>> = dao.getAllIncentives()

    fun getPlotsForProject(projectId: Long): Flow<List<PlotEntity>> = dao.getPlotsForProject(projectId)
    fun getLeadsForPlot(plotId: Long): Flow<List<LeadEntity>> = dao.getLeadsForPlot(plotId)
    fun getInstallmentsForBooking(bookingId: Long): Flow<List<InstallmentEntity>> = dao.getInstallmentsForBooking(bookingId)

    suspend fun insertProject(project: ProjectEntity): Long = dao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = dao.updateProject(project)

    suspend fun insertPlot(plot: PlotEntity): Long = dao.insertPlot(plot)
    suspend fun updatePlot(plot: PlotEntity) = dao.updatePlot(plot)

    suspend fun reservePlot(plotId: Long, leadId: Long?, purchaserName: String?, purchaserPhone: String?) {
        val plot = dao.getPlotById(plotId) ?: return
        dao.updatePlot(
            plot.copy(
                status = PlotStatus.RESERVED,
                purchaserName = purchaserName,
                purchaserPhone = purchaserPhone,
                reservedLeadId = leadId
            )
        )
    }

    suspend fun insertLead(lead: LeadEntity): Long = dao.insertLead(lead)
    suspend fun updateLead(lead: LeadEntity) = dao.updateLead(lead)
    suspend fun updateLeadStage(leadId: Long, newStage: LeadStage) {
        // Find lead and update
    }
    suspend fun deleteLead(lead: LeadEntity) = dao.deleteLead(lead)

    suspend fun createBooking(
        plotId: Long,
        projectId: Long,
        leadId: Long?,
        buyerName: String,
        buyerPhone: String,
        buyerEmail: String,
        buyerPan: String,
        agreementValue: Double,
        govtValue: Double,
        downPayment: Double,
        paymentMethod: PaymentMethod,
        paymentRef: String
    ): Long {
        val plot = dao.getPlotById(plotId)
        val project = dao.getProjectById(projectId)
        val plotNo = plot?.plotNumber ?: "Plot"
        val projName = project?.name ?: "Project"

        val booking = BookingEntity(
            plotId = plotId,
            projectId = projectId,
            leadId = leadId,
            plotNumber = plotNo,
            projectName = projName,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            buyerEmail = buyerEmail,
            buyerPan = buyerPan,
            agreementValue = agreementValue,
            govtValue = govtValue,
            downPayment = downPayment,
            bookingDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            status = BookingStatus.PENDING_CRM,
            approvedByCrm = false,
            approvedByAccounts = false,
            approvedByManagement = false,
            paymentMethod = paymentMethod,
            paymentRef = paymentRef
        )
        val bkgId = dao.insertBooking(booking)

        // Mark plot as BOOKED
        plot?.let {
            dao.updatePlot(
                it.copy(
                    status = PlotStatus.BOOKED,
                    purchaserName = buyerName,
                    purchaserPhone = buyerPhone,
                    purchaserEmail = buyerEmail
                )
            )
        }

        // Auto-generate initial installment plan
        val remaining = agreementValue - downPayment
        val instList = listOf(
            InstallmentEntity(
                bookingId = bkgId,
                stageName = "Booking Down Payment",
                percentage = if (agreementValue > 0) (downPayment / agreementValue) * 100 else 20.0,
                amount = downPayment,
                dueDate = booking.bookingDate,
                paidDate = booking.bookingDate,
                status = InstallmentStatus.PAID,
                receiptNumber = "RCT-${System.currentTimeMillis() % 10000}",
                paymentRef = paymentRef
            ),
            InstallmentEntity(
                bookingId = bkgId,
                stageName = "Agreement Signing (25%)",
                percentage = 25.0,
                amount = remaining * 0.35,
                dueDate = "Next Month",
                status = InstallmentStatus.UPCOMING
            ),
            InstallmentEntity(
                bookingId = bkgId,
                stageName = "Infrastructure & Demarcation (35%)",
                percentage = 35.0,
                amount = remaining * 0.40,
                dueDate = "In 3 Months",
                status = InstallmentStatus.UPCOMING
            ),
            InstallmentEntity(
                bookingId = bkgId,
                stageName = "Final Registration & Handover",
                percentage = 20.0,
                amount = remaining * 0.25,
                dueDate = "In 6 Months",
                status = InstallmentStatus.UPCOMING
            )
        )
        dao.insertInstallments(instList)

        // Insert initial incentive record (2%)
        dao.insertIncentive(
            IncentiveEntity(
                bookingId = bkgId,
                plotNumber = plotNo,
                recipientName = "Direct Sales Executive",
                recipientRole = "Sales Team",
                percentage = 2.0,
                totalAmount = agreementValue * 0.02,
                disbursedAmount = 0.0,
                isDisbursed = false,
                paymentRef = "Pending Booking Final Approval"
            )
        )

        return bkgId
    }

    suspend fun advanceApproval(bookingId: Long, currentStatus: BookingStatus) {
        when (currentStatus) {
            BookingStatus.PENDING_CRM -> dao.approveCrm(bookingId, BookingStatus.PENDING_ACCOUNTS)
            BookingStatus.PENDING_ACCOUNTS -> dao.approveAccounts(bookingId, BookingStatus.PENDING_MANAGEMENT)
            BookingStatus.PENDING_MANAGEMENT -> dao.approveManagement(bookingId, BookingStatus.APPROVED)
            else -> {}
        }
    }

    suspend fun cancelBooking(bookingId: Long, reason: String, refund: Double) {
        val bkg = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(
            bkg.copy(
                status = BookingStatus.CANCELLED,
                cancellationReason = reason,
                refundAmount = refund
            )
        )
        // Reset plot to AVAILABLE
        val plot = dao.getPlotById(bkg.plotId)
        plot?.let {
            dao.updatePlot(
                it.copy(
                    status = PlotStatus.AVAILABLE,
                    purchaserName = null,
                    purchaserPhone = null,
                    purchaserEmail = null,
                    reservedLeadId = null
                )
            )
        }
    }

    suspend fun recordInstallmentPayment(installment: InstallmentEntity, paymentRef: String) {
        val today = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val receiptNo = "RCT-${System.currentTimeMillis() % 100000}"
        dao.updateInstallment(
            installment.copy(
                status = InstallmentStatus.PAID,
                paidDate = today,
                paymentRef = paymentRef,
                receiptNumber = receiptNo
            )
        )
    }

    suspend fun recordTreasuryTransfer(
        fromProjectId: Long,
        fromProjectName: String,
        toProjectId: Long,
        toProjectName: String,
        amount: Double,
        narration: String
    ): Long {
        val ref = "TRF-${System.currentTimeMillis() % 100000}"
        val today = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        val transfer = TreasuryTransferEntity(
            fromProjectId = fromProjectId,
            fromProjectName = fromProjectName,
            toProjectId = toProjectId,
            toProjectName = toProjectName,
            amount = amount,
            transferDate = today,
            narration = narration,
            referenceNumber = ref,
            tallyVoucherNo = null,
            isSyncedToTally = false
        )
        return dao.insertTreasuryTransfer(transfer)
    }

    suspend fun syncTransferToTally(transferId: Long) {
        val voucherNo = "JV-${(1000..9999).random()}"
        dao.markTransferSynced(transferId, voucherNo)
    }

    suspend fun disburseIncentive(incentive: IncentiveEntity, paymentRef: String) {
        val today = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        dao.updateIncentive(
            incentive.copy(
                disbursedAmount = incentive.totalAmount,
                isDisbursed = true,
                disbursalDate = today,
                paymentRef = paymentRef
            )
        )
    }
}

package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.BookingStatus
import com.example.data.model.LeadStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PlotStatus
import com.example.data.remote.*
import com.example.data.repository.TerraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TerraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TerraRepository
    val supabaseService = SupabaseService()
    val whatsAppService = WhatsAppService()

    private val _supabaseStatus = MutableStateFlow<SupabaseConnectionState>(SupabaseConnectionState.Idle)
    val supabaseStatus: StateFlow<SupabaseConnectionState> = _supabaseStatus.asStateFlow()

    private val _supabaseSyncMessage = MutableStateFlow<String?>(null)
    val supabaseSyncMessage: StateFlow<String?> = _supabaseSyncMessage.asStateFlow()

    private val _lastWhatsAppResult = MutableStateFlow<WhatsAppResult?>(null)
    val lastWhatsAppResult: StateFlow<WhatsAppResult?> = _lastWhatsAppResult.asStateFlow()

    val projects: StateFlow<List<ProjectEntity>>
    val plots: StateFlow<List<PlotEntity>>
    val leads: StateFlow<List<LeadEntity>>
    val bookings: StateFlow<List<BookingEntity>>
    val installments: StateFlow<List<InstallmentEntity>>
    val treasuryTransfers: StateFlow<List<TreasuryTransferEntity>>
    val incentives: StateFlow<List<IncentiveEntity>>

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    private val _selectedPlot = MutableStateFlow<PlotEntity?>(null)
    val selectedPlot: StateFlow<PlotEntity?> = _selectedPlot.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val db = TerraDatabase.getDatabase(application, viewModelScope)
        repository = TerraRepository(db.terraDao())

        projects = repository.allProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        plots = repository.allPlots
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        leads = repository.allLeads
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        bookings = repository.allBookings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        installments = repository.allInstallments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        treasuryTransfers = repository.allTreasuryTransfers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        incentives = repository.allIncentives
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun selectProject(id: Long?) {
        _selectedProjectId.value = id
    }

    fun selectPlot(plot: PlotEntity?) {
        _selectedPlot.value = plot
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addProject(
        name: String,
        location: String,
        totalPlots: Int,
        totalAreaSqft: Double,
        baseRate: Double,
        bankName: String,
        bankAccountNo: String,
        ifscCode: String
    ) {
        viewModelScope.launch {
            val project = ProjectEntity(
                name = name,
                location = location,
                totalPlots = totalPlots,
                totalAreaSqft = totalAreaSqft,
                baseRatePerSqft = baseRate,
                status = "Live",
                description = "Master planned plotted community.",
                bankName = bankName,
                bankAccountNo = bankAccountNo,
                ifscCode = ifscCode,
                currentBalance = 1000000.0
            )
            repository.insertProject(project)
        }
    }

    fun addPlot(
        projectId: Long,
        plotNumber: String,
        areaSqft: Double,
        ratePerSqft: Double,
        premiumCharge: Double,
        facing: String
    ) {
        viewModelScope.launch {
            val plot = PlotEntity(
                projectId = projectId,
                plotNumber = plotNumber,
                areaSqft = areaSqft,
                ratePerSqft = ratePerSqft,
                premiumCharge = premiumCharge,
                facing = facing,
                status = PlotStatus.AVAILABLE
            )
            repository.insertPlot(plot)
        }
    }

    fun reservePlot(plotId: Long, leadId: Long?, name: String, phone: String) {
        viewModelScope.launch {
            repository.reservePlot(plotId, leadId, name, phone)
        }
    }

    fun addLead(
        projectId: Long,
        plotId: Long?,
        name: String,
        phone: String,
        email: String,
        budget: Double,
        source: String,
        notes: String,
        meetingDate: String?
    ) {
        viewModelScope.launch {
            val lead = LeadEntity(
                projectId = projectId,
                plotId = plotId,
                name = name,
                phone = phone,
                email = email,
                budget = budget,
                source = source,
                stage = LeadStage.NEW,
                notes = notes,
                meetingDate = meetingDate
            )
            repository.insertLead(lead)
        }
    }

    fun updateLeadStage(lead: LeadEntity, newStage: LeadStage) {
        viewModelScope.launch {
            repository.updateLead(lead.copy(stage = newStage))
        }
    }

    fun deleteLead(lead: LeadEntity) {
        viewModelScope.launch {
            repository.deleteLead(lead)
        }
    }

    fun bookPlot(
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
    ) {
        viewModelScope.launch {
            repository.createBooking(
                plotId = plotId,
                projectId = projectId,
                leadId = leadId,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                buyerEmail = buyerEmail,
                buyerPan = buyerPan,
                agreementValue = agreementValue,
                govtValue = govtValue,
                downPayment = downPayment,
                paymentMethod = paymentMethod,
                paymentRef = paymentRef
            )
        }
    }

    fun advanceBookingApproval(bookingId: Long, currentStatus: BookingStatus) {
        viewModelScope.launch {
            repository.advanceApproval(bookingId, currentStatus)
        }
    }

    fun cancelBooking(bookingId: Long, reason: String, refund: Double) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId, reason, refund)
        }
    }

    fun payInstallment(installment: InstallmentEntity, paymentRef: String) {
        viewModelScope.launch {
            repository.recordInstallmentPayment(installment, paymentRef)
        }
    }

    fun addTreasuryTransfer(
        fromProjectId: Long,
        fromProjectName: String,
        toProjectId: Long,
        toProjectName: String,
        amount: Double,
        narration: String
    ) {
        viewModelScope.launch {
            repository.recordTreasuryTransfer(
                fromProjectId = fromProjectId,
                fromProjectName = fromProjectName,
                toProjectId = toProjectId,
                toProjectName = toProjectName,
                amount = amount,
                narration = narration
            )
        }
    }

    fun syncTransferToTally(transferId: Long) {
        viewModelScope.launch {
            repository.syncTransferToTally(transferId)
        }
    }

    fun disburseIncentive(incentive: IncentiveEntity, paymentRef: String) {
        viewModelScope.launch {
            repository.disburseIncentive(incentive, paymentRef)
        }
    }

    fun generateTallySalesXml(booking: BookingEntity): String {
        val partyName = "Customer - ${booking.buyerName}"
        return """
            <ENVELOPE>
              <HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>
              <BODY>
                <IMPORTDATA>
                  <REQUESTDESC>
                    <REPORTNAME>Vouchers</REPORTNAME>
                    <STATICVARIABLES><SVCURRENTCOMPANY>Terra Real Estate Ltd</SVCURRENTCOMPANY></STATICVARIABLES>
                  </REQUESTDESC>
                  <REQUESTDATA>
                    <TALLYMESSAGE xmlns:UDF="TallyUDF">
                      <VOUCHER VCHTYPE="Sales" ACTION="Create">
                        <DATE>20260917</DATE>
                        <VOUCHERTYPENAME>Sales</VOUCHERTYPENAME>
                        <PARTYLEDGERNAME>$partyName</PARTYLEDGERNAME>
                        <NARRATION>Booking Sale for ${booking.plotNumber} - Ref: BKG-${booking.id}</NARRATION>
                        <ALLLEDGERENTRIES.LIST>
                          <LEDGERNAME>$partyName</LEDGERNAME>
                          <ISDEEMEDPOSITIVE>YES</ISDEEMEDPOSITIVE>
                          <AMOUNT>-${booking.agreementValue}</AMOUNT>
                        </ALLLEDGERENTRIES.LIST>
                        <ALLLEDGERENTRIES.LIST>
                          <LEDGERNAME>Plot Sales Revenue</LEDGERNAME>
                          <ISDEEMEDPOSITIVE>NO</ISDEEMEDPOSITIVE>
                          <AMOUNT>${booking.agreementValue}</AMOUNT>
                        </ALLLEDGERENTRIES.LIST>
                      </VOUCHER>
                    </TALLYMESSAGE>
                  </REQUESTDATA>
                </IMPORTDATA>
              </BODY>
            </ENVELOPE>
        """.trimIndent()
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseStatus.value = SupabaseConnectionState.Connecting
            val res = supabaseService.testConnection()
            _supabaseStatus.value = res
        }
    }

    fun syncAllBookingsToSupabase() {
        viewModelScope.launch {
            _supabaseSyncMessage.value = "Syncing with Supabase ${AppConfig.SUPABASE_PROJECT_ID}..."
            val currentBookings = bookings.value
            var count = 0
            for (b in currentBookings) {
                supabaseService.syncBookingToCloud(
                    bookingId = b.id,
                    plotNumber = b.plotNumber,
                    projectName = b.projectName,
                    buyerName = b.buyerName,
                    buyerPhone = b.buyerPhone,
                    agreementValue = b.agreementValue,
                    status = b.status.name
                )
                count++
            }
            _supabaseSyncMessage.value = "Synced $count booking(s) to Supabase cloud successfully."
        }
    }

    fun sendBookingWhatsApp(
        booking: BookingEntity,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val result = whatsAppService.sendBookingConfirmationApi(
                customerName = booking.buyerName,
                customerPhone = booking.buyerPhone,
                projectName = booking.projectName,
                plotNumber = booking.plotNumber,
                bookingAmount = booking.downPayment,
                totalPrice = booking.agreementValue,
                bookingDate = booking.bookingDate
            )
            _lastWhatsAppResult.value = result
            if (context != null && result.mode != "API") {
                val msg = whatsAppService.buildBookingMessage(
                    customerName = booking.buyerName,
                    projectName = booking.projectName,
                    plotNumber = booking.plotNumber,
                    bookingAmount = booking.downPayment,
                    totalPrice = booking.agreementValue,
                    bookingDate = booking.bookingDate
                )
                whatsAppService.launchWhatsAppIntent(context, booking.buyerPhone, msg)
            }
        }
    }

    fun sendInstallmentWhatsApp(
        installment: InstallmentEntity,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val msg = whatsAppService.buildEmiStatementMessage(
                customerName = installment.buyerName,
                projectDetails = "${installment.projectName} · Plot #${installment.plotNumber}",
                totalContractPrice = installment.amount * 5.0,
                totalAmountPaid = installment.amount,
                remainingBalance = installment.amount * 4.0,
                installmentNumber = installment.installmentNumber,
                totalInstallments = 5,
                nextDueDate = installment.dueDate,
                nextDueAmount = installment.amount
            )
            val deepLink = whatsAppService.getWhatsAppDeepLink(installment.buyerPhone, msg)
            _lastWhatsAppResult.value = WhatsAppResult(
                success = true,
                mode = "DEEP_LINK",
                message = "WhatsApp EMI reminder prepared for ${installment.buyerName}",
                deepLinkUrl = deepLink
            )
            if (context != null) {
                whatsAppService.launchWhatsAppIntent(context, installment.buyerPhone, msg)
            }
        }
    }

    fun dismissWhatsAppResult() {
        _lastWhatsAppResult.value = null
    }

    fun dismissSyncMessage() {
        _supabaseSyncMessage.value = null
    }
}

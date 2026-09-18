package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BookingStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.LeadStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PlotStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        PlotEntity::class,
        LeadEntity::class,
        BookingEntity::class,
        InstallmentEntity::class,
        TreasuryTransferEntity::class,
        IncentiveEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TerraDatabase : RoomDatabase() {

    abstract fun terraDao(): TerraDao

    companion object {
        @Volatile
        private var INSTANCE: TerraDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TerraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TerraDatabase::class.java,
                    "terra_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateData(database.terraDao())
                    }
                }
            }

            suspend fun prepopulateData(dao: TerraDao) {
                // Prepopulate Projects
                val p1Id = dao.insertProject(
                    ProjectEntity(
                        name = "Grand Meadows",
                        location = "North Corridor, Highway 44",
                        totalPlots = 64,
                        totalAreaSqft = 96000.0,
                        baseRatePerSqft = 2200.0,
                        status = "Live",
                        description = "Gated luxury villa community with clubhouse, landscaped parks, and solar infrastructure.",
                        bankName = "HDFC Bank Ltd",
                        bankAccountNo = "50200088921102",
                        ifscCode = "HDFC0001248",
                        currentBalance = 4250000.0
                    )
                )

                val p2Id = dao.insertProject(
                    ProjectEntity(
                        name = "Royal Palms Estate",
                        location = "East Valley Road, Sector 8",
                        totalPlots = 48,
                        totalAreaSqft = 72000.0,
                        baseRatePerSqft = 2500.0,
                        status = "Live",
                        description = "Serene countryside plotted development surrounded by palm orchards and private lake access.",
                        bankName = "ICICI Bank Ltd",
                        bankAccountNo = "003405012984",
                        ifscCode = "ICIC0000034",
                        currentBalance = 1850000.0
                    )
                )

                val p3Id = dao.insertProject(
                    ProjectEntity(
                        name = "Sierra Vista",
                        location = "Highland Foothills, Plot Zone B",
                        totalPlots = 32,
                        totalAreaSqft = 48000.0,
                        baseRatePerSqft = 3100.0,
                        status = "Upcoming",
                        description = "Exclusive hillside residential enclave overlooking panoramic green mountain ridges.",
                        bankName = "State Bank of India",
                        bankAccountNo = "38920194821",
                        ifscCode = "SBIN0004921",
                        currentBalance = 920000.0
                    )
                )

                // Prepopulate Plots for Grand Meadows
                val plot1 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #101",
                        areaSqft = 1200.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 50000.0,
                        facing = "East",
                        status = PlotStatus.BOOKED,
                        purchaserName = "Rajesh Kumar",
                        purchaserPhone = "+91 98450 12345",
                        purchaserEmail = "rajesh.kumar@gmail.com"
                    )
                )

                val plot2 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #102",
                        areaSqft = 1500.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 0.0,
                        facing = "North",
                        status = PlotStatus.RESERVED,
                        purchaserName = "Ananya Sen",
                        purchaserPhone = "+91 98201 54321",
                        purchaserEmail = "ananya.sen@outlook.com"
                    )
                )

                val plot3 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #103",
                        areaSqft = 1200.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 0.0,
                        facing = "West",
                        status = PlotStatus.AVAILABLE
                    )
                )

                val plot4 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #104",
                        areaSqft = 2400.0,
                        ratePerSqft = 2300.0,
                        premiumCharge = 120000.0,
                        facing = "North-East Corner",
                        status = PlotStatus.AVAILABLE
                    )
                )

                val plot5 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #105",
                        areaSqft = 1500.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 0.0,
                        facing = "East",
                        status = PlotStatus.SOLD,
                        purchaserName = "Vikram Aditya",
                        purchaserPhone = "+91 99160 88219"
                    )
                )

                val plot6 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #106",
                        areaSqft = 1200.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 0.0,
                        facing = "South",
                        status = PlotStatus.AVAILABLE
                    )
                )

                val plot7 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #107",
                        areaSqft = 1800.0,
                        ratePerSqft = 2250.0,
                        premiumCharge = 75000.0,
                        facing = "Park Facing",
                        status = PlotStatus.AVAILABLE
                    )
                )

                val plot8 = dao.insertPlot(
                    PlotEntity(
                        projectId = p1Id,
                        plotNumber = "Plot #108",
                        areaSqft = 1200.0,
                        ratePerSqft = 2200.0,
                        premiumCharge = 0.0,
                        facing = "East",
                        status = PlotStatus.AVAILABLE
                    )
                )

                // Prepopulate Leads
                dao.insertLead(
                    LeadEntity(
                        projectId = p1Id,
                        plotId = plot3,
                        name = "Suresh Menon",
                        phone = "+91 98840 91234",
                        email = "suresh.menon@techcorp.in",
                        budget = 2800000.0,
                        source = "Walk-in Site Visit",
                        stage = LeadStage.NEGOTIATING,
                        notes = "Interested in East facing plot. Requested 5% discount on registration charges."
                    )
                )

                dao.insertLead(
                    LeadEntity(
                        projectId = p1Id,
                        plotId = plot4,
                        name = "Priya Sharma",
                        phone = "+91 97412 88345",
                        email = "priya.sharma@investors.com",
                        budget = 5500000.0,
                        source = "Digital Campaign",
                        stage = LeadStage.MEETING_SCHEDULED,
                        notes = "Looking for corner plot with double road frontage for luxury duplex.",
                        meetingDate = "Tomorrow, 11:30 AM"
                    )
                )

                dao.insertLead(
                    LeadEntity(
                        projectId = p1Id,
                        plotId = plot2,
                        name = "Ananya Sen",
                        phone = "+91 98201 54321",
                        email = "ananya.sen@outlook.com",
                        budget = 3500000.0,
                        source = "BDO Partner Referral",
                        stage = LeadStage.CONTACTED,
                        notes = "Token advance promised by end of week. Plot #102 put on hold."
                    )
                )

                // Prepopulate Bookings
                val bkgId = dao.insertBooking(
                    BookingEntity(
                        plotId = plot1,
                        projectId = p1Id,
                        plotNumber = "Plot #101",
                        projectName = "Grand Meadows",
                        buyerName = "Rajesh Kumar",
                        buyerPhone = "+91 98450 12345",
                        buyerEmail = "rajesh.kumar@gmail.com",
                        buyerPan = "ABCDE1234F",
                        agreementValue = 2690000.0,
                        govtValue = 1850000.0,
                        downPayment = 500000.0,
                        bookingDate = "15 Sep 2026",
                        status = BookingStatus.PENDING_ACCOUNTS,
                        approvedByCrm = true,
                        approvedByAccounts = false,
                        approvedByManagement = false,
                        paymentMethod = PaymentMethod.RTGS_NEFT,
                        paymentRef = "HDFC-RTGS-9921448"
                    )
                )

                // Prepopulate Installments for Booking #1
                dao.insertInstallments(
                    listOf(
                        InstallmentEntity(
                            bookingId = bkgId,
                            stageName = "Token Advance",
                            percentage = 18.5,
                            amount = 500000.0,
                            dueDate = "15 Sep 2026",
                            paidDate = "15 Sep 2026",
                            status = InstallmentStatus.PAID,
                            receiptNumber = "RCT-2026-001",
                            paymentRef = "HDFC-RTGS-9921448"
                        ),
                        InstallmentEntity(
                            bookingId = bkgId,
                            stageName = "Agreement Signing (25%)",
                            percentage = 25.0,
                            amount = 672500.0,
                            dueDate = "05 Oct 2026",
                            status = InstallmentStatus.UPCOMING
                        ),
                        InstallmentEntity(
                            bookingId = bkgId,
                            stageName = "Road & Utility Laying (25%)",
                            percentage = 25.0,
                            amount = 672500.0,
                            dueDate = "20 Nov 2026",
                            status = InstallmentStatus.UPCOMING
                        ),
                        InstallmentEntity(
                            bookingId = bkgId,
                            stageName = "Registration & Khata (31.5%)",
                            percentage = 31.5,
                            amount = 845000.0,
                            dueDate = "15 Dec 2026",
                            status = InstallmentStatus.UPCOMING
                        )
                    )
                )

                // Prepopulate Treasury Transfers
                dao.insertTreasuryTransfer(
                    TreasuryTransferEntity(
                        fromProjectId = p1Id,
                        fromProjectName = "Grand Meadows",
                        toProjectId = p2Id,
                        toProjectName = "Royal Palms Estate",
                        amount = 1000000.0,
                        transferDate = "12 Sep 2026",
                        narration = "Working capital reallocation for boundary wall construction",
                        referenceNumber = "TRF-2026-102",
                        tallyVoucherNo = "JV-2026-041",
                        isSyncedToTally = true
                    )
                )

                // Prepopulate Incentives
                dao.insertIncentive(
                    IncentiveEntity(
                        bookingId = bkgId,
                        plotNumber = "Plot #101",
                        recipientName = "Amit Verma",
                        recipientRole = "Sales Executive",
                        percentage = 2.0,
                        totalAmount = 53800.0,
                        disbursedAmount = 25000.0,
                        isDisbursed = false,
                        disbursalDate = null,
                        paymentRef = "Pending Accounts Approval"
                    )
                )
            }
        }
    }
}

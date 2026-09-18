package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingStatus
import com.example.data.model.PlotStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TerraDao {

    // Projects
    @Query("SELECT * FROM projects ORDER BY id ASC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    // Plots
    @Query("SELECT * FROM plots WHERE projectId = :projectId ORDER BY id ASC")
    fun getPlotsForProject(projectId: Long): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots ORDER BY id ASC")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE id = :id LIMIT 1")
    suspend fun getPlotById(id: Long): PlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<PlotEntity>)

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Query("UPDATE plots SET status = :status, purchaserName = :name, purchaserPhone = :phone WHERE id = :plotId")
    suspend fun updatePlotPurchaser(plotId: Long, status: PlotStatus, name: String?, phone: String?)

    // Leads
    @Query("SELECT * FROM leads ORDER BY createdAt DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE plotId = :plotId ORDER BY createdAt DESC")
    fun getLeadsForPlot(plotId: Long): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY id DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET approvedByCrm = 1, status = :status WHERE id = :id")
    suspend fun approveCrm(id: Long, status: BookingStatus)

    @Query("UPDATE bookings SET approvedByAccounts = 1, status = :status WHERE id = :id")
    suspend fun approveAccounts(id: Long, status: BookingStatus)

    @Query("UPDATE bookings SET approvedByManagement = 1, status = :status WHERE id = :id")
    suspend fun approveManagement(id: Long, status: BookingStatus)

    // Installments
    @Query("SELECT * FROM installments WHERE bookingId = :bookingId ORDER BY id ASC")
    fun getInstallmentsForBooking(bookingId: Long): Flow<List<InstallmentEntity>>

    @Query("SELECT * FROM installments ORDER BY id ASC")
    fun getAllInstallments(): Flow<List<InstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<InstallmentEntity>)

    @Update
    suspend fun updateInstallment(installment: InstallmentEntity)

    // Treasury Transfers
    @Query("SELECT * FROM treasury_transfers ORDER BY id DESC")
    fun getAllTreasuryTransfers(): Flow<List<TreasuryTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreasuryTransfer(transfer: TreasuryTransferEntity): Long

    @Query("UPDATE treasury_transfers SET isSyncedToTally = 1, tallyVoucherNo = :voucherNo WHERE id = :id")
    suspend fun markTransferSynced(id: Long, voucherNo: String)

    // Incentives
    @Query("SELECT * FROM incentives ORDER BY id DESC")
    fun getAllIncentives(): Flow<List<IncentiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncentive(incentive: IncentiveEntity): Long

    @Update
    suspend fun updateIncentive(incentive: IncentiveEntity)
}

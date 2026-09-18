package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.PlotStatus
import com.example.ui.TerraViewModel
import com.example.ui.components.BookingStatusBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: TerraViewModel,
    onNavigateToSiteMap: () -> Unit,
    onNavigateToLeads: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToTreasury: () -> Unit,
    onNavigateToNewBooking: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val plots by viewModel.plots.collectAsState()
    val leads by viewModel.leads.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val treasuryTransfers by viewModel.treasuryTransfers.collectAsState()

    val totalSalesValue = bookings
        .filter { it.status != BookingStatus.CANCELLED }
        .sumOf { it.agreementValue }

    val pendingApprovalsCount = bookings.count {
        it.status == BookingStatus.PENDING_CRM ||
        it.status == BookingStatus.PENDING_ACCOUNTS ||
        it.status == BookingStatus.PENDING_MANAGEMENT
    }

    val totalTreasuryBalance = projects.sumOf { it.currentBalance }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Welcome Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TERRA OVERVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Executive Real Estate Dashboard",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Live monitoring of land parcels, buyer pipeline, and Tally accounting sync.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 2x2 Stat Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Gross Sales",
                        value = "₹%.1f L".format(totalSalesValue / 100000.0),
                        subtitle = "${bookings.size} total bookings",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = TerracottaPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Leads",
                        value = "${leads.size}",
                        subtitle = "Prospects in pipeline",
                        icon = Icons.Default.People,
                        accentColor = CobaltReserved,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Approvals Due",
                        value = "$pendingApprovalsCount",
                        subtitle = "CRM / Accounts / Mgmt",
                        icon = Icons.Default.PendingActions,
                        accentColor = AmberPending,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Treasury Balance",
                        value = "₹%.1f L".format(totalTreasuryBalance / 100000.0),
                        subtitle = "${projects.size} project bank a/cs",
                        icon = Icons.Default.AccountBalance,
                        accentColor = EmeraldAvailable,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToNewBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book Plot", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToSiteMap,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Site Map", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onNavigateToTreasury,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tally Sync", fontSize = 13.sp)
                }
            }
        }

        // Connected Cloud & API Services Panel
        item {
            val supabaseStatus by viewModel.supabaseStatus.collectAsState()
            val syncMessage by viewModel.supabaseSyncMessage.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PaperSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TerracottaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Connected Services",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Supabase & Meta WhatsApp Cloud API",
                                fontSize = 11.sp,
                                color = InkSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldAvailable.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LIVE .ENV",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAvailable
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                // Supabase Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Supabase Database",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                            Text(
                                text = "zolbuckwnjsxfgqqkcjj",
                                fontSize = 10.sp,
                                color = InkSecondary
                            )
                        }
                        Text(
                            text = "https://zolbuckwnjsxfgqqkcjj.supabase.co",
                            fontSize = 11.sp,
                            color = InkSecondary,
                            maxLines = 1
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.testSupabaseConnection() },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Test Ping", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.syncAllBookingsToSupabase() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Cloud", fontSize = 11.sp)
                        }
                    }
                }

                // Supabase Status Display
                when (val status = supabaseStatus) {
                    is com.example.data.remote.SupabaseConnectionState.Connecting -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Text("Connecting to Supabase endpoint...", fontSize = 12.sp, color = InkSecondary)
                        }
                    }
                    is com.example.data.remote.SupabaseConnectionState.Connected -> {
                        Text(
                            text = "✓ ${status.message}",
                            fontSize = 11.sp,
                            color = EmeraldAvailable,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is com.example.data.remote.SupabaseConnectionState.Error -> {
                        Text(
                            text = "⚠ ${status.error}",
                            fontSize = 11.sp,
                            color = RedDestructive
                        )
                    }
                    else -> Unit
                }

                if (!syncMessage.isNullOrBlank()) {
                    Text(
                        text = "ℹ $syncMessage",
                        fontSize = 11.sp,
                        color = CobaltReserved
                    )
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                // Meta WhatsApp Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "WhatsApp Cloud API",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkPrimary
                            )
                            Text(
                                text = "ID: 1126770290524197",
                                fontSize = 10.sp,
                                color = InkSecondary
                            )
                        }
                        Text(
                            text = "Templates: plot_booking_confirmation · emi_statement",
                            fontSize = 11.sp,
                            color = InkSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldAvailable.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAvailable
                        )
                    }
                }
            }
        }

        // Projects Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Projects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSiteMap) {
                    Text("View All Plots", color = TerracottaPrimary, fontSize = 13.sp)
                }
            }
        }

        items(projects) { project ->
            val projectPlots = plots.filter { it.projectId == project.id }
            val soldOrBooked = projectPlots.count { it.status == PlotStatus.BOOKED || it.status == PlotStatus.SOLD }
            val available = projectPlots.count { it.status == PlotStatus.AVAILABLE }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.selectProject(project.id)
                        onNavigateToSiteMap()
                    }
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = project.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkPrimary
                            )
                            Text(
                                text = project.location,
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = EmeraldAvailable,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = {
                            if (projectPlots.isNotEmpty()) soldOrBooked.toFloat() / projectPlots.size else 0.3f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TerracottaPrimary,
                        trackColor = PaperSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$available Available / ${projectPlots.size.coerceAtLeast(project.totalPlots)} Total Plots",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                        Text(
                            text = "Rate: ₹${project.baseRatePerSqft.toInt()}/sqft",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkPrimary
                        )
                    }
                }
            }
        }

        // Recent Bookings & Approvals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Bookings & Approvals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToBookings) {
                    Text("View All", color = TerracottaPrimary, fontSize = 13.sp)
                }
            }
        }

        items(bookings.take(3)) { booking ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaperSurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToBookings() }
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${booking.plotNumber} • ${booking.buyerName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = InkPrimary
                        )
                        BookingStatusBadge(status = booking.status)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Agreement: ₹%.2f L".format(booking.agreementValue / 100000.0),
                            fontSize = 13.sp,
                            color = InkSecondary
                        )
                        Text(
                            text = "Down Pay: ₹%.2f L".format(booking.downPayment / 100000.0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkPrimary
                        )
                    }
                }
            }
        }
    }
}

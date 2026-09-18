package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlotEntity
import com.example.data.model.PlotStatus
import com.example.ui.TerraViewModel
import com.example.ui.components.PlotStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteMapperScreen(
    viewModel: TerraViewModel,
    onNavigateToBookingForm: (plotId: Long, projectId: Long) -> Unit
) {
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsState()
    val plots by viewModel.plots.collectAsState()
    val leads by viewModel.leads.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<PlotStatus?>(null) }
    var selectedPlotForSheet by remember { mutableStateOf<PlotEntity?>(null) }
    var showReserveDialog by remember { mutableStateOf(false) }
    var showAddPlotDialog by remember { mutableStateOf(false) }

    // Reserve dialog states
    var reserveName by remember { mutableStateOf("") }
    var reservePhone by remember { mutableStateOf("") }

    // Add plot dialog states
    var newPlotNumber by remember { mutableStateOf("") }
    var newPlotArea by remember { mutableStateOf("1500") }
    var newPlotFacing by remember { mutableStateOf("East") }

    val filteredPlots = plots.filter { plot ->
        (selectedProjectId == null || plot.projectId == selectedProjectId) &&
        (selectedStatusFilter == null || plot.status == selectedStatusFilter)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Site Map & Inventory",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${filteredPlots.size} plots shown",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddPlotDialog = true },
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plot")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Project Selector Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedProjectId == null,
                            onClick = { viewModel.selectProject(null) },
                            label = { Text("All Projects") }
                        )
                    }
                    items(projects) { proj ->
                        FilterChip(
                            selected = selectedProjectId == proj.id,
                            onClick = { viewModel.selectProject(proj.id) },
                            label = { Text(proj.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null },
                            label = { Text("All Statuses") }
                        )
                    }
                    items(PlotStatus.entries.toTypedArray()) { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == status) null else status
                            },
                            label = { Text(status.label) }
                        )
                    }
                }
            }
        },
        containerColor = PaperBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredPlots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LayersClear,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = InkMuted
                        )
                        Text("No plots match selected filters", color = InkSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPlots) { plot ->
                        val plotBorderColor = when (plot.status) {
                            PlotStatus.AVAILABLE -> EmeraldAvailable
                            PlotStatus.RESERVED -> CobaltReserved
                            PlotStatus.BOOKED -> OrangeBooked
                            PlotStatus.SOLD -> RedDestructive
                        }

                        val estimatedTotal = (plot.areaSqft * plot.ratePerSqft) + plot.premiumCharge

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PaperSurface)
                                .border(1.5.dp, plotBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { selectedPlotForSheet = plot }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = plot.plotNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = InkPrimary
                                    )
                                    PlotStatusBadge(status = plot.status)
                                }

                                Text(
                                    text = "${plot.areaSqft.toInt()} sqft • ${plot.facing}",
                                    fontSize = 12.sp,
                                    color = InkSecondary
                                )

                                Text(
                                    text = "₹%.2f L".format(estimatedTotal / 100000.0),
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaDark
                                )

                                if (plot.purchaserName != null) {
                                    Text(
                                        text = "Buyer: ${plot.purchaserName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = InkPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Plot Details Bottom Sheet
    selectedPlotForSheet?.let { plot ->
        val associatedLeads = leads.filter { it.plotId == plot.id }
        val estimatedTotal = (plot.areaSqft * plot.ratePerSqft) + plot.premiumCharge

        ModalBottomSheet(
            onDismissRequest = { selectedPlotForSheet = null },
            containerColor = PaperSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = plot.plotNumber,
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Dimensions: ${plot.areaSqft.toInt()} sqft • ${plot.facing}",
                            fontSize = 13.sp,
                            color = InkSecondary
                        )
                    }
                    PlotStatusBadge(status = plot.status)
                }

                HorizontalDivider(color = BorderSubtle)

                // Financial Breakdown
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PRICE BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Rate (₹${plot.ratePerSqft.toInt()}/sqft)", color = InkSecondary, fontSize = 13.sp)
                        Text("₹%.2f L".format((plot.areaSqft * plot.ratePerSqft) / 100000.0), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    if (plot.premiumCharge > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Corner / Park Premium", color = InkSecondary, fontSize = 13.sp)
                            Text("₹%.2f L".format(plot.premiumCharge / 100000.0), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Estimated Value", fontWeight = FontWeight.Bold, color = InkPrimary, fontSize = 14.sp)
                        Text(
                            "₹%.2f L".format(estimatedTotal / 100000.0),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TerracottaDark,
                            fontSize = 16.sp
                        )
                    }
                }

                // Purchaser Info if Booked/Reserved
                if (plot.purchaserName != null) {
                    HorizontalDivider(color = BorderSubtle)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ASSIGNED PURCHASER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkMuted,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plot.purchaserName ?: "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(plot.purchaserPhone ?: "", color = InkSecondary, fontSize = 12.sp)
                            }
                            if (!plot.purchaserPhone.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${plot.purchaserPhone}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = EmeraldAvailable)
                                }
                            }
                        }
                    }
                }

                // Leads Assigned to Plot
                if (associatedLeads.isNotEmpty()) {
                    HorizontalDivider(color = BorderSubtle)
                    Text(
                        text = "LEADS ENQUIRING (${associatedLeads.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )
                    associatedLeads.forEach { lead ->
                        Text(
                            text = "• ${lead.name} (${lead.stage.label}) - Budget ₹%.1f L".format(lead.budget / 100000.0),
                            fontSize = 13.sp,
                            color = InkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (plot.status == PlotStatus.AVAILABLE) {
                        OutlinedButton(
                            onClick = {
                                showReserveDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reserve Plot")
                        }

                        Button(
                            onClick = {
                                val pId = plot.id
                                val prjId = plot.projectId
                                selectedPlotForSheet = null
                                onNavigateToBookingForm(pId, prjId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Book Plot")
                        }
                    } else if (plot.status == PlotStatus.RESERVED) {
                        Button(
                            onClick = {
                                val pId = plot.id
                                val prjId = plot.projectId
                                selectedPlotForSheet = null
                                onNavigateToBookingForm(pId, prjId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Convert to Formal Booking")
                        }
                    }
                }
            }
        }
    }

    // Reserve Plot Dialog
    if (showReserveDialog && selectedPlotForSheet != null) {
        val plot = selectedPlotForSheet!!
        AlertDialog(
            onDismissRequest = { showReserveDialog = false },
            title = { Text("Reserve ${plot.plotNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Place temporary hold on this plot for a prospective buyer:")
                    OutlinedTextField(
                        value = reserveName,
                        onValueChange = { reserveName = it },
                        label = { Text("Buyer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reservePhone,
                        onValueChange = { reservePhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reserveName.isNotBlank()) {
                            viewModel.reservePlot(plot.id, null, reserveName, reservePhone)
                            showReserveDialog = false
                            selectedPlotForSheet = null
                            reserveName = ""
                            reservePhone = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Confirm Reserve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReserveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Plot Dialog
    if (showAddPlotDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlotDialog = false },
            title = { Text("Add Land Plot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPlotNumber,
                        onValueChange = { newPlotNumber = it },
                        label = { Text("Plot Number (e.g. Plot #109)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPlotArea,
                        onValueChange = { newPlotArea = it },
                        label = { Text("Area (sqft)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPlotFacing,
                        onValueChange = { newPlotFacing = it },
                        label = { Text("Facing (e.g. East, North, Corner)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pId = selectedProjectId ?: projects.firstOrNull()?.id ?: 1L
                        val area = newPlotArea.toDoubleOrNull() ?: 1200.0
                        val prj = projects.find { it.id == pId }
                        val rate = prj?.baseRatePerSqft ?: 2200.0
                        if (newPlotNumber.isNotBlank()) {
                            viewModel.addPlot(pId, newPlotNumber, area, rate, 0.0, newPlotFacing)
                            showAddPlotDialog = false
                            newPlotNumber = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Create Plot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

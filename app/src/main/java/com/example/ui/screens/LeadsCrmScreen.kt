package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.LeadEntity
import com.example.data.model.LeadStage
import com.example.ui.TerraViewModel
import com.example.ui.components.LeadStageBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsCrmScreen(
    viewModel: TerraViewModel,
    onNavigateToBookingWithLead: (leadId: Long, projectId: Long, plotId: Long?) -> Unit
) {
    val context = LocalContext.current
    val leads by viewModel.leads.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val plots by viewModel.plots.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStageFilter by remember { mutableStateOf<LeadStage?>(null) }
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var selectedLeadForStageChange by remember { mutableStateOf<LeadEntity?>(null) }

    // Add Lead Form State
    var leadName by remember { mutableStateOf("") }
    var leadPhone by remember { mutableStateOf("") }
    var leadEmail by remember { mutableStateOf("") }
    var leadBudget by remember { mutableStateOf("3000000") }
    var leadSource by remember { mutableStateOf("Walk-in Site Visit") }
    var leadNotes by remember { mutableStateOf("") }

    val filteredLeads = leads.filter { lead ->
        (selectedStageFilter == null || lead.stage == selectedStageFilter) &&
        (searchQuery.isBlank() || lead.name.contains(searchQuery, ignoreCase = true) || lead.phone.contains(searchQuery))
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
                            text = "Leads Pipeline & CRM",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${filteredLeads.size} prospects active",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddLeadDialog = true },
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Lead")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by buyer name or phone...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = InkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PaperSurface,
                        unfocusedContainerColor = PaperSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stage Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStageFilter == null,
                            onClick = { selectedStageFilter = null },
                            label = { Text("All Stages (${leads.size})") }
                        )
                    }
                    items(LeadStage.entries.toTypedArray()) { stage ->
                        val count = leads.count { it.stage == stage }
                        FilterChip(
                            selected = selectedStageFilter == stage,
                            onClick = {
                                selectedStageFilter = if (selectedStageFilter == stage) null else stage
                            },
                            label = { Text("${stage.label} ($count)") }
                        )
                    }
                }
            }
        },
        containerColor = PaperBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredLeads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterListOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = InkMuted
                            )
                            Text("No leads in this stage or search query", color = InkSecondary)
                        }
                    }
                }
            } else {
                items(filteredLeads) { lead ->
                    val plot = plots.find { it.id == lead.plotId }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = lead.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = "Source: ${lead.source}",
                                        fontSize = 12.sp,
                                        color = InkSecondary
                                    )
                                }
                                LeadStageBadge(stage = lead.stage)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Budget: ₹%.1f L".format(lead.budget / 100000.0),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = TerracottaDark
                                )
                                if (plot != null) {
                                    Text(
                                        text = "Interested: ${plot.plotNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CobaltReserved
                                    )
                                }
                            }

                            if (lead.notes.isNotBlank()) {
                                Text(
                                    text = "\"${lead.notes}\"",
                                    fontSize = 13.sp,
                                    color = InkSecondary,
                                    lineHeight = 18.sp
                                )
                            }

                            if (!lead.meetingDate.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = AmberPending
                                    )
                                    Text(
                                        text = "Meeting: ${lead.meetingDate}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AmberPending
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))

                            // Action Shortcuts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Phone Call
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "Call",
                                            tint = EmeraldAvailable,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // WhatsApp Shortcut
                                    IconButton(
                                        onClick = {
                                            val cleanPhone = lead.phone.replace("+", "").replace(" ", "")
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = "WhatsApp",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Move Stage Button
                                    OutlinedButton(
                                        onClick = { selectedLeadForStageChange = lead },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Move Stage", fontSize = 12.sp)
                                    }
                                }

                                // Convert to Booking
                                Button(
                                    onClick = {
                                        onNavigateToBookingWithLead(lead.id, lead.projectId, lead.plotId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Book", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Move Stage Dialog
    selectedLeadForStageChange?.let { lead ->
        AlertDialog(
            onDismissRequest = { selectedLeadForStageChange = null },
            title = { Text("Update Pipeline Stage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select current progression for ${lead.name}:")
                    LeadStage.entries.forEach { stage ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (lead.stage == stage) PaperSurfaceVariant else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = lead.stage == stage,
                                onClick = {
                                    viewModel.updateLeadStage(lead, stage)
                                    selectedLeadForStageChange = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LeadStageBadge(stage = stage)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedLeadForStageChange = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Lead Dialog
    if (showAddLeadDialog) {
        AlertDialog(
            onDismissRequest = { showAddLeadDialog = false },
            title = { Text("Add Prospective Buyer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = leadName,
                        onValueChange = { leadName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = leadPhone,
                        onValueChange = { leadPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = leadEmail,
                        onValueChange = { leadEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = leadBudget,
                        onValueChange = { leadBudget = it },
                        label = { Text("Budget (INR)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = leadNotes,
                        onValueChange = { leadNotes = it },
                        label = { Text("Requirement / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pId = projects.firstOrNull()?.id ?: 1L
                        val bgt = leadBudget.toDoubleOrNull() ?: 2500000.0
                        if (leadName.isNotBlank() && leadPhone.isNotBlank()) {
                            viewModel.addLead(
                                projectId = pId,
                                plotId = null,
                                name = leadName,
                                phone = leadPhone,
                                email = leadEmail,
                                budget = bgt,
                                source = leadSource,
                                notes = leadNotes,
                                meetingDate = null
                            )
                            showAddLeadDialog = false
                            leadName = ""
                            leadPhone = ""
                            leadEmail = ""
                            leadNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Add Lead")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLeadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

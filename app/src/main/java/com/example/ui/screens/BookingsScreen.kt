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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookingEntity
import com.example.data.model.BookingStatus
import com.example.ui.TerraViewModel
import com.example.ui.components.BookingStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    viewModel: TerraViewModel,
    onNavigateToInstallments: (bookingId: Long) -> Unit,
    onNavigateToNewBooking: () -> Unit
) {
    val bookings by viewModel.bookings.collectAsState()
    var selectedBookingForApproval by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedBookingForCancel by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedBookingForTallyXml by remember { mutableStateOf<BookingEntity?>(null) }

    var cancelReason by remember { mutableStateOf("") }
    var refundAmount by remember { mutableStateOf("") }

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
                            text = "Plot Bookings & Approvals",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${bookings.size} formal bookings recorded",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = onNavigateToNewBooking,
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Booking")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (bookings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No bookings made yet.", color = InkSecondary)
                    }
                }
            } else {
                items(bookings) { booking ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(PaperSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${booking.plotNumber} • ${booking.projectName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = "Buyer: ${booking.buyerName} (${booking.buyerPhone})",
                                        fontSize = 13.sp,
                                        color = InkSecondary
                                    )
                                }
                                BookingStatusBadge(status = booking.status)
                            }

                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                            // Financials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("AGREEMENT VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkMuted)
                                    Text(
                                        "₹%.2f L".format(booking.agreementValue / 100000.0),
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDark
                                    )
                                }
                                Column {
                                    Text("DOWN PAYMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkMuted)
                                    Text(
                                        "₹%.2f L".format(booking.downPayment / 100000.0),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkPrimary
                                    )
                                }
                                Column {
                                    Text("GOVT VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkMuted)
                                    Text(
                                        "₹%.2f L".format(booking.govtValue / 100000.0),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = InkSecondary
                                    )
                                }
                            }

                            // Sequential 3-Step Approval Pipeline Indicators
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "3-TIER APPROVAL WORKFLOW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkMuted
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ApprovalStagePill(
                                        title = "1. CRM",
                                        isApproved = booking.approvedByCrm,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ApprovalStagePill(
                                        title = "2. Accounts",
                                        isApproved = booking.approvedByAccounts,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ApprovalStagePill(
                                        title = "3. Mgmt",
                                        isApproved = booking.approvedByManagement,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { onNavigateToInstallments(booking.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Installments", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { selectedBookingForTallyXml = booking },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tally XML", fontSize = 12.sp)
                                    }

                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    OutlinedButton(
                                        onClick = { viewModel.sendBookingWhatsApp(booking, context) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldAvailable),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldAvailable.copy(alpha = 0.5f))),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 12.sp)
                                    }
                                }

                                if (booking.status != BookingStatus.APPROVED && booking.status != BookingStatus.CANCELLED) {
                                    Button(
                                        onClick = {
                                            viewModel.advanceBookingApproval(booking.id, booking.status)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = when (booking.status) {
                                                BookingStatus.PENDING_CRM -> "Approve CRM"
                                                BookingStatus.PENDING_ACCOUNTS -> "Approve A/C"
                                                BookingStatus.PENDING_MANAGEMENT -> "Final Signoff"
                                                else -> "Approve"
                                            },
                                            fontSize = 12.sp
                                        )
                                    }
                                } else if (booking.status == BookingStatus.APPROVED) {
                                    TextButton(
                                        onClick = {
                                            selectedBookingForCancel = booking
                                            refundAmount = booking.downPayment.toString()
                                        }
                                    ) {
                                        Text("Cancel Booking", color = RedDestructive, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancellation Dialog
    selectedBookingForCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { selectedBookingForCancel = null },
            title = { Text("Cancel Booking #${booking.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cancelling this booking will release ${booking.plotNumber} back to Available inventory.")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = refundAmount,
                        onValueChange = { refundAmount = it },
                        label = { Text("Refund Settlement Amount (INR)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ref = refundAmount.toDoubleOrNull() ?: 0.0
                        viewModel.cancelBooking(booking.id, cancelReason, ref)
                        selectedBookingForCancel = null
                        cancelReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDestructive)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBookingForCancel = null }) {
                    Text("Back")
                }
            }
        )
    }

    // Tally XML Dialog
    selectedBookingForTallyXml?.let { booking ->
        val xmlContent = viewModel.generateTallySalesXml(booking)
        AlertDialog(
            onDismissRequest = { selectedBookingForTallyXml = null },
            title = { Text("Tally Prime Sales Voucher XML") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Standard Tally Prime schema for ledger entry & sales integration:",
                        fontSize = 12.sp,
                        color = InkSecondary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PaperSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = xmlContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = InkPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBookingForTallyXml = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    val whatsAppResult by viewModel.lastWhatsAppResult.collectAsState()
    val screenContext = androidx.compose.ui.platform.LocalContext.current

    whatsAppResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissWhatsAppResult() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null,
                        tint = if (result.success) EmeraldAvailable else TerracottaPrimary
                    )
                    Text("WhatsApp Cloud Dispatch")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = result.message,
                        fontSize = 13.sp,
                        color = InkPrimary
                    )
                    Text(
                        text = "Mode: ${result.mode} · Phone ID: 1126770290524197",
                        fontSize = 11.sp,
                        color = InkSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        result.deepLinkUrl?.let { url ->
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            screenContext.startActivity(intent)
                        }
                        viewModel.dismissWhatsAppResult()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable)
                ) {
                    Text("Open WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissWhatsAppResult() }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ApprovalStagePill(
    title: String,
    isApproved: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isApproved) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
    val textColor = if (isApproved) EmeraldAvailable else InkMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

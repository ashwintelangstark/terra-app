package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.local.InstallmentEntity
import com.example.data.model.InstallmentStatus
import com.example.ui.TerraViewModel
import com.example.ui.components.InstallmentStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentsScreen(
    viewModel: TerraViewModel,
    highlightBookingId: Long? = null
) {
    val installments by viewModel.installments.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    var selectedInstallmentForPayment by remember { mutableStateOf<InstallmentEntity?>(null) }
    var paymentRefInput by remember { mutableStateOf("") }

    val displayInstallments = if (highlightBookingId != null) {
        installments.filter { it.bookingId == highlightBookingId }
    } else {
        installments
    }

    val totalAmount = displayInstallments.sumOf { it.amount }
    val collectedAmount = displayInstallments
        .filter { it.status == InstallmentStatus.PAID }
        .sumOf { it.amount }
    val pendingAmount = totalAmount - collectedAmount

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Installment Schedules & Ledger",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Construction-linked milestone billing & receipt generation",
                    fontSize = 12.sp,
                    color = InkSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metric banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaperSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("COLLECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAvailable)
                        Text(
                            "₹%.2f L".format(collectedAmount / 100000.0),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }
                    Column {
                        Text("OUTSTANDING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberPending)
                        Text(
                            "₹%.2f L".format(pendingAmount / 100000.0),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDark
                        )
                    }
                    Column {
                        Text("TOTAL DEMAND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkMuted)
                        Text(
                            "₹%.2f L".format(totalAmount / 100000.0),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
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
            if (displayInstallments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No installments recorded.", color = InkSecondary)
                    }
                }
            } else {
                items(displayInstallments) { inst ->
                    val booking = bookings.find { it.id == inst.bookingId }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = inst.stageName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = InkPrimary
                                    )
                                    if (booking != null) {
                                        Text(
                                            text = "${booking.plotNumber} • ${booking.buyerName}",
                                            fontSize = 12.sp,
                                            color = InkSecondary
                                        )
                                    }
                                }
                                InstallmentStatusBadge(status = inst.status)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Amount Due: ₹%.2f L".format(inst.amount / 100000.0),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDark
                                    )
                                    Text(
                                        text = "Due Date: ${inst.dueDate}",
                                        fontSize = 12.sp,
                                        color = InkSecondary
                                    )
                                }

                                if (inst.status == InstallmentStatus.UPCOMING || inst.status == InstallmentStatus.OVERDUE) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        OutlinedButton(
                                            onClick = { viewModel.sendInstallmentWhatsApp(inst, context) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldAvailable),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldAvailable.copy(alpha = 0.5f))),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                selectedInstallmentForPayment = inst
                                                paymentRefInput = "UTR-${(100000..999999).random()}"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Record Receipt", fontSize = 12.sp)
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        OutlinedButton(
                                            onClick = { viewModel.sendInstallmentWhatsApp(inst, context) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldAvailable),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldAvailable.copy(alpha = 0.5f))),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Send Statement", fontSize = 11.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Paid on ${inst.paidDate ?: "Settled"}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = EmeraldAvailable
                                            )
                                            Text(
                                                text = inst.receiptNumber ?: "Verified",
                                                fontSize = 11.sp,
                                                color = InkSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog
    selectedInstallmentForPayment?.let { inst ->
        AlertDialog(
            onDismissRequest = { selectedInstallmentForPayment = null },
            title = { Text("Collect Installment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Milestone: ${inst.stageName}")
                    Text(
                        text = "Amount: ₹%.2f L".format(inst.amount / 100000.0),
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDark
                    )
                    OutlinedTextField(
                        value = paymentRefInput,
                        onValueChange = { paymentRefInput = it },
                        label = { Text("UTR / Cheque / Bank Reference") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.payInstallment(inst, paymentRefInput)
                        selectedInstallmentForPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInstallmentForPayment = null }) {
                    Text("Cancel")
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

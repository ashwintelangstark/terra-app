package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
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
import com.example.data.local.IncentiveEntity
import com.example.ui.TerraViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncentivesScreen(
    viewModel: TerraViewModel
) {
    val incentives by viewModel.incentives.collectAsState()
    var selectedIncentiveForPayout by remember { mutableStateOf<IncentiveEntity?>(null) }
    var payoutRefInput by remember { mutableStateOf("") }

    val totalCommission = incentives.sumOf { it.totalAmount }
    val totalDisbursed = incentives.sumOf { it.disbursedAmount }
    val pendingDisbursal = totalCommission - totalDisbursed

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "BDO & Sales Incentives",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Channel partner commissions and sales team disbursals",
                    fontSize = 12.sp,
                    color = InkSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        Text("DISBURSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAvailable)
                        Text(
                            "₹%.2f L".format(totalDisbursed / 100000.0),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                    }
                    Column {
                        Text("PAYABLE DUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberPending)
                        Text(
                            "₹%.2f L".format(pendingDisbursal / 100000.0),
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDark
                        )
                    }
                    Column {
                        Text("TOTAL EARNED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkMuted)
                        Text(
                            "₹%.2f L".format(totalCommission / 100000.0),
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
            if (incentives.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No incentive records logged yet.", color = InkSecondary)
                    }
                }
            } else {
                items(incentives) { incentive ->
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
                                        text = "${incentive.recipientName} (${incentive.recipientRole})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = "On ${incentive.plotNumber} • ${incentive.percentage}% Commission",
                                        fontSize = 12.sp,
                                        color = InkSecondary
                                    )
                                }

                                if (incentive.isDisbursed) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "DISBURSED",
                                            color = EmeraldAvailable,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PENDING",
                                            color = AmberPending,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Commission: ₹%.2f L".format(incentive.totalAmount / 100000.0),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaDark
                                    )
                                    Text(
                                        text = if (incentive.isDisbursed) "Paid: ${incentive.disbursalDate}" else incentive.paymentRef ?: "Pending",
                                        fontSize = 12.sp,
                                        color = InkSecondary
                                    )
                                }

                                if (!incentive.isDisbursed) {
                                    Button(
                                        onClick = {
                                            selectedIncentiveForPayout = incentive
                                            payoutRefInput = "NEFT-${(100000..999999).random()}"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Disburse Payout", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payout Dialog
    selectedIncentiveForPayout?.let { incentive ->
        AlertDialog(
            onDismissRequest = { selectedIncentiveForPayout = null },
            title = { Text("Disburse Incentive") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recipient: ${incentive.recipientName}")
                    Text(
                        text = "Amount: ₹%.2f L".format(incentive.totalAmount / 100000.0),
                        fontWeight = FontWeight.Bold,
                        color = TerracottaDark
                    )
                    OutlinedTextField(
                        value = payoutRefInput,
                        onValueChange = { payoutRefInput = it },
                        label = { Text("Bank Payment Ref / UTR") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disburseIncentive(incentive, payoutRefInput)
                        selectedIncentiveForPayout = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAvailable)
                ) {
                    Text("Confirm Disbursal")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedIncentiveForPayout = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

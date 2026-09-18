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
import com.example.data.local.TreasuryTransferEntity
import com.example.ui.TerraViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen(
    viewModel: TerraViewModel
) {
    val projects by viewModel.projects.collectAsState()
    val transfers by viewModel.treasuryTransfers.collectAsState()

    var showNewTransferDialog by remember { mutableStateOf(false) }
    var selectedTransferForXml by remember { mutableStateOf<TreasuryTransferEntity?>(null) }

    // New Transfer Form
    var fromProjectIndex by remember { mutableIntStateOf(0) }
    var toProjectIndex by remember { mutableIntStateOf(1) }
    var transferAmount by remember { mutableStateOf("500000") }
    var transferNarration by remember { mutableStateOf("Boundary and drainage civil works allocation") }

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
                            text = "Treasury & Tally Sync",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Inter-project fund transfers and accounting sync",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    }

                    FloatingActionButton(
                        onClick = { showNewTransferDialog = true },
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "New Transfer")
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
            // Project Bank Accounts Header
            item {
                Text(
                    text = "PROJECT DESIGNATED ESCROW & BANK ACCOUNTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkMuted,
                    letterSpacing = 0.5.sp
                )
            }

            items(projects) { project ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaperSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = project.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = InkPrimary
                            )
                            Text(
                                text = "${project.bankName} • A/C: ${project.bankAccountNo}",
                                fontSize = 12.sp,
                                color = InkSecondary
                            )
                            Text(
                                text = "IFSC: ${project.ifscCode}",
                                fontSize = 11.sp,
                                color = InkMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹%.2f L".format(project.currentBalance / 100000.0),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAvailable
                            )
                            Text("Available", fontSize = 10.sp, color = InkMuted)
                        }
                    }
                }
            }

            // Transfer Logs Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTER-PROJECT TRANSFERS & TALLY PRIME STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            items(transfers) { transfer ->
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
                            Text(
                                text = "${transfer.fromProjectName} ➔ ${transfer.toProjectName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = InkPrimary
                            )
                            Text(
                                text = "₹%.2f L".format(transfer.amount / 100000.0),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaDark
                            )
                        }

                        Text(
                            text = "\"${transfer.narration}\"",
                            fontSize = 12.sp,
                            color = InkSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${transfer.transferDate} • Ref: ${transfer.referenceNumber}",
                                fontSize = 11.sp,
                                color = InkMuted
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (transfer.isSyncedToTally) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFDCFCE7))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Tally: ${transfer.tallyVoucherNo}",
                                            color = EmeraldAvailable,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.syncTransferToTally(transfer.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CobaltReserved),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Sync Tally", fontSize = 11.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { selectedTransferForXml = transfer },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Code,
                                        contentDescription = "View XML",
                                        tint = InkSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Transfer Dialog
    if (showNewTransferDialog && projects.size >= 2) {
        AlertDialog(
            onDismissRequest = { showNewTransferDialog = false },
            title = { Text("Inter-Project Fund Transfer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("From Project: ${projects[fromProjectIndex % projects.size].name}")
                    Text("To Project: ${projects[(toProjectIndex) % projects.size].name}")
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Transfer Amount (INR)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = transferNarration,
                        onValueChange = { transferNarration = it },
                        label = { Text("Narration / Purpose") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmount.toDoubleOrNull() ?: 100000.0
                        val fromP = projects[fromProjectIndex % projects.size]
                        val toP = projects[toProjectIndex % projects.size]
                        viewModel.addTreasuryTransfer(
                            fromProjectId = fromP.id,
                            fromProjectName = fromP.name,
                            toProjectId = toP.id,
                            toProjectName = toP.name,
                            amount = amt,
                            narration = transferNarration
                        )
                        showNewTransferDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Record Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTransferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // XML Dialog
    selectedTransferForXml?.let { transfer ->
        val xml = """
            <ENVELOPE>
              <HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>
              <BODY>
                <IMPORTDATA>
                  <REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME></REQUESTDESC>
                  <REQUESTDATA>
                    <TALLYMESSAGE xmlns:UDF="TallyUDF">
                      <VOUCHER VCHTYPE="Journal" ACTION="Create">
                        <DATE>20260917</DATE>
                        <NARRATION>${transfer.narration} (Ref: ${transfer.referenceNumber})</NARRATION>
                        <ALLLEDGERENTRIES.LIST>
                          <LEDGERNAME>${transfer.toProjectName} Escrow</LEDGERNAME>
                          <ISDEEMEDPOSITIVE>YES</ISDEEMEDPOSITIVE>
                          <AMOUNT>-${transfer.amount}</AMOUNT>
                        </ALLLEDGERENTRIES.LIST>
                        <ALLLEDGERENTRIES.LIST>
                          <LEDGERNAME>${transfer.fromProjectName} Escrow</LEDGERNAME>
                          <ISDEEMEDPOSITIVE>NO</ISDEEMEDPOSITIVE>
                          <AMOUNT>${transfer.amount}</AMOUNT>
                        </ALLLEDGERENTRIES.LIST>
                      </VOUCHER>
                    </TALLYMESSAGE>
                  </REQUESTDATA>
                </IMPORTDATA>
              </BODY>
            </ENVELOPE>
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { selectedTransferForXml = null },
            title = { Text("Tally Journal Voucher XML") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PaperSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = xml,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = InkPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTransferForXml = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

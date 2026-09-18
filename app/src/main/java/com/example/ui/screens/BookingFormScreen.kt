package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.ui.TerraViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    viewModel: TerraViewModel,
    initialPlotId: Long? = null,
    initialProjectId: Long? = null,
    initialLeadId: Long? = null,
    onBookingCreated: () -> Unit,
    onBack: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val plots by viewModel.plots.collectAsState()
    val leads by viewModel.leads.collectAsState()

    val targetLead = leads.find { it.id == initialLeadId }
    val availablePlots = plots.filter { it.status == com.example.data.model.PlotStatus.AVAILABLE || it.id == initialPlotId }

    var selectedPlotId by remember {
        mutableStateOf(initialPlotId ?: availablePlots.firstOrNull()?.id ?: 1L)
    }

    val selectedPlot = plots.find { it.id == selectedPlotId }
    val defaultAgreementVal = selectedPlot?.let {
        (it.areaSqft * it.ratePerSqft) + it.premiumCharge
    } ?: 2500000.0

    // Form inputs
    var buyerName by remember { mutableStateOf(targetLead?.name ?: "") }
    var buyerPhone by remember { mutableStateOf(targetLead?.phone ?: "") }
    var buyerEmail by remember { mutableStateOf(targetLead?.email ?: "") }
    var buyerPan by remember { mutableStateOf("") }
    var agreementValStr by remember { mutableStateOf(defaultAgreementVal.toInt().toString()) }
    var govtValStr by remember { mutableStateOf((defaultAgreementVal * 0.70).toInt().toString()) }
    var downPaymentStr by remember { mutableStateOf((defaultAgreementVal * 0.20).toInt().toString()) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.RTGS_NEFT) }
    var paymentRef by remember { mutableStateOf("UTR-${(100000..999999).random()}") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Plot Booking",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PaperBackground)
            )
        },
        containerColor = PaperBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Plot Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. LAND PARCEL DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )

                    if (selectedPlot != null) {
                        Text(
                            text = "${selectedPlot.plotNumber} (${selectedPlot.areaSqft.toInt()} sqft • ${selectedPlot.facing})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "Base Rate: ₹${selectedPlot.ratePerSqft.toInt()}/sqft • Premium: ₹${selectedPlot.premiumCharge.toInt()}",
                            fontSize = 13.sp,
                            color = InkSecondary
                        )
                    }
                }
            }

            // Section 2: Buyer Info
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. BUYER IDENTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it },
                        label = { Text("Purchaser Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buyerPhone,
                        onValueChange = { buyerPhone = it },
                        label = { Text("Contact Mobile (+91)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buyerEmail,
                        onValueChange = { buyerEmail = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buyerPan,
                        onValueChange = { buyerPan = it.uppercase() },
                        label = { Text("PAN Card / Tax ID (e.g. ABCDE1234F)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 3: Financial Terms
            Card(
                colors = CardDefaults.cardColors(containerColor = PaperSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3. FINANCIAL & PAYMENT TERMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkMuted,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = agreementValStr,
                        onValueChange = { agreementValStr = it },
                        label = { Text("Total Agreement Value (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = govtValStr,
                        onValueChange = { govtValStr = it },
                        label = { Text("Govt / Circle Rate Value (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = downPaymentStr,
                        onValueChange = { downPaymentStr = it },
                        label = { Text("Initial Down Payment (INR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text("Bank Payment Ref / UTR / Cheque No.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (buyerName.isNotBlank() && buyerPhone.isNotBlank()) {
                        val agr = agreementValStr.toDoubleOrNull() ?: defaultAgreementVal
                        val gvt = govtValStr.toDoubleOrNull() ?: (agr * 0.70)
                        val down = downPaymentStr.toDoubleOrNull() ?: (agr * 0.20)
                        val prjId = selectedPlot?.projectId ?: initialProjectId ?: 1L

                        viewModel.bookPlot(
                            plotId = selectedPlotId,
                            projectId = prjId,
                            leadId = initialLeadId,
                            buyerName = buyerName,
                            buyerPhone = buyerPhone,
                            buyerEmail = buyerEmail,
                            buyerPan = if (buyerPan.isBlank()) "ABCDE1234F" else buyerPan,
                            agreementValue = agr,
                            govtValue = gvt,
                            downPayment = down,
                            paymentMethod = selectedPaymentMethod,
                            paymentRef = paymentRef
                        )
                        onBookingCreated()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Generate Formal Booking & EMI Schedule",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

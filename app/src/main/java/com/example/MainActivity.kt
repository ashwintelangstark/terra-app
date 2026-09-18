package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.TerraViewModel
import com.example.ui.screens.*
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PaperSurface
import com.example.ui.theme.TerraTheme
import com.example.ui.theme.TerracottaPrimary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Overview", Icons.Default.Dashboard)
    data object SiteMap : Screen("site_map", "Site Map", Icons.Default.Map)
    data object Leads : Screen("leads", "Leads", Icons.Default.People)
    data object Bookings : Screen("bookings", "Bookings", Icons.Default.Assignment)
    data object Installments : Screen("installments", "Ledger", Icons.Default.Payments)
    data object Treasury : Screen("treasury", "Treasury", Icons.Default.AccountBalance)
    data object Incentives : Screen("incentives", "Incentives", Icons.Default.MonetizationOn)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TerraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TerraTheme {
                TerraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TerraApp(viewModel: TerraViewModel) {
    var currentScreen by remember { mutableStateOf<String>(Screen.Dashboard.route) }
    var bookingFormPlotId by remember { mutableStateOf<Long?>(null) }
    var bookingFormProjectId by remember { mutableStateOf<Long?>(null) }
    var bookingFormLeadId by remember { mutableStateOf<Long?>(null) }
    var highlightBookingId by remember { mutableStateOf<Long?>(null) }

    val bottomNavScreens = listOf(
        Screen.Dashboard,
        Screen.SiteMap,
        Screen.Leads,
        Screen.Bookings,
        Screen.Treasury
    )

    Scaffold(
        bottomBar = {
            if (currentScreen != "booking_form") {
                NavigationBar(
                    containerColor = PaperSurface,
                    contentColor = TerracottaPrimary,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentScreen == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen.route },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TerracottaPrimary,
                                selectedTextColor = TerracottaPrimary,
                                indicatorColor = PaperBackground
                            )
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = PaperBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Dashboard.route -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSiteMap = { currentScreen = Screen.SiteMap.route },
                        onNavigateToLeads = { currentScreen = Screen.Leads.route },
                        onNavigateToBookings = { currentScreen = Screen.Bookings.route },
                        onNavigateToTreasury = { currentScreen = Screen.Treasury.route },
                        onNavigateToNewBooking = {
                            bookingFormPlotId = null
                            bookingFormProjectId = null
                            bookingFormLeadId = null
                            currentScreen = "booking_form"
                        }
                    )
                }

                Screen.SiteMap.route -> {
                    SiteMapperScreen(
                        viewModel = viewModel,
                        onNavigateToBookingForm = { plotId, projectId ->
                            bookingFormPlotId = plotId
                            bookingFormProjectId = projectId
                            bookingFormLeadId = null
                            currentScreen = "booking_form"
                        }
                    )
                }

                Screen.Leads.route -> {
                    LeadsCrmScreen(
                        viewModel = viewModel,
                        onNavigateToBookingWithLead = { leadId, projectId, plotId ->
                            bookingFormLeadId = leadId
                            bookingFormProjectId = projectId
                            bookingFormPlotId = plotId
                            currentScreen = "booking_form"
                        }
                    )
                }

                Screen.Bookings.route -> {
                    BookingsScreen(
                        viewModel = viewModel,
                        onNavigateToInstallments = { bkgId ->
                            highlightBookingId = bkgId
                            currentScreen = Screen.Installments.route
                        },
                        onNavigateToNewBooking = {
                            bookingFormPlotId = null
                            bookingFormProjectId = null
                            bookingFormLeadId = null
                            currentScreen = "booking_form"
                        }
                    )
                }

                Screen.Installments.route -> {
                    InstallmentsScreen(
                        viewModel = viewModel,
                        highlightBookingId = highlightBookingId
                    )
                }

                Screen.Treasury.route -> {
                    TreasuryScreen(
                        viewModel = viewModel
                    )
                }

                Screen.Incentives.route -> {
                    IncentivesScreen(
                        viewModel = viewModel
                    )
                }

                "booking_form" -> {
                    BookingFormScreen(
                        viewModel = viewModel,
                        initialPlotId = bookingFormPlotId,
                        initialProjectId = bookingFormProjectId,
                        initialLeadId = bookingFormLeadId,
                        onBookingCreated = {
                            currentScreen = Screen.Bookings.route
                        },
                        onBack = {
                            currentScreen = Screen.Bookings.route
                        }
                    )
                }
            }
        }
    }
}

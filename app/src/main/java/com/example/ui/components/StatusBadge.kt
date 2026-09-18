package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.InstallmentStatus
import com.example.data.model.LeadStage
import com.example.data.model.PlotStatus
import com.example.ui.theme.*

@Composable
fun PlotStatusBadge(status: PlotStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        PlotStatus.AVAILABLE -> Color(0xFFE8F5E9) to EmeraldAvailable
        PlotStatus.RESERVED -> Color(0xFFE3F2FD) to CobaltReserved
        PlotStatus.BOOKED -> Color(0xFFFFF3E0) to OrangeBooked
        PlotStatus.SOLD -> Color(0xFFFFEBEE) to RedDestructive
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LeadStageBadge(stage: LeadStage, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (stage) {
        LeadStage.NEW -> Color(0xFFEDE7F6) to PurpleAccent
        LeadStage.CONTACTED -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
        LeadStage.MEETING_SCHEDULED -> Color(0xFFFEF3C7) to AmberPending
        LeadStage.NEGOTIATING -> Color(0xFFFFEDD5) to OrangeBooked
        LeadStage.CONVERTED -> Color(0xFFDCFCE7) to EmeraldAvailable
        LeadStage.DROPPED -> Color(0xFFFEE2E2) to RedDestructive
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = stage.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun BookingStatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        BookingStatus.PENDING_CRM -> Color(0xFFFEF3C7) to AmberPending
        BookingStatus.PENDING_ACCOUNTS -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
        BookingStatus.PENDING_MANAGEMENT -> Color(0xFFEDE7F6) to PurpleAccent
        BookingStatus.APPROVED -> Color(0xFFDCFCE7) to EmeraldAvailable
        BookingStatus.CANCELLED -> Color(0xFFFEE2E2) to RedDestructive
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun InstallmentStatusBadge(status: InstallmentStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        InstallmentStatus.PAID -> Color(0xFFDCFCE7) to EmeraldAvailable
        InstallmentStatus.UPCOMING -> Color(0xFFF1F5F9) to InkSecondary
        InstallmentStatus.OVERDUE -> Color(0xFFFEE2E2) to RedDestructive
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

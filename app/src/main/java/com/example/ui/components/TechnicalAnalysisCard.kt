package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TechnicalAnalysisResult
import com.example.model.TechnicalSignal
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard

@Composable
fun TechnicalAnalysisCard(
    analysis: TechnicalAnalysisResult,
    modifier: Modifier = Modifier
) {
    val signalColor = when (analysis.signal) {
        TechnicalSignal.STRONG_BUY, TechnicalSignal.BUY -> BullGreen
        TechnicalSignal.STRONG_SELL, TechnicalSignal.SELL -> BearRed
        TechnicalSignal.NEUTRAL -> AccentGold
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TradingDarkBorder, RoundedCornerShape(12.dp))
            .testTag("ict_analysis_card_${analysis.symbol}"),
        shape = RoundedCornerShape(12.dp),
        color = TradingDarkCard
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Symbol, Market Type, ICT Signal Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(signalColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = analysis.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${analysis.marketType} (ICT)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Glowing Signal Badge
                Box(
                    modifier = Modifier
                        .background(signalColor.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                        .border(1.dp, signalColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = analysis.signal.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = signalColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Institutional Bias & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = analysis.institutionalBias,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Algorithmic Confidence: ${analysis.confidence}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Confidence progress bar
            LinearProgressIndicator(
                progress = { analysis.confidence / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = AccentCyan,
                trackColor = Color(0x3300D8F6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Column ICT PD Array Breakdown: Structure, DOL, Array
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IctPill(
                    label = "STRUCTURE",
                    value = if (analysis.marketStructure.contains("MSS")) "MSS Confirmed" else "BOS Trend",
                    subText = "Displacement Leg",
                    valueColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                IctPill(
                    label = "DRAW ON LIQ.",
                    value = if (analysis.drawOnLiquidity.contains("BSL")) "BSL Pool" else "SSL Pool",
                    subText = "External Range",
                    valueColor = if (analysis.drawOnLiquidity.contains("BSL")) BullGreen else BearRed,
                    modifier = Modifier.weight(1f)
                )
                IctPill(
                    label = "PD ARRAY",
                    value = if (analysis.premiumDiscount.contains("Discount")) "Discount OTE" else "Premium",
                    subText = "0.62 - 0.79 Fib",
                    valueColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detailed PD Arrays: FVG & Order Block
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0B101B),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TradingDarkBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📦 FAIR VALUE GAP (FVG)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(analysis.fairValueGap, fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🧱 INSTITUTIONAL ORDER BLOCK (OB)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Text(analysis.orderBlock, fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Execution Blueprint (OTE Entry, Target DOL, Invalidation)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F141E),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SMART MONEY EXECUTION PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "R : R ${analysis.riskReward}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("OTE Entry Zone", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(analysis.entryZone, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Target DOL (BSL/SSL)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(analysis.targetPrice, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Invalidation", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(analysis.stopLoss, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BearRed)
                        }
                    }
                }
            }

            // Killzone Timing Badge
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Timing: ${analysis.killzoneTiming}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun IctPill(
    label: String,
    value: String,
    subText: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF0F141E),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = label, fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = subText, fontSize = 9.sp, color = Color(0xFF64748B))
        }
    }
}

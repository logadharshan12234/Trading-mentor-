package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketInstrument
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import java.util.Locale

@Composable
fun CandlestickChart(
    instrument: MarketInstrument,
    modifier: Modifier = Modifier,
    initialChartType: String = "CANDLE" // "CANDLE" or "LINE"
) {
    val candles = instrument.candles
    if (candles.isEmpty()) return

    var chartMode by remember { mutableStateOf(initialChartType) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val activeCandle = selectedIndex?.let { idx ->
        if (idx in candles.indices) candles[idx] else candles.last()
    } ?: candles.last()

    val minPrice = candles.minOf { it.low }.coerceAtMost(instrument.sslPool.toFloat() * 0.998f)
    val maxPrice = candles.maxOf { it.high }.coerceAtLeast(instrument.bslPool.toFloat() * 1.002f)
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001f)
    val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1f)

    val isForex = instrument.category.name == "FOREX"
    val fmt = if (isForex) "%.4f" else if (instrument.price < 10) "%.4f" else "%.2f"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TradingDarkBorder, RoundedCornerShape(12.dp))
            .testTag("candlestick_chart_${instrument.symbol}"),
        shape = RoundedCornerShape(12.dp),
        color = TradingDarkCard
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Symbol, Active Candle Info, Chart Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = instrument.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (instrument.changePercent >= 0) BullGreen.copy(alpha = 0.2f) else BearRed.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ICT 4H PD Arrays",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (instrument.changePercent >= 0) BullGreen else BearRed
                            )
                        }
                    }
                    Text(
                        text = "O: ${String.format(Locale.US, fmt, activeCandle.open)} | H: ${String.format(Locale.US, fmt, activeCandle.high)} | L: ${String.format(Locale.US, fmt, activeCandle.low)} | C: ${String.format(Locale.US, fmt, activeCandle.close)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { chartMode = if (chartMode == "CANDLE") "LINE" else "CANDLE" },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (chartMode == "CANDLE") Icons.Default.Timeline else Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Toggle Chart Style",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ICT Legend: FVG, Order Block, BSL/SSL, 50% EQ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("FVG Imbalance", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(AccentGold.copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Order Block", fontSize = 9.sp, color = AccentGold, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BSL / SSL", fontSize = 9.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (selectedIndex != null) "Tap to unfreeze" else "Drag to inspect",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(candles) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val candleWidth = size.width / candles.size
                                    val idx = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                }
                            )
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val candleCount = candles.size
                    val candleSpacing = canvasWidth / candleCount
                    val candleBodyWidth = (candleSpacing * 0.65f).coerceAtLeast(2f)

                    val topPadding = 12f
                    val bottomPadding = 34f
                    val plotHeight = canvasHeight - topPadding - bottomPadding
                    val volumeAreaHeight = 26f

                    // Helper to convert price to canvas Y
                    fun priceToY(price: Float): Float {
                        return topPadding + (1f - (price - minPrice) / priceRange) * plotHeight
                    }

                    // 1. Draw ICT 50% Equilibrium Line (Dividing Premium & Discount)
                    val eqY = priceToY(instrument.equilibrium50.toFloat())
                    drawLine(
                        color = Color(0x55E2E8F0),
                        start = Offset(0f, eqY),
                        end = Offset(canvasWidth, eqY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // 2. Draw Buy-Side Liquidity (BSL) target line
                    val bslY = priceToY(instrument.bslPool.toFloat())
                    drawLine(
                        color = BullGreen.copy(alpha = 0.7f),
                        start = Offset(0f, bslY),
                        end = Offset(canvasWidth, bslY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // 3. Draw Sell-Side Liquidity (SSL) target line
                    val sslY = priceToY(instrument.sslPool.toFloat())
                    drawLine(
                        color = BearRed.copy(alpha = 0.7f),
                        start = Offset(0f, sslY),
                        end = Offset(canvasWidth, sslY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // 4. Draw Institutional Order Block (OB) Shaded Zone
                    val obTopY = priceToY(instrument.orderBlockHigh.toFloat())
                    val obBottomY = priceToY(instrument.orderBlockLow.toFloat())
                    val obHeight = (Math.abs(obBottomY - obTopY)).coerceAtLeast(4f)
                    drawRect(
                        color = AccentGold.copy(alpha = 0.18f),
                        topLeft = Offset(0f, minOf(obTopY, obBottomY)),
                        size = Size(canvasWidth, obHeight)
                    )
                    drawLine(
                        color = AccentGold.copy(alpha = 0.45f),
                        start = Offset(0f, minOf(obTopY, obBottomY)),
                        end = Offset(canvasWidth, minOf(obTopY, obBottomY)),
                        strokeWidth = 1f
                    )

                    // 5. Draw Fair Value Gap (FVG / BISI / SIBI) Shaded Zone
                    val fvgTopY = priceToY(instrument.fvgHigh.toFloat())
                    val fvgBottomY = priceToY(instrument.fvgLow.toFloat())
                    val fvgHeight = (Math.abs(fvgBottomY - fvgTopY)).coerceAtLeast(4f)
                    drawRect(
                        color = AccentCyan.copy(alpha = 0.16f),
                        topLeft = Offset(canvasWidth * 0.45f, minOf(fvgTopY, fvgBottomY)),
                        size = Size(canvasWidth * 0.55f, fvgHeight)
                    )
                    drawLine(
                        color = AccentCyan.copy(alpha = 0.6f),
                        start = Offset(canvasWidth * 0.45f, minOf(fvgTopY, fvgBottomY)),
                        end = Offset(canvasWidth, minOf(fvgTopY, fvgBottomY)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = AccentCyan.copy(alpha = 0.6f),
                        start = Offset(canvasWidth * 0.45f, maxOf(fvgTopY, fvgBottomY)),
                        end = Offset(canvasWidth, maxOf(fvgTopY, fvgBottomY)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // 6. Draw Displacement Volume Bars at bottom
                    for (i in candles.indices) {
                        val candle = candles[i]
                        val xCenter = i * candleSpacing + (candleSpacing / 2f)
                        val volHeight = (candle.volume / maxVolume) * volumeAreaHeight
                        val volY = canvasHeight - volHeight
                        val isBull = candle.close >= candle.open
                        drawRect(
                            color = if (isBull) BullGreen.copy(alpha = 0.28f) else BearRed.copy(alpha = 0.28f),
                            topLeft = Offset(xCenter - (candleBodyWidth / 2f), volY),
                            size = Size(candleBodyWidth, volHeight)
                        )
                    }

                    // 7. Draw Line or Candlesticks
                    if (chartMode == "LINE") {
                        val linePath = Path()
                        val fillPath = Path()
                        var firstX = 0f
                        var lastX = 0f

                        for (i in candles.indices) {
                            val candle = candles[i]
                            val x = i * candleSpacing + (candleSpacing / 2f)
                            val y = priceToY(candle.close)

                            if (i == 0) {
                                firstX = x
                                linePath.moveTo(x, y)
                                fillPath.moveTo(x, canvasHeight - bottomPadding)
                                fillPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            if (i == candles.size - 1) lastX = x
                        }
                        fillPath.lineTo(lastX, canvasHeight - bottomPadding)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            color = AccentCyan.copy(alpha = 0.15f)
                        )
                        drawPath(
                            path = linePath,
                            color = AccentCyan,
                            style = Stroke(width = 2.5f)
                        )
                    } else {
                        // Candlestick rendering
                        for (i in candles.indices) {
                            val candle = candles[i]
                            val xCenter = i * candleSpacing + (candleSpacing / 2f)
                            val isBull = candle.close >= candle.open
                            val color = if (isBull) BullGreen else BearRed

                            val highY = priceToY(candle.high)
                            val lowY = priceToY(candle.low)
                            val openY = priceToY(candle.open)
                            val closeY = priceToY(candle.close)

                            // Wick line
                            drawLine(
                                color = color,
                                start = Offset(xCenter, highY),
                                end = Offset(xCenter, lowY),
                                strokeWidth = 1.5f
                            )

                            // Body rectangle
                            val bodyTop = minOf(openY, closeY)
                            val bodyHeight = (Math.abs(closeY - openY)).coerceAtLeast(2f)
                            drawRect(
                                color = color,
                                topLeft = Offset(xCenter - (candleBodyWidth / 2f), bodyTop),
                                size = Size(candleBodyWidth, bodyHeight)
                            )
                        }
                    }

                    // 8. Crosshair for selected candle
                    selectedIndex?.let { selIdx ->
                        if (selIdx in candles.indices) {
                            val candle = candles[selIdx]
                            val x = selIdx * candleSpacing + (candleSpacing / 2f)
                            val y = priceToY(candle.close)

                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(x, 0f),
                                end = Offset(x, canvasHeight),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )

                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )

                            drawCircle(
                                color = AccentCyan,
                                radius = 4.5f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // Bottom Labels: BSL Target, 50% EQ, SSL Pool
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SSL: ${String.format(Locale.US, fmt, instrument.sslPool)}",
                    fontSize = 10.sp,
                    color = BearRed,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "50% EQ: ${String.format(Locale.US, fmt, instrument.equilibrium50)}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "BSL: ${String.format(Locale.US, fmt, instrument.bslPool)}",
                    fontSize = 10.sp,
                    color = BullGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

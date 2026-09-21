package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCategory
import com.example.model.MarketInstrument
import com.example.ui.theme.BearRed
import com.example.ui.theme.BullGreen
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import java.util.Locale

@Composable
fun TickerRibbon(
    instruments: List<MarketInstrument>,
    selectedCategory: MarketCategory,
    onInstrumentClick: (MarketInstrument) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = if (selectedCategory == MarketCategory.ALL) {
        instruments
    } else {
        instruments.filter { it.category == selectedCategory }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticker_ribbon"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtered, key = { it.symbol }) { item ->
            TickerItemCard(
                instrument = item,
                onClick = { onInstrumentClick(item) }
            )
        }
    }
}

@Composable
private fun TickerItemCard(
    instrument: MarketInstrument,
    onClick: () -> Unit
) {
    val isBull = instrument.changePercent >= 0
    val trendColor = if (isBull) BullGreen else BearRed
    val isForex = instrument.category == MarketCategory.FOREX
    val priceStr = if (isForex) {
        String.format(Locale.US, "%.4f", instrument.price)
    } else if (instrument.price < 10) {
        String.format(Locale.US, "$%.4f", instrument.price)
    } else {
        String.format(Locale.US, "$%,.2f", instrument.price)
    }

    Surface(
        modifier = Modifier
            .width(138.dp)
            .border(1.dp, TradingDarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("ticker_card_${instrument.symbol}"),
        shape = RoundedCornerShape(10.dp),
        color = TradingDarkCard
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Top: Symbol & Mini Sparkline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = instrument.category.iconText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = instrument.symbol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Mini Sparkline Graph
                MiniSparkline(
                    candles = instrument.candles,
                    color = trendColor,
                    modifier = Modifier
                        .width(36.dp)
                        .height(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom: Price & % pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                val sign = if (isBull) "+" else ""
                Box(
                    modifier = Modifier
                        .background(trendColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$sign${String.format(Locale.US, "%.1f", instrument.changePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniSparkline(
    candles: List<com.example.model.CandleData>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (candles.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val min = candles.minOf { it.close }
        val max = candles.maxOf { it.close }
        val range = (max - min).coerceAtLeast(0.0001f)

        val step = width / (candles.size - 1)
        val path = Path()

        candles.forEachIndexed { idx, c ->
            val x = idx * step
            val y = height - ((c.close - min) / range) * height
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5f)
        )
    }
}

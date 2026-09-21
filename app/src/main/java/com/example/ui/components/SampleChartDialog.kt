package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import com.example.ui.theme.TradingDarkSurface
import kotlin.random.Random

data class SampleChartPreset(
    val id: String,
    val title: String,
    val assetClass: String,
    val description: String,
    val isBullish: Boolean
)

@Composable
fun SampleChartDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Bitmap, String) -> Unit
) {
    val context = LocalContext.current

    // Zero-permission Photo Picker for gallery selection
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    onImageSelected(bitmap, "Analyze this trading chart under pure ICT / Smart Money Concepts. Identify Liquidity sweeps (BSL/SSL), Market Structure Shifts (MSS), Fair Value Gaps (FVG), Order Blocks, and Optimal Trade Entry (OTE).")
                    onDismiss()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val presets = remember {
        listOf(
            SampleChartPreset(
                id = "xau_usd_gold_sweep",
                title = "XAU/USD Gold NY AM Sweep & OTE",
                assetClass = "Commodities • 15M ICT",
                description = "Liquidity purge of Asian session highs followed by displacement creating BISI Fair Value Gap into OTE (0.62-0.79 Fib).",
                isBullish = true
            ),
            SampleChartPreset(
                id = "btc_liquidity_sweep",
                title = "BTC/USD Liquidity Sweep & MSS into FVG",
                assetClass = "Crypto • 4H ICT",
                description = "Asian session SSL sweep followed by aggressive displacement creating a 4H BISI Fair Value Gap.",
                isBullish = true
            ),
            SampleChartPreset(
                id = "nvda_order_block",
                title = "NVDA Daily Institutional Order Block",
                assetClass = "Stocks • Daily ICT",
                description = "Mitigation of clean down-close Order Block in Discount array with NY AM displacement.",
                isBullish = true
            ),
            SampleChartPreset(
                id = "eur_usd_judas_swing",
                title = "EUR/USD London Open Judas Swing",
                assetClass = "Forex • 15M ICT",
                description = "Classic London manipulation run on Asian highs followed by MSS into 15M OTE (0.62-0.79 Fib).",
                isBullish = true
            ),
            SampleChartPreset(
                id = "eth_breaker_block",
                title = "ETH/USD Bearish Breaker & SIBI Imbalance",
                assetClass = "Crypto • 4H ICT",
                description = "Failed higher high sweeping BSL liquidity followed by sharp displacement through previous swing low.",
                isBullish = false
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TradingDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, TradingDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Multimodal Chart Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Upload chart photo or pick pattern preset",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gallery Button
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Choose from Photo Gallery",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "OR SELECT REALISTIC CHART PRESET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets list
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        val previewBitmap = remember(preset.id) {
                            generateSyntheticChartBitmap(preset)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TradingDarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    onImageSelected(
                                        previewBitmap,
                                        "Analyze this ${preset.title} (${preset.assetClass}) chart image and provide technical indicators, pattern identification, and entry/exit levels."
                                    )
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = TradingDarkCard
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = previewBitmap.asImageBitmap(),
                                    contentDescription = preset.title,
                                    modifier = Modifier
                                        .size(60.dp, 44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = preset.assetClass,
                                        fontSize = 10.sp,
                                        color = AccentCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = preset.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1
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

private fun generateSyntheticChartBitmap(preset: SampleChartPreset): Bitmap {
    val width = 480
    val height = 300
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dark Background
    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0F141E") }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Grid lines
    val gridPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        strokeWidth = 1f
    }
    for (y in 50..250 step 50) {
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
    }

    // Draw Candlesticks
    val candleCount = 24
    val spacing = width.toFloat() / candleCount
    val candleBodyWidth = spacing * 0.6f

    val greenPaint = Paint().apply { color = android.graphics.Color.parseColor("#00C087") }
    val redPaint = Paint().apply { color = android.graphics.Color.parseColor("#F43F5E") }

    val random = Random(preset.id.hashCode())
    var currentY = if (preset.isBullish) 200f else 100f

    for (i in 0 until candleCount) {
        val x = i * spacing + (spacing / 2f)
        val trend = if (preset.isBullish) -3f else 3f
        val drift = (random.nextFloat() - 0.45f) * 18f + trend
        val openY = currentY
        val closeY = (openY + drift).coerceIn(40f, 260f)

        val highY = minOf(openY, closeY) - random.nextFloat() * 14f
        val lowY = maxOf(openY, closeY) + random.nextFloat() * 14f

        val isBull = closeY <= openY // in canvas, lower Y is higher price
        val paint = if (isBull) greenPaint else redPaint

        // Wick
        paint.strokeWidth = 2f
        canvas.drawLine(x, highY, x, lowY, paint)

        // Body
        val top = minOf(openY, closeY)
        val bottom = maxOf(openY, closeY).coerceAtLeast(top + 2f)
        canvas.drawRect(RectF(x - candleBodyWidth / 2f, top, x + candleBodyWidth / 2f, bottom), paint)

        currentY = closeY
    }

    // Title label on top-left of bitmap
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 18f
        isFakeBoldText = true
    }
    canvas.drawText(preset.title, 14f, 28f, textPaint)

    return bitmap
}

package com.example.model

import android.graphics.Bitmap

enum class MarketCategory(val label: String, val iconText: String) {
    ALL("All Markets", "🌐"),
    CRYPTO("Crypto", "🪙"),
    STOCKS("Stocks", "📈"),
    FOREX("Forex", "💱"),
    COMMODITIES("Commodities", "🏆")
}

enum class TechnicalSignal(val label: String, val score: Int) {
    STRONG_BUY("BULLISH EXPANSION", 2),
    BUY("DISCOUNT OTE BUY", 1),
    NEUTRAL("RANGE CONSOLIDATION", 0),
    SELL("PREMIUM OTE SELL", -1),
    STRONG_SELL("BEARISH EXPANSION", -2)
}

data class CandleData(
    val timestamp: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float,
    val isFvg: Boolean = false,
    val fvgTop: Float = 0f,
    val fvgBottom: Float = 0f
)

data class MarketInstrument(
    val symbol: String,
    val name: String,
    val category: MarketCategory,
    val price: Double,
    val changePercent: Double,
    val changeAmount: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: String,
    // Pure ICT Institutional Metrics
    val institutionalBias: String, // "Bullish Order Flow", "Bearish Delivery", "Internal Rebalance"
    val marketStructure: String,  // "MSS Confirmed (Displacement)", "BOS Continuation", "Liquidity Sweep"
    val bslPool: Double,          // Buy-Side Liquidity Pool (Equal Highs / Old Highs)
    val sslPool: Double,          // Sell-Side Liquidity Pool (Equal Lows / Old Lows)
    val fvgHigh: Double,          // Fair Value Gap (Upper boundary)
    val fvgLow: Double,           // Fair Value Gap (Lower boundary)
    val orderBlockHigh: Double,   // Institutional Order Block Zone High
    val orderBlockLow: Double,    // Institutional Order Block Zone Low
    val equilibrium50: Double,    // 50% Equilibrium (Discount below, Premium above)
    val killzoneWindow: String = "NY AM Session (08:30 - 11:00 EST)",
    val candles: List<CandleData> = emptyList(),
    val signal: TechnicalSignal = TechnicalSignal.BUY
)

data class TechnicalAnalysisResult(
    val symbol: String,
    val marketType: String,
    val institutionalBias: String, // "Bullish Order Flow", "Bearish Delivery"
    val signal: TechnicalSignal,
    val confidence: Int, // e.g. 91%
    val marketStructure: String,   // "MSS with Displacement", "BOS Bullish", "Liquidity Purge"
    val drawOnLiquidity: String,   // "BSL: Buy-Side Liquidity Pool at $94,800"
    val fairValueGap: String,      // "4H BISI ($91,200 - $91,850) [Unmitigated]"
    val orderBlock: String,        // "Bullish Order Block ($90,400 - $90,950)"
    val premiumDiscount: String,   // "Discount Zone (OTE 0.62 - 0.79 Fib)"
    val killzoneTiming: String,    // "NY AM Killzone & Silver Bullet Window"
    val entryZone: String,         // "OTE 0.62 - 0.705 Fib Entry"
    val targetPrice: String,       // Target Draw on Liquidity (BSL / SSL)
    val stopLoss: String,          // Invalidation (Beyond OB Mean Threshold)
    val riskReward: String,        // "1 : 3.4"
    val timeframe: String = "4H / 15M",
    val summary: String
)

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBitmap: Bitmap? = null,
    val associatedInstrument: MarketInstrument? = null,
    val technicalAnalysis: TechnicalAnalysisResult? = null,
    val followUpChips: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

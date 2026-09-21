package com.example.data

import com.example.model.CandleData
import com.example.model.MarketCategory
import com.example.model.MarketInstrument
import com.example.model.TechnicalAnalysisResult
import com.example.model.TechnicalSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class MarketRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _instruments = MutableStateFlow<List<MarketInstrument>>(createInitialInstruments())
    val instruments: StateFlow<List<MarketInstrument>> = _instruments.asStateFlow()

    init {
        startLiveTickerStream()
    }

    private fun startLiveTickerStream() {
        scope.launch {
            while (true) {
                delay(2500) // Live market tick update every 2.5 seconds
                tickRandomInstrument()
            }
        }
    }

    private fun tickRandomInstrument() {
        val current = _instruments.value.toMutableList()
        if (current.isEmpty()) return

        // Pick 2-3 instruments to tick
        val count = Random.nextInt(2, 4)
        for (i in 0 until count) {
            val idx = Random.nextInt(current.size)
            val inst = current[idx]

            // Realistic micro-tick: -0.25% to +0.25%
            val deltaPct = (Random.nextDouble(-0.35, 0.40))
            val newPrice = (inst.price * (1.0 + deltaPct / 100.0)).coerceAtLeast(0.0001)
            val newChangeAmount = inst.changeAmount + (newPrice - inst.price)
            val newChangePercent = (newChangeAmount / (newPrice - newChangeAmount)) * 100.0

            // Update high/low
            val newHigh = maxOf(inst.high24h, newPrice)
            val newLow = minOf(inst.low24h, newPrice)

            // Update last candle
            val updatedCandles = inst.candles.toMutableList()
            if (updatedCandles.isNotEmpty()) {
                val last = updatedCandles.last()
                val updatedLast = last.copy(
                    close = newPrice.toFloat(),
                    high = maxOf(last.high, newPrice.toFloat()),
                    low = minOf(last.low, newPrice.toFloat()),
                    volume = last.volume + Random.nextInt(5, 50).toFloat()
                )
                updatedCandles[updatedCandles.size - 1] = updatedLast
            }

            // ICT Signal based on Market Structure & Premium/Discount
            val isDiscount = newPrice < inst.equilibrium50
            val newSignal = when {
                inst.marketStructure.contains("MSS") && isDiscount -> TechnicalSignal.STRONG_BUY
                isDiscount && newChangePercent > 0 -> TechnicalSignal.BUY
                !isDiscount && newChangePercent < 0 -> TechnicalSignal.SELL
                inst.marketStructure.contains("Bearish") && !isDiscount -> TechnicalSignal.STRONG_SELL
                else -> TechnicalSignal.NEUTRAL
            }

            current[idx] = inst.copy(
                price = newPrice,
                changePercent = newChangePercent,
                changeAmount = newChangeAmount,
                high24h = newHigh,
                low24h = newLow,
                candles = updatedCandles,
                signal = newSignal
            )
        }
        _instruments.value = current
    }

    fun findInstrument(query: String): MarketInstrument? {
        val q = query.trim().lowercase(Locale.ROOT)
        val list = _instruments.value

        // Direct symbol match
        list.firstOrNull { it.symbol.lowercase(Locale.ROOT) == q }?.let { return it }

        // Cleaned symbol match e.g. "btc" in "btc/usd"
        list.firstOrNull {
            val cleanSym = it.symbol.lowercase(Locale.ROOT).replace("/", "")
            cleanSym.startsWith(q) || cleanSym.contains(q)
        }?.let { return it }

        // Name match
        list.firstOrNull { it.name.lowercase(Locale.ROOT).contains(q) }?.let { return it }

        // Keyword synonyms
        if (q.contains("bitcoin") || q.contains("btc")) return list.firstOrNull { it.symbol.startsWith("BTC") }
        if (q.contains("ethereum") || q.contains("eth")) return list.firstOrNull { it.symbol.startsWith("ETH") }
        if (q.contains("solana") || q.contains("sol")) return list.firstOrNull { it.symbol.startsWith("SOL") }
        if (q.contains("nvidia") || q.contains("nvda")) return list.firstOrNull { it.symbol == "NVDA" }
        if (q.contains("apple") || q.contains("aapl")) return list.firstOrNull { it.symbol == "AAPL" }
        if (q.contains("tesla") || q.contains("tsla")) return list.firstOrNull { it.symbol == "TSLA" }
        if (q.contains("spy") || q.contains("s&p") || q.contains("sp500")) return list.firstOrNull { it.symbol == "SPY" }
        if (q.contains("euro") || q.contains("eur")) return list.firstOrNull { it.symbol == "EUR/USD" }
        if (q.contains("pound") || q.contains("gbp")) return list.firstOrNull { it.symbol == "GBP/USD" }
        if (q.contains("yen") || q.contains("jpy")) return list.firstOrNull { it.symbol == "USD/JPY" }
        if (q.contains("gold") || q.contains("xau")) return list.firstOrNull { it.symbol.contains("XAU") }
        if (q.contains("silver") || q.contains("xag")) return list.firstOrNull { it.symbol.contains("XAG") }

        // Category fallback
        if (q.contains("crypto")) return list.firstOrNull { it.category == MarketCategory.CRYPTO }
        if (q.contains("stock")) return list.firstOrNull { it.category == MarketCategory.STOCKS }
        if (q.contains("forex") || q.contains("fx")) return list.firstOrNull { it.category == MarketCategory.FOREX }
        if (q.contains("commodity") || q.contains("commodities") || q.contains("metal")) return list.firstOrNull { it.category == MarketCategory.COMMODITIES }

        return null
    }

    fun getAutomatedTechnicalAnalysis(instrument: MarketInstrument): TechnicalAnalysisResult {
        val isBullish = instrument.changePercent >= 0
        val isForex = instrument.category == MarketCategory.FOREX
        val fmt = if (isForex) "%.4f" else if (instrument.price < 10) "%.4f" else "%.2f"

        val bias = if (isBullish) {
            "Bullish Order Flow (Displacement into BSL)"
        } else {
            "Bearish Delivery (Institutional Distribution into SSL)"
        }

        val structure = if (isBullish) {
            "MSS (Market Structure Shift) with Displacement Leg"
        } else {
            "Bearish MSS (Liquidity Sweep & Break of Internal Lows)"
        }

        val drawOnLiquidity = if (isBullish) {
            "BSL (Buy-Side Liquidity Pool): Equal Highs at ${String.format(Locale.US, fmt, instrument.bslPool)}"
        } else {
            "SSL (Sell-Side Liquidity Pool): Equal Lows at ${String.format(Locale.US, fmt, instrument.sslPool)}"
        }

        val fvgLabel = if (isBullish) {
            "4H BISI (Buyside Imbalance): ${String.format(Locale.US, fmt, instrument.fvgLow)} - ${String.format(Locale.US, fmt, instrument.fvgHigh)} [Unmitigated]"
        } else {
            "4H SIBI (Sellside Imbalance): ${String.format(Locale.US, fmt, instrument.fvgLow)} - ${String.format(Locale.US, fmt, instrument.fvgHigh)} [Active Resistance]"
        }

        val obLabel = if (isBullish) {
            "Bullish Order Block (Last Down-Close Candle): ${String.format(Locale.US, fmt, instrument.orderBlockLow)} - ${String.format(Locale.US, fmt, instrument.orderBlockHigh)}"
        } else {
            "Bearish Order Block (Last Up-Close Candle): ${String.format(Locale.US, fmt, instrument.orderBlockLow)} - ${String.format(Locale.US, fmt, instrument.orderBlockHigh)}"
        }

        val premDisc = if (instrument.price < instrument.equilibrium50) {
            "Discount Array (OTE 0.62 - 0.79 Retracement)"
        } else {
            "Premium Array (Above 50% Equilibrium at ${String.format(Locale.US, fmt, instrument.equilibrium50)})"
        }

        val entryZone = if (isBullish) {
            "${String.format(Locale.US, fmt, instrument.orderBlockLow)} - ${String.format(Locale.US, fmt, instrument.fvgLow)}"
        } else {
            "${String.format(Locale.US, fmt, instrument.fvgHigh)} - ${String.format(Locale.US, fmt, instrument.orderBlockHigh)}"
        }

        val targetPrice = if (isBullish) {
            String.format(Locale.US, fmt, instrument.bslPool)
        } else {
            String.format(Locale.US, fmt, instrument.sslPool)
        }

        val stopLoss = if (isBullish) {
            String.format(Locale.US, fmt, instrument.orderBlockLow * 0.992)
        } else {
            String.format(Locale.US, fmt, instrument.orderBlockHigh * 1.008)
        }

        val summary = """
            ### 🏛️ ICT Smart Money Analysis: ${instrument.name} (${instrument.symbol})

            • **Institutional Bias**: $bias
            • **Market Structure**: $structure
            • **Draw on Liquidity (DOL)**: $drawOnLiquidity
            • **Fair Value Gap**: $fvgLabel
            • **Order Block (OB)**: $obLabel
            • **Equilibrium Array**: $premDisc
            • **Killzone Window**: ${instrument.killzoneWindow}
        """.trimIndent()

        return TechnicalAnalysisResult(
            symbol = instrument.symbol,
            marketType = instrument.category.label,
            institutionalBias = bias,
            signal = instrument.signal,
            confidence = (84 + (Math.abs(instrument.changePercent) * 2.2)).toInt().coerceIn(80, 96),
            marketStructure = structure,
            drawOnLiquidity = drawOnLiquidity,
            fairValueGap = fvgLabel,
            orderBlock = obLabel,
            premiumDiscount = premDisc,
            killzoneTiming = instrument.killzoneWindow,
            entryZone = entryZone,
            targetPrice = targetPrice,
            stopLoss = stopLoss,
            riskReward = "1 : 3.4",
            timeframe = "4H / 15M Execution",
            summary = summary
        )
    }

    fun getMarketOverviewText(): String {
        val list = _instruments.value
        val sb = StringBuilder()
        sb.append("ICT Institutional Market Snapshot (Smart Money Concepts):\n")
        MarketCategory.entries.filter { it != MarketCategory.ALL }.forEach { cat ->
            sb.append("\n**${cat.label}**:\n")
            list.filter { it.category == cat }.take(3).forEach { item ->
                val sign = if (item.changePercent >= 0) "+" else ""
                val priceStr = if (item.category == MarketCategory.FOREX) {
                    String.format(Locale.US, "%.4f", item.price)
                } else {
                    String.format(Locale.US, "$%,.2f", item.price)
                }
                sb.append("• ${item.symbol}: $priceStr ($sign${String.format(Locale.US, "%.2f", item.changePercent)}%) | Bias: ${item.institutionalBias} | DOL Target: ${item.bslPool}\n")
            }
        }
        return sb.toString()
    }

    private fun createInitialInstruments(): List<MarketInstrument> {
        val list = mutableListOf<MarketInstrument>()

        // 1. CRYPTO
        list.add(
            createIctInstrument(
                symbol = "BTC/USD",
                name = "Bitcoin",
                category = MarketCategory.CRYPTO,
                basePrice = 89450.0,
                changePercent = 3.42,
                volume = "$38.4B",
                structure = "MSS Bullish with Clean Displacement",
                bias = "Bullish Order Flow (Internal to External Range Run)"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "ETH/USD",
                name = "Ethereum",
                category = MarketCategory.CRYPTO,
                basePrice = 3120.0,
                changePercent = 2.15,
                volume = "$19.8B",
                structure = "Displacement into 4H BISI Gap",
                bias = "Bullish Expansion (Draw on Equal Highs BSL)"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "SOL/USD",
                name = "Solana",
                category = MarketCategory.CRYPTO,
                basePrice = 178.50,
                changePercent = 5.64,
                volume = "$8.2B",
                structure = "BOS Bullish & Institutional Accumulation",
                bias = "Aggressive Bullish Delivery (Silver Bullet Target)"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "XRP/USD",
                name = "Ripple",
                category = MarketCategory.CRYPTO,
                basePrice = 0.8420,
                changePercent = -1.18,
                volume = "$2.4B",
                structure = "Liquidity Sweep of Session Lows",
                bias = "Judas Swing Manipulation (Reversal Watch)"
            )
        )

        // 2. EQUITIES / STOCKS
        list.add(
            createIctInstrument(
                symbol = "NVDA",
                name = "NVIDIA Corporation",
                category = MarketCategory.STOCKS,
                basePrice = 142.80,
                changePercent = 4.12,
                volume = "$24.5B",
                structure = "Displacement out of Daily Bullish Order Block",
                bias = "Bullish Expansion (Unmitigated All-Time BSL Target)"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "AAPL",
                name = "Apple Inc.",
                category = MarketCategory.STOCKS,
                basePrice = 228.40,
                changePercent = 1.08,
                volume = "$12.1B",
                structure = "Mitigating 1H Bullish Breaker Block",
                bias = "Institutional Accumulation in Discount Array"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "TSLA",
                name = "Tesla Inc.",
                category = MarketCategory.STOCKS,
                basePrice = 345.20,
                changePercent = -2.85,
                volume = "$16.7B",
                structure = "Liquidity Purge of Previous Day High followed by MSS",
                bias = "Bearish Delivery into Sell-Side Liquidity (SSL)"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "SPY",
                name = "S&P 500 ETF Trust",
                category = MarketCategory.STOCKS,
                basePrice = 592.30,
                changePercent = 0.88,
                volume = "$45.2B",
                structure = "Balanced Price Range (BPR) Rebalance",
                bias = "Bullish Premium Expansion (NY AM Killzone)"
            )
        )

        // 3. FOREX
        list.add(
            createIctInstrument(
                symbol = "EUR/USD",
                name = "Euro / US Dollar",
                category = MarketCategory.FOREX,
                basePrice = 1.0540,
                changePercent = 0.38,
                volume = "$420B",
                structure = "London Open Judas Swing into 15M OTE",
                bias = "Bullish Reversal into London BSL Pool"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "GBP/USD",
                name = "British Pound / US Dollar",
                category = MarketCategory.FOREX,
                basePrice = 1.2680,
                changePercent = -0.42,
                volume = "$280B",
                structure = "Bearish Order Block Rejection at NY AM Open",
                bias = "Bearish Delivery targeting Asian Lows SSL"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "USD/JPY",
                name = "US Dollar / Japanese Yen",
                category = MarketCategory.FOREX,
                basePrice = 154.30,
                changePercent = 0.45,
                volume = "$310B",
                structure = "External Range Liquidity (ERL) Run",
                bias = "Institutional Premium Expansion"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "AUD/USD",
                name = "Australian Dollar / US Dollar",
                category = MarketCategory.FOREX,
                basePrice = 0.6590,
                changePercent = 0.72,
                volume = "$150B",
                structure = "Fair Value Gap (BISI) Re-accumulation",
                bias = "Bullish OTE Continuation"
            )
        )

        // Commodity Markets: Gold (XAU/USD) & Silver (XAG/USD) - Premier ICT Assets
        list.add(
            createIctInstrument(
                symbol = "XAU/USD",
                name = "Gold / US Dollar",
                category = MarketCategory.COMMODITIES,
                basePrice = 2658.40,
                changePercent = 1.34,
                volume = "$195B",
                structure = "NY AM Sweep of Asian Highs -> Bullish MSS Displacement",
                bias = "Institutional Bullish Expansion targeting BSL Pools"
            )
        )
        list.add(
            createIctInstrument(
                symbol = "XAG/USD",
                name = "Silver / US Dollar",
                category = MarketCategory.COMMODITIES,
                basePrice = 31.85,
                changePercent = -0.68,
                volume = "$48B",
                structure = "SMT Divergence vs Gold with 15M SIBI Mitigation",
                bias = "Discount Liquidity Sweep (SSL Rebalance into OTE)"
            )
        )

        return list
    }

    private fun createIctInstrument(
        symbol: String,
        name: String,
        category: MarketCategory,
        basePrice: Double,
        changePercent: Double,
        volume: String,
        structure: String,
        bias: String
    ): MarketInstrument {
        val isBullish = changePercent >= 0
        val changeAmount = basePrice * (changePercent / 100.0)
        val high24h = basePrice * (1.0 + Math.max(0.012, changePercent / 100.0 + 0.016))
        val low24h = basePrice * (1.0 - Math.max(0.012, Math.abs(changePercent / 100.0) + 0.014))

        // ICT Arrays
        val bslPool = basePrice * 1.036 // Buy-Side Liquidity pool
        val sslPool = basePrice * 0.964 // Sell-Side Liquidity pool
        val equilibrium50 = (high24h + low24h) / 2.0

        val fvgHigh = if (isBullish) basePrice * 0.994 else basePrice * 1.018
        val fvgLow = if (isBullish) basePrice * 0.985 else basePrice * 1.009

        val orderBlockHigh = if (isBullish) basePrice * 0.982 else basePrice * 1.026
        val orderBlockLow = if (isBullish) basePrice * 0.974 else basePrice * 1.017

        val signal = when {
            isBullish && structure.contains("MSS") -> TechnicalSignal.STRONG_BUY
            isBullish -> TechnicalSignal.BUY
            !isBullish && structure.contains("MSS") -> TechnicalSignal.STRONG_SELL
            else -> TechnicalSignal.SELL
        }

        val candles = generateIctCandles(basePrice, 28, changePercent, fvgHigh.toFloat(), fvgLow.toFloat())

        return MarketInstrument(
            symbol = symbol,
            name = name,
            category = category,
            price = basePrice,
            changePercent = changePercent,
            changeAmount = changeAmount,
            high24h = high24h,
            low24h = low24h,
            volume = volume,
            institutionalBias = bias,
            marketStructure = structure,
            bslPool = bslPool,
            sslPool = sslPool,
            fvgHigh = fvgHigh,
            fvgLow = fvgLow,
            orderBlockHigh = orderBlockHigh,
            orderBlockLow = orderBlockLow,
            equilibrium50 = equilibrium50,
            killzoneWindow = "NY AM Killzone & Silver Bullet (08:30 - 11:00 EST)",
            candles = candles,
            signal = signal
        )
    }

    private fun generateIctCandles(
        currentPrice: Double,
        count: Int,
        trendBias: Double,
        fvgUpper: Float,
        fvgLower: Float
    ): List<CandleData> {
        val candles = mutableListOf<CandleData>()
        val now = System.currentTimeMillis()
        val interval = 4 * 3600 * 1000L // 4H candles

        var rollingPrice = currentPrice * (1.0 - (trendBias / 100.0) * 0.7)
        val random = Random(currentPrice.hashCode())

        for (i in 0 until count) {
            val timestamp = now - ((count - i) * interval)
            val open = rollingPrice.toFloat()
            val drift = (random.nextFloat() - 0.47f + (trendBias / 300.0).toFloat()) * (rollingPrice.toFloat() * 0.03f)
            val close = if (i == count - 1) currentPrice.toFloat() else (open + drift)
            val high = maxOf(open, close) + (random.nextFloat() * rollingPrice.toFloat() * 0.015f)
            val low = minOf(open, close) - (random.nextFloat() * rollingPrice.toFloat() * 0.015f)
            val vol = (random.nextInt(120, 950) * (currentPrice / 100.0)).toFloat().coerceAtLeast(50f)

            // Mark Fair Value Gap around recent impulse candles (e.g. index count - 5)
            val isFvgCandle = (i == count - 5 || i == count - 6)
            candles.add(
                CandleData(
                    timestamp = timestamp,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = vol,
                    isFvg = isFvgCandle,
                    fvgTop = if (isFvgCandle) fvgUpper else 0f,
                    fvgBottom = if (isFvgCandle) fvgLower else 0f
                )
            )
            rollingPrice = close.toDouble()
        }
        return candles
    }
}

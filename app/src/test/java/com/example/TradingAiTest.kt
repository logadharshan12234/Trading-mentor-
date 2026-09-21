package com.example

import com.example.data.MarketRepository
import com.example.model.MarketCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TradingAiTest {

    @Test
    fun testMarketRepository_containsAllAssetClassesIncludingCommodities() {
        val repo = MarketRepository()
        val list = repo.instruments.value

        val hasCrypto = list.any { it.category == MarketCategory.CRYPTO }
        val hasStocks = list.any { it.category == MarketCategory.STOCKS }
        val hasForex = list.any { it.category == MarketCategory.FOREX }
        val hasCommodities = list.any { it.category == MarketCategory.COMMODITIES }

        assertTrue("Should contain Crypto instruments", hasCrypto)
        assertTrue("Should contain Stock instruments", hasStocks)
        assertTrue("Should contain Forex instruments", hasForex)
        assertTrue("Should contain Commodity instruments", hasCommodities)
    }

    @Test
    fun testMarketRepository_findCommodityInstruments() {
        val repo = MarketRepository()

        val gold = repo.findInstrument("xauusd")
        assertNotNull("Should find Gold by xauusd", gold)
        assertEquals("XAU/USD", gold?.symbol)
        assertEquals(MarketCategory.COMMODITIES, gold?.category)

        val silver = repo.findInstrument("XAG/USD")
        assertNotNull("Should find Silver by XAG/USD", silver)
        assertEquals("XAG/USD", silver?.symbol)
        assertEquals(MarketCategory.COMMODITIES, silver?.category)
    }

    @Test
    fun testMarketRepository_findInstrument() {
        val repo = MarketRepository()

        val btc = repo.findInstrument("btc")
        assertNotNull("Should find BTC", btc)
        assertEquals("BTC/USD", btc?.symbol)

        val nvda = repo.findInstrument("nvidia")
        assertNotNull("Should find NVIDIA", nvda)
        assertEquals("NVDA", nvda?.symbol)

        val eur = repo.findInstrument("euro")
        assertNotNull("Should find EUR/USD", eur)
        assertEquals("EUR/USD", eur?.symbol)
    }

    @Test
    fun testMarketRepository_automatedTechnicalAnalysis() {
        val repo = MarketRepository()
        val btc = repo.findInstrument("BTC/USD")
        assertNotNull(btc)

        val analysis = repo.getAutomatedTechnicalAnalysis(btc!!)
        assertEquals("BTC/USD", analysis.symbol)
        assertTrue(analysis.confidence in 50..100)
        assertTrue(analysis.targetPrice.isNotBlank())
        assertTrue(analysis.stopLoss.isNotBlank())
        assertTrue("Should have Draw on Liquidity", analysis.drawOnLiquidity.isNotBlank())
        assertTrue("Should have Fair Value Gap", analysis.fairValueGap.isNotBlank())
        assertTrue("Should have Order Block", analysis.orderBlock.isNotBlank())
        assertTrue("Should have Market Structure", analysis.marketStructure.isNotBlank())
    }
}

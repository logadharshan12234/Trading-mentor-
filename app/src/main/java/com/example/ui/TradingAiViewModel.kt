package com.example.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiTradingClient
import com.example.data.MarketRepository
import com.example.model.ChatMessage
import com.example.model.MarketCategory
import com.example.model.MarketInstrument
import com.example.model.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TradingUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val attachedImage: Bitmap? = null,
    val selectedCategory: MarketCategory = MarketCategory.ALL,
    val isGenerating: Boolean = false,
    val showChartDialog: Boolean = false
)

class TradingAiViewModel(
    private val marketRepository: MarketRepository = MarketRepository(),
    private val geminiClient: GeminiTradingClient = GeminiTradingClient()
) : ViewModel() {

    val instruments: StateFlow<List<MarketInstrument>> = marketRepository.instruments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    init {
        // Welcome message with pure ICT Smart Money Concepts
        val welcomeMessage = ChatMessage(
            sender = MessageSender.AI,
            text = """
                ### 🏛️ ICT Trading AI
                Institutional market analysis for **Commodities, Forex, Crypto, and Stocks** using pure **ICT / Smart Money Concepts**.

                • **No Lagging Indicators**: Zero RSI, MACD, or retail formulas.
                • **Liquidity**: BSL / SSL sweeps & Judas Swings.
                • **Structure**: MSS with energetic displacement.
                • **PD Arrays**: Fair Value Gaps (FVG) & Order Blocks (OB).
                • **Timing**: London Open, NY AM Killzone & Silver Bullet.

                *Tap any asset above or choose a prompt below.*
            """.trimIndent(),
            followUpChips = listOf(
                "🏆 XAU/USD Gold Draw on Liquidity",
                "🥈 XAG/USD Silver SMT & FVG",
                "🏛️ BTC/USD Draw on Liquidity",
                "📦 NVDA Order Block & FVG",
                "💱 EUR/USD London Judas Swing",
                "📷 Analyze Chart in ICT"
            )
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcomeMessage))
    }

    fun onInputTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun attachImage(bitmap: Bitmap, prompt: String? = null) {
        _uiState.value = _uiState.value.copy(
            attachedImage = bitmap,
            inputText = if (prompt != null && _uiState.value.inputText.isBlank()) prompt else _uiState.value.inputText
        )
    }

    fun removeAttachedImage() {
        _uiState.value = _uiState.value.copy(attachedImage = null)
    }

    fun selectCategory(category: MarketCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleChartDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showChartDialog = show)
    }

    fun clearChat() {
        val welcome = _uiState.value.messages.firstOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcome),
            inputText = "",
            attachedImage = null
        )
    }

    fun onTickerClicked(instrument: MarketInstrument) {
        sendPrompt("Analyze ${instrument.symbol} under ICT: identify Draw on Liquidity (BSL/SSL), Market Structure Shift, FVG, and Order Block")
    }

    fun sendPrompt(promptText: String) {
        val image = _uiState.value.attachedImage
        val text = promptText.trim()
        if (text.isBlank() && image == null) return

        // 1. Add User Message
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = text,
            imageBitmap = image
        )

        // 2. Add temporary AI Thinking Message
        val loadingMsg = ChatMessage(
            sender = MessageSender.AI,
            text = "",
            isLoading = true
        )

        val updatedList = _uiState.value.messages + userMsg + loadingMsg
        _uiState.value = _uiState.value.copy(
            messages = updatedList,
            inputText = "",
            attachedImage = null,
            isGenerating = true
        )

        viewModelScope.launch {
            // Find if any instrument matches the query
            val matchedInstrument = marketRepository.findInstrument(text)
            val marketContext = marketRepository.getMarketOverviewText()

            val (responseText, analysis) = geminiClient.queryTradingAi(
                prompt = text,
                imageBitmap = image,
                matchedInstrument = matchedInstrument,
                marketOverviewContext = marketContext
            )

            // Dynamic pure ICT follow-up chips
            val chips = when {
                matchedInstrument != null -> listOf(
                    "🎯 Draw on Liquidity (BSL/SSL)",
                    "📦 Fair Value Gap (FVG)",
                    "⚡ Market Structure Shift (MSS)",
                    "🕒 Killzone & Silver Bullet Timing"
                )
                image != null -> listOf(
                    "🎯 OTE 0.62-0.79 Entry Zone",
                    "🧱 Order Block Invalidation",
                    "📦 FVG Imbalance Confirmation"
                )
                else -> listOf(
                    "🏛️ BTC/USD Draw on Liquidity",
                    "📦 NVDA Order Block & FVG",
                    "💱 EUR/USD London Judas Swing",
                    "📷 Analyze Chart in ICT"
                )
            }

            // Replace loading message with actual result
            val currentMsgs = _uiState.value.messages.toMutableList()
            if (currentMsgs.isNotEmpty() && currentMsgs.last().isLoading) {
                currentMsgs.removeAt(currentMsgs.size - 1)
            }

            val finalAiMsg = ChatMessage(
                sender = MessageSender.AI,
                text = responseText,
                associatedInstrument = matchedInstrument,
                technicalAnalysis = analysis,
                followUpChips = chips,
                isLoading = false
            )

            currentMsgs.add(finalAiMsg)
            _uiState.value = _uiState.value.copy(
                messages = currentMsgs,
                isGenerating = false
            )
        }
    }
}

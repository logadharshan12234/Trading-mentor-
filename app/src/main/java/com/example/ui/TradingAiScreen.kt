package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MarketCategory
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.SampleChartDialog
import com.example.ui.components.TickerRibbon
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BullGreen
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.TradingDarkBg
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import com.example.ui.theme.TradingDarkSurface

@Composable
fun TradingAiScreen(
    viewModel: TradingAiViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(TradingDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("trading_ai_screen"),
        containerColor = TradingDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar: AI Header & Status
            TopHeaderBar(
                onNewChatClick = { viewModel.clearChat() }
            )

            // Market Category Filter Pills
            MarketCategoryPills(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // Real-time Live Ticker Ribbon
            TickerRibbon(
                instruments = instruments,
                selectedCategory = uiState.selectedCategory,
                onInstrumentClick = { viewModel.onTickerClicked(it) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TradingDarkBorder)
            )

            // Conversational AI Chat Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onChipClick = { chipPrompt ->
                            if (chipPrompt.contains("Upload Chart") || chipPrompt.contains("Chart Photo")) {
                                viewModel.toggleChartDialog(true)
                            } else {
                                viewModel.sendPrompt(chipPrompt)
                            }
                        }
                    )
                }
            }

            // ChatGPT / Gemini Style Floating Input Bar
            ChatInputBar(
                inputText = uiState.inputText,
                onInputTextChange = { viewModel.onInputTextChange(it) },
                attachedImage = uiState.attachedImage,
                onRemoveAttachedImage = { viewModel.removeAttachedImage() },
                onAttachImageClick = { viewModel.toggleChartDialog(true) },
                onSendClick = { viewModel.sendPrompt(uiState.inputText) },
                isGenerating = uiState.isGenerating
            )
        }

        // Multimodal Chart Upload & Presets Dialog
        AnimatedVisibility(
            visible = uiState.showChartDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SampleChartDialog(
                onDismiss = { viewModel.toggleChartDialog(false) },
                onImageSelected = { bitmap, prompt ->
                    viewModel.attachImage(bitmap, prompt)
                }
            )
        }
    }
}

@Composable
private fun TopHeaderBar(
    onNewChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TradingDarkSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(GeminiPurple, GeminiBlue, AccentCyan)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Trading AI Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ICT Trading AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x2200D8F6), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ICT • SMC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(BullGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Institutional Live: Commodities • Stocks • Forex • Crypto",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddComment,
                    contentDescription = "New Chat",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MarketCategoryPills(
    selectedCategory: MarketCategory,
    onCategorySelected: (MarketCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MarketCategory.entries) { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else TradingDarkCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) AccentCyan else TradingDarkBorder
                ),
                modifier = Modifier
                    .clickable { onCategorySelected(category) }
                    .testTag("category_pill_${category.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.iconText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AccentCyan else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

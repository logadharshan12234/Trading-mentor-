package com.example.ui.components

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.TradingDarkBorder
import com.example.ui.theme.TradingDarkCard
import com.example.ui.theme.TradingDarkSurface

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    attachedImage: Bitmap?,
    onRemoveAttachedImage: () -> Unit,
    onAttachImageClick: () -> Unit,
    onSendClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = Color.Transparent
    ) {
        Column {
            // Attached Image Thumbnail Preview Badge
            if (attachedImage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TradingDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .testTag("attached_image_preview")
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = attachedImage.asImageBitmap(),
                            contentDescription = "Attached Chart",
                            modifier = Modifier
                                .size(42.dp, 32.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Chart Attached",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Multimodal analysis enabled",
                                fontSize = 9.sp,
                                color = AccentCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color(0xFF334155), CircleShape)
                                .clickable { onRemoveAttachedImage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ChatGPT / Gemini Style Input Pill
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = TradingDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, TradingDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment / Camera Icon
                    IconButton(
                        onClick = onAttachImageClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("attach_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Chart Image",
                            tint = if (attachedImage != null) AccentCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Input Text Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Ask about stocks, forex, crypto, or upload a chart...",
                                style = TextStyle(
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(AccentCyan)
                        )
                    }

                    // Send Button
                    val canSend = (inputText.isNotBlank() || attachedImage != null) && !isGenerating
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) AccentCyan else Color(0xFF1E293B)
                            )
                            .clickable(enabled = canSend) { onSendClick() }
                            .testTag("send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) Color(0xFF0F172A) else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

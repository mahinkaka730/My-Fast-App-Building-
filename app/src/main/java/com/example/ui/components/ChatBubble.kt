package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.RetrofitClient
import com.example.data.api.WebSource
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.squareup.moshi.Types
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    isSpeakingThis: Boolean,
    onSpeakClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val webSources = remember(message.sourceLinksJson) {
        if (!message.sourceLinksJson.isNullOrBlank()) {
            try {
                val adapter = RetrofitClient.jsonParser.adapter<List<WebSource>>(
                    Types.newParameterizedType(List::class.java, WebSource::class.java)
                )
                adapter.fromJson(message.sourceLinksJson) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 12.dp)
            .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(ElectricBlue, NeonPurple))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color(0xFF1B2844) else SurfaceCard
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isUser) ElectricBlue.copy(alpha = 0.4f) else NeonPurple.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header label if AI
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "KAKA AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (message.isVoice) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Attached Image if available
                    if (!message.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Attached photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Message Content Text
                    Text(
                        text = message.content,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    // Grounding / Web Search Sources
                    if (webSources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🌐 ইন্টারনেট তথ্যসূত্র (Sources):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            webSources.take(4).forEach { source ->
                                AssistChip(
                                    onClick = {
                                        try {
                                            uriHandler.openUri(source.uri)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "লিংক খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = source.title.take(24) + if (source.title.length > 24) "..." else "",
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = "Source",
                                            modifier = Modifier.size(14.dp),
                                            tint = ElectricBlue
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFF0F1A2D),
                                        labelColor = TextSecondary
                                    ),
                                    border = BorderStroke(1.dp, Color(0x3338BDF8))
                                )
                            }
                        }
                    }

                    // Bottom actions: Time, Speak & Copy
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = timeString,
                            fontSize = 10.sp,
                            color = TextTertiary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isUser) {
                                IconButton(
                                    onClick = onSpeakClick,
                                    modifier = Modifier.size(28.dp).testTag("speak_button")
                                ) {
                                    Icon(
                                        imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isSpeakingThis) "Stop Voice" else "Listen",
                                        tint = if (isSpeakingThis) NeonPurple else ElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    Toast.makeText(context, "টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp).testTag("copy_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

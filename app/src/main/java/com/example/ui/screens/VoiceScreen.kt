package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.VoiceWaveAnimation
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BgDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.voice.SpeechState

data class VoiceCommandItem(
    val title: String,
    val command: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isListening = speechState is SpeechState.Listening
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            Toast.makeText(context, "মাইক্রোফোন অনুমতি ছাড়া ভয়েস ইনপুট সম্ভব নয়", Toast.LENGTH_SHORT).show()
        }
    }

    val commandsList = listOf(
        VoiceCommandItem("YouTube খোলো", "YouTube খোলো", Icons.Default.PlayArrow, Color(0xFFFF0000)),
        VoiceCommandItem("TikTok খোলো", "TikTok খোলো", Icons.Default.Videocam, Color(0xFF00F2FE)),
        VoiceCommandItem("Chrome খোলো", "Chrome খোলো", Icons.Default.Web, ElectricBlue),
        VoiceCommandItem("Play Store খোলো", "Play Store খোলো", Icons.Default.Shop, AccentCyan),
        VoiceCommandItem("সময় বলো", "সময় বলো", Icons.Default.AccessTime, NeonPurple),
        VoiceCommandItem("মেসেজ লিখে দাও", "মেসেজ লিখে দাও", Icons.AutoMirrored.Filled.Chat, Color(0xFF10B981)),
        VoiceCommandItem("ইন্টারনেটে খুঁজুন", "ইন্টারনেটে খুঁজে দাও", Icons.Default.Search, Color(0xFFF59E0B)),
        VoiceCommandItem("ক্যামেরা খোলো", "ক্যামেরা খোলো", Icons.Default.CameraAlt, Color(0xFFEC4899)),
        VoiceCommandItem("ডিভাইস সেটিংস", "সেটিংস খোলো", Icons.Default.Settings, Color(0xFF64748B)),
        VoiceCommandItem("গুগল ম্যাপ", "ম্যাপ খোলো", Icons.Default.Map, Color(0xFF3B82F6))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ভয়েস কমান্ড সেন্টার",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.ttsManager.stop() },
                            modifier = Modifier.testTag("voice_stop_tts_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "থামান", tint = NeonPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Central Animated Voice Orb
            VoiceWaveAnimation(
                isListening = isListening,
                modifier = Modifier.size(160.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isListening) listOf(NeonPurple, Color(0xFF6B21A8))
                                else listOf(ElectricBlue, Color(0xFF1E3A8A))
                            )
                        )
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.toggleListening()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("main_voice_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "মাইক্রোফোন",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status message
            Text(
                text = when {
                    isListening -> "শুনছি... আপনার কমান্ড বলুন"
                    speechState is SpeechState.Processing -> "প্রক্রিয়াধীন হচ্ছে..."
                    isSpeaking -> "KAKA AI উত্তর বলছে..."
                    else -> "কথা বলতে মাইক বাটনে ট্যাপ করুন"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isListening) NeonPurple else ElectricBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "নিচের যেকোনো কমান্ড সরাসরি বলতে বা ট্যাপ করতে পারেন:",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of supported voice commands
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(commandsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.processUserInput(item.command, isVoice = true)
                            }
                            .testTag("command_card_${item.command.replace(" ", "_")}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.media.ImageProcessor
import com.example.ui.components.ChatBubble
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val messages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingStatus by viewModel.loadingStatusText.collectAsState()
    val selectedImage by viewModel.selectedChatImage.collectAsState()

    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isListening = speechState is SpeechState.Listening
    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    val listState = rememberLazyListState()

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bmp = ImageProcessor.loadBitmapFromUri(context, uri)
                viewModel.setChatImage(bmp)
            }
        }
    }

    // Microphone permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            Toast.makeText(context, "ভয়েস ইনপুটের জন্য মাইক্রোফোন অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = SurfaceDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KAKA AI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "বাংলা ও ইংরেজি এআই সহকারী",
                                fontSize = 11.sp,
                                color = AccentCyan
                            )
                        }
                    }
                },
                actions = {
                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.ttsManager.stop() },
                            modifier = Modifier.testTag("stop_tts_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "ভয়েস থামান", tint = NeonPurple)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.requestClearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "চ্যাট মুছুন", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Chat message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isSpeakingThis = isSpeaking,
                        onSpeakClick = {
                            if (isSpeaking) {
                                viewModel.ttsManager.stop()
                            } else {
                                viewModel.ttsManager.speak(msg.content)
                            }
                        }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = loadingStatus,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Quick suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    "সময় বলো" to "সময় বলো",
                    "YouTube খোলো" to "YouTube খোলো",
                    "TikTok খোলো" to "TikTok খোলো",
                    "ইন্টারনেটে সার্চ" to "ইন্টারনেটে খুঁজে দাও",
                    "মেসেজ লিখো" to "মেসেজ লিখে দাও",
                    "Play Store" to "Play Store খোলো",
                    "বাংলাদেশ সম্পর্কে বলো" to "বাংলাদেশ সম্পর্কে বলো"
                )
                chips.forEach { (label, command) ->
                    AssistChip(
                        onClick = { viewModel.processUserInput(command) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SurfaceCard,
                            labelColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, Color(0x3338BDF8))
                    )
                }
            }

            // Attached image preview if selected
            AnimatedVisibility(visible = selectedImage != null) {
                if (selectedImage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ElectricBlue, RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                bitmap = selectedImage!!.asImageBitmap(),
                                contentDescription = "Attached thumbnail",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { viewModel.setChatImage(null) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .background(Color(0xCC000000), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ছবি সংযুক্ত করা হয়েছে। এই বিষয়ে প্রশ্ন করুন।",
                            fontSize = 12.sp,
                            color = AccentCyan
                        )
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach image
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.testTag("attach_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "ছবি যোগ করুন",
                        tint = ElectricBlue
                    )
                }

                // Text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.onInputTextChanged(it) },
                    placeholder = {
                        Text(
                            if (isListening) "শুনছি... কথা বলুন" else "কাকা এআই-কে জিজ্ঞাসা করুন...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Color(0xFF2A364F),
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() || selectedImage != null) {
                                viewModel.processUserInput(inputText)
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                // Mic or Send button
                if (inputText.isNotBlank() || selectedImage != null) {
                    IconButton(
                        onClick = {
                            viewModel.processUserInput(inputText)
                            keyboardController?.hide()
                        },
                        modifier = Modifier
                            .background(ElectricBlue, CircleShape)
                            .size(46.dp)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "পাঠান",
                            tint = SurfaceDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.toggleListening()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .background(if (isListening) NeonPurple else ElectricBlue, CircleShape)
                            .size(46.dp)
                            .testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ভয়েস ইনপুট",
                            tint = SurfaceDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

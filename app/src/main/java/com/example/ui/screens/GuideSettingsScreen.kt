package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BgDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val autoSpeak by viewModel.autoSpeakTts.collectAsState()
    val speechLocale by viewModel.speechLocale.collectAsState()
    val savedApiKey by viewModel.customApiKey.collectAsState()
    val isBengaliTtsAvailable by viewModel.ttsManager.isBengaliSupported.collectAsState()

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "নির্দেশিকা ও সেটিংস",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Section 1: Settings
            Text(
                text = "অ্যাপ্লিকেশন সেটিংস (Settings)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auto speak toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = AccentCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("অটো-স্পিচ (স্বয়ংক্রিয় পাঠ)", fontSize = 14.sp, color = TextPrimary)
                                Text("এআই উত্তর স্বয়ংক্রিয়ভাবে মুখে বলবে", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.setAutoSpeakTts(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SurfaceDark,
                                checkedTrackColor = ElectricBlue
                            ),
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech recognition locale
                    Text("ভয়েস ইনপুট ভাষা (Speech Locale):", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val locales = listOf(
                            "bn-BD" to "বাংলা (BD)",
                            "bn-IN" to "বাংলা (IN)",
                            "en-US" to "English (US)"
                        )
                        locales.forEach { (tag, label) ->
                            FilterChip(
                                selected = speechLocale == tag,
                                onClick = { viewModel.setSpeechLocale(tag) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricBlue,
                                    selectedLabelColor = SurfaceDark,
                                    containerColor = SurfaceDark,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TTS Voice pack status & install button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("গুগল বাংলা ভয়েস (TTS Voice)", fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = if (isBengaliTtsAvailable) "বাংলা ভয়েস প্রস্তুত রয়েছে" else "বাংলা ভয়েস ডাটা ইনস্টল করা নেই",
                                fontSize = 11.sp,
                                color = if (isBengaliTtsAvailable) Color(0xFF10B981) else AccentPink
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.ttsManager.openTtsSettings() },
                            modifier = Modifier.testTag("open_tts_settings_button")
                        ) {
                            Text("TTS সেটিংস", fontSize = 11.sp, color = ElectricBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gemini API Key config
                    Text("Gemini API Key (ঐচ্ছিক কাস্টম কী):", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = NeonPurple) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_api_key_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = Color(0xFF2A364F),
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.setCustomApiKey(apiKeyInput.trim())
                            Toast.makeText(context, "API Key সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.align(Alignment.End).testTag("save_api_key_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সংরক্ষণ করুন", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Bengali Voice Commands & Setup Instructions
            Text(
                text = "ভয়েস কমান্ড ও সেটআপ নির্দেশিকা",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "১. সমর্থিত মূল ভয়েস কমান্ডসমূহ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val commands = listOf(
                        "• 'YouTube খোলো' -> সরাসরি ইউটিউব অ্যাপ অথবা ব্রাউজারে খোলে।",
                        "• 'TikTok খোলো' -> সরাসরি টিকটক অ্যাপ অথবা ব্রাউজারে খোলে।",
                        "• 'Chrome খোলো' -> গুগল ক্রোম বা ডিফল্ট ব্রাউজার চালু করে।",
                        "• 'Play Store খোলো' -> গুগল প্লে স্টোর ওপেন করে।",
                        "• 'সময় বলো' -> বর্তমান সময় স্পষ্টভাবে বাংলায় বলে শোনায়।",
                        "• 'মেসেজ লিখে দাও' -> এসএমএস প্রেরণের ডায়ালগ খোলে যাতে ব্যবহারকারী নিশ্চিত করে পাঠাতে পারেন।",
                        "• 'ইন্টারনেটে খুঁজে দাও' -> গুগল সার্চ বা এআই গ্রাউন্ডিং তথ্যসূত্রসহ উত্তর দেয়।",
                        "• '[অ্যাপের নাম] খোলো' বা 'open [app]' -> ডিভাইসে ইনস্টল থাকা যেকোনো অ্যাপ চালু করে।",
                        "• 'ক্যামেরা খোলো' / 'সেটিংস খোলো' / 'ম্যাপ খোলো' -> সিস্টেম কন্ট্রোল।"
                    )
                    commands.forEach {
                        Text(text = it, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "২. বাংলা টেক্সট-টু-স্পিচ (TTS) সক্রিয়করণ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "যদি আপনার ফোনে কাকা এআই বাংলায় কথা না বলে, তবে উপরের 'TTS সেটিংস' বাটনে ক্লিক করে Google Speech Engine-এ গিয়ে Bengali (Bangladesh) অথবা Bengali (India) ভয়েস প্যাক ডাউনলোড করুন।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: App Testing Guide
            Text(
                text = "অ্যাপ্লিকেশন টেস্টিং নির্দেশিকা (How to Test)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val testingSteps = listOf(
                        "১. ভয়েস রিকগনিশন টেস্ট: 'ভয়েস' ট্যাবে গিয়ে বড় মাইক বাটনে ট্যাপ করুন এবং পরিষ্কার উচ্চারণে 'সময় বলো' বলুন। অ্যাসিস্ট্যান্ট সঠিক সময় বলবে।",
                        "২. অ্যাপ লঞ্চিং টেস্ট: মাইকে বলুন 'YouTube খোলো' বা 'Play Store খোলো'। অ্যাপটি ডিভাইস ইন্টেন্ট ব্যবহার করে সংশ্লিষ্ট অ্যাপ খুলবে।",
                        "৩. সুরক্ষিত এসএমএস টেস্ট: বলুন 'মেসেজ লিখে দাও কাল সকালে মিটিং'। অ্যাপটি স্বয়ংক্রিয়ভাবে খসড়া ডায়ালগ দেখাবে এবং আপনার অনুমোদন ছাড়া কখনোই মেসেজ পাঠাবে না।",
                        "৪. ইন্টারনেট সার্চ ও তথ্যসূত্র: চ্যাটে জিজ্ঞাসা করুন 'বাংলাদেশের বর্তমান আবহাওয়া কেমন?' অথবা 'বিশ্বকাপ ফুটবল বিজয়ী'। এআই ইন্টারনেট তথ্যসূত্র সহ উত্তর দেখাবে।",
                        "৫. ফটো এডিটর টেস্ট: 'মিডিয়া' ট্যাবে গিয়ে ছবি নির্বাচন করুন এবং সাদা-কালো, সেপিয়া ফিল্টার ও রোটেশন যাচাই করুন।"
                    )
                    testingSteps.forEach { step ->
                        Text(text = step, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: Build, Sign & Export APK Guide
            Text(
                text = "APK তৈরি, সাইন ও এক্সপোর্ট নির্দেশিকা (Build & Export APK)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val buildSteps = listOf(
                        "১. AI Studio থেকে সরাসরি ডাউনলোড: AI Studio এর Settings মেনুতে গিয়ে 'Download ZIP' বা 'Export APK' অপশন ব্যবহার করতে পারেন।",
                        "২. লোকাল মেশিনে বিল্ড করা:\n   ক. প্রজেক্টটি ডাউনলোড করে আনজিপ করুন।\n   খ. টার্মিনালে রান করুন: `./gradlew assembleDebug` (ডিবাগ এপিকে তৈরির জন্য) যা `app/build/outputs/apk/debug/` ফোল্ডারে APK ফাইল তৈরি করবে।",
                        "৩. রিলিজ APK সাইনিং (Signing for Production):\n   ক. Keytool কমান্ড দিয়ে কি-স্টোর তৈরি করুন: `keytool -genkey -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`\n   খ. এনভায়রনমেন্ট ভেরিয়েবল সেট করুন অথবা `app/build.gradle.kts`-এ সাইনিং কনফিগ কনফিগার করে `./gradlew assembleRelease` রান করুন।",
                        "৪. অ্যান্ড্রয়েড ফোনে ইনস্টল: তৈরিকৃত APK ফাইলটি সরাসরি ফোনে নিয়ে 'Install Unknown Apps' অনুমোদন দিয়ে ইনস্টল করা যাবে।"
                    )
                    buildSteps.forEach { step ->
                        Text(text = step, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger zone: Clear all chat
            Button(
                onClick = { viewModel.requestClearChat() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("clear_all_data_button")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সব চ্যাট হিস্ট্রি রিসেট করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

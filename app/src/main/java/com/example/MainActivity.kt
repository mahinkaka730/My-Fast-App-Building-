package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.SmsDraftDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.GuideSettingsScreen
import com.example.ui.screens.MediaEditorScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.KakaAiTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KakaAiTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableIntStateOf(0) }

    val smsDraft by viewModel.smsDraft.collectAsState()
    val confirmationState by viewModel.confirmationState.collectAsState()

    // Listen to UI messages
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Android back button handling
    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    val navItems = listOf(
        NavItem("চ্যাট", Icons.Default.ChatBubble, "nav_chat"),
        NavItem("ভয়েস", Icons.Default.Mic, "nav_voice"),
        NavItem("মিডিয়া", Icons.Default.PhotoLibrary, "nav_media"),
        NavItem("নির্দেশিকা", Icons.Default.Info, "nav_guide")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001E2E),
                            selectedTextColor = ElectricBlue,
                            indicatorColor = ElectricBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        containerColor = BgDark,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> ChatScreen(viewModel = viewModel)
                1 -> VoiceScreen(viewModel = viewModel)
                2 -> MediaEditorScreen(viewModel = viewModel)
                3 -> GuideSettingsScreen(viewModel = viewModel)
            }
        }

        // SMS Draft confirmation dialog
        if (smsDraft != null) {
            SmsDraftDialog(
                initialRecipient = smsDraft!!.first,
                initialBody = smsDraft!!.second,
                onDismiss = { viewModel.dismissSmsDraft() },
                onConfirm = { recipient, body ->
                    viewModel.confirmSendSms(recipient, body)
                }
            )
        }

        // Clear chat confirmation dialog
        if (confirmationState == "CLEAR_CHAT") {
            ConfirmationDialog(
                title = "সব চ্যাট মুছে ফেলতে চান?",
                message = "এটি আপনার সমস্ত পূর্ববর্তী বার্তা এবং কথোপকথনের রেকর্ড স্থায়ীভাবে মুছে ফেলবে। আপনি কি নিশ্চিত?",
                confirmLabel = "মুছে ফেলুন",
                dismissLabel = "বাতিল",
                onConfirm = { viewModel.confirmClearChat() },
                onDismiss = { viewModel.dismissConfirmation() }
            )
        }
    }
}

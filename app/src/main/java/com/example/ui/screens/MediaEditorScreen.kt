package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.ImageProcessor
import com.example.media.PhotoFilter
import com.example.ui.theme.AccentCyan
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
fun MediaEditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Photo Edit, 1: AI Image, 2: Video Tools

    val editedBitmap by viewModel.mediaEditedBitmap.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    val aiPrompt by viewModel.aiImagePrompt.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()
    val generatedAiBitmap by viewModel.generatedAiBitmap.collectAsState()

    val selectedVideoUri by viewModel.selectedVideoUri.collectAsState()

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadMediaImage(uri)
        }
    }

    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.setSelectedVideoUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ফটো ও ভিডিও স্টুডিও",
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
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = ElectricBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ফটো এডিটর", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("এআই ছবি তৈরি", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("ভিডিও টুলস", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (selectedTab) {
                0 -> PhotoEditorTab(
                    bitmap = editedBitmap,
                    activeFilter = activeFilter,
                    onPickClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onFilterSelect = { viewModel.applyFilterToMedia(it) },
                    onRotate = { viewModel.rotateMedia90() },
                    onCropSquare = { viewModel.cropMediaSquare() },
                    onCrop4x3 = { viewModel.cropMedia4x3() },
                    onShare = {
                        editedBitmap?.let { ImageProcessor.shareBitmap(context, it) }
                    }
                )
                1 -> AiImageTab(
                    prompt = aiPrompt,
                    onPromptChange = { viewModel.setAiImagePrompt(it) },
                    isGenerating = isGeneratingImage,
                    generatedBitmap = generatedAiBitmap,
                    onGenerate = { viewModel.generateAiImage(aiPrompt) },
                    onShare = {
                        generatedAiBitmap?.let { ImageProcessor.shareBitmap(context, it) }
                    }
                )
                2 -> VideoToolsTab(
                    videoUri = selectedVideoUri,
                    onPickVideo = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoEditorTab(
    bitmap: Bitmap?,
    activeFilter: PhotoFilter,
    onPickClick: () -> Unit,
    onFilterSelect: (PhotoFilter) -> Unit,
    onRotate: () -> Unit,
    onCropSquare: () -> Unit,
    onCrop4x3: () -> Unit,
    onShare: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clickable { onPickClick() }
                    .testTag("pick_photo_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x3338BDF8))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "গ্যালারি থেকে ছবি নির্বাচন করুন",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ক্রপ, রোটেট এবং বিশেষ কালার ফিল্টার প্রয়োগ করুন",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Edited Image Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action toolbar: Rotate, Crops, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onRotate,
                    modifier = Modifier.testTag("rotate_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "ঘোরান", tint = ElectricBlue)
                }
                IconButton(
                    onClick = onCropSquare,
                    modifier = Modifier.testTag("crop_square_button")
                ) {
                    Icon(Icons.Default.CropSquare, contentDescription = "১:১ ক্রপ", tint = ElectricBlue)
                }
                IconButton(
                    onClick = onCrop4x3,
                    modifier = Modifier.testTag("crop_4x3_button")
                ) {
                    Icon(Icons.Default.Crop, contentDescription = "৪:৩ ক্রপ", tint = ElectricBlue)
                }
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_image_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "শেয়ার করুন", tint = NeonPurple)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filters Carousel
            Text(
                text = "ফিল্টার নির্বাচন করুন:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhotoFilter.values().forEach { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { onFilterSelect(filter) },
                        label = { Text(filter.bengaliName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = SurfaceDark,
                            containerColor = SurfaceCard,
                            labelColor = TextPrimary
                        ),
                        border = BorderStroke(1.dp, if (activeFilter == filter) ElectricBlue else Color(0x3338BDF8))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPickClick,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("অন্য ছবি নির্বাচন করুন", color = TextPrimary)
            }
        }
    }
}

@Composable
private fun AiImageTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    isGenerating: Boolean,
    generatedBitmap: Bitmap?,
    onGenerate: () -> Unit,
    onShare: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "এআই ইমেজ জেনারেটর (Gemini Vision)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "বাংলা বা ইংরেজিতে বিস্তারিত প্রম্পট লিখুন, এআই স্বয়ংক্রিয়ভাবে চিত্র তৈরি করবে।",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            placeholder = { Text("যেমন: একটি সাইবারপাঙ্ক শহরের বাংলা রোবট, নিয়ন লাইট...") },
            minLines = 3,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_image_prompt_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = Color(0xFF2A364F),
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGenerate,
            enabled = !isGenerating && prompt.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_image_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ছবি তৈরি হচ্ছে...", color = Color.White)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("এআই ছবি তৈরি করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (generatedBitmap != null) {
            Text(
                text = "ফলাফল:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, NeonPurple, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = generatedBitmap.asImageBitmap(),
                    contentDescription = "AI Generated Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = SurfaceDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ছবি শেয়ার / সংরক্ষণ করুন", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VideoToolsTab(
    videoUri: Uri?,
    onPickVideo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (videoUri == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { onPickVideo() }
                    .testTag("pick_video_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, Color(0x3338BDF8))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ডিভাইস থেকে ভিডিও নির্বাচন করুন",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ভিডিও প্রিভিউ ও ট্রিম ইন্টারভ্যাল টুল",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ভিডিও লোড হয়েছে",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "URI: ${videoUri.path ?: videoUri.toString()}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ভিডিও ট্রিম ও প্লেব্যাক ফিচার:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "অ্যান্ড্রয়েড মিডিয়া সিলেক্টর দ্বারা ভিডিওটি নিরাপদে যুক্ত হয়েছে। উন্নত ফ্রেম এডিটিং ও ট্রিম করার জন্য ডিভাইস প্লেয়ার ব্যবহার করতে পারেন অথবা এআই ভিডিও জেনারেশনের জন্য Gemini Omni মডেল প্রয়োজন হয়।",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onPickVideo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("অন্য ভিডিও বাছাই করুন", color = TextPrimary)
                    }
                }
            }
        }
    }
}

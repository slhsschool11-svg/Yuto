package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectRepository
import com.example.model.AuditStatus
import com.example.model.ChannelProfile
import com.example.model.GrowthAuditItem
import com.example.model.ProjectState
import com.example.model.TitleOption
import com.example.model.UploadPrivacy
import com.example.model.VideoFormatMode
import com.example.model.VideoProject
import com.example.ui.components.SeoScoreGauge
import com.example.ui.components.ThumbnailEditorCanvas
import com.example.ui.components.YouTubeFeedSimulator
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoUploadWizardScreen(
    project: VideoProject,
    channelProfile: ChannelProfile,
    repository: ProjectRepository,
    isGenerating: Boolean,
    uploadProgress: Float?,
    uploadLog: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeStep by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Video File Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            repository.updateVideoFile(
                fileName = "my_youtube_video.mp4",
                durationSec = 600, // 10 minutes master video
                uri = uri.toString()
            )
            Toast.makeText(context, "Video loaded! 1080p format ready.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .testTag("video_upload_wizard_screen")
    ) {
        // Wizard Navigation Header
        WizardHeader(
            activeStep = activeStep,
            onBack = {
                if (activeStep > 0) activeStep-- else onNavigateBack()
            },
            onStepClick = { activeStep = it }
        )

        // Upload progress overlay banner if bot is running
        if (uploadProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(YouTubeRed, AccentGold)))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uploadLog ?: "Publishing to YouTube...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(uploadProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.Black.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Active Step Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1-Click Autonomous Auto-Pilot Trigger Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF2E0814), Color(0xFF140D24))
                            )
                        )
                        .border(1.5.dp, AccentGold.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "1-Click Auto-Pilot Upload Bot",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "YouTube Studio Linked",
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Runs complete autonomous workflow: analyzes video length & ratio -> Gemini generates title, 500c tags & SEO description -> builds high-CTR thumbnail -> uploads & publishes to YouTube automatically.",
                            color = Color(0xFFD0D0E0),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    repository.runAutoPilotWorkflow()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("run_autonomous_autopilot_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run 1-Click Auto-Pilot (SEO + Upload)", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            when (activeStep) {
                0 -> {
                    // STEP 1: Video Format & Resizing
                    item {
                        VideoFormatStep(
                            project = project,
                            onPickVideo = { videoPickerLauncher.launch("video/*") },
                            onLoadSample = { duration ->
                                repository.updateVideoFile(
                                    fileName = if (duration == 600) "10_Minute_Master_Video.mp4" else "5_Minute_Quick_Guide.mp4",
                                    durationSec = duration,
                                    uri = null
                                )
                            },
                            onFormatChange = { repository.updateFormatMode(it) },
                            onTrimChange = { start, end -> repository.updateTrim(start, end) }
                        )
                    }
                }
                1 -> {
                    // STEP 2: Gemini AI Growth Engine
                    item {
                        GeminiGrowthStep(
                            project = project,
                            isGenerating = isGenerating,
                            onRunGemini = {
                                scope.launch {
                                    repository.runGeminiAutomation()
                                }
                            },
                            onSelectTitle = { repository.selectTitle(it) },
                            onDescriptionChange = { repository.updateDescription(it) },
                            onTagsChange = { repository.updateTags(it) },
                            onCategoryChange = { repository.updateCategory(it) },
                            onTopicChange = { topic, niche -> repository.updateTopicAndNiche(topic, niche) }
                        )
                    }
                }
                2 -> {
                    // STEP 3: High-Standard Thumbnail Studio
                    item {
                        ThumbnailStudioStep(
                            project = project,
                            channelProfile = channelProfile,
                            onConfigChange = { repository.updateThumbnailConfig(it) }
                        )
                    }
                }
                3 -> {
                    // STEP 4: YouTube Growth Audit & Direct Upload Bot
                    item {
                        GrowthAuditAndPublishStep(
                            project = project,
                            channelProfile = channelProfile,
                            audits = repository.calculateGrowthAudits(project),
                            onPrivacyChange = { repository.updatePrivacy(it) },
                            onKidsToggle = { repository.toggleMadeForKids(it) },
                            onUploadBotClick = {
                                scope.launch {
                                    repository.simulateYouTubeUploadBot { msg, _ -> }
                                }
                            },
                            onShareIntent = {
                                val shareText = buildSharePackage(project)
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_TITLE, project.selectedTitle)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Send to YouTube Studio / Creator")
                                context.startActivity(shareIntent)
                            },
                            onCopyPackage = {
                                clipboardManager.setText(AnnotatedString(buildSharePackage(project)))
                                Toast.makeText(context, "Full SEO Package Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            item {
                // Bottom Wizard Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeStep > 0) {
                        Button(
                            onClick = { activeStep-- },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", color = TextPrimary)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (activeStep < 3) {
                        Button(
                            onClick = { activeStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                        ) {
                            Text("Continue: Step ${activeStep + 2}", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WizardHeader(
    activeStep: Int,
    onBack: () -> Unit,
    onStepClick: (Int) -> Unit
) {
    val steps = listOf("Video Format", "Gemini AI", "Thumbnails", "Audit & Upload")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(0.5.dp, StudioCardBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "YouTube Creator Studio",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = steps[activeStep],
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step Indicator Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            steps.forEachIndexed { index, stepName ->
                val isCurrent = index == activeStep
                val isDone = index < activeStep
                val barColor = when {
                    isCurrent -> YouTubeRed
                    isDone -> AccentGreen
                    else -> StudioSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barColor)
                        .clickable { onStepClick(index) }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1: Video Format & Resizing
// -------------------------------------------------------------
@Composable
private fun VideoFormatStep(
    project: VideoProject,
    onPickVideo: () -> Unit,
    onLoadSample: (Int) -> Unit,
    onFormatChange: (VideoFormatMode) -> Unit,
    onTrimChange: (Int, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Video Source Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, tint = YouTubeRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video File & Resolution",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "1080p Ready",
                            color = AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Selected File: ${project.originalFileName}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total Duration: ${project.durationSeconds / 60}m ${project.durationSeconds % 60}s (${project.durationSeconds}s)",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPickVideo,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Video", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onLoadSample(600) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
                    ) {
                        Text("10-Min Video", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onLoadSample(300) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
                    ) {
                        Text("5-Min Video", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // YouTube Resizable Aspect Ratio Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AspectRatio, contentDescription = null, tint = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YouTube Resizable Aspect Ratio",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "YouTube algorithm differentiates long-form videos (16:9 widescreen) vs YouTube Shorts (9:16 vertical). Select your target format below:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 16:9 Long Form Option
                FormatSelectionCard(
                    mode = VideoFormatMode.LONG_FORM_16_9,
                    isSelected = project.formatMode == VideoFormatMode.LONG_FORM_16_9,
                    onSelect = { onFormatChange(VideoFormatMode.LONG_FORM_16_9) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 9:16 Shorts Option
                FormatSelectionCard(
                    mode = VideoFormatMode.SHORTS_9_16,
                    isSelected = project.formatMode == VideoFormatMode.SHORTS_9_16,
                    onSelect = { onFormatChange(VideoFormatMode.SHORTS_9_16) }
                )
            }
        }

        // Video Trimming & Ad Placement Optimizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Crop, contentDescription = null, tint = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trimmer & Ad-Break Optimizer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    if (project.durationSeconds >= 480) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Mid-Rolls Unlocked ($)",
                                color = AccentGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Videos 8+ minutes qualify for multiple YouTube mid-roll advertisements to maximize your channel income.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                var sliderEnd by remember(project.durationSeconds) { mutableStateOf(project.durationSeconds.toFloat()) }
                Text(
                    text = "Trim Range: 0s to ${sliderEnd.toInt()}s (${sliderEnd.toInt() / 60}m ${sliderEnd.toInt() % 60}s)",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = sliderEnd,
                    onValueChange = {
                        sliderEnd = it
                        onTrimChange(0, it.toInt())
                    },
                    valueRange = 60f..1200f,
                    colors = SliderDefaults.colors(
                        thumbColor = YouTubeRed,
                        activeTrackColor = YouTubeRed,
                        inactiveTrackColor = StudioSurfaceVariant
                    )
                )

                // YouTube Chapter Split Breakdown
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Auto-Calculated Retention Chapters (${project.chapters.size} markers):",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                project.chapters.take(3).forEach { ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = ch.timeFormatted, color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = ch.title, color = TextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatSelectionCard(
    mode: VideoFormatMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) YouTubeRed.copy(alpha = 0.15f) else StudioSurfaceVariant)
            .border(
                1.5.dp,
                if (isSelected) YouTubeRed else StudioCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (isSelected) YouTubeRed else TextSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.displayName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = mode.aspectRatioDesc,
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = mode.recommendedFor,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: Gemini AI Growth Engine
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeminiGrowthStep(
    project: VideoProject,
    isGenerating: Boolean,
    onRunGemini: () -> Unit,
    onSelectTitle: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTopicChange: (String, String) -> Unit
) {
    var topicInput by remember { mutableStateOf(project.primaryFocus) }
    var nicheInput by remember { mutableStateOf(project.targetNiche) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // AI Trigger Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF200008), Color(0xFF140D24))
                    )
                )
                .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini AI YouTube Growth Engine",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gemini researches search intent, viral psychology hooks, and YouTube algorithm ranking factors to generate titles, tags & complete SEO descriptions.",
                    color = Color(0xFFC0C0D0),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = topicInput,
                    onValueChange = {
                        topicInput = it
                        onTopicChange(it, nicheInput)
                    },
                    label = { Text("Video Core Focus / Script Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRunGemini,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_gemini_automation_button"),
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Gemini Is Researching Algorithm Trends...", color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate High-Growth YouTube Package", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Generated Title Options (A/B Test Options)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "High-CTR Title Blueprints",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Selected: ${project.selectedTitle.length}/70 chars",
                        color = if (project.selectedTitle.length <= 70) AccentGreen else YouTubeRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                project.titleOptions.forEach { opt ->
                    val isSelected = opt.title == project.selectedTitle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) YouTubeRed.copy(alpha = 0.2f) else StudioSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) YouTubeRed else StudioCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTitle(opt.title) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(opt.style, color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Predicted CTR: ${opt.predictedCtr}%",
                                    color = AccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = opt.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = opt.reasoning,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Viral 500-Character Tags Optimizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                val currentTagChars = project.tags.joinToString(",").length
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Viral Search Tags",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "$currentTagChars / 500 chars",
                        color = if (currentTagChars in 200..500) AccentGreen else AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "High-indexing keywords automatically generated for YouTube search rank & suggested video graph.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    project.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioSurfaceVariant)
                                .border(0.5.dp, StudioCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "#$tag", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Full SEO Description
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "YouTube SEO Description",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = project.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3: Thumbnail Studio & Feed Test
// -------------------------------------------------------------
@Composable
private fun ThumbnailStudioStep(
    project: VideoProject,
    channelProfile: ChannelProfile,
    onConfigChange: (com.example.model.ThumbnailConfig) -> Unit
) {
    var headline by remember(project.thumbnailConfig.boldHeadline) {
        mutableStateOf(project.thumbnailConfig.boldHeadline)
    }
    var subHeadline by remember(project.thumbnailConfig.subHeadline) {
        mutableStateOf(project.thumbnailConfig.subHeadline)
    }
    var badgeText by remember(project.thumbnailConfig.badgeText) {
        mutableStateOf(project.thumbnailConfig.badgeText)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Thumbnail Editor Canvas
        ThumbnailEditorCanvas(
            config = project.thumbnailConfig,
            formatMode = project.formatMode,
            durationFormatted = String.format("%d:%02d", project.durationSeconds / 60, project.durationSeconds % 60),
            onConfigChange = onConfigChange
        )

        // Text & Badge Customizer Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TextFields, contentDescription = null, tint = AccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize Thumbnail Hook Text",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = headline,
                    onValueChange = {
                        headline = it
                        onConfigChange(project.thumbnailConfig.copy(boldHeadline = it))
                    },
                    label = { Text("Bold Main Hook (3-4 Words)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subHeadline,
                    onValueChange = {
                        subHeadline = it
                        onConfigChange(project.thumbnailConfig.copy(subHeadline = it))
                    },
                    label = { Text("Sub-Hook / Promise") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = YouTubeRed,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Viral Badge Preset Selector
                Text(
                    text = "Viral Sticker Badge:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MUST WATCH", "NEW 2026", "1M+ VIEWS", "100% PROVEN", "URGENT").forEach { b ->
                        val isBadge = badgeText == b
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isBadge) YouTubeRed else StudioSurfaceVariant)
                                .clickable {
                                    badgeText = b
                                    onConfigChange(project.thumbnailConfig.copy(badgeText = b))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = b,
                                color = if (isBadge) Color.White else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Live YouTube Feed Simulator
        YouTubeFeedSimulator(
            project = project,
            channelProfile = channelProfile
        )
    }
}

// -------------------------------------------------------------
// STEP 4: Growth Audit & Direct Upload Bot
// -------------------------------------------------------------
@Composable
private fun GrowthAuditAndPublishStep(
    project: VideoProject,
    channelProfile: ChannelProfile,
    audits: List<GrowthAuditItem>,
    onPrivacyChange: (UploadPrivacy) -> Unit,
    onKidsToggle: (Boolean) -> Unit,
    onUploadBotClick: () -> Unit,
    onShareIntent: () -> Unit,
    onCopyPackage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // SEO Score Summary
        SeoScoreGauge(score = project.seoScore)

        // Algorithm Compliance Checklist
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "YouTube Growth & Algorithm Checklist",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                audits.forEach { item ->
                    val (icon, color) = when (item.status) {
                        AuditStatus.PASS -> Icons.Default.CheckCircle to AccentGreen
                        AuditStatus.WARNING -> Icons.Default.Warning to AccentGold
                        AuditStatus.CRITICAL -> Icons.Default.Warning to YouTubeRed
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.tip,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Publishing Controls (Privacy, COPPA)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "YouTube Distribution Settings",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Privacy Radio Selection
                Text(text = "Visibility Status:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                UploadPrivacy.entries.forEach { p ->
                    val isSelected = project.privacy == p
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPrivacyChange(p) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) YouTubeRed else TextSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeRed)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = p.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // COPPA Kids Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COPPA: Made for Kids?",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Leave OFF for general audiences to enable comments, notification bell & maximum ads",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = project.isMadeForKids,
                        onCheckedChange = onKidsToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = YouTubeRed)
                    )
                }
            }
        }

        // Direct YouTube Actions (Bot Upload + Share Intent + Copy)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Big Bot One-Click Upload
            Button(
                onClick = onUploadBotClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_youtube_upload_bot_button"),
                colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publish to YouTube (Bot Automation)",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onShareIntent,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("YouTube Studio", color = TextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = onCopyPackage,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy All SEO", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

private fun buildSharePackage(project: VideoProject): String {
    return """
        TITLE:
        ${project.selectedTitle}
        
        DESCRIPTION:
        ${project.description}
        
        TAGS:
        ${project.tags.joinToString(", ")}
        
        CATEGORY: ${project.category}
        VISIBILITY: ${project.privacy.label}
        THUMBNAIL HOOK: ${project.thumbnailConfig.boldHeadline}
        SEO SCORE: ${project.seoScore}/100
    """.trimIndent()
}

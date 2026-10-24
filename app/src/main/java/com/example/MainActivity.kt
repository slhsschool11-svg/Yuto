package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ProjectRepository
import com.example.model.VideoProject
import com.example.ui.components.StudioTopBar
import com.example.ui.components.YouTubeGoogleSignInModal
import com.example.ui.components.WorkingModelGuideModal
import com.example.ui.screens.ChannelSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ResearchTrendsScreen
import com.example.ui.screens.VideoUploadWizardScreen
import com.example.ui.screens.GeminiChatScreen
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

import androidx.compose.material.icons.automirrored.filled.TrendingUp

enum class StudioScreen(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    UPLOAD_WIZARD("AI Upload", Icons.Default.RocketLaunch),
    CHAT("AI Copilot", Icons.Default.AutoAwesome),
    TRENDS("Research", Icons.AutoMirrored.Filled.TrendingUp),
    SETTINGS("Channel", Icons.Default.ManageAccounts)
}

class MainActivity : ComponentActivity() {

    private val repository = ProjectRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(repository = repository)
            }
        }
    }
}

@Composable
fun MainAppContent(repository: ProjectRepository) {
    var currentScreen by remember { mutableStateOf(StudioScreen.DASHBOARD) }
    val scope = rememberCoroutineScope()

    val currentProject by repository.currentProject.collectAsStateWithLifecycle()
    val projectsHistory by repository.projectsHistory.collectAsStateWithLifecycle()
    val channelProfile by repository.channelProfile.collectAsStateWithLifecycle()
    val researchInsights by repository.researchInsights.collectAsStateWithLifecycle()
    val isGenerating by repository.isGenerating.collectAsStateWithLifecycle()
    val uploadProgress by repository.uploadProgress.collectAsStateWithLifecycle()
    val uploadLog by repository.uploadLog.collectAsStateWithLifecycle()
    val chatMessages by repository.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by repository.isChatLoading.collectAsStateWithLifecycle()
    val googleSession by repository.googleSession.collectAsStateWithLifecycle()

    var showAuthModal by remember { mutableStateOf(false) }
    var showGuideModal by remember { mutableStateOf(false) }

    // Back handling for sub-screens
    BackHandler(enabled = currentScreen != StudioScreen.DASHBOARD) {
        currentScreen = StudioScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("yuto_main_scaffold"),
        containerColor = StudioBackground,
        topBar = {
            StudioTopBar(
                channelProfile = channelProfile,
                onOpenGuide = { showGuideModal = true },
                onOpenAuthModal = { showAuthModal = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("studio_bottom_navigation"),
                containerColor = StudioSurface,
                tonalElevation = 8.dp
            ) {
                StudioScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = YouTubeRed,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = YouTubeRed
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                StudioScreen.DASHBOARD -> {
                    DashboardScreen(
                        currentProject = currentProject,
                        channelProfile = channelProfile,
                        projectsHistory = projectsHistory,
                        onStartUploadWizard = { currentScreen = StudioScreen.UPLOAD_WIZARD },
                        onRunAutoPilot = {
                            scope.launch {
                                repository.runAutoPilotWorkflow()
                            }
                            currentScreen = StudioScreen.UPLOAD_WIZARD
                        },
                        onOpenGuide = { showGuideModal = true },
                        onOpenGoogleAuth = { showAuthModal = true },
                        onViewProject = {
                            currentScreen = StudioScreen.UPLOAD_WIZARD
                        },
                        onOpenTrends = { currentScreen = StudioScreen.TRENDS }
                    )
                }

                StudioScreen.UPLOAD_WIZARD -> {
                    VideoUploadWizardScreen(
                        project = currentProject,
                        channelProfile = channelProfile,
                        repository = repository,
                        isGenerating = isGenerating,
                        uploadProgress = uploadProgress,
                        uploadLog = uploadLog,
                        onNavigateBack = { currentScreen = StudioScreen.DASHBOARD }
                    )
                }

                StudioScreen.CHAT -> {
                    GeminiChatScreen(
                        messages = chatMessages,
                        isLoading = isChatLoading,
                        onSendMessage = { text ->
                            scope.launch {
                                repository.sendChatMessage(text)
                            }
                        },
                        onClearChat = { repository.clearChat() }
                    )
                }

                StudioScreen.TRENDS -> {
                    ResearchTrendsScreen(
                        insights = researchInsights,
                        onApplyKeywordToProject = { kw ->
                            repository.updateTopicAndNiche(kw, currentProject.targetNiche)
                            currentScreen = StudioScreen.UPLOAD_WIZARD
                        }
                    )
                }

                StudioScreen.SETTINGS -> {
                    ChannelSettingsScreen(
                        channelProfile = channelProfile,
                        onSaveProfile = { /* state handled */ }
                    )
                }
            }
        }
    }

    // Google / YouTube Studio Sign-In Modal
    if (showAuthModal) {
        YouTubeGoogleSignInModal(
            session = googleSession,
            onDismiss = { showAuthModal = false },
            onToggleSignIn = { signedIn ->
                repository.toggleGoogleSignIn(signedIn)
            }
        )
    }

    // Interactive Working Model Guide
    if (showGuideModal) {
        WorkingModelGuideModal(
            onDismiss = { showGuideModal = false },
            onStartUploadNow = {
                showGuideModal = false
                currentScreen = StudioScreen.UPLOAD_WIZARD
            }
        )
    }
}

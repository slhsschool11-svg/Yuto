package com.example.data

import com.example.model.AuditStatus
import com.example.model.ChannelProfile
import com.example.model.ChapterTimestamp
import com.example.model.GrowthAuditItem
import com.example.model.ProjectState
import com.example.model.ResearchInsight
import com.example.model.ThumbnailConfig
import com.example.model.TitleOption
import com.example.model.TrendKeyword
import com.example.model.UploadPrivacy
import com.example.model.VideoFormatMode
import com.example.model.VideoProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.service.GeminiClientService

import com.example.model.GoogleAccountSession
import com.example.service.UploadStepStatus
import com.example.service.YouTubeApiService
import com.example.service.YouTubeChannelAccount

class ProjectRepository(
    val geminiClientService: GeminiClientService = GeminiClientService(),
    val youTubeApiService: YouTubeApiService = YouTubeApiService(),
    private val geminiRepository: GeminiRepository = GeminiRepository()
) {
    private val _currentProject = MutableStateFlow(createInitialProject())
    val currentProject: StateFlow<VideoProject> = _currentProject.asStateFlow()

    private val _projectsHistory = MutableStateFlow(createSampleProjects())
    val projectsHistory: StateFlow<List<VideoProject>> = _projectsHistory.asStateFlow()

    private val _channelProfile = MutableStateFlow(ChannelProfile())
    val channelProfile: StateFlow<ChannelProfile> = _channelProfile.asStateFlow()

    private val _googleSession = MutableStateFlow(GoogleAccountSession())
    val googleSession: StateFlow<GoogleAccountSession> = _googleSession.asStateFlow()

    private val _youTubeAccount = MutableStateFlow(YouTubeChannelAccount())
    val youTubeAccount: StateFlow<YouTubeChannelAccount> = _youTubeAccount.asStateFlow()

    private val _autoPilotStatus = MutableStateFlow<UploadStepStatus>(UploadStepStatus.Idle)
    val autoPilotStatus: StateFlow<UploadStepStatus> = _autoPilotStatus.asStateFlow()

    private val _researchInsights = MutableStateFlow(createSampleResearch())
    val researchInsights: StateFlow<List<ResearchInsight>> = _researchInsights.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _uploadLog = MutableStateFlow<String?>(null)
    val uploadLog: StateFlow<String?> = _uploadLog.asStateFlow()

    suspend fun runAutoPilotWorkflow() {
        val proj = _currentProject.value
        val channel = _youTubeAccount.value
        val updatedProject = youTubeApiService.executeAutonomousUpload(
            project = proj,
            channel = channel,
            geminiService = geminiClientService,
            onProgressUpdate = { status ->
                _autoPilotStatus.value = status
                _uploadProgress.value = if (status.progress > 0) status.progress else null
                _uploadLog.value = status.description
            }
        )

        _currentProject.value = updatedProject
        _projectsHistory.update { listOf(updatedProject) + it }
        kotlinx.coroutines.delay(1000)
    }

    fun toggleYouTubeAuth(enabled: Boolean) {
        _youTubeAccount.update { it.copy(isAuthenticated = enabled) }
        _channelProfile.update { it.copy(isConnected = enabled) }
        _googleSession.update { it.copy(isSignedIn = enabled) }
    }

    fun toggleGoogleSignIn(enabled: Boolean) {
        toggleYouTubeAuth(enabled)
    }

    // Multi-turn Gemini Chat Conversation State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(createInitialChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    suspend fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(role = MessageRole.USER, content = userText.trim())
        _chatMessages.update { it + userMsg }
        _isChatLoading.value = true

        val botReplyText = geminiClientService.sendChatMessage(
            history = _chatMessages.value,
            userMessage = userText.trim()
        )

        val botMsg = ChatMessage(role = MessageRole.ASSISTANT, content = botReplyText)
        _chatMessages.update { it + botMsg }
        _isChatLoading.value = false
    }

    fun clearChat() {
        _chatMessages.value = createInitialChatMessages()
    }

    fun updateVideoFile(fileName: String, durationSec: Int, uri: String?) {
        val format = if (durationSec <= 60) VideoFormatMode.SHORTS_9_16 else VideoFormatMode.LONG_FORM_16_9
        _currentProject.update { current ->
            current.copy(
                originalFileName = fileName,
                durationSeconds = durationSec,
                videoUri = uri,
                formatMode = format,
                trimStartSeconds = 0,
                trimEndSeconds = durationSec,
                videoTitle = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                    .replaceFirstChar { it.uppercase() }
            )
        }
    }

    fun updateFormatMode(formatMode: VideoFormatMode) {
        _currentProject.update { it.copy(formatMode = formatMode) }
    }

    fun updateTrim(startSec: Int, endSec: Int) {
        _currentProject.update {
            it.copy(
                trimStartSeconds = startSec,
                trimEndSeconds = endSec,
                durationSeconds = (endSec - startSec).coerceAtLeast(1)
            )
        }
    }

    fun updateTopicAndNiche(topic: String, niche: String) {
        _currentProject.update {
            it.copy(
                primaryFocus = topic,
                targetNiche = niche
            )
        }
    }

    fun selectTitle(title: String) {
        _currentProject.update { it.copy(selectedTitle = title) }
    }

    fun updateDescription(description: String) {
        _currentProject.update { it.copy(description = description) }
    }

    fun updateTags(tags: List<String>) {
        _currentProject.update { it.copy(tags = tags) }
    }

    fun updatePrivacy(privacy: UploadPrivacy) {
        _currentProject.update { it.copy(privacy = privacy) }
    }

    fun updateCategory(category: String) {
        _currentProject.update { it.copy(category = category) }
    }

    fun updateThumbnailConfig(config: ThumbnailConfig) {
        _currentProject.update { it.copy(thumbnailConfig = config) }
    }

    fun toggleMadeForKids(isKids: Boolean) {
        _currentProject.update { it.copy(isMadeForKids = isKids) }
    }

    suspend fun runGeminiAutomation() {
        _isGenerating.value = true
        val proj = _currentProject.value
        val result = geminiRepository.generateGrowthPackage(
            videoTopic = proj.primaryFocus.ifBlank { proj.videoTitle },
            targetNiche = proj.targetNiche,
            durationMinutes = (proj.durationSeconds / 60).coerceAtLeast(1),
            isShorts = proj.formatMode == VideoFormatMode.SHORTS_9_16
        )

        _currentProject.update {
            it.copy(
                titleOptions = result.titles,
                selectedTitle = result.selectedTitle,
                description = result.description,
                tags = result.tags,
                chapters = result.chapters,
                thumbnailConfig = result.thumbnailConfig,
                seoScore = result.seoScore,
                category = result.category,
                state = ProjectState.READY
            )
        }
        _isGenerating.value = false
    }

    suspend fun simulateYouTubeUploadBot(onStep: (String, Float) -> Unit) {
        _uploadProgress.value = 0.05f
        _uploadLog.value = "Authenticating with YouTube API v3 (Channel: ${_channelProfile.value.channelName})..."
        onStep(_uploadLog.value!!, 0.05f)
        kotlinx.coroutines.delay(600)

        _uploadProgress.value = 0.20f
        _uploadLog.value = "Initiating Resumable Media Chunk Upload (${_currentProject.value.formatMode.displayName})..."
        onStep(_uploadLog.value!!, 0.20f)
        kotlinx.coroutines.delay(800)

        _uploadProgress.value = 0.50f
        _uploadLog.value = "Transferring 1080p video stream & applying video format bounds..."
        onStep(_uploadLog.value!!, 0.50f)
        kotlinx.coroutines.delay(900)

        _uploadProgress.value = 0.70f
        _uploadLog.value = "Pushing High-CTR Thumbnail, Chapters & 500-char SEO Tags..."
        onStep(_uploadLog.value!!, 0.70f)
        kotlinx.coroutines.delay(700)

        _uploadProgress.value = 0.88f
        _uploadLog.value = "Running Automated YouTube Partner & Copyright Checks (Zero Strikes detected)..."
        onStep(_uploadLog.value!!, 0.88f)
        kotlinx.coroutines.delay(700)

        _uploadProgress.value = 1.0f
        _uploadLog.value = "Successfully Published! Video is live with status: ${_currentProject.value.privacy.label}"
        onStep(_uploadLog.value!!, 1.0f)

        _currentProject.update { it.copy(state = ProjectState.PUBLISHED) }
        _projectsHistory.update { listOf(_currentProject.value) + it }
        kotlinx.coroutines.delay(1200)
        _uploadProgress.value = null
        _uploadLog.value = null
    }

    fun calculateGrowthAudits(project: VideoProject): List<GrowthAuditItem> {
        val list = mutableListOf<GrowthAuditItem>()

        // 1. Title Length Audit
        if (project.selectedTitle.length in 35..70) {
            list.add(
                GrowthAuditItem(
                    title = "Title Length Optimal (${project.selectedTitle.length}/70 chars)",
                    status = AuditStatus.PASS,
                    tip = "Fits completely on mobile screens without ellipsis truncation."
                )
            )
        } else if (project.selectedTitle.length > 70) {
            list.add(
                GrowthAuditItem(
                    title = "Title Too Long (${project.selectedTitle.length} chars)",
                    status = AuditStatus.WARNING,
                    tip = "YouTube cuts titles off after ~70 characters on mobile home feeds."
                )
            )
        } else {
            list.add(
                GrowthAuditItem(
                    title = "Title Too Short",
                    status = AuditStatus.WARNING,
                    tip = "Add power words or curiosity hooks to increase click-through rate."
                )
            )
        }

        // 2. High-CTR Hook Words
        val powerWords = listOf("how", "secret", "never", "stop", "best", "step", "guide", "2026", "easy", "proven", "free", "viral")
        val hasPowerWord = powerWords.any { project.selectedTitle.contains(it, ignoreCase = true) }
        if (hasPowerWord) {
            list.add(
                GrowthAuditItem(
                    title = "High-CTR Psychological Triggers Active",
                    status = AuditStatus.PASS,
                    tip = "Title contains high-retention curiosity/urgency words."
                )
            )
        } else {
            list.add(
                GrowthAuditItem(
                    title = "No Power Trigger Words",
                    status = AuditStatus.WARNING,
                    tip = "Include words like 'Secret', 'Proven', or 'Step-by-Step' to boost CTR."
                )
            )
        }

        // 3. Chapters & Timestamps
        if (project.chapters.size >= 3) {
            list.add(
                GrowthAuditItem(
                    title = "Video Chapters Active (${project.chapters.size} markers)",
                    status = AuditStatus.PASS,
                    tip = "YouTube search rewards chapters with Google Featured Rich Snippets."
                )
            )
        } else {
            list.add(
                GrowthAuditItem(
                    title = "Missing Timestamps / Chapters",
                    status = AuditStatus.CRITICAL,
                    tip = "Adding timestamps boosts retention and ranks in Google search results."
                )
            )
        }

        // 4. Tags saturation
        val tagChars = project.tags.joinToString(",").length
        if (tagChars in 150..500) {
            list.add(
                GrowthAuditItem(
                    title = "Tag Saturation Strong ($tagChars/500 chars)",
                    status = AuditStatus.PASS,
                    tip = "Covers high-intent search terms across target niche."
                )
            )
        } else {
            list.add(
                GrowthAuditItem(
                    title = "Tags Under-utilized ($tagChars/500 chars)",
                    status = AuditStatus.WARNING,
                    tip = "Fill closer to 400-500 characters for maximum search indexing."
                )
            )
        }

        // 5. Thumbnail text readability
        if (project.thumbnailConfig.boldHeadline.split(" ").size in 1..4) {
            list.add(
                GrowthAuditItem(
                    title = "Thumbnail Text Hook Concise (${project.thumbnailConfig.boldHeadline.split(" ").size} words)",
                    status = AuditStatus.PASS,
                    tip = "Keep to 3-4 words max so it's readable on tiny smartphone displays."
                )
            )
        } else {
            list.add(
                GrowthAuditItem(
                    title = "Thumbnail Text Too Wordy",
                    status = AuditStatus.WARNING,
                    tip = "Viewers only glance for 0.4 seconds. Reduce to 3 large punchy words."
                )
            )
        }

        // 6. COPPA Made For Kids
        list.add(
            GrowthAuditItem(
                title = if (project.isMadeForKids) "Marked as Made for Kids" else "Not Made for Kids (Comments & Ads Enabled)",
                status = AuditStatus.PASS,
                tip = if (project.isMadeForKids) "Targeted ads and comments disabled." else "Full monetization & notification bell enabled."
            )
        )

        return list
    }

    private fun createInitialProject(): VideoProject {
        val defaultTitles = listOf(
            TitleOption(
                title = "How I Automated My ENTIRE YouTube Channel With Gemini AI (Step-by-Step)",
                style = "Authority How-To",
                predictedCtr = 97,
                reasoning = "High search volume + clear promise of automation workflow."
            ),
            TitleOption(
                title = "The Secret Gemini AI Strategy That's Breaking YouTube in 2026",
                style = "Curiosity Gap",
                predictedCtr = 95,
                reasoning = "Curiosity and fear of missing out trigger massive clicks."
            ),
            TitleOption(
                title = "Stop Editing Manually! Build a YouTube AI Bot in 10 Minutes",
                style = "Urgency / Speed",
                predictedCtr = 94,
                reasoning = "Direct command + quick 10-minute payoff."
            )
        )

        val defaultChapters = listOf(
            ChapterTimestamp("0:00", "The Exact Automation Secret"),
            ChapterTimestamp("1:20", "Why Manual Uploading is Obsolete"),
            ChapterTimestamp("3:45", "Gemini AI Script & SEO Workflow"),
            ChapterTimestamp("6:30", "Auto-Generated High-CTR Thumbnails"),
            ChapterTimestamp("8:15", "One-Click Publishing Pipeline"),
            ChapterTimestamp("9:40", "Next Action Steps")
        )

        val defaultTags = listOf(
            "youtube automation",
            "gemini ai youtube",
            "how to grow on youtube",
            "viral video seo",
            "youtube upload bot",
            "high ctr thumbnail",
            "video monetization",
            "algorithm hack 2026",
            "youtube growth studio",
            "ai content creator"
        )

        return VideoProject(
            videoTitle = "YouTube AI Automation Blueprint",
            selectedTitle = defaultTitles.first().title,
            titleOptions = defaultTitles,
            description = """
                How I Automated My ENTIRE YouTube Channel With Gemini AI (Step-by-Step)
                
                In this masterclass, discover how to leverage Google's latest Gemini AI models to completely automate your YouTube channel growth—from viral titles and auto-generated high CTR thumbnails to 500-character SEO tags and instant publishing.
                
                ⏱️ TIMESTAMPS:
                0:00 - The Exact Automation Secret
                1:20 - Why Manual Uploading is Obsolete
                3:45 - Gemini AI Script & SEO Workflow
                6:30 - Auto-Generated High-CTR Thumbnails
                8:15 - One-Click Publishing Pipeline
                9:40 - Next Action Steps
                
                🚀 FREE CREATOR TOOLKIT:
                • Download Yuto Studio: https://creatorsuite.io/yuto
                • Join our 25K+ Creator Discord: https://discord.gg/creators
                
                🔔 Don't forget to LIKE and SUBSCRIBE for weekly YouTube growth tactics!
                #YouTubeAutomation #GeminiAI #YouTubeGrowth #ContentCreator #VideoSEO
            """.trimIndent(),
            tags = defaultTags,
            chapters = defaultChapters,
            thumbnailConfig = ThumbnailConfig(
                boldHeadline = "100% AUTOMATED!",
                subHeadline = "Gemini AI YouTube Bot",
                badgeText = "GAME CHANGER"
            ),
            seoScore = 96
        )
    }

    private fun createSampleProjects(): List<VideoProject> {
        return listOf(
            VideoProject(
                videoTitle = "10 Viral YouTube Shorts Hacks",
                selectedTitle = "Do NOT Post Another Short Until You Do THIS (Algorithm Secret)",
                durationSeconds = 54,
                formatMode = VideoFormatMode.SHORTS_9_16,
                seoScore = 98,
                state = ProjectState.PUBLISHED,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            VideoProject(
                videoTitle = "How I Got 100K Subscribers",
                selectedTitle = "From 0 to 100K Subscribers: The Mathematical Strategy",
                durationSeconds = 640,
                formatMode = VideoFormatMode.LONG_FORM_16_9,
                seoScore = 93,
                state = ProjectState.PUBLISHED,
                createdAt = System.currentTimeMillis() - 172800000L
            )
        )
    }

    private fun createSampleResearch(): List<ResearchInsight> {
        return listOf(
            ResearchInsight(
                topicName = "AI Automation & Video Workflows",
                viralOpportunityScore = 97,
                audienceRetentionTip = "Introduce your high-stakes payoff before the 0:08 second mark to prevent drop-off.",
                bestUploadWindow = "Tuesday & Thursday 2:00 PM – 4:30 PM EST",
                highPerformingKeywords = listOf(
                    TrendKeyword("gemini ai youtube bot", "620K/mo", "Low", "+230% breakout", true),
                    TrendKeyword("auto generate youtube thumbnail", "450K/mo", "Medium", "+110%", false),
                    TrendKeyword("youtube algorithm 2026 update", "890K/mo", "Low", "+180% breakout", true),
                    TrendKeyword("youtube shorts monetization tricks", "510K/mo", "Medium", "+95%", false)
                )
            ),
            ResearchInsight(
                topicName = "Faceless Channels & Cash Cow Niches",
                viralOpportunityScore = 91,
                audienceRetentionTip = "Use continuous visual pattern interrupts every 4-6 seconds with zooms and sound cues.",
                bestUploadWindow = "Saturday 9:00 AM – 11:30 AM EST",
                highPerformingKeywords = listOf(
                    TrendKeyword("faceless youtube channel automation", "380K/mo", "Low", "+140%", true),
                    TrendKeyword("best niche for youtube ad revenue", "290K/mo", "Low", "+75%", false),
                    TrendKeyword("ai voiceover for youtube", "410K/mo", "Medium", "+85%", false)
                )
            )
        )
    }

    private fun createInitialChatMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "👋 Welcome to Yuto Copilot! I'm your dedicated YouTube Growth Strategist powered by Gemini 3.8 Flash.\n\nAsk me anything:\n• 'Suggest 5 high-CTR titles for a tech video'\n• 'How to structure the first 30 seconds for 70%+ retention?'\n• 'What are breakout keywords in my niche?'\n• 'Write a viral YouTube Shorts hook'\n\nWhat are you planning to film or upload next?"
            )
        )
    }
}

package com.example.model

import java.util.UUID

enum class UploadPrivacy(val label: String) {
    PUBLIC("Public (Instant Distribution)"),
    UNLISTED("Unlisted (Review First)"),
    PRIVATE("Private (Channel Team)"),
    SCHEDULED("Scheduled (Prime Time)")
}

enum class VideoFormatMode(val displayName: String, val aspectRatioDesc: String, val recommendedFor: String) {
    LONG_FORM_16_9("Standard 16:9", "1920x1080 (Landscape)", "Long-form 5-60 min videos & highest ad CPM"),
    SHORTS_9_16("YouTube Shorts 9:16", "1080x1920 (Vertical)", "Viral short videos under 60s & rapid subscriber growth")
}

data class TitleOption(
    val title: String,
    val style: String, // e.g. "Curiosity Hook", "Urgency / FOMO", "How-To Authority", "A/B Challenger"
    val predictedCtr: Int, // 80 - 98%
    val reasoning: String
)

data class ChapterTimestamp(
    val timeFormatted: String, // e.g. "0:00"
    val title: String
)

data class ThumbnailConfig(
    val boldHeadline: String = "THE SECRET TRUTH",
    val subHeadline: String = "100% Proven Method",
    val badgeText: String = "MUST WATCH",
    val bgTheme: String = "NEON_DARK", // NEON_DARK, CRIMSON_FIRE, CYBER_BLUE, GOLD_LUXURY
    val highContrastFilter: Boolean = true,
    val includeFaceReactionGuide: Boolean = true,
    val selectedImageUrl: String? = null
)

data class GrowthAuditItem(
    val title: String,
    val status: AuditStatus,
    val tip: String
)

enum class AuditStatus {
    PASS,
    WARNING,
    CRITICAL
}

enum class ProjectState {
    DRAFT,
    OPTIMIZING,
    READY,
    UPLOADING,
    PUBLISHED
}

data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val videoTitle: String = "Untitled Video Project",
    val videoUri: String? = null,
    val originalFileName: String = "video_recording.mp4",
    val durationSeconds: Int = 480, // Default 8 mins (great for mid-roll ads!)
    val formatMode: VideoFormatMode = VideoFormatMode.LONG_FORM_16_9,
    val trimStartSeconds: Int = 0,
    val trimEndSeconds: Int = 480,
    val targetNiche: String = "Tech & AI Automation",
    val primaryFocus: String = "AI YouTube Automation with Gemini",
    
    // AI Generated Growth Metadata
    val selectedTitle: String = "How I Automated My ENTIRE YouTube Channel With Gemini AI (Step-by-Step)",
    val titleOptions: List<TitleOption> = emptyList(),
    val description: String = "",
    val tags: List<String> = emptyList(),
    val chapters: List<ChapterTimestamp> = emptyList(),
    val category: String = "Science & Technology",
    val privacy: UploadPrivacy = UploadPrivacy.PUBLIC,
    val scheduledDate: String = "Tomorrow at 2:00 PM EST (Peak)",
    val isMadeForKids: Boolean = false,
    
    // Thumbnail Config
    val thumbnailConfig: ThumbnailConfig = ThumbnailConfig(),
    
    // Growth Engine
    val seoScore: Int = 94,
    val projectedReachScore: Int = 89,
    val state: ProjectState = ProjectState.READY,
    val createdAt: Long = System.currentTimeMillis()
)

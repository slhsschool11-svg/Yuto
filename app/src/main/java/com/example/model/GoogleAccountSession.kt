package com.example.model

data class GoogleAccountSession(
    val email: String = "slhsschool11@gmail.com",
    val displayName: String = "Creator Alpha",
    val channelTitle: String = "Alpha Studio Tech",
    val channelHandle: String = "@alphastudiotech",
    val channelAvatarLetter: String = "A",
    val subscriberCount: String = "32.4K",
    val totalVideos: Int = 158,
    val isSignedIn: Boolean = true,
    val oauthScopesGranted: List<String> = listOf(
        "https://www.googleapis.com/auth/youtube.upload",
        "https://www.googleapis.com/auth/youtube.readonly",
        "https://www.googleapis.com/auth/userinfo.email"
    ),
    val privacyLevel: String = "Enterprise AES-256 Token Encryption",
    val tokenExpiry: String = "Refreshed via Google Secure Session"
)

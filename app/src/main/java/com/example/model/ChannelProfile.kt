package com.example.model

data class ChannelProfile(
    val channelName: String = "Creator Alpha Studio",
    val channelHandle: String = "@creatoralphastudio",
    val subscriberCount: String = "28.5K",
    val totalViews: String = "3.2M",
    val isConnected: Boolean = true,
    val isMonetizationEnabled: Boolean = true,
    val primaryNiche: String = "Technology & AI",
    val defaultFooterLinks: String = "🔥 Join our Discord: discord.gg/creators\n📸 Follow Instagram: @creatoralpha\n💼 Business: partnerships@creatoralpha.com",
    val targetAudienceLocation: String = "United States / Global English"
)

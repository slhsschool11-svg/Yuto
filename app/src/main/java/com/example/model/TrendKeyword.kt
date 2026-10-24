package com.example.model

data class TrendKeyword(
    val keyword: String,
    val searchVolume: String, // e.g. "850K/mo"
    val competition: String, // "Low", "Medium", "High"
    val growthRate: String, // "+145% this week"
    val isBreakout: Boolean = false
)

data class ResearchInsight(
    val topicName: String,
    val viralOpportunityScore: Int, // 0 - 100
    val audienceRetentionTip: String,
    val bestUploadWindow: String,
    val highPerformingKeywords: List<TrendKeyword>
)

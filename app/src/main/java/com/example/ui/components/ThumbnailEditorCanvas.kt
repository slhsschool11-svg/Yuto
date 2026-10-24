package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThumbnailConfig
import com.example.model.VideoFormatMode
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed
import com.example.ui.theme.YouTubeRedDark

@Composable
fun ThumbnailEditorCanvas(
    config: ThumbnailConfig,
    formatMode: VideoFormatMode,
    durationFormatted: String = "8:00",
    onConfigChange: (ThumbnailConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val isShorts = formatMode == VideoFormatMode.SHORTS_9_16
    val canvasRatio = if (isShorts) 9f / 16f else 16f / 9f

    val bgGradient = when (config.bgTheme) {
        "CRIMSON_FIRE" -> Brush.radialGradient(
            colors = listOf(Color(0xFFFF1744), Color(0xFF660011), Color(0xFF140003)),
            radius = 600f
        )
        "CYBER_BLUE" -> Brush.radialGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF003666), Color(0xFF050B14)),
            radius = 600f
        )
        "GOLD_LUXURY" -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFD700), Color(0xFF8B6508), Color(0xFF141005)),
            radius = 600f
        )
        else -> Brush.radialGradient( // NEON_DARK
            colors = listOf(Color(0xFF2E1A47), Color(0xFF160D24), Color(0xFF07050A)),
            radius = 600f
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("thumbnail_editor_canvas")
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "High-CTR Thumbnail Studio",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (isShorts) "9:16 Shorts" else "16:9 Standard HD",
                color = AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Thumbnail Canvas Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(canvasRatio)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, StudioCardBorder, RoundedCornerShape(12.dp))
        ) {
            val width = maxWidth
            val height = maxHeight

            // Background canvas with decorative lighting rays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgGradient)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Vignette border and decorative spotlight circles
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.75f, size.height * 0.35f),
                            radius = size.width * 0.45f
                        )
                    )
                }

                // Viral Badge (Top Left)
                if (config.badgeText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(YouTubeRed, AccentGold)))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = config.badgeText.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Center Bold Headline & Subtitle
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // High-CTR Main Headline
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = config.boldHeadline.uppercase(),
                            color = AccentGold,
                            style = TextStyle(
                                fontSize = if (isShorts) 20.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(3f, 3f),
                                    blurRadius = 6f
                                )
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Punchy Subtitle
                    if (config.subHeadline.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(YouTubeRed.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = config.subHeadline,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // YouTube Duration Stamp in Bottom-Right
                if (!isShorts) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = durationFormatted,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Preset Theme Selectors
        Text(
            text = "Thumbnail Lighting Style:",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeChip(
                label = "Neon Dark",
                selected = config.bgTheme == "NEON_DARK",
                accentColor = Color(0xFF9C27B0),
                onClick = { onConfigChange(config.copy(bgTheme = "NEON_DARK")) }
            )
            ThemeChip(
                label = "Crimson Fire",
                selected = config.bgTheme == "CRIMSON_FIRE",
                accentColor = YouTubeRed,
                onClick = { onConfigChange(config.copy(bgTheme = "CRIMSON_FIRE")) }
            )
            ThemeChip(
                label = "Cyber Blue",
                selected = config.bgTheme == "CYBER_BLUE",
                accentColor = AccentCyan,
                onClick = { onConfigChange(config.copy(bgTheme = "CYBER_BLUE")) }
            )
            ThemeChip(
                label = "Gold Luxury",
                selected = config.bgTheme == "GOLD_LUXURY",
                accentColor = AccentGold,
                onClick = { onConfigChange(config.copy(bgTheme = "GOLD_LUXURY")) }
            )
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accentColor.copy(alpha = 0.25f) else StudioSurfaceVariant)
            .border(
                1.dp,
                if (selected) accentColor else StudioCardBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

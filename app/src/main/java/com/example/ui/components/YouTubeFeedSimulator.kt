package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChannelProfile
import com.example.model.VideoProject
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YouTubeRed

@Composable
fun YouTubeFeedSimulator(
    project: VideoProject,
    channelProfile: ChannelProfile,
    modifier: Modifier = Modifier
) {
    var isFeedDarkTheme by remember { mutableStateOf(true) }

    val feedBg = if (isFeedDarkTheme) Color(0xFF0F0F0F) else Color(0xFFFFFFFF)
    val feedTextPrimary = if (isFeedDarkTheme) Color(0xFFF1F1F1) else Color(0xFF0F0F0F)
    val feedTextSecondary = if (isFeedDarkTheme) Color(0xFFAAAAAA) else Color(0xFF606060)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("youtube_feed_simulator")
    ) {
        // Simulator Header with Dark/Light toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "YouTube Feed Preview (A/B Test)",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Dark/Light toggle pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StudioSurfaceVariant)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(20.dp))
                    .clickable { isFeedDarkTheme = !isFeedDarkTheme }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFeedDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Toggle Mode",
                    tint = AccentGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isFeedDarkTheme) "Dark Feed" else "Light Feed",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulated YouTube Mobile Feed Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(feedBg)
                .border(1.dp, if (isFeedDarkTheme) Color(0xFF282828) else Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live Thumbnail preview in simulator
                ThumbnailEditorCanvas(
                    config = project.thumbnailConfig,
                    formatMode = project.formatMode,
                    durationFormatted = String.format("%d:%02d", project.durationSeconds / 60, project.durationSeconds % 60),
                    onConfigChange = { /* View only in simulator */ },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube Metadata Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Channel Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = channelProfile.channelName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Channel stats
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.selectedTitle,
                            color = feedTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = channelProfile.channelName,
                                color = feedTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = feedTextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = " • 128K views • 2 hours ago",
                                color = feedTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = feedTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

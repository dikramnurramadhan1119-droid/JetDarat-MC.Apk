package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcGoldBorder
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcWarmGold

@Composable
fun JdmcSplashOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    var ignited by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) ignited = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (ignited) 1f else 0.86f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "jdmc_splash_scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(350))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JdmcObsidianBlack)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                JdmcLightYellow.copy(alpha = 0.16f),
                                JdmcWarmGold.copy(alpha = 0.05f),
                                JdmcObsidianBlack
                            ),
                            center = center,
                            radius = size.minDimension * 0.55f
                        )
                    )
                }
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clickable(onClick = onDismiss)
                .padding(24.dp)
                .testTag("jdmc_splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = JdmcObsidianBlack,
                    border = BorderStroke(2.dp, JdmcLightYellow.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .size(164.dp)
                        .scale(logoScale)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.jdmc_club_logo),
                        contentDescription = "JDMC Official Crest",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "JDMC DIGITAL HQ",
                    style = MaterialTheme.typography.displaySmall,
                    color = JdmcLightYellow,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "JET DARAT MOTORCYCLE CLUB • LOS SANTOS",
                    style = MaterialTheme.typography.labelLarge,
                    color = JdmcMetallicSilver,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, JdmcGoldBorder)
                ) {
                    Text(
                        text = "\"Satu Jalan, Satu Mimpi, Satu Keluarga.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = JdmcLightYellow,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    color = JdmcLightYellow,
                    trackColor = JdmcCarbonElevated,
                    modifier = Modifier
                        .width(180.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
                Text(
                    text = "INITIALIZING HEADQUARTERS • SATU MIMPI RP",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Tap anywhere to enter",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcBrushedSilver
                )
            }
        }
    }
}

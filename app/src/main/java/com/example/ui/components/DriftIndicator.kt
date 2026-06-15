package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DriftIndicator(
    score: Double,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "DriftScoreAnimation"
    )

    // Determine clinical color-coding
    val indicatorColor = when {
        score <= 20.0 -> Color(0xFF10B981) // Green (Stable)
        score <= 45.0 -> Color(0xFFFBBF24) // Yellow (Elevated Drift)
        else -> Color(0xFFEF4444)          // Red (Critical Drift Alert)
    }

    val statusText = when {
        score <= 20.0 -> "STABLE BASELINE"
        score <= 45.0 -> "ELEVATED SYSTEM DRIFT"
        else -> "CRITICAL VARIANCE ALERT"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(170.dp)
    ) {
        // Draw the concentric physiological drift circle
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Background ring track
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dynamic progress arc
            drawArc(
                color = indicatorColor,
                startAngle = -90f,
                sweepAngle = (animatedScore / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Inner clinical diagnostics text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = String.format(java.util.Locale.getDefault(), "%.1f%%", score),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor,
                    fontSize = 32.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "DRIFT INDEX",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InteractiveTrendView(
    logs: List<DailyLog>,
    modifier: Modifier = Modifier
) {
    var selectedChannel by remember { mutableStateOf("Glucose") } // "Glucose", "Blood Pressure", "SpO2"
    val chronologicalLogs = remember(logs) {
        logs.sortedBy { it.timestamp }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Channel Selector HUD Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOMETRIC CORRELATION",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp
                    )
                )

                Row {
                    listOf("Glucose", "BP", "SpO2").forEach { channel ->
                        val isSelected = selectedChannel == channel
                        TextButton(
                            onClick = { selectedChannel = channel },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(channel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chronologicalLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "INSUFFICIENT TELEMETRY DATA\nLog biometric readings to visualize trends.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Y Axis Indicators
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(40.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        val labels = when (selectedChannel) {
                            "Glucose" -> listOf("200", "150", "100", "50")
                            "BP" -> listOf("180", "140", "100", "60")
                            else -> listOf("100%", "95%", "90%", "85%")
                        }
                        labels.forEach { label ->
                            Text(
                                label,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Coordinate Canvas
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // Draw horizontal gridline helpers
                        val gridLinesCount = 3
                        paintGridAndAxes(this, gridLinesCount, canvasWidth, canvasHeight)

                        if (chronologicalLogs.size < 2) {
                            // If only 1 entry, draw a solitary diagnostic node
                            val log = chronologicalLogs[0]
                            val valY = getNormalizedY(log, selectedChannel, canvasHeight)
                            drawCircle(
                                color = getChannelColor(selectedChannel),
                                radius = 6.dp.toPx(),
                                center = Offset(canvasWidth / 2f, valY)
                            )
                            return@Canvas
                        }

                        // Plot coordinate vectors
                        val points = mutableListOf<Offset>()
                        val stepX = canvasWidth / (chronologicalLogs.size - 1)

                        chronologicalLogs.forEachIndexed { index, log ->
                            val posX = index * stepX
                            val posY = getNormalizedY(log, selectedChannel, canvasHeight)
                            points.add(Offset(posX, posY))
                        }

                        // Draw spline paths
                        val strokeColor = getChannelColor(selectedChannel)
                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val p0 = points[i - 1]
                                val p1 = points[i]
                                cubicTo(
                                    (p0.x + p1.x) / 2f, p0.y,
                                    (p0.x + p1.x) / 2f, p1.y,
                                    p1.x, p1.y
                                )
                            }
                        }
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw symptom tags / annotation correlation markers
                        chronologicalLogs.forEachIndexed { index, log ->
                            val posX = index * stepX
                            val posY = getNormalizedY(log, selectedChannel, canvasHeight)

                            // Nodes
                            drawCircle(
                                color = strokeColor,
                                radius = 4.dp.toPx(),
                                center = Offset(posX, posY)
                            )

                            // Subjective symptoms trigger color-coded flags on the chart
                            if (log.symptoms.isNotBlank() && log.symptoms != "Biometrics Logged" && log.symptoms != "Symptom Free" && log.symptoms != "Document Parsing") {
                                val flagColor = when {
                                    log.symptoms.contains("dizz", ignoreCase = true) -> Color(0xFFFBBF24) // Yellow Warning
                                    log.symptoms.contains("pain", ignoreCase = true) || log.symptoms.contains("nausea", ignoreCase = true) -> Color(0xFFEF4444) // Red Alert
                                    else -> Color(0xFF3B82F6) // Blue general symptom
                                }

                                // Glowing symptom flag drop markers
                                drawCircle(
                                    color = flagColor,
                                    radius = 7.dp.toPx(),
                                    center = Offset(posX, posY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(posX, posY)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timeline X Legend & Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 46.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                    val sampleIndices = if (chronologicalLogs.size <= 4) {
                        chronologicalLogs.indices.toList()
                    } else {
                        listOf(0, chronologicalLogs.size / 2, chronologicalLogs.size - 1)
                    }

                    chronologicalLogs.forEachIndexed { index, log ->
                        if (sampleIndices.contains(index)) {
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Map details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(getChannelColor(selectedChannel), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Objective Vital", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Severe Symptom (Pain/Nausea)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFBBF24), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mild Symptom (Dizziness)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }
    }
}

private fun paintGridAndAxes(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    lines: Int,
    width: Float,
    height: Float
) {
    for (i in 1..lines) {
        val gy = (height / (lines + 1)) * i
        drawScope.drawLine(
            color = Color.LightGray.copy(alpha = 0.1f),
            start = Offset(0f, gy),
            end = Offset(width, gy),
            strokeWidth = 1f
        )
    }
}

/**
 * Maps the raw biometric value into a normal range fitting the Y height coordinate bounds.
 */
private fun getNormalizedY(log: DailyLog, channel: String, height: Float): Float {
    val value = when (channel) {
        "Glucose" -> log.glucose ?: 100.0
        "BP" -> log.bpSystolic?.toDouble() ?: 120.0
        else -> log.spo2 ?: 98.0
    }

    val minVal = when (channel) {
        "Glucose" -> 40.0
        "BP" -> 60.0
        else -> 80.0
    }

    val maxVal = when (channel) {
        "Glucose" -> 220.0
        "BP" -> 200.0
        else -> 100.0
    }

    val fraction = (value - minVal) / (maxVal - minVal)
    val invertedFraction = 1f - fraction.toFloat() // Canvas 0 is at the top, so we invert Y!
    return height * invertedFraction.coerceIn(0.1f, 0.9f)
}

private fun getChannelColor(channel: String): Color {
    return when (channel) {
        "Glucose" -> Color(0xFF10B981) // Emerald Green
        "BP" -> Color(0xFFF59E0B)      // Intense Amber
        else -> Color(0xFF3B82F6)      // Cobalt Blue
    }
}

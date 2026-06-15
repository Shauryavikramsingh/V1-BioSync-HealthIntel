package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun BiometricHudCard(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val gluc by viewModel.glucoseInput.collectAsState()
    val sys by viewModel.bpSystolicInput.collectAsState()
    val dia by viewModel.bpDiastolicInput.collectAsState()
    val spo2 by viewModel.spo2Input.collectAsState()

    var glucoseUnit by remember { mutableStateOf("mg/dL") } // "mg/dL" or "mmol/L"

    // Validations & Flags
    val glucNum = gluc.toDoubleOrNull()
    val sysNum = sys.toIntOrNull()
    val diaNum = dia.toIntOrNull()
    val spo2Num = spo2.toDoubleOrNull()

    val glucoseAlert = remember(glucNum, glucoseUnit) {
        if (glucNum == null) null
        else {
            val mgDlValue = if (glucoseUnit == "mmol/L") glucNum * 18.0 else glucNum
            when {
                mgDlValue < 70.0 -> "Hypoglycemia Alert"
                mgDlValue > 180.0 -> "Hyperglycemia Warning"
                else -> null
            }
        }
    }

    val bpAlert = remember(sysNum, diaNum) {
        when {
            sysNum == null && diaNum == null -> null
            sysNum != null && sysNum >= 140 -> "Stage 2 Hypertension"
            diaNum != null && diaNum >= 90 -> "Stage 2 Diastolic High"
            sysNum != null && sysNum < 90 -> "Hypotension Alert"
            else -> null
        }
    }

    val spo2Alert = remember(spo2Num) {
        if (spo2Num == null) null
        else {
            when {
                spo2Num > 100.0 || spo2Num < 50.0 -> "Invalid Range"
                spo2Num < 92.0 -> "Hypoxemia Critical alert!"
                spo2Num < 95.0 -> "Borderline Oxygen sat"
                else -> null
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BIOMETRIC INPUT HUD",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                // Glucose Auto-conversion Unit Toggle Button
                TextButton(
                    onClick = {
                        val currentGluc = gluc.toDoubleOrNull()
                        if (currentGluc != null) {
                            if (glucoseUnit == "mg/dL") {
                                // Convert mg/dL to mmol/L: divide by 18
                                val converted = String.format(java.util.Locale.getDefault(), "%.1f", currentGluc / 18.0)
                                viewModel.setGlucose(converted)
                                glucoseUnit = "mmol/L"
                            } else {
                                // Convert mmol/L to mg/dL: multiply by 18
                                val converted = Math.round(currentGluc * 18.0).toString()
                                viewModel.setGlucose(converted)
                                glucoseUnit = "mg/dL"
                            }
                        } else {
                            glucoseUnit = if (glucoseUnit == "mg/dL") "mmol/L" else "mg/dL"
                        }
                    },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Unit: $glucoseUnit (Tap to Convert)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Glucose Field
                OutlinedTextField(
                    value = gluc,
                    onValueChange = { viewModel.setGlucose(it) },
                    label = { Text("Glucose ($glucoseUnit)", fontSize = 11.sp) },
                    placeholder = { Text(if (glucoseUnit == "mg/dL") "100" else "5.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = glucoseAlert != null,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Blood Pressure Fields Duo (Compactly styled)
                Column(
                    modifier = Modifier.weight(1.2f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = sys,
                            onValueChange = { viewModel.setBpSystolic(it) },
                            label = { Text("SYS mmHg", fontSize = 9.sp) },
                            placeholder = { Text("120") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = bpAlert != null,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dia,
                            onValueChange = { viewModel.setBpDiastolic(it) },
                            label = { Text("DIA mmHg", fontSize = 9.sp) },
                            placeholder = { Text("80") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = bpAlert != null,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Oxygen SpO2 Field
                OutlinedTextField(
                    value = spo2,
                    onValueChange = { viewModel.setSpo2(it) },
                    label = { Text("SpO2 %", fontSize = 11.sp) },
                    placeholder = { Text("98") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = spo2Alert != null,
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Clinical validation flags
            if (glucoseAlert != null || bpAlert != null || spo2Alert != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    glucoseAlert?.let {
                        ClinicalStatusFlag(label = it, color = if (it.contains("Warning") || it.contains("Alert")) Color(0xFFEF4444) else Color(0xFF10B981))
                    }
                    bpAlert?.let {
                        ClinicalStatusFlag(label = it, color = Color(0xFFF59E0B))
                    }
                    spo2Alert?.let {
                        ClinicalStatusFlag(label = it, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalStatusFlag(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Alert Detail",
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

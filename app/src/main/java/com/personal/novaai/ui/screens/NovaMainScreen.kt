package com.personal.novaai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.novaai.domain.model.AgentTaskPlan
import com.personal.novaai.domain.model.PlanStep
import com.personal.novaai.domain.model.StepStatus
import com.personal.novaai.ui.components.AnimatedAiOrb
import com.personal.novaai.ui.theme.*

@Composable
fun NovaMainScreen(
    isListening: Boolean,
    amplitude: Float,
    statusText: String,
    currentPlan: AgentTaskPlan?,
    onVoiceClick: () -> Unit,
    onSendCommand: (String) -> Unit,
    onApproveStep: (PlanStep) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = NavyDeep,
        bottomBar = {
            NavigationBar(
                containerColor = NavySurface,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Core") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Agent") },
                    label = { Text("Agent") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Apps") },
                    label = { Text("Hub") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Config") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOVA AI",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Text(
                        text = "AUTONOMOUS AGENT ORCHESTRATOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = VioletGlow
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONLINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> CoreDashboardTab(
                    isListening = isListening,
                    amplitude = amplitude,
                    statusText = statusText,
                    onVoiceClick = onVoiceClick,
                    onQuickAction = { onSendCommand(it) }
                )
                1 -> LiveAgentTab(
                    currentPlan = currentPlan,
                    onApproveStep = onApproveStep
                )
                2 -> AppIntegrationHubTab(onSelectApp = { onSendCommand("Open $it") })
                3 -> ConfigurationTab()
            }

            Spacer(modifier = Modifier.weight(1f))

            // Live Terminal Status Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurface)
                    .border(1.dp, VioletNeon.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = statusText,
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multilingual Command Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NavyCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Hindi / English / Hinglish aadesh dein...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ElectricBlue,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendCommand(textInput.trim())
                            textInput = ""
                        } else {
                            onVoiceClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (textInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = "Send",
                        tint = if (isListening) CrimsonError else ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CoreDashboardTab(
    isListening: Boolean,
    amplitude: Float,
    statusText: String,
    onVoiceClick: () -> Unit,
    onQuickAction: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedAiOrb(
            isListening = isListening,
            amplitude = amplitude,
            modifier = Modifier.clickable { onVoiceClick() }
        )

        Text(
            text = if (isListening) "Listening to Hindi/English..." else "Tap Orb or speak command",
            color = if (isListening) ElectricBlue else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Grid
        Text(
            text = "VERIFIED AGENT WORKFLOWS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VioletGlow,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                title = "WhatsApp Om",
                desc = "Send ❤️ message",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("whatsapp open massage om ❤️") }
            )
            QuickActionButton(
                title = "Play Music",
                desc = "YouTube trending",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("play trending music on youtube") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                title = "Open Camera",
                desc = "Snap hardware",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("camera open karo") }
            )
            QuickActionButton(
                title = "Settings",
                desc = "Wi-Fi & System",
                modifier = Modifier.weight(1f),
                onClick = { onQuickAction("open wifi settings") }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun LiveAgentTab(
    currentPlan: AgentTaskPlan?,
    onApproveStep: (PlanStep) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AGENT ORCHESTRATION GRAPH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (currentPlan == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavySurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active task in progress. Give a command to plan steps.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = "Goal: ${currentPlan.userGoal}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Intent: ${currentPlan.intent} | ${currentPlan.reasoning}",
                color = VioletGlow,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentPlan.steps) { step ->
                    StepCard(step = step, onApprove = { onApproveStep(step) })
                }
            }
        }
    }
}

@Composable
fun StepCard(step: PlanStep, onApprove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(
                1.dp,
                if (step.requiresApproval) AmberWarning else GlassBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Step ${step.stepId}: ${step.action}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Text(
                    text = "Target: ${step.target}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (step.requiresApproval) {
                    Text(
                        text = "⚠️ SENSITIVE ACTION — Explicit Consent Required",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberWarning
                    )
                }
            }

            if (step.requiresApproval && step.status == StepStatus.WAITING_APPROVAL) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("APPROVE", fontSize = 11.sp, color = NavyDeep)
                }
            } else {
                Text(
                    text = step.status.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (step.status == StepStatus.COMPLETED) EmeraldSuccess else VioletGlow
                )
            }
        }
    }
}

@Composable
fun AppIntegrationHubTab(onSelectApp: (String) -> Unit) {
    val apps = listOf(
        "WhatsApp" to "Official messaging & chat launcher",
        "YouTube" to "Video, music and media stream",
        "Camera" to "Direct image capture hardware",
        "Chrome" to "Web search & browsing",
        "Settings" to "System Wi-Fi & Device configuration",
        "Calculator" to "Fast arithmetic engine",
        "Google Maps" to "Navigation & location tracking"
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(apps) { (name, desc) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .clickable { onSelectApp(name) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = desc, fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.Launch, contentDescription = "Launch", tint = ElectricBlue)
                }
            }
        }
    }
}

@Composable
fun ConfigurationTab() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AGENT SECURITY & ENGINE CONFIG",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConfigItem(title = "Language Mode", value = "Hindi / English / Hinglish")
        ConfigItem(title = "Screen Vision", value = "MediaProjection (Consent Enforced)")
        ConfigItem(title = "Permanent Keystore", value = "Verified (Seamless updates)")
        ConfigItem(title = "Encryption", value = "Android Keystore AES-256")
        ConfigItem(title = "Backend Service", value = "FastAPI + PostgreSQL Ready")
    }
}

@Composable
fun ConfigItem(title: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletGlow)
            Text(text = value, fontSize = 13.sp, color = TextPrimary)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

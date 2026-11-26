package com.personal.novaai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.personal.novaai.data.remote.NovaRemoteBrain
import com.personal.novaai.domain.model.AgentTaskPlan
import com.personal.novaai.domain.model.PlanStep
import com.personal.novaai.domain.model.StepStatus
import com.personal.novaai.engine.integration.MobileIntegrationHub
import com.personal.novaai.engine.voice.VoiceEngine
import com.personal.novaai.ui.screens.NovaMainScreen
import com.personal.novaai.ui.theme.NovaColorScheme
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var voiceEngine: VoiceEngine
    private lateinit var integrationHub: MobileIntegrationHub
    private val remoteBrain = NovaRemoteBrain()

    private var isListeningState by mutableStateOf(false)
    private var amplitudeState by mutableStateOf(0f)
    private var statusTextState by mutableStateOf("> NOVA AI Core Active. Awaiting commands...")
    private var currentPlanState by mutableStateOf<AgentTaskPlan?>(null)

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceEngine.startListening()
        } else {
            statusTextState = "> Audio permission is required for voice commands."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        integrationHub = MobileIntegrationHub(this)
        voiceEngine = VoiceEngine(this) { spokenText ->
            processUserGoal(spokenText)
        }

        lifecycleScope.launch {
            voiceEngine.isListening.collect { listening ->
                isListeningState = listening
                if (listening) {
                    statusTextState = "> Listening to Hindi / English voice input..."
                }
            }
        }

        lifecycleScope.launch {
            voiceEngine.spokenAmplitude.collect { amp ->
                amplitudeState = amp
            }
        }

        setContent {
            MaterialTheme(colorScheme = NovaColorScheme) {
                NovaMainScreen(
                    isListening = isListeningState,
                    amplitude = amplitudeState,
                    statusText = statusTextState,
                    currentPlan = currentPlanState,
                    onVoiceClick = {
                        checkAudioAndListen()
                    },
                    onSendCommand = { command ->
                        processUserGoal(command)
                    },
                    onApproveStep = { step ->
                        executeStepDirect(step)
                    }
                )
            }
        }
    }

    private fun checkAudioAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (isListeningState) {
                voiceEngine.stopListening()
            } else {
                voiceEngine.startListening()
            }
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun processUserGoal(goal: String) {
        statusTextState = "> 🧠 Formulating plan for: \"$goal\"..."
        lifecycleScope.launch {
            val a11y = com.personal.novaai.engine.orchestrator.NovaAccessibilityEngine.instance
            if (a11y == null) {
                statusTextState = "> ⚠️ कृपया Accessibility Settings में जाकर 'NOVA AI' को ON करें!"
                val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                return@launch
            }

            val result = remoteBrain.planGoal(goal)
            result.onSuccess { plan ->
                currentPlanState = plan
                statusTextState = "> Plan ready: ${plan.intent} (${plan.steps.size} steps)"
                voiceEngine.speak(plan.spokenReply)

                // Minimize so target screen appears immediately
                moveTaskToBack(true)

                // Execute autonomous steps sequentially
                executePlanSequence(plan)
            }.onFailure { error ->
                statusTextState = "> Error formulating plan: ${error.message}"
                voiceEngine.speak("Aadesh process karne me samasya aayi.")
            }
        }
    }

    private fun executePlanSequence(plan: AgentTaskPlan) {
        lifecycleScope.launch {
            for (step in plan.steps) {
                if (step.requiresApproval) {
                    step.status = StepStatus.WAITING_APPROVAL
                    statusTextState = "> Step ${step.stepId} requires user approval."
                    return@launch
                }

                step.status = StepStatus.IN_PROGRESS
                statusTextState = "> Executing ${step.action}: ${step.target}..."

                val execResult = integrationHub.executeStep(step)
                if (execResult.isSuccess) {
                    step.status = StepStatus.COMPLETED
                    step.resultMessage = execResult.getOrNull()
                } else {
                    step.status = StepStatus.FAILED
                    step.resultMessage = execResult.exceptionOrNull()?.message
                }
                delay(2000)
            }
            statusTextState = "> ✅ All autonomous steps executed successfully!"
        }
    }

    private fun executeStepDirect(step: PlanStep) {
        lifecycleScope.launch {
            step.status = StepStatus.IN_PROGRESS
            val execResult = integrationHub.executeStep(step)
            if (execResult.isSuccess) {
                step.status = StepStatus.COMPLETED
                step.resultMessage = execResult.getOrNull()
                statusTextState = "> ✅ Sensitive action approved and executed."
            } else {
                step.status = StepStatus.FAILED
                step.resultMessage = execResult.exceptionOrNull()?.message
                statusTextState = "> ❌ Action failed: ${step.resultMessage}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceEngine.release()
    }
}

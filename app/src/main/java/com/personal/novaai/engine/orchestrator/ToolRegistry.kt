package com.personal.novaai.engine.orchestrator

import com.personal.novaai.domain.model.PlanStep
import com.personal.novaai.domain.model.ToolDefinition

class ToolRegistry {
    private val tools = mutableMapOf<String, ToolDefinition>()

    init {
        registerBuiltinTools()
    }

    private fun registerBuiltinTools() {
        register(
            ToolDefinition(
                name = "OPEN_APP",
                description = "Launches an authorized Android app via intent package or URI",
                parameters = mapOf("target" to "package_name or alias"),
                requiresApproval = false
            )
        )
        register(
            ToolDefinition(
                name = "SEARCH_IN_APP",
                description = "Searches for a contact, song or keyword inside a target app",
                parameters = mapOf("app" to "target_app", "query" to "search_text"),
                requiresApproval = false
            )
        )
        register(
            ToolDefinition(
                name = "SEND_MESSAGE",
                description = "Sends an SMS or WhatsApp message to a specific contact",
                parameters = mapOf("app" to "app_name", "target" to "contact_name", "message" to "text"),
                requiresApproval = true // Strict user approval gate
            )
        )
        register(
            ToolDefinition(
                name = "CALL_CONTACT",
                description = "Initiates a telephone call via official Android Dialer",
                parameters = mapOf("contact" to "name_or_number"),
                requiresApproval = true // Sensitive action
            )
        )
        register(
            ToolDefinition(
                name = "SET_ALARM",
                description = "Creates a clock alarm or timer using official AlarmClock API",
                parameters = mapOf("hour" to "int", "minute" to "int", "message" to "string"),
                requiresApproval = false
            )
        )
        register(
            ToolDefinition(
                name = "OPEN_URL",
                description = "Opens a verified web URL in Android Chrome or default browser",
                parameters = mapOf("url" to "https_link"),
                requiresApproval = false
            )
        )
        register(
            ToolDefinition(
                name = "SYSTEM_SETTING",
                description = "Opens specific Android settings screen (Wi-Fi, Bluetooth, etc.)",
                parameters = mapOf("target" to "setting_name"),
                requiresApproval = false
            )
        )
    }

    fun register(tool: ToolDefinition) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): ToolDefinition? = tools[name]

    fun getAllTools(): List<ToolDefinition> = tools.values.toList()

    fun validateStep(step: PlanStep): Boolean {
        val tool = getTool(step.action) ?: return false
        return true
    }
}

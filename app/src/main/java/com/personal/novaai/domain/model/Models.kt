package com.personal.novaai.domain.model

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>,
    val requiresApproval: Boolean = false
)

data class PlanStep(
    val stepId: Int,
    val action: String,
    val target: String,
    val parameters: Map<String, Any> = emptyMap(),
    val requiresApproval: Boolean = false,
    var status: StepStatus = StepStatus.PENDING,
    var resultMessage: String? = null
)

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    WAITING_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class AgentTaskPlan(
    val taskId: String,
    val userGoal: String,
    val intent: String,
    val reasoning: String,
    val spokenReply: String,
    val steps: List<PlanStep>
)

data class MemoryItem(
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "general",
    val isSensitive: Boolean = false
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val plan: AgentTaskPlan? = null
)

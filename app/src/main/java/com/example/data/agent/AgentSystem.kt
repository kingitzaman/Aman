package com.example.data.agent

enum class RiskLevel {
    LOW,
    HIGH,
    CRITICAL
}

enum class ToolPermissionPolicy {
    ALLOW,
    DENY,
    ASK_EVERY_TIME
}

data class AgentTool(
    val name: String,
    val description: String,
    val inputSchema: String,
    val outputSchema: String,
    val riskLevel: RiskLevel,
    var policy: ToolPermissionPolicy = when (riskLevel) {
        RiskLevel.LOW -> ToolPermissionPolicy.ALLOW
        RiskLevel.HIGH -> ToolPermissionPolicy.ASK_EVERY_TIME
        RiskLevel.CRITICAL -> ToolPermissionPolicy.ASK_EVERY_TIME
    },
    val isEnabled: Boolean = true
)

sealed class ToolExecutionStatus {
    object Idle : ToolExecutionStatus()
    data class PermissionRequired(val tool: AgentTool, val actionDescription: String) : ToolExecutionStatus()
    data class Executing(val toolName: String) : ToolExecutionStatus()
    data class Success(val toolName: String, val resultSummary: String) : ToolExecutionStatus()
    data class Denied(val toolName: String, val reason: String) : ToolExecutionStatus()
    data class Failed(val toolName: String, val error: String) : ToolExecutionStatus()
}

object AgentRegistry {
    val defaultTools = listOf(
        AgentTool(
            name = "web_search",
            description = "Search configured live search provider for up-to-date web documents.",
            inputSchema = "{\"query\": \"string\", \"category\": \"string\"}",
            outputSchema = "{\"results\": \"array\"}",
            riskLevel = RiskLevel.LOW,
            policy = ToolPermissionPolicy.ALLOW
        ),
        AgentTool(
            name = "read_webpage",
            description = "Extract sanitized text from a verified public URL.",
            inputSchema = "{\"url\": \"string\"}",
            outputSchema = "{\"content\": \"string\"}",
            riskLevel = RiskLevel.LOW,
            policy = ToolPermissionPolicy.ALLOW
        ),
        AgentTool(
            name = "run_code_sandbox",
            description = "Execute arbitrary code inside an isolated backend sandbox container.",
            inputSchema = "{\"language\": \"string\", \"code\": \"string\"}",
            outputSchema = "{\"stdout\": \"string\", \"exitCode\": \"int\"}",
            riskLevel = RiskLevel.HIGH,
            policy = ToolPermissionPolicy.ASK_EVERY_TIME
        ),
        AgentTool(
            name = "delete_user_data",
            description = "Permanently delete conversations, memories, or user records.",
            inputSchema = "{\"target\": \"string\", \"id\": \"string\"}",
            outputSchema = "{\"deleted\": \"boolean\"}",
            riskLevel = RiskLevel.CRITICAL,
            policy = ToolPermissionPolicy.ASK_EVERY_TIME
        )
    )
}

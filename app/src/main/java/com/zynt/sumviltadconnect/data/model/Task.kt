package com.zynt.sumviltadconnect.data.model

data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String, // pending, in_progress, completed
    val progress: Int = 0, // Made non-nullable with default value to fix null safety issues
    val feedback: String?,
    val user_id: Int,
    val created_at: String?,
    val updated_at: String?,
    val user: User? = null,
    val farmer: User? = null
) {
    val isCompleted: Boolean
        get() = status == "completed" || progress >= 100

    val statusDisplayName: String
        get() = when(status) {
            "pending" -> "Pending"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            else -> status.replaceFirstChar { it.uppercase() }
        }

    val progressPercentage: String
        get() = "$progress%"
}

data class TaskResponse(
    val data: List<Task>? = null,
    val tasks: List<Task>? = null, // Keep for compatibility
    val task: Task? = null,
    val message: String? = null,
    val success: Boolean = true,
    val error: String? = null
)

data class TaskProgressRequest(
    val progress: Int
)

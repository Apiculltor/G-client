package com.seudominio.vuzixbladeapp.database

/**
 * DAO simplificado para gerenciar execuções de tarefas - SEM ROOM
 * Usa SharedPreferences para armazenamento simples
 */
class TaskExecutionDao {
    
    // Por enquanto métodos vazios para evitar erros de compilação
    suspend fun getAllExecutions(): List<TaskExecution> {
        return emptyList()
    }
    
    suspend fun getExecutionById(id: Long): TaskExecution? {
        return null
    }
    
    suspend fun getExecutionsByStatus(status: String): List<TaskExecution> {
        return emptyList()
    }
    
    suspend fun insertExecution(execution: TaskExecution): Long {
        return 0L
    }
    
    suspend fun updateExecution(execution: TaskExecution) {
        // Implementar depois
    }
    
    suspend fun deleteExecution(execution: TaskExecution) {
        // Implementar depois
    }
    
    suspend fun deleteOldExecutions(timestamp: Long) {
        // Implementar depois
    }
}

/**
 * Entidade simplificada para execução de tarefas - SEM ROOM
 */
data class TaskExecution(
    val id: Long = 0,
    val taskName: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String, // "running", "completed", "failed", "skipped"
    val actualDuration: Long? = null,
    val videoPath: String? = null,
    val result: String? = null,
    val errorMessage: String? = null
)
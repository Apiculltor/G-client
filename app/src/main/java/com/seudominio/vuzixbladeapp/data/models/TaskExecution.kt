package com.seudominio.vuzixbladeapp.data.models

/**
 * Registro de execução de uma tarefa
 * Armazena dados sobre quando e como uma tarefa foi executada
 */
data class TaskExecution(
    val id: Long = 0,
    val taskId: Long,                    // ID da tarefa relacionada
    val scheduledTime: Long,             // Quando deveria ter sido executada
    val startTime: Long?,                // Quando realmente começou
    val endTime: Long?,                  // Quando terminou
    val status: ExecutionStatus,         // Status da execução
    val actualDuration: Int?,            // Duração real em minutos
    val videoPath: String?,              // Caminho do vídeo gravado
    val notes: String? = null,           // Observações do usuário
    val confidence: Float = 0.0f,        // Confiança da análise de IA (0-1)
    val detectedActivity: String? = null, // Atividade detectada pela IA
    val feedbackGiven: String? = null,   // Feedback fornecido ao usuário
    val createdAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Calcula se a tarefa foi executada no horário
     */
    fun isOnTime(toleranceMinutes: Int = 10): Boolean {
        if (startTime == null) return false
        val diffInMinutes = Math.abs(startTime - scheduledTime) / (60 * 1000)
        return diffInMinutes <= toleranceMinutes
    }
    
    /**
     * Calcula a duração real da execução
     */
    fun getActualDurationMinutes(): Int? {
        return if (startTime != null && endTime != null) {
            ((endTime - startTime) / (60 * 1000)).toInt()
        } else {
            actualDuration
        }
    }
    
    /**
     * Verifica se a execução foi bem-sucedida
     */
    fun isSuccessful(): Boolean {
        return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.PARTIAL
    }
    
    /**
     * Calcula score da execução (0-100)
     */
    fun getExecutionScore(): Int {
        var score = 0
        
        // Base score por status
        score += when (status) {
            ExecutionStatus.COMPLETED -> 40
            ExecutionStatus.PARTIAL -> 25
            ExecutionStatus.SKIPPED -> 10
            ExecutionStatus.MISSED -> 0
            ExecutionStatus.IN_PROGRESS -> 20
        }
        
        // Bonus por pontualidade
        if (isOnTime()) score += 20
        
        // Bonus por duração apropriada
        val actualDur = getActualDurationMinutes()
        if (actualDur != null && actualDur > 0) {
            score += 20
        }
        
        // Bonus por confiança da IA
        score += (confidence * 20).toInt()
        
        return minOf(score, 100)
    }
}

/**
 * Status de execução da tarefa
 */
enum class ExecutionStatus(val displayName: String, val color: Int) {
    IN_PROGRESS("Em Andamento", 0xFF2196F3.toInt()),  // Azul
    COMPLETED("Concluída", 0xFF4CAF50.toInt()),       // Verde
    PARTIAL("Parcial", 0xFFFF9800.toInt()),           // Laranja
    SKIPPED("Pulada", 0xFF9E9E9E.toInt()),            // Cinza
    MISSED("Perdida", 0xFFF44336.toInt())             // Vermelho
}
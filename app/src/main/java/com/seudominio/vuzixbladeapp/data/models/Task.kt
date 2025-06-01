package com.seudominio.vuzixbladeapp.data.models

import java.util.*

/**
 * Modelo de dados para uma tarefa/atividade do usuário
 * Representa uma atividade que o usuário quer formar como hábito
 */
data class Task(
    val id: Long = 0,
    val name: String,                    // Ex: "Beber água", "Exercício", "Meditação"
    val description: String,             // Descrição detalhada da atividade
    val scheduledTime: String,           // Horário programado (HH:mm)
    val duration: Int,                   // Duração esperada em minutos
    val daysOfWeek: List<Int>,          // Dias da semana (1=Domingo, 7=Sábado)
    val isActive: Boolean = true,        // Se a tarefa está ativa
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val reminderMinutes: Int = 5,        // Lembrete X minutos antes
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Verifica se a tarefa deve ser executada hoje
     */
    fun shouldExecuteToday(): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return isActive && daysOfWeek.contains(today)
    }
    
    /**
     * Calcula o próximo horário de execução
     */
    fun getNextExecutionTime(): Calendar? {
        if (!isActive) return null
        
        val now = Calendar.getInstance()
        val taskTime = Calendar.getInstance()
        
        // Parse do horário (HH:mm)
        val timeParts = scheduledTime.split(":")
        if (timeParts.size != 2) return null
        
        taskTime.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        taskTime.set(Calendar.MINUTE, timeParts[1].toInt())
        taskTime.set(Calendar.SECOND, 0)
        taskTime.set(Calendar.MILLISECOND, 0)
        
        // Se já passou hoje, procurar próximo dia válido
        if (taskTime.before(now) || !shouldExecuteToday()) {
            for (i in 1..7) {
                taskTime.add(Calendar.DAY_OF_YEAR, 1)
                val dayOfWeek = taskTime.get(Calendar.DAY_OF_WEEK)
                if (daysOfWeek.contains(dayOfWeek)) {
                    return taskTime
                }
            }
        }
        
        return if (shouldExecuteToday()) taskTime else null
    }
    
    /**
     * Verifica se está no horário de executar (com tolerância)
     */
    fun isTimeToExecute(toleranceMinutes: Int = 5): Boolean {
        if (!shouldExecuteToday()) return false
        
        val now = Calendar.getInstance()
        val taskTime = getNextExecutionTime() ?: return false
        
        val diffInMinutes = Math.abs(now.timeInMillis - taskTime.timeInMillis) / (60 * 1000)
        return diffInMinutes <= toleranceMinutes
    }
}

/**
 * Prioridade da tarefa
 */
enum class TaskPriority(val displayName: String, val color: Int) {
    LOW("Baixa", 0xFF4CAF50.toInt()),      // Verde
    MEDIUM("Média", 0xFFFF9800.toInt()),   // Laranja  
    HIGH("Alta", 0xFFF44336.toInt())       // Vermelho
}

/**
 * Categoria da tarefa
 */
enum class TaskCategory(val displayName: String, val icon: String) {
    PERSONAL("Pessoal", "👤"),
    HEALTH("Saúde", "💪"),
    WORK("Trabalho", "💼"),
    LEARNING("Aprendizado", "📚"),
    SOCIAL("Social", "👥"),
    EXERCISE("Exercício", "🏃"),
    MEDITATION("Meditação", "🧘"),
    NUTRITION("Nutrição", "🥗"),
    OTHER("Outro", "📋")
}
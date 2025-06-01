package com.seudominio.vuzixbladeapp.data.models

/**
 * Relatório de progresso de hábitos
 * Consolidação de dados para análise de progresso
 */
data class HabitReport(
    val id: Long = 0,
    val reportType: ReportType,          // Tipo do relatório (diário, semanal, mensal)
    val startDate: Long,                 // Início do período
    val endDate: Long,                   // Fim do período
    val totalTasks: Int,                 // Total de tarefas programadas
    val completedTasks: Int,             // Tarefas concluídas
    val partialTasks: Int,               // Tarefas parcialmente concluídas
    val missedTasks: Int,                // Tarefas perdidas
    val averageScore: Float,             // Score médio das execuções
    val totalExecutionTime: Int,         // Tempo total de execução (minutos)
    val onTimePercentage: Float,         // Percentual de execuções no horário
    val mostProductiveTime: String?,     // Horário mais produtivo (HH:mm)
    val leastProductiveTime: String?,    // Horário menos produtivo (HH:mm)
    val topPerformingCategory: TaskCategory?, // Categoria com melhor performance
    val improvementSuggestions: List<String>, // Sugestões de melhoria
    val createdAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Calcula taxa de sucesso (0-100%)
     */
    fun getSuccessRate(): Float {
        return if (totalTasks > 0) {
            ((completedTasks + partialTasks * 0.5f) / totalTasks * 100)
        } else 0f
    }
    
    /**
     * Calcula taxa de consistência (baseada em execuções no horário)
     */
    fun getConsistencyRate(): Float = onTimePercentage
    
    /**
     * Gera resumo textual do relatório
     */
    fun getSummary(): String {
        val successRate = getSuccessRate()
        val consistency = getConsistencyRate()
        
        return buildString {
            append("📊 ${reportType.displayName}\n")
            append("✅ Taxa de Sucesso: ${String.format("%.1f", successRate)}%\n")
            append("⏰ Consistência: ${String.format("%.1f", consistency)}%\n")
            append("📈 Score Médio: ${String.format("%.1f", averageScore)}/100\n")
            
            if (mostProductiveTime != null) {
                append("🌟 Melhor Horário: $mostProductiveTime\n")
            }
            
            if (topPerformingCategory != null) {
                append("🏆 Melhor Categoria: ${topPerformingCategory.displayName}\n")
            }
        }
    }
    
    /**
     * Gera feedback baseado no desempenho
     */
    fun getPerformanceFeedback(): String {
        val successRate = getSuccessRate()
        
        return when {
            successRate >= 90 -> "🌟 Excelente! Você está dominando seus hábitos!"
            successRate >= 70 -> "👍 Muito bem! Continue assim!"
            successRate >= 50 -> "⚡ Bom progresso, mas há espaço para melhoria"
            successRate >= 30 -> "💪 Não desista! Pequenos passos levam a grandes resultados"
            else -> "🎯 Vamos focar em hábitos mais simples primeiro"
        }
    }
}

/**
 * Tipo de relatório
 */
enum class ReportType(val displayName: String, val durationDays: Int) {
    DAILY("Relatório Diário", 1),
    WEEKLY("Relatório Semanal", 7),
    MONTHLY("Relatório Mensal", 30),
    CUSTOM("Período Personalizado", 0)
}

/**
 * Dados estatísticos consolidados para dashboard
 */
data class DashboardStats(
    val todayTasks: Int,                 // Tarefas de hoje
    val todayCompleted: Int,             // Concluídas hoje
    val currentStreak: Int,              // Sequência atual de dias
    val longestStreak: Int,              // Maior sequência já alcançada
    val weeklySuccessRate: Float,        // Taxa de sucesso da semana
    val totalHabitsFormed: Int,          // Total de hábitos formados
    val activeHabits: Int,               // Hábitos ativos
    val nextTaskTime: String?,           // Próxima tarefa (HH:mm)
    val nextTaskName: String?            // Nome da próxima tarefa
)
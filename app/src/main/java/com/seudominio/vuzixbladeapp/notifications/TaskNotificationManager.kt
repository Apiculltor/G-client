package com.seudominio.vuzixbladeapp.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.seudominio.vuzixbladeapp.MainActivity
import com.seudominio.vuzixbladeapp.R
import com.seudominio.vuzixbladeapp.data.models.Task

/**
 * Gerenciador de notificações para tarefas
 * Compatível com API 22 (sem NotificationChannel)
 */
class TaskNotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TaskNotificationManager"
        private const val NOTIFICATION_ID_REMINDER = 1001
        private const val NOTIFICATION_ID_EXECUTION = 1002
        private const val NOTIFICATION_ID_FEEDBACK = 1003
        private const val NOTIFICATION_ID_PROGRESS = 1004
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Mostrar notificação de lembrete
     */
    fun showTaskReminder(task: Task) {
        Log.d(TAG, "Exibindo lembrete para: ${task.name}")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", task.id)
            putExtra("action", "reminder")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_notification) // Você precisa adicionar este ícone
            .setContentTitle("🔔 Lembrete: ${task.name}")
            .setContentText("Em ${task.reminderMinutes} minutos: ${task.description}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${task.category.icon} ${task.name}\n\n" +
                        "Inicia em ${task.reminderMinutes} minutos\n" +
                        "Duração: ${task.duration} min\n" +
                        "Categoria: ${task.category.displayName}"))
            .setPriority(when (task.priority) {
                com.seudominio.vuzixbladeapp.data.models.TaskPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                com.seudominio.vuzixbladeapp.data.models.TaskPriority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
                com.seudominio.vuzixbladeapp.data.models.TaskPriority.LOW -> NotificationCompat.PRIORITY_LOW
            })
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_play, // Ícone de play
                "Iniciar Agora",
                createActionIntent(task, "start_now")
            )
            .addAction(
                R.drawable.ic_skip, // Ícone de pular
                "Pular",
                createActionIntent(task, "skip")
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_REMINDER + task.id.toInt(), notification)
    }
    
    /**
     * Mostrar notificação de execução
     */
    fun showTaskExecution(task: Task) {
        Log.d(TAG, "Exibindo notificação de execução para: ${task.name}")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", task.id)
            putExtra("action", "execute")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_notification_active) // Ícone ativo
            .setContentTitle("⏰ Hora de: ${task.name}")
            .setContentText("Vamos começar! Duração: ${task.duration} min")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${task.category.icon} É hora de ${task.name.lowercase()}!\n\n" +
                        "📋 ${task.description}\n" +
                        "⏱ Duração esperada: ${task.duration} minutos\n" +
                        "🎯 Prioridade: ${task.priority.displayName}\n\n" +
                        "O Vuzix Blade iniciará a gravação automaticamente."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(false) // Não remove automaticamente
            .setOngoing(true) // Notificação persistente
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_play,
                "Iniciar",
                createActionIntent(task, "start_execution")
            )
            .addAction(
                R.drawable.ic_pause,
                "Adiar 5min",
                createActionIntent(task, "snooze")
            )
            .addAction(
                R.drawable.ic_skip,
                "Pular",
                createActionIntent(task, "skip")
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_EXECUTION + task.id.toInt(), notification)
    }
    
    /**
     * Mostrar notificação de feedback
     */
    fun showTaskFeedback(task: Task, feedback: String, score: Int) {
        Log.d(TAG, "Exibindo feedback para: ${task.name} - Score: $score")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", task.id)
            putExtra("action", "feedback")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val emoji = when {
            score >= 90 -> "🌟"
            score >= 70 -> "👍"
            score >= 50 -> "⚡"
            score >= 30 -> "💪"
            else -> "🎯"
        }
        
        val notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_feedback)
            .setContentTitle("$emoji ${task.name} Concluída!")
            .setContentText("Score: $score/100 - $feedback")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$emoji Parabéns! Você concluiu: ${task.name}\n\n" +
                        "📊 Score da execução: $score/100\n" +
                        "💬 Feedback: $feedback\n\n" +
                        "Continue assim para formar este hábito!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_report,
                "Ver Relatório",
                createActionIntent(task, "view_report")
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_FEEDBACK + task.id.toInt(), notification)
    }
    
    /**
     * Mostrar progresso diário
     */
    fun showDailyProgress(completedTasks: Int, totalTasks: Int, successRate: Float) {
        Log.d(TAG, "Exibindo progresso diário: $completedTasks/$totalTasks (${successRate}%)")
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "daily_report")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PROGRESS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val emoji = when {
            successRate >= 90 -> "🌟"
            successRate >= 70 -> "🎉"
            successRate >= 50 -> "👍"
            else -> "💪"
        }
        
        val notification = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_progress)
            .setContentTitle("$emoji Progresso do Dia")
            .setContentText("$completedTasks/$totalTasks tarefas (${String.format("%.1f", successRate)}%)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$emoji Progresso de Hoje\n\n" +
                        "✅ Concluídas: $completedTasks\n" +
                        "📋 Total programadas: $totalTasks\n" +
                        "📈 Taxa de sucesso: ${String.format("%.1f", successRate)}%\n\n" +
                        "Continue formando seus hábitos!"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_PROGRESS, notification)
    }
    
    /**
     * Criar intent para ações da notificação
     */
    private fun createActionIntent(task: Task, action: String): PendingIntent {
        val intent = Intent(context, TaskNotificationActionReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("action", action)
        }
        
        return PendingIntent.getBroadcast(
            context,
            (task.id * 100 + action.hashCode()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    /**
     * Cancelar notificação específica
     */
    fun cancelTaskNotification(taskId: Long) {
        notificationManager.cancel(NOTIFICATION_ID_REMINDER + taskId.toInt())
        notificationManager.cancel(NOTIFICATION_ID_EXECUTION + taskId.toInt())
        notificationManager.cancel(NOTIFICATION_ID_FEEDBACK + taskId.toInt())
    }
    
    /**
     * Cancelar todas as notificações
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
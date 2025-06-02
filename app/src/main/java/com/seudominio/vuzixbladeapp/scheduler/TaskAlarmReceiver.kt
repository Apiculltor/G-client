package com.seudominio.vuzixbladeapp.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.monitoring.TaskMonitor
import com.seudominio.vuzixbladeapp.notifications.TaskNotificationManager

/**
 * Receiver simplificado para alarmes de tarefas
 * Gerencia lembretes e execuções programadas
 */
class TaskAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "TaskAlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarme recebido: ${intent.action}")
        
        val taskId = intent.getLongExtra("task_id", -1)
        val taskName = intent.getStringExtra("task_name") ?: "Tarefa"
        val isReminder = intent.getBooleanExtra("is_reminder", false)
        
        if (taskId == -1L) {
            Log.e(TAG, "ID da tarefa inválido no alarme")
            return
        }
        
        try {
            when (intent.action) {
                "TASK_REMINDER" -> {
                    handleTaskReminder(context, taskId, taskName)
                }
                
                "TASK_EXECUTION" -> {
                    handleTaskExecution(context, taskId, taskName)
                }
                
                Intent.ACTION_BOOT_COMPLETED -> {
                    handleBootCompleted(context)
                }
                
                else -> {
                    Log.w(TAG, "Ação não reconhecida: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar alarme: ${e.message}")
        }
    }
    
    /**
     * Tratar lembrete de tarefa
     */
    private fun handleTaskReminder(context: Context, taskId: Long, taskName: String) {
        Log.d(TAG, "Processando lembrete para tarefa: $taskName (ID: $taskId)")
        
        // Exibir notificação de lembrete
        val notificationManager = TaskNotificationManager(context)
        notificationManager.showBasicNotification("Lembrete", "Em breve: $taskName")
        
        Log.d(TAG, "Lembrete exibido para tarefa: $taskName")
    }
    
    /**
     * Tratar execução de tarefa
     */
    private fun handleTaskExecution(context: Context, taskId: Long, taskName: String) {
        Log.d(TAG, "Processando execução para tarefa: $taskName (ID: $taskId)")
        
        // Exibir notificação de execução
        val notificationManager = TaskNotificationManager(context)
        notificationManager.showBasicNotification("Hora da Tarefa", "Iniciando: $taskName")
        
        // Iniciar monitoramento automático
        val taskMonitor = TaskMonitor(context)
        taskMonitor.startAutomaticMonitoring(taskId, taskName, 30)
        
        Log.d(TAG, "Execução iniciada para tarefa: $taskName")
    }
    
    /**
     * Tratar reinicialização do sistema
     */
    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Sistema reiniciado, reagendando todas as tarefas")
        // Implementar depois se necessário
    }
}
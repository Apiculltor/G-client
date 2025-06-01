package com.seudominio.vuzixbladeapp.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.data.dao.TaskDao
import com.seudominio.vuzixbladeapp.monitoring.TaskMonitor
import com.seudominio.vuzixbladeapp.notifications.TaskNotificationManager

/**
 * Receiver para alarmes de tarefas
 * Gerencia lembretes e execuções programadas
 */
class TaskAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "TaskAlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarme recebido: ${intent.action}")
        
        val taskId = intent.getLongExtra(TaskScheduler.EXTRA_TASK_ID, -1)
        val taskName = intent.getStringExtra(TaskScheduler.EXTRA_TASK_NAME) ?: "Tarefa"
        val isReminder = intent.getBooleanExtra(TaskScheduler.EXTRA_IS_REMINDER, false)
        
        if (taskId == -1L) {
            Log.e(TAG, "ID da tarefa inválido no alarme")
            return
        }
        
        try {
            when (intent.action) {
                TaskScheduler.ACTION_TASK_REMINDER -> {
                    handleTaskReminder(context, taskId, taskName)
                }
                
                TaskScheduler.ACTION_TASK_EXECUTION -> {
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
        
        val taskDao = TaskDao(context)
        val task = taskDao.getTaskById(taskId)
        
        if (task == null) {
            Log.w(TAG, "Tarefa não encontrada para lembrete: $taskId")
            return
        }
        
        if (!task.isActive) {
            Log.d(TAG, "Tarefa $taskName não está ativa, ignorando lembrete")
            return
        }
        
        // Exibir notificação de lembrete
        val notificationManager = TaskNotificationManager(context)
        notificationManager.showTaskReminder(task)
        
        Log.d(TAG, "Lembrete exibido para tarefa: $taskName")
    }
    
    /**
     * Tratar execução de tarefa
     */
    private fun handleTaskExecution(context: Context, taskId: Long, taskName: String) {
        Log.d(TAG, "Processando execução para tarefa: $taskName (ID: $taskId)")
        
        val taskDao = TaskDao(context)
        val task = taskDao.getTaskById(taskId)
        
        if (task == null) {
            Log.w(TAG, "Tarefa não encontrada para execução: $taskId")
            return
        }
        
        if (!task.isActive) {
            Log.d(TAG, "Tarefa $taskName não está ativa, ignorando execução")
            return
        }
        
        // Verificar se é realmente hora de executar (tolerância de 5 minutos)
        if (!task.isTimeToExecute(5)) {
            Log.d(TAG, "Não é hora de executar $taskName, reagendando")
            val scheduler = TaskScheduler(context)
            scheduler.scheduleTask(task)
            return
        }
        
        // Exibir notificação de execução
        val notificationManager = TaskNotificationManager(context)
        notificationManager.showTaskExecution(task)
        
        // Iniciar monitoramento automático
        val taskMonitor = TaskMonitor(context)
        taskMonitor.startAutomaticMonitoring(task)
        
        Log.d(TAG, "Execução iniciada para tarefa: $taskName")
    }
    
    /**
     * Tratar reinicialização do sistema
     */
    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Sistema reiniciado, reagendando todas as tarefas")
        
        try {
            val scheduler = TaskScheduler(context)
            scheduler.rescheduleAllTasks()
            
            Log.d(TAG, "Tarefas reagendadas com sucesso após reinicialização")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reagendar tarefas após reinicialização: ${e.message}")
        }
    }
}
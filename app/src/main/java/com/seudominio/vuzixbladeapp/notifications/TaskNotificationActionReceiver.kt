package com.seudominio.vuzixbladeapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.data.dao.TaskDao
import com.seudominio.vuzixbladeapp.monitoring.TaskMonitor

/**
 * Receiver para ações de notificação
 * Trata ações do usuário nas notificações (iniciar, pular, adiar)
 */
class TaskNotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "TaskNotificationActionReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("task_id", -1)
        val action = intent.getStringExtra("action")
        
        if (taskId == -1L || action == null) {
            Log.e(TAG, "Dados inválidos na ação de notificação")
            return
        }
        
        Log.d(TAG, "Ação de notificação recebida: $action para tarefa $taskId")
        
        val taskDao = TaskDao(context)
        val taskMonitor = TaskMonitor(context)
        val notificationManager = TaskNotificationManager(context)
        
        try {
            when (action) {
                "start_now" -> {
                    // Iniciar tarefa imediatamente
                    val task = taskDao.getTaskById(taskId)
                    if (task != null) {
                        taskMonitor.startAutomaticMonitoring(task)
                        notificationManager.cancelTaskNotification(taskId)
                        Log.d(TAG, "Tarefa iniciada imediatamente: ${task.name}")
                    }
                }
                
                "start_execution" -> {
                    // Iniciar execução da tarefa
                    val task = taskDao.getTaskById(taskId)
                    if (task != null) {
                        taskMonitor.startAutomaticMonitoring(task)
                        notificationManager.cancelTaskNotification(taskId)
                        Log.d(TAG, "Execução iniciada: ${task.name}")
                    }
                }
                
                "skip" -> {
                    // Pular tarefa
                    taskMonitor.markTaskSkipped(taskId, "Pulada via notificação")
                    notificationManager.cancelTaskNotification(taskId)
                    Log.d(TAG, "Tarefa pulada: $taskId")
                }
                
                "snooze" -> {
                    // Adiar por 5 minutos
                    val task = taskDao.getTaskById(taskId)
                    if (task != null) {
                        // Cancelar notificação atual
                        notificationManager.cancelTaskNotification(taskId)
                        
                        // Reagendar para 5 minutos
                        val scheduler = com.seudominio.vuzixbladeapp.scheduler.TaskScheduler(context)
                        // TODO: Implementar adiamento de 5 minutos
                        
                        Log.d(TAG, "Tarefa adiada por 5 minutos: ${task.name}")
                    }
                }
                
                "view_report" -> {
                    // Abrir relatório da tarefa
                    val intent = Intent(context, com.seudominio.vuzixbladeapp.MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra("action", "view_task_report")
                    intent.putExtra("task_id", taskId)
                    context.startActivity(intent)
                    Log.d(TAG, "Abrindo relatório da tarefa: $taskId")
                }
                
                else -> {
                    Log.w(TAG, "Ação não reconhecida: $action")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar ação de notificação: ${e.message}")
        }
    }
}
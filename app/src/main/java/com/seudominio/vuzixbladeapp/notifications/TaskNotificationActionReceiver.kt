package com.seudominio.vuzixbladeapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.monitoring.TaskMonitor

/**
 * Receiver simplificado para ações de notificação
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
        
        val taskMonitor = TaskMonitor(context)
        val notificationManager = TaskNotificationManager(context)
        
        try {
            when (action) {
                "start_now", "start_execution" -> {
                    // Iniciar tarefa
                    taskMonitor.startAutomaticMonitoring(taskId, "Task_$taskId", 30)
                    notificationManager.cancelAllNotifications()
                    Log.d(TAG, "Tarefa iniciada: $taskId")
                }
                
                "skip" -> {
                    // Pular tarefa
                    Log.d(TAG, "Tarefa pulada: $taskId")
                    notificationManager.cancelAllNotifications()
                }
                
                "snooze" -> {
                    // Adiar por 5 minutos
                    notificationManager.cancelAllNotifications()
                    Log.d(TAG, "Tarefa adiada por 5 minutos: $taskId")
                }
                
                "view_report" -> {
                    // Abrir relatório da tarefa
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
package com.seudominio.vuzixbladeapp.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log

/**
 * Gerenciador de notificações simplificado
 * Compatível com API 22 (sem NotificationChannel e sem NotificationCompat)
 */
class TaskNotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TaskNotificationManager"
        private const val NOTIFICATION_ID_BASIC = 1001
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Mostrar notificação básica
     */
    fun showBasicNotification(title: String, message: String) {
        Log.d(TAG, "Exibindo notificação: $title")
        
        try {
            val notification = Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID_BASIC, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exibir notificação: ${e.message}")
        }
    }
    
    /**
     * Cancelar todas as notificações
     */
    fun cancelAllNotifications() {
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar notificações: ${e.message}")
        }
    }
}
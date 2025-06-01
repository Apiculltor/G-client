package com.seudominio.vuzixbladeapp.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.data.dao.TaskDao
import com.seudominio.vuzixbladeapp.data.models.Task
import java.util.*

/**
 * Gerenciador de agendamento de tarefas
 * Responsável por programar alarmes para execução e lembretes
 */
class TaskScheduler(private val context: Context) {
    
    companion object {
        private const val TAG = "TaskScheduler"
        const val ACTION_TASK_REMINDER = "com.seudominio.vuzixbladeapp.TASK_REMINDER"
        const val ACTION_TASK_EXECUTION = "com.seudominio.vuzixbladeapp.TASK_EXECUTION"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_NAME = "task_name"
        const val EXTRA_IS_REMINDER = "is_reminder"
    }
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val taskDao = TaskDao(context)
    
    /**
     * Agendar todas as tarefas ativas
     */
    fun scheduleAllActiveTasks() {
        Log.d(TAG, "Agendando todas as tarefas ativas...")
        
        val activeTasks = taskDao.getActiveTasks()
        Log.d(TAG, "Encontradas ${activeTasks.size} tarefas ativas")
        
        activeTasks.forEach { task ->
            scheduleTask(task)
        }
    }
    
    /**
     * Agendar uma tarefa específica
     */
    fun scheduleTask(task: Task) {
        if (!task.isActive) {
            Log.d(TAG, "Tarefa ${task.name} não está ativa, pulando agendamento")
            return
        }
        
        Log.d(TAG, "Agendando tarefa: ${task.name} para ${task.scheduledTime}")
        
        // Cancelar agendamentos anteriores
        cancelTaskAlarms(task.id)
        
        // Agendar para todos os dias da semana configurados
        task.daysOfWeek.forEach { dayOfWeek ->
            scheduleTaskForDay(task, dayOfWeek)
        }
    }
    
    /**
     * Agendar tarefa para um dia específico da semana
     */
    private fun scheduleTaskForDay(task: Task, dayOfWeek: Int) {
        try {
            val timeParts = task.scheduledTime.split(":")
            if (timeParts.size != 2) {
                Log.e(TAG, "Formato de horário inválido: ${task.scheduledTime}")
                return
            }
            
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            // Calcular horário de execução
            val executionTime = getNextAlarmTime(dayOfWeek, hour, minute)
            
            // Calcular horário do lembrete (X minutos antes)
            val reminderTime = Calendar.getInstance().apply {
                timeInMillis = executionTime
                add(Calendar.MINUTE, -task.reminderMinutes)
            }.timeInMillis
            
            // Agendar lembrete
            if (task.reminderMinutes > 0) {
                scheduleAlarm(
                    task.id * 1000 + dayOfWeek * 10 + 1, // ID único para lembrete
                    reminderTime,
                    createReminderIntent(task),
                    "Lembrete para ${task.name}"
                )
            }
            
            // Agendar execução
            scheduleAlarm(
                task.id * 1000 + dayOfWeek * 10 + 2, // ID único para execução
                executionTime,
                createExecutionIntent(task),
                "Execução de ${task.name}"
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao agendar tarefa ${task.name} para dia $dayOfWeek: ${e.message}")
        }
    }
    
    /**
     * Calcular próximo horário de alarme para o dia da semana
     */
    private fun getNextAlarmTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        
        // Configurar para o dia da semana desejado
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Se já passou desta semana, agendar para próxima semana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis
    }
    
    /**
     * Agendar um alarme específico
     */
    private fun scheduleAlarm(requestCode: Long, triggerTime: Long, intent: PendingIntent, description: String) {
        try {
            // Para API 22, usar AlarmManager.RTC_WAKEUP simples
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, intent)
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = triggerTime
            
            Log.d(TAG, "Alarme agendado: $description para ${calendar.time}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao agendar alarme: ${e.message}")
        }
    }
    
    /**
     * Criar intent para lembrete
     */
    private fun createReminderIntent(task: Task): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TASK_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_NAME, task.name)
            putExtra(EXTRA_IS_REMINDER, true)
        }
        
        return PendingIntent.getBroadcast(
            context,
            (task.id * 1000).toInt(), // ID único para o PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    /**
     * Criar intent para execução
     */
    private fun createExecutionIntent(task: Task): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_TASK_EXECUTION
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_NAME, task.name)
            putExtra(EXTRA_IS_REMINDER, false)
        }
        
        return PendingIntent.getBroadcast(
            context,
            (task.id * 1000 + 1).toInt(), // ID único para o PendingIntent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    
    /**
     * Cancelar todos os alarmes de uma tarefa
     */
    fun cancelTaskAlarms(taskId: Long) {
        try {
            // Cancelar para todos os dias da semana (1-7)
            for (dayOfWeek in 1..7) {
                // Cancelar lembrete
                val reminderRequestCode = (taskId * 1000 + dayOfWeek * 10 + 1).toInt()
                val reminderIntent = Intent(context, TaskAlarmReceiver::class.java)
                val reminderPendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderRequestCode,
                    reminderIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(reminderPendingIntent)
                
                // Cancelar execução
                val executionRequestCode = (taskId * 1000 + dayOfWeek * 10 + 2).toInt()
                val executionIntent = Intent(context, TaskAlarmReceiver::class.java)
                val executionPendingIntent = PendingIntent.getBroadcast(
                    context,
                    executionRequestCode,
                    executionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(executionPendingIntent)
            }
            
            Log.d(TAG, "Alarmes cancelados para tarefa ID: $taskId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar alarmes da tarefa $taskId: ${e.message}")
        }
    }
    
    /**
     * Cancelar todos os alarmes do sistema
     */
    fun cancelAllAlarms() {
        Log.d(TAG, "Cancelando todos os alarmes...")
        
        val activeTasks = taskDao.getActiveTasks()
        activeTasks.forEach { task ->
            cancelTaskAlarms(task.id)
        }
    }
    
    /**
     * Reagendar tarefas (útil após reinicialização do dispositivo)
     */
    fun rescheduleAllTasks() {
        Log.d(TAG, "Reagendando todas as tarefas...")
        
        cancelAllAlarms()
        scheduleAllActiveTasks()
    }
    
    /**
     * Verificar próximas tarefas (para debugging)
     */
    fun getUpcomingTasks(hours: Int = 24): List<Pair<Task, Calendar>> {
        val now = Calendar.getInstance()
        val endTime = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, hours)
        }
        
        return taskDao.getActiveTasks()
            .mapNotNull { task ->
                task.getNextExecutionTime()?.let { nextTime ->
                    if (nextTime.after(now) && nextTime.before(endTime)) {
                        Pair(task, nextTime)
                    } else null
                }
            }
            .sortedBy { (_, time) -> time.timeInMillis }
    }
}
package com.seudominio.vuzixbladeapp.monitoring

import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.MediaCaptureManager
import com.seudominio.vuzixbladeapp.VuzixConnectivityManager
import com.seudominio.vuzixbladeapp.data.dao.TaskDao
import com.seudominio.vuzixbladeapp.data.dao.TaskExecutionDao
import com.seudominio.vuzixbladeapp.data.models.ExecutionStatus
import com.seudominio.vuzixbladeapp.data.models.Task
import com.seudominio.vuzixbladeapp.data.models.TaskExecution
import com.seudominio.vuzixbladeapp.notifications.TaskNotificationManager
import java.util.*

/**
 * Monitor de execução de tarefas
 * Responsável por iniciar gravação automática e monitorar progresso
 */
class TaskMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "TaskMonitor"
        private const val MAX_MONITORING_TIME_MINUTES = 60 // Tempo máximo de monitoramento
    }
    
    private val taskDao = TaskDao(context)
    private val executionDao = TaskExecutionDao(context)
    private val notificationManager = TaskNotificationManager(context)
    private val connectivityManager = VuzixConnectivityManager(context)
    
    // Estado atual do monitoramento
    private var currentTask: Task? = null
    private var currentExecution: TaskExecution? = null
    private var monitoringStartTime: Long = 0
    private var isMonitoring = false
    
    /**
     * Iniciar monitoramento automático de uma tarefa
     */
    fun startAutomaticMonitoring(task: Task) {
        Log.d(TAG, "Iniciando monitoramento automático para: ${task.name}")
        
        if (isMonitoring) {
            Log.w(TAG, "Já existe um monitoramento ativo, finalizando anterior")
            stopCurrentMonitoring()
        }
        
        currentTask = task
        monitoringStartTime = System.currentTimeMillis()
        isMonitoring = true
        
        // Criar registro de execução
        val scheduledTime = task.getNextExecutionTime()?.timeInMillis ?: System.currentTimeMillis()
        val execution = TaskExecution(
            taskId = task.id,
            scheduledTime = scheduledTime,
            startTime = System.currentTimeMillis(),
            endTime = null,
            status = ExecutionStatus.IN_PROGRESS,
            actualDuration = null,
            videoPath = null,
            notes = "Monitoramento automático iniciado"
        )
        
        val executionId = executionDao.insertExecution(execution)
        if (executionId != -1L) {
            currentExecution = execution.copy(id = executionId)
            Log.d(TAG, "Registro de execução criado com ID: $executionId")
        }
        
        // Iniciar captura de vídeo automática
        startAutomaticRecording(task)
        
        // Enviar notificação para S24 Ultra
        notifyS24AboutTaskStart(task)
        
        // Agendar verificação automática após o tempo esperado da tarefa
        scheduleAutomaticCheck(task.duration)
    }
    
    /**
     * Iniciar gravação automática
     */
    private fun startAutomaticRecording(task: Task) {
        try {
            Log.d(TAG, "Iniciando gravação automática para: ${task.name}")
            
            // Criar nome do arquivo baseado na tarefa e timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "task_${task.id}_${timestamp}.mp4"
            val outputPath = "${context.externalCacheDir?.absolutePath}/$fileName"
            
            // Iniciar gravação através do MediaCaptureManager
            val intent = Intent("com.seudominio.vuzixbladeapp.START_RECORDING")
            intent.putExtra("output_path", outputPath)
            intent.putExtra("task_id", task.id)
            intent.putExtra("task_name", task.name)
            intent.putExtra("auto_stop_minutes", task.duration + 5) // 5 min extra de segurança
            
            context.sendBroadcast(intent)
            
            // Atualizar execução com caminho do vídeo
            currentExecution?.let { execution ->
                val updatedExecution = execution.copy(videoPath = outputPath)
                executionDao.updateExecution(updatedExecution)
                currentExecution = updatedExecution
            }
            
            Log.d(TAG, "Gravação automática iniciada: $outputPath")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar gravação automática: ${e.message}")
        }
    }
    
    /**
     * Parar monitoramento atual
     */
    fun stopCurrentMonitoring() {
        Log.d(TAG, "Parando monitoramento atual")
        
        if (!isMonitoring) {
            Log.d(TAG, "Nenhum monitoramento ativo")
            return
        }
        
        val endTime = System.currentTimeMillis()
        val actualDuration = ((endTime - monitoringStartTime) / (60 * 1000)).toInt()
        
        // Parar gravação
        stopAutomaticRecording()
        
        // Atualizar registro de execução
        currentExecution?.let { execution ->
            val updatedExecution = execution.copy(
                endTime = endTime,
                actualDuration = actualDuration,
                status = ExecutionStatus.COMPLETED
            )
            executionDao.updateExecution(updatedExecution)
            
            // Enviar dados para análise no S24 Ultra
            sendExecutionDataForAnalysis(updatedExecution)
        }
        
        // Limpar estado
        isMonitoring = false
        currentTask = null
        currentExecution = null
        monitoringStartTime = 0
    }
    
    /**
     * Parar gravação automática
     */
    private fun stopAutomaticRecording() {
        try {
            val intent = Intent("com.seudominio.vuzixbladeapp.STOP_RECORDING")
            context.sendBroadcast(intent)
            Log.d(TAG, "Gravação automática parada")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar gravação automática: ${e.message}")
        }
    }
    
    /**
     * Notificar S24 Ultra sobre início da tarefa
     */
    private fun notifyS24AboutTaskStart(task: Task) {
        try {
            val message = mapOf(
                "message_type" to "task_started",
                "task_id" to task.id,
                "task_name" to task.name,
                "task_category" to task.category.name,
                "expected_duration" to task.duration,
                "start_time" to System.currentTimeMillis(),
                "description" to task.description
            )
            
            connectivityManager.sendMessage(message)
            Log.d(TAG, "S24 Ultra notificado sobre início da tarefa: ${task.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao notificar S24 Ultra: ${e.message}")
        }
    }
    
    /**
     * Enviar dados de execução para análise
     */
    private fun sendExecutionDataForAnalysis(execution: TaskExecution) {
        try {
            val message = mapOf(
                "message_type" to "execution_completed",
                "execution_id" to execution.id,
                "task_id" to execution.taskId,
                "start_time" to execution.startTime,
                "end_time" to execution.endTime,
                "duration" to execution.actualDuration,
                "video_path" to execution.videoPath,
                "status" to execution.status.name
            )
            
            connectivityManager.sendMessage(message)
            Log.d(TAG, "Dados de execução enviados para análise")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar dados para análise: ${e.message}")
        }
    }
    
    /**
     * Agendar verificação automática
     */
    private fun scheduleAutomaticCheck(durationMinutes: Int) {
        // Implementar timer para verificação automática após duração esperada
        val checkDelay = (durationMinutes + 2) * 60 * 1000L // +2 min de tolerância
        
        android.os.Handler().postDelayed({
            if (isMonitoring) {
                performAutomaticCheck()
            }
        }, checkDelay)
    }
    
    /**
     * Realizar verificação automática
     */
    private fun performAutomaticCheck() {
        Log.d(TAG, "Realizando verificação automática")
        
        val currentTask = this.currentTask ?: return
        val currentExecution = this.currentExecution ?: return
        
        val elapsedMinutes = ((System.currentTimeMillis() - monitoringStartTime) / (60 * 1000)).toInt()
        
        // Se passou muito tempo, parar automaticamente
        if (elapsedMinutes >= MAX_MONITORING_TIME_MINUTES) {
            Log.w(TAG, "Tempo máximo de monitoramento atingido, parando automaticamente")
            stopCurrentMonitoring()
            return
        }
        
        // Se passou o tempo esperado da tarefa, perguntar ao usuário
        if (elapsedMinutes >= currentTask.duration) {
            promptUserForCompletion(currentTask, elapsedMinutes)
        }
    }
    
    /**
     * Perguntar ao usuário sobre conclusão
     */
    private fun promptUserForCompletion(task: Task, elapsedMinutes: Int) {
        Log.d(TAG, "Perguntando ao usuário sobre conclusão da tarefa")
        
        // Criar notificação interativa
        val intent = Intent("com.seudominio.vuzixbladeapp.TASK_COMPLETION_PROMPT")
        intent.putExtra("task_id", task.id)
        intent.putExtra("elapsed_minutes", elapsedMinutes)
        
        context.sendBroadcast(intent)
    }
    
    /**
     * Marcar tarefa como concluída pelo usuário
     */
    fun markTaskCompleted(taskId: Long, userNotes: String = "") {
        if (!isMonitoring || currentTask?.id != taskId) {
            Log.w(TAG, "Tentativa de marcar como concluída tarefa não monitorada")
            return
        }
        
        Log.d(TAG, "Marcando tarefa como concluída: $taskId")
        
        currentExecution?.let { execution ->
            val updatedExecution = execution.copy(
                endTime = System.currentTimeMillis(),
                status = ExecutionStatus.COMPLETED,
                notes = userNotes
            )
            executionDao.updateExecution(updatedExecution)
        }
        
        stopCurrentMonitoring()
    }
    
    /**
     * Marcar tarefa como pulada
     */
    fun markTaskSkipped(taskId: Long, reason: String = "") {
        Log.d(TAG, "Marcando tarefa como pulada: $taskId")
        
        val task = taskDao.getTaskById(taskId) ?: return
        val scheduledTime = task.getNextExecutionTime()?.timeInMillis ?: System.currentTimeMillis()
        
        val execution = TaskExecution(
            taskId = taskId,
            scheduledTime = scheduledTime,
            startTime = null,
            endTime = null,
            status = ExecutionStatus.SKIPPED,
            notes = reason
        )
        
        executionDao.insertExecution(execution)
        
        if (isMonitoring && currentTask?.id == taskId) {
            stopCurrentMonitoring()
        }
    }
    
    /**
     * Obter status do monitoramento atual
     */
    fun getCurrentMonitoringStatus(): MonitoringStatus? {
        if (!isMonitoring || currentTask == null) return null
        
        val elapsedMinutes = ((System.currentTimeMillis() - monitoringStartTime) / (60 * 1000)).toInt()
        
        return MonitoringStatus(
            task = currentTask!!,
            execution = currentExecution!!,
            elapsedMinutes = elapsedMinutes,
            isActive = isMonitoring
        )
    }
    
    /**
     * Verificar se uma tarefa específica está sendo monitorada
     */
    fun isTaskBeingMonitored(taskId: Long): Boolean {
        return isMonitoring && currentTask?.id == taskId
    }
}

/**
 * Status atual do monitoramento
 */
data class MonitoringStatus(
    val task: Task,
    val execution: TaskExecution,
    val elapsedMinutes: Int,
    val isActive: Boolean
)
package com.seudominio.vuzixbladeapp.monitoring

import android.content.Context
import android.content.Intent
import android.util.Log
import com.seudominio.vuzixbladeapp.VuzixConnectivityManager
import com.seudominio.vuzixbladeapp.database.TaskExecution

/**
 * Monitor de execução de tarefas - Versão simplificada
 * Responsável por iniciar gravação automática e monitorar progresso
 */
class TaskMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "TaskMonitor"
        private const val MAX_MONITORING_TIME_MINUTES = 60 // Tempo máximo de monitoramento
    }
    
    private val connectivityManager = VuzixConnectivityManager(context)
    
    // Estado atual do monitoramento  
    private var currentTaskId: Long? = null
    private var currentTaskName: String? = null
    private var currentExecution: TaskExecution? = null
    private var monitoringStartTime: Long = 0
    private var isMonitoring = false
    
    /**
     * Iniciar monitoramento automático de uma tarefa
     */
    fun startAutomaticMonitoring(taskId: Long, taskName: String, duration: Int) {
        Log.d(TAG, "Iniciando monitoramento automático para: $taskName")
        
        if (isMonitoring) {
            Log.w(TAG, "Já existe um monitoramento ativo, finalizando anterior")
            stopCurrentMonitoring()
        }
        
        currentTaskId = taskId
        currentTaskName = taskName
        monitoringStartTime = System.currentTimeMillis()
        isMonitoring = true
        
        // Criar registro de execução
        val execution = TaskExecution(
            id = 0,
            taskName = taskName,
            startTime = System.currentTimeMillis(),
            endTime = null,
            status = "running",
            actualDuration = null,
            videoPath = null,
            result = "Monitoramento automático iniciado"
        )
        
        currentExecution = execution
        Log.d(TAG, "Registro de execução criado para: $taskName")
        
        // Iniciar captura de vídeo automática
        startAutomaticRecording(taskName)
        
        // Enviar notificação para S24 Ultra
        notifyS24AboutTaskStart(taskName)
        
        // Agendar verificação automática após o tempo esperado da tarefa
        scheduleAutomaticCheck(duration)
    }
    
    /**
     * Iniciar gravação automática
     */
    private fun startAutomaticRecording(taskName: String) {
        try {
            Log.d(TAG, "Iniciando gravação automática para: $taskName")
            
            // Criar nome do arquivo baseado na tarefa e timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "task_${taskName}_${timestamp}.mp4"
            val outputPath = "${context.externalCacheDir?.absolutePath}/$fileName"
            
            // Iniciar gravação através do MediaCaptureManager
            val intent = Intent("com.seudominio.vuzixbladeapp.START_RECORDING")
            intent.putExtra("output_path", outputPath)
            intent.putExtra("task_name", taskName)
            intent.putExtra("auto_stop_minutes", 65) // 65 min de segurança
            
            context.sendBroadcast(intent)
            
            // Atualizar execução com caminho do vídeo
            currentExecution?.let { execution ->
                currentExecution = execution.copy(videoPath = outputPath)
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
        val actualDuration = ((endTime - monitoringStartTime) / (60 * 1000)).toLong()
        
        // Parar gravação
        stopAutomaticRecording()
        
        // Atualizar registro de execução
        currentExecution?.let { execution ->
            val updatedExecution = execution.copy(
                endTime = endTime,
                actualDuration = actualDuration,
                status = "completed"
            )
            currentExecution = updatedExecution
            
            // Enviar dados para análise no S24 Ultra
            sendExecutionDataForAnalysis(updatedExecution)
        }
        
        // Limpar estado
        isMonitoring = false
        currentTaskId = null
        currentTaskName = null
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
    private fun notifyS24AboutTaskStart(taskName: String) {
        try {
            val messageData = "TASK_STARTED|$taskName|${System.currentTimeMillis()}"
            connectivityManager.sendMessage(messageData)
            Log.d(TAG, "S24 Ultra notificado sobre início da tarefa: $taskName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao notificar S24 Ultra: ${e.message}")
        }
    }
    
    /**
     * Enviar dados de execução para análise
     */
    private fun sendExecutionDataForAnalysis(execution: TaskExecution) {
        try {
            val messageData = "EXECUTION_COMPLETED|${execution.taskName}|${execution.startTime}|${execution.endTime}|${execution.actualDuration}|${execution.videoPath}"
            connectivityManager.sendMessage(messageData)
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
        
        val currentTaskName = this.currentTaskName ?: return
        
        val elapsedMinutes = ((System.currentTimeMillis() - monitoringStartTime) / (60 * 1000)).toInt()
        
        // Se passou muito tempo, parar automaticamente
        if (elapsedMinutes >= MAX_MONITORING_TIME_MINUTES) {
            Log.w(TAG, "Tempo máximo de monitoramento atingido, parando automaticamente")
            stopCurrentMonitoring()
            return
        }
        
        // Perguntar ao usuário sobre conclusão
        promptUserForCompletion(currentTaskName, elapsedMinutes)
    }
    
    /**
     * Perguntar ao usuário sobre conclusão
     */
    private fun promptUserForCompletion(taskName: String, elapsedMinutes: Int) {
        Log.d(TAG, "Perguntando ao usuário sobre conclusão da tarefa")
        
        // Criar notificação interativa
        val intent = Intent("com.seudominio.vuzixbladeapp.TASK_COMPLETION_PROMPT")
        intent.putExtra("task_name", taskName)
        intent.putExtra("elapsed_minutes", elapsedMinutes)
        
        context.sendBroadcast(intent)
    }
    
    /**
     * Verificar se uma tarefa específica está sendo monitorada
     */
    fun isTaskBeingMonitored(taskId: Long): Boolean {
        return isMonitoring && currentTaskId == taskId
    }
}
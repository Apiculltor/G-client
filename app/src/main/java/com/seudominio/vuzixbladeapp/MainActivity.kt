package com.seudominio.vuzixbladeapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vuzix.hud.actionmenu.ActionMenuActivity
import com.seudominio.vuzixbladeapp.data.dao.TaskDao
import com.seudominio.vuzixbladeapp.data.dao.TaskExecutionDao
import com.seudominio.vuzixbladeapp.data.models.Task
import com.seudominio.vuzixbladeapp.monitoring.TaskMonitor
import com.seudominio.vuzixbladeapp.notifications.TaskNotificationManager
import com.seudominio.vuzixbladeapp.scheduler.TaskScheduler
import java.text.SimpleDateFormat
import java.util.*

/**
 * MainActivity para Vuzix Blade 1.5 - ASSISTENTE DE ROTINA
 * Integra captura A/V com sistema completo de formação de hábitos
 * Funcionalidades:
 * 1. Estabelecer atividades e horários
 * 2. Notificações e registro de vídeo automático
 * 3. Verificação de execução e feedback
 * 4. Registro de dados para relatórios
 * 5. Lista de tarefas sempre atualizada
 */
class MainActivity : ActionMenuActivity(), SurfaceHolder.Callback {
    
    companion object {
        private const val TAG = "VuzixBladeApp"
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val UPDATE_INTERVAL = 30000L // Atualizar a cada 30 segundos
    }
    
    // === SISTEMA DE CAPTURA A/V ===
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var surfaceView: SurfaceView? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var isRecording = false
    private var currentRecordingPath: String? = null
    
    // === CONECTIVIDADE VUZIX ===
    private lateinit var connectivityReceiver: VuzixConnectivityReceiver
    private var connectivityManager: VuzixConnectivityManager? = null
    
    // === SISTEMA DE HÁBITOS ===
    private lateinit var taskDao: TaskDao
    private lateinit var executionDao: TaskExecutionDao
    private lateinit var taskScheduler: TaskScheduler
    private lateinit var taskMonitor: TaskMonitor
    private lateinit var notificationManager: TaskNotificationManager
    
    // === ESTADO ATUAL ===
    private var currentTasks: List<Task> = emptyList()
    private var nextTask: Task? = null
    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTasksDisplay()
            handler.postDelayed(this, UPDATE_INTERVAL)
        }
    }
    
    // === BROADCAST RECEIVERS ===
    private val habitSystemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.seudominio.vuzixbladeapp.START_RECORDING" -> {
                    val taskId = intent.getLongExtra("task_id", -1)
                    val outputPath = intent.getStringExtra("output_path")
                    startAutomaticRecording(taskId, outputPath)
                }
                "com.seudominio.vuzixbladeapp.STOP_RECORDING" -> {
                    stopAutomaticRecording()
                }
                "com.seudominio.vuzixbladeapp.TASK_COMPLETION_PROMPT" -> {
                    val taskId = intent.getLongExtra("task_id", -1)
                    val elapsedMinutes = intent.getIntExtra("elapsed_minutes", 0)
                    showTaskCompletionPrompt(taskId, elapsedMinutes)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 Vuzix Blade Assistente de Rotina iniciado")
        
        // Inicializar sistemas
        initializeHabitSystem()
        setupCameraPreview()
        setupConnectivity()
        setupUIControls()
        setupBroadcastReceivers()
        
        // Verificar permissões
        checkPermissions()
        
        // Configurar HUD
        setupActionMenu()
        
        // Processar intent se veio de notificação
        handleIncomingIntent(intent)
        
        // Inicializar interface
        updateUIStatus()
        
        // Iniciar atualizações automáticas
        handler.post(updateRunnable)
    }
    
    /**
     * INICIALIZAÇÃO DO SISTEMA DE HÁBITOS
     */
    private fun initializeHabitSystem() {
        Log.d(TAG, "Inicializando sistema de hábitos...")
        
        try {
            // Inicializar DAOs
            taskDao = TaskDao(this)
            executionDao = TaskExecutionDao(this)
            
            // Inicializar gerenciadores
            taskScheduler = TaskScheduler(this)
            taskMonitor = TaskMonitor(this)
            notificationManager = TaskNotificationManager(this)
            
            // Agendar todas as tarefas ativas
            taskScheduler.scheduleAllActiveTasks()
            
            // Carregar tarefas de hoje
            loadTodayTasks()
            
            Log.d(TAG, "✅ Sistema de hábitos inicializado com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar sistema de hábitos: ${e.message}")
            Toast.makeText(this, "Erro ao inicializar sistema de hábitos", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * CARREGAR TAREFAS DO DIA
     */
    private fun loadTodayTasks() {
        currentTasks = taskDao.getTodayTasks()
        nextTask = taskDao.getNextTask()
        
        Log.d(TAG, "📋 Carregadas ${currentTasks.size} tarefas para hoje")
        Log.d(TAG, "⏰ Próxima tarefa: ${nextTask?.name ?: "Nenhuma"}")
    }
    
    /**
     * CONFIGURAÇÃO DE BROADCAST RECEIVERS
     */
    private fun setupBroadcastReceivers() {
        val filter = IntentFilter().apply {
            addAction("com.seudominio.vuzixbladeapp.START_RECORDING")
            addAction("com.seudominio.vuzixbladeapp.STOP_RECORDING")
            addAction("com.seudominio.vuzixbladeapp.TASK_COMPLETION_PROMPT")
        }
        registerReceiver(habitSystemReceiver, filter)
    }
    
    /**
     * GRAVAÇÃO AUTOMÁTICA PARA TAREFA
     */
    private fun startAutomaticRecording(taskId: Long, outputPath: String?) {
        if (isRecording) {
            Log.w(TAG, "Já existe uma gravação ativa")
            return
        }
        
        val task = taskDao.getTaskById(taskId)
        if (task == null) {
            Log.e(TAG, "Tarefa não encontrada para gravação: $taskId")
            return
        }
        
        Log.d(TAG, "🎬 Iniciando gravação automática para: ${task.name}")
        
        val finalOutputPath = outputPath ?: "${externalCacheDir?.absolutePath}/task_${taskId}_${System.currentTimeMillis()}.mp4"
        currentRecordingPath = finalOutputPath
        
        try {
            // Configurar MediaRecorder para tarefa específica
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(finalOutputPath)
                
                // Configurações otimizadas para Vuzix Blade
                setVideoSize(480, 480)
                setVideoFrameRate(24)
                setVideoEncodingBitRate(1000000) // 1Mbps para economizar espaço
                setAudioEncodingBitRate(64000)   // 64kbps
                
                camera?.unlock()
                setCamera(camera)
                setPreviewDisplay(surfaceHolder?.surface)
                
                prepare()
                start()
            }
            
            isRecording = true
            updateRecordingStatus(true)
            updateFeedbackDisplay("🎬 Gravando", "Tarefa: ${task.name}", 1.0f)
            
            // Notificar S24 Ultra sobre início da gravação
            sendTaskRecordingStartedToS24(task, finalOutputPath)
            
            Log.d(TAG, "✅ Gravação automática iniciada: $finalOutputPath")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na gravação automática: ${e.message}")
            updateFeedbackDisplay("❌ Erro gravação", e.message ?: "Erro desconhecido", 0.0f)
        }
    }
    
    /**
     * PARAR GRAVAÇÃO AUTOMÁTICA
     */
    private fun stopAutomaticRecording() {
        if (!isRecording) return
        
        Log.d(TAG, "⏹ Parando gravação automática")
        
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            
            camera?.lock()
            isRecording = false
            updateRecordingStatus(false)
            
            // Enviar dados para S24 Ultra processar
            currentRecordingPath?.let { path ->
                sendRecordingDataToS24(path)
            }
            
            currentRecordingPath = null
            updateFeedbackDisplay("✅ Gravação finalizada", "Enviando para análise...", 1.0f)
            
            Log.d(TAG, "✅ Gravação automática finalizada")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao parar gravação: ${e.message}")
        }
    }
    
    /**
     * PROMPT DE CONCLUSÃO DE TAREFA
     */
    private fun showTaskCompletionPrompt(taskId: Long, elapsedMinutes: Int) {
        val task = taskDao.getTaskById(taskId) ?: return
        
        Log.d(TAG, "🤔 Perguntando sobre conclusão: ${task.name} (${elapsedMinutes}min)")
        
        updateFeedbackDisplay(
            "✅ ${task.name} concluída?",
            "Tempo decorrido: ${elapsedMinutes}min",
            1.0f
        )
        
        // Mostrar botões de ação no HUD (será implementado no layout)
        val btnCompleted = findViewById<android.widget.Button>(R.id.btn_task_completed)
        val btnContinue = findViewById<android.widget.Button>(R.id.btn_task_continue)
        val btnSkip = findViewById<android.widget.Button>(R.id.btn_task_skip)
        
        btnCompleted?.apply {
            visibility = android.view.View.VISIBLE
            setOnClickListener {
                taskMonitor.markTaskCompleted(taskId, "Confirmado pelo usuário")
                visibility = android.view.View.GONE
                btnContinue?.visibility = android.view.View.GONE
                btnSkip?.visibility = android.view.View.GONE
                updateTasksDisplay()
            }
        }
        
        btnContinue?.apply {
            visibility = android.view.View.VISIBLE
            setOnClickListener {
                Log.d(TAG, "Usuário escolheu continuar tarefa")
                visibility = android.view.View.GONE
                btnCompleted?.visibility = android.view.View.GONE
                btnSkip?.visibility = android.view.View.GONE
            }
        }
        
        btnSkip?.apply {
            visibility = android.view.View.VISIBLE
            setOnClickListener {
                taskMonitor.markTaskSkipped(taskId, "Pulada pelo usuário")
                visibility = android.view.View.GONE
                btnCompleted?.visibility = android.view.View.GONE
                btnContinue?.visibility = android.view.View.GONE
                updateTasksDisplay()
            }
        }
    }
    
    /**
     * ATUALIZAR DISPLAY DE TAREFAS
     */
    private fun updateTasksDisplay() {
        loadTodayTasks()
        
        // Atualizar próxima tarefa
        val nextTaskView = findViewById<android.widget.TextView>(R.id.next_task)
        nextTaskView?.text = if (nextTask != null) {
            "${nextTask!!.category.icon} ${nextTask!!.name}\n⏰ ${nextTask!!.scheduledTime}"
        } else {
            "✅ Todas as tarefas concluídas!"
        }
        
        // Atualizar lista de tarefas de hoje
        val todayTasksView = findViewById<android.widget.TextView>(R.id.today_tasks)
        val completedToday = executionDao.getTodayExecutions().filter { it.isSuccessful() }.size
        todayTasksView?.text = "📊 Hoje: $completedToday/${currentTasks.size} concluídas"
        
        // Verificar se há tarefas para executar agora
        val tasksToExecute = taskDao.getTasksToExecuteNow()
        if (tasksToExecute.isNotEmpty() && !isRecording) {
            val task = tasksToExecute.first()
            Log.d(TAG, "⚠️ Tarefa deve ser executada agora: ${task.name}")
            updateFeedbackDisplay(
                "⏰ Hora de: ${task.name}",
                "Duração: ${task.duration}min",
                1.0f
            )
        }
        
        // Atualizar status de monitoramento
        val monitoringStatus = taskMonitor.getCurrentMonitoringStatus()
        if (monitoringStatus != null) {
            updateFeedbackDisplay(
                "🔄 ${monitoringStatus.task.name}",
                "Executando há ${monitoringStatus.elapsedMinutes}min",
                1.0f
            )
        }
    }
    
    /**
     * ENVIAR DADOS PARA S24 ULTRA
     */
    private fun sendTaskRecordingStartedToS24(task: Task, videoPath: String) {
        try {
            val message = mapOf(
                "message_type" to "task_recording_started",
                "task_id" to task.id,
                "task_name" to task.name,
                "task_category" to task.category.name,
                "video_path" to videoPath,
                "expected_duration" to task.duration,
                "timestamp" to System.currentTimeMillis()
            )
            
            connectivityManager?.sendMessage(message)
            Log.d(TAG, "📡 S24 Ultra notificado sobre gravação de: ${task.name}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao notificar S24 Ultra: ${e.message}")
        }
    }
    
    private fun sendRecordingDataToS24(videoPath: String) {
        try {
            val message = mapOf(
                "message_type" to "video_analysis_request",
                "video_path" to videoPath,
                "timestamp" to System.currentTimeMillis(),
                "device" to "vuzix_blade_1.5"
            )
            
            connectivityManager?.sendMessage(message)
            Log.d(TAG, "📡 Dados de vídeo enviados para análise")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar dados: ${e.message}")
        }
    }
    
    /**
     * PROCESSAR INTENT DE NOTIFICAÇÃO
     */
    private fun handleIncomingIntent(intent: Intent?) {
        intent?.let { 
            val taskId = it.getLongExtra("task_id", -1)
            val action = it.getStringExtra("action")
            
            if (taskId != -1L && action != null) {
                Log.d(TAG, "📱 Intent recebido: $action para tarefa $taskId")
                
                when (action) {
                    "reminder" -> {
                        val task = taskDao.getTaskById(taskId)
                        task?.let { t ->
                            updateFeedbackDisplay(
                                "🔔 Lembrete: ${t.name}",
                                "Em ${t.reminderMinutes} minutos",
                                1.0f
                            )
                        }
                    }
                    "execute" -> {
                        val task = taskDao.getTaskById(taskId)
                        task?.let { t ->
                            taskMonitor.startAutomaticMonitoring(t)
                        }
                    }
                }
            }
        }
    }
    
    // === RESTO DO CÓDIGO ORIGINAL (setupCameraPreview, etc.) ===
    
    private fun setupCameraPreview() {
        surfaceView = findViewById(R.id.surface_view)
        surfaceHolder = surfaceView?.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
    
    private fun setupConnectivity() {
        connectivityReceiver = VuzixConnectivityReceiver()
        connectivityManager = VuzixConnectivityManager(this)
        
        Log.d(TAG, "Status conectividade: ${connectivityManager?.getConnectionInfo()}")
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            initializeCamera()
        }
    }
    
    private fun setupActionMenu() {
        // Menu será implementado com ações de hábitos
    }
    
    private fun initializeCamera() {
        try {
            camera = Camera.open()
            camera?.setPreviewDisplay(surfaceHolder)
            
            val parameters = camera?.parameters
            parameters?.setPreviewSize(480, 480)
            camera?.parameters = parameters
            
            camera?.startPreview()
            Log.d(TAG, "✅ Câmera inicializada")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar câmera: ${e.message}")
            Toast.makeText(this, "Erro ao acessar câmera", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupUIControls() {
        // Controles básicos + controles de hábitos
        val btnManualRecord = findViewById<android.widget.Button>(R.id.btn_manual_record)
        
        btnManualRecord?.setOnClickListener {
            if (!isRecording) {
                startManualRecording()
            } else {
                stopAutomaticRecording()
            }
        }
        
        updateConnectionStatus(false)
        updateRecordingStatus(false)
    }
    
    private fun startManualRecording() {
        startAutomaticRecording(-1, null) // -1 indica gravação manual
    }
    
    private fun updateUIStatus() {
        val connectionStatus = connectivityManager?.isConnected() ?: false
        updateConnectionStatus(connectionStatus)
        updateRecordingStatus(isRecording)
        updateTasksDisplay()
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        
        updateFeedbackDisplay(
            "🚀 Assistente de Rotina",
            "Pronto às $currentTime",
            1.0f
        )
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        val connectionStatusView = findViewById<android.widget.TextView>(R.id.connection_status)
        connectionStatusView?.apply {
            text = if (connected) "📡 S24 Conectado" else "📡 Desconectado"
            setTextColor(if (connected) 0xFF00FF00.toInt() else 0xFFFF6600.toInt())
        }
    }
    
    private fun updateRecordingStatus(recording: Boolean) {
        val recordingStatusView = findViewById<android.widget.TextView>(R.id.recording_status)
        recordingStatusView?.apply {
            text = if (recording) "🔴 Gravando" else "⚪ Pronto"
            setTextColor(if (recording) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
        }
    }
    
    private fun updateFeedbackDisplay(result: String, details: String = "", confidence: Float = 0.0f) {
        val feedbackResult = findViewById<android.widget.TextView>(R.id.feedback_result)
        val feedbackDetails = findViewById<android.widget.TextView>(R.id.feedback_details)
        val feedbackConfidence = findViewById<android.widget.TextView>(R.id.feedback_confidence)
        
        feedbackResult?.text = result
        feedbackDetails?.text = details
        
        if (confidence > 0.0f) {
            feedbackConfidence?.text = "Confiança: ${(confidence * 100).toInt()}%"
            feedbackConfidence?.visibility = android.view.View.VISIBLE
        } else {
            feedbackConfidence?.visibility = android.view.View.GONE
        }
        
        Log.d(TAG, "🔄 Feedback: $result | $details")
    }
    
    // Implementação SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface criada")
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface alterada: ${width}x${height}")
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destruída")
        releaseCamera()
    }
    
    private fun releaseCamera() {
        camera?.apply {
            stopPreview()
            release()
        }
        camera = null
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    initializeCamera()
                } else {
                    Toast.makeText(this, "Permissões necessárias para funcionamento", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Limpar recursos
        handler.removeCallbacks(updateRunnable)
        try {
            unregisterReceiver(habitSystemReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao desregistrar receiver: ${e.message}")
        }
        
        releaseCamera()
        if (isRecording) {
            stopAutomaticRecording()
        }
        
        Log.d(TAG, "🛑 MainActivity destruída")
    }
    
    override fun onResume() {
        super.onResume()
        updateTasksDisplay()
    }
}
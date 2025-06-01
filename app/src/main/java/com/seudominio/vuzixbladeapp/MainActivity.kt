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
import android.widget.TextView
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.vuzix.hud.actionmenu.ActionMenuActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * MainActivity para Vuzix Blade 1.5 - ASSISTENTE DE ROTINA
 * Versão adaptada para testes em emulador
 * Integra captura A/V com sistema completo de formação de hábitos
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
    
    // === ESTADO ATUAL ===
    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTasksDisplay()
            handler.postDelayed(this, UPDATE_INTERVAL)
        }
    }
    
    // === DADOS SIMULADOS PARA TESTE ===
    private val simulatedTasks = listOf(
        "🧘 Meditação - 06:30",
        "💪 Exercício - 07:00", 
        "💧 Beber Água - 08:00",
        "📚 Leitura - 21:00",
        "🚶 Caminhada - 19:30"
    )
    
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
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "🚀 Vuzix Blade Assistente de Rotina iniciado (Modo Teste)")
        
        // Inicializar sistemas
        initializeTestSystem()
        setupCameraPreview()
        setupUIControls()
        setupBroadcastReceivers()
        
        // Verificar permissões
        checkPermissions()
        
        // Processar intent se veio de notificação
        handleIncomingIntent(intent)
        
        // Inicializar interface
        updateUIStatus()
        
        // Iniciar atualizações automáticas
        handler.post(updateRunnable)
    }
    
    /**
     * INICIALIZAÇÃO DO SISTEMA DE TESTE
     */
    private fun initializeTestSystem() {
        Log.d(TAG, "Inicializando sistema de teste...")
        
        try {
            Log.d(TAG, "✅ Sistema de teste inicializado com ${simulatedTasks.size} tarefas simuladas")
            Toast.makeText(this, "Sistema de Teste Ativo", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar sistema de teste: ${e.message}")
            Toast.makeText(this, "Erro ao inicializar sistema", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * CONFIGURAÇÃO DE BROADCAST RECEIVERS
     */
    private fun setupBroadcastReceivers() {
        val filter = IntentFilter().apply {
            addAction("com.seudominio.vuzixbladeapp.START_RECORDING")
            addAction("com.seudominio.vuzixbladeapp.STOP_RECORDING")
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
        
        Log.d(TAG, "🎬 Iniciando gravação automática para tarefa: $taskId")
        
        val finalOutputPath = outputPath ?: "${externalCacheDir?.absolutePath}/task_${taskId}_${System.currentTimeMillis()}.mp4"
        currentRecordingPath = finalOutputPath
        
        try {
            // Verificar se a câmera está disponível
            if (camera == null) {
                Log.w(TAG, "Câmera não disponível, simulando gravação")
                simulateRecording()
                return
            }
            
            // Configurar MediaRecorder para tarefa específica
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setOutputFile(finalOutputPath)
                
                // Configurações otimizadas
                setVideoSize(480, 480)
                setVideoFrameRate(24)
                setVideoEncodingBitRate(1000000) // 1Mbps
                setAudioEncodingBitRate(64000)   // 64kbps
                
                camera?.unlock()
                setCamera(camera)
                setPreviewDisplay(surfaceHolder?.surface)
                
                prepare()
                start()
            }
            
            isRecording = true
            updateRecordingStatus(true)
            updateFeedbackDisplay("🎬 Gravando", "Arquivo: $finalOutputPath", 1.0f)
            
            Log.d(TAG, "✅ Gravação automática iniciada: $finalOutputPath")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro na gravação automática: ${e.message}")
            simulateRecording()
        }
    }
    
    /**
     * SIMULAÇÃO DE GRAVAÇÃO PARA TESTES
     */
    private fun simulateRecording() {
        isRecording = true
        updateRecordingStatus(true)
        updateFeedbackDisplay("🎬 Simulando Gravação", "Modo de teste ativo", 1.0f)
        
        // Simular gravação por 5 segundos
        handler.postDelayed({
            stopAutomaticRecording()
        }, 5000)
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
            
            currentRecordingPath = null
            updateFeedbackDisplay("✅ Gravação finalizada", "Dados processados", 1.0f)
            
            Log.d(TAG, "✅ Gravação automática finalizada")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao parar gravação: ${e.message}")
            isRecording = false
            updateRecordingStatus(false)
            updateFeedbackDisplay("✅ Teste finalizado", "Simulação concluída", 1.0f)
        }
    }
    
    /**
     * ATUALIZAR DISPLAY DE TAREFAS
     */
    private fun updateTasksDisplay() {
        // Simular próxima tarefa
        val nextTaskView = findViewById<TextView>(R.id.next_task)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nextTask = when {
            currentHour < 7 -> "🧘 Meditação - 06:30"
            currentHour < 8 -> "💪 Exercício - 07:00"
            currentHour < 19 -> "💧 Beber Água - a cada hora"
            currentHour < 21 -> "🚶 Caminhada - 19:30"
            else -> "📚 Leitura - 21:00"
        }
        
        nextTaskView?.text = "🎯 PRÓXIMA TAREFA\n$nextTask"
        
        // Simular tarefas de hoje
        val todayTasksView = findViewById<TextView>(R.id.today_tasks)
        val completedToday = (currentHour / 4) // Simular progresso
        todayTasksView?.text = "📊 Hoje: $completedToday/${simulatedTasks.size} concluídas"
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
                updateFeedbackDisplay("📱 Ação: $action", "Tarefa ID: $taskId", 1.0f)
            }
        }
    }
    
    private fun setupCameraPreview() {
        surfaceView = findViewById(R.id.surface_view)
        surfaceHolder = surfaceView?.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
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
            Log.e(TAG, "⚠️ Câmera não disponível (emulador): ${e.message}")
            // Continuar sem câmera em emulador
        }
    }
    
    private fun setupUIControls() {
        // Controles básicos
        val btnManualRecord = findViewById<Button>(R.id.btn_manual_record)
        
        btnManualRecord?.setOnClickListener {
            if (!isRecording) {
                startManualRecording()
            } else {
                stopAutomaticRecording()
            }
        }
        
        // Botões de teste
        val btnTestTask = findViewById<Button>(R.id.btn_test_task)
        btnTestTask?.setOnClickListener {
            testTaskExecution()
        }
        
        val btnTestNotification = findViewById<Button>(R.id.btn_test_notification)
        btnTestNotification?.setOnClickListener {
            testNotificationSystem()
        }
        
        updateConnectionStatus(true) // Simular conexão para teste
        updateRecordingStatus(false)
    }
    
    private fun startManualRecording() {
        startAutomaticRecording(-1, null) // -1 indica gravação manual
    }
    
    private fun testTaskExecution() {
        updateFeedbackDisplay("🧪 Teste de Tarefa", "Simulando execução...", 1.0f)
        
        handler.postDelayed({
            startAutomaticRecording(1, null)
        }, 1000)
    }
    
    private fun testNotificationSystem() {
        updateFeedbackDisplay("🔔 Teste Notificação", "Sistema funcionando", 1.0f)
        Toast.makeText(this, "Notificação de teste enviada", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUIStatus() {
        updateConnectionStatus(true) // Simular conexão
        updateRecordingStatus(isRecording)
        updateTasksDisplay()
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        
        updateFeedbackDisplay(
            "🚀 Assistente de Rotina",
            "Modo Teste - $currentTime",
            1.0f
        )
    }
    
    private fun updateConnectionStatus(connected: Boolean) {
        val connectionStatusView = findViewById<TextView>(R.id.connection_status)
        connectionStatusView?.apply {
            text = if (connected) "📡 S24 Conectado (Simulado)" else "📡 Desconectado"
            setTextColor(if (connected) 0xFF00FF00.toInt() else 0xFFFF6600.toInt())
        }
    }
    
    private fun updateRecordingStatus(recording: Boolean) {
        val recordingStatusView = findViewById<TextView>(R.id.recording_status)
        recordingStatusView?.apply {
            text = if (recording) "🔴 Gravando" else "⚪ Pronto"
            setTextColor(if (recording) 0xFFFF0000.toInt() else 0xFF00FF00.toInt())
        }
        
        val btnManualRecord = findViewById<Button>(R.id.btn_manual_record)
        btnManualRecord?.text = if (recording) "⏹ Parar" else "🎬 Gravar"
    }
    
    private fun updateFeedbackDisplay(result: String, details: String = "", confidence: Float = 0.0f) {
        val feedbackResult = findViewById<TextView>(R.id.feedback_result)
        val feedbackDetails = findViewById<TextView>(R.id.feedback_details)
        val feedbackConfidence = findViewById<TextView>(R.id.feedback_confidence)
        
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
                    Toast.makeText(this, "Permissões concedidas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Continuando sem câmera (modo teste)", Toast.LENGTH_SHORT).show()
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

package com.seudominio.vuzixbladeapp

import android.content.Context
import android.hardware.Camera
import android.media.MediaRecorder
import android.util.Log
import android.view.SurfaceHolder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gerenciador de captura de mídia para Vuzix Blade 1.5
 * Implementa as melhores práticas recomendadas pela Vuzix para API 22
 */
class MediaCaptureManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MediaCaptureManager"
        
        // Configurações específicas para Vuzix Blade
        private const val PREVIEW_WIDTH = 480
        private const val PREVIEW_HEIGHT = 480
        private const val VIDEO_FRAME_RATE = 24
        private const val AUDIO_SAMPLE_RATE = 44100
    }
    
    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentOutputFile: String? = null
    
    // Callback para notificar eventos de captura
    interface CaptureCallback {
        fun onRecordingStarted(outputFile: String)
        fun onRecordingStopped(outputFile: String, duration: Long)
        fun onRecordingError(error: String)
        fun onCameraInitialized()
        fun onCameraError(error: String)
    }
    
    private var captureCallback: CaptureCallback? = null
    
    fun setCaptureCallback(callback: CaptureCallback) {
        this.captureCallback = callback
    }
    
    /**
     * Inicializa a câmera seguindo as práticas recomendadas pela Vuzix
     */
    fun initializeCamera(surfaceHolder: SurfaceHolder): Boolean {
        return try {
            // Liberar câmera anterior se existir
            releaseCamera()
            
            // Abrir câmera
            camera = Camera.open()
            
            // Configurar preview display
            camera?.setPreviewDisplay(surfaceHolder)
            
            // Configurar parâmetros específicos do Vuzix Blade
            val parameters = camera?.parameters
            parameters?.let { params ->
                // Definir resolução de preview igual à resolução do display
                params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                
                // Configurar formato de preview
                params.previewFormat = android.graphics.ImageFormat.NV21
                
                // Vuzix Blade tem foco fixo - não configurar autofocus
                // As configurações 3A (AE, AWB) usam os padrões otimizados
                
                // Aplicar configurações
                camera?.parameters = params
            }
            
            // Iniciar preview
            camera?.startPreview()
            
            Log.d(TAG, "Câmera inicializada com sucesso - ${PREVIEW_WIDTH}x${PREVIEW_HEIGHT}")
            captureCallback?.onCameraInitialized()
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar câmera: ${e.message}")
            captureCallback?.onCameraError("Erro ao inicializar câmera: ${e.message}")
            false
        }
    }
    
    /**
     * Inicia gravação de vídeo com áudio
     */
    fun startRecording(): Boolean {
        if (isRecording) {
            Log.w(TAG, "Gravação já está em andamento")
            return false
        }
        
        if (camera == null) {
            Log.e(TAG, "Câmera não inicializada")
            captureCallback?.onRecordingError("Câmera não inicializada")
            return false
        }
        
        return try {
            // Criar arquivo de saída
            currentOutputFile = createOutputFile()
            
            // Configurar MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                // Configurar fontes - seguindo ordem recomendada
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                
                // Configurar formato de saída
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // Configurar encoders
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                
                // Configurar arquivo de saída
                setOutputFile(currentOutputFile)
                
                // Configurar áudio
                setAudioSamplingRate(AUDIO_SAMPLE_RATE)
                setAudioEncodingBitRate(128000) // 128 kbps
                
                // Configurar vídeo - resolução igual ao preview
                setVideoSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                setVideoFrameRate(VIDEO_FRAME_RATE)
                setVideoEncodingBitRate(2000000) // 2 Mbps
                
                // Configurar orientação
                setOrientationHint(0) // Vuzix Blade orientação padrão
                
                // Destravar câmera antes de usar no MediaRecorder
                camera?.unlock()
                setCamera(camera)
                
                // Preparar e iniciar
                prepare()
                start()
            }
            
            isRecording = true
            Log.d(TAG, "Gravação iniciada: $currentOutputFile")
            captureCallback?.onRecordingStarted(currentOutputFile!!)
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar gravação: ${e.message}")
            captureCallback?.onRecordingError("Erro ao iniciar gravação: ${e.message}")
            
            // Limpar estado em caso de erro
            mediaRecorder?.release()
            mediaRecorder = null
            camera?.lock()
            false
        }
    }
    
    /**
     * Para a gravação
     */
    fun stopRecording(): String? {
        if (!isRecording) {
            Log.w(TAG, "Nenhuma gravação em andamento")
            return null
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            // Parar MediaRecorder
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            
            // Retravar câmera
            camera?.lock()
            
            // Pequeno delay recomendado pela Vuzix após parar gravação
            Thread.sleep(500)
            
            val duration = System.currentTimeMillis() - startTime
            isRecording = false
            
            Log.d(TAG, "Gravação finalizada: $currentOutputFile")
            captureCallback?.onRecordingStopped(currentOutputFile!!, duration)
            
            currentOutputFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar gravação: ${e.message}")
            captureCallback?.onRecordingError("Erro ao parar gravação: ${e.message}")
            null
        }
    }
    
    /**
     * Captura uma foto
     */
    fun capturePhoto(callback: Camera.PictureCallback): Boolean {
        if (camera == null) {
            Log.e(TAG, "Câmera não inicializada")
            return false
        }
        
        if (isRecording) {
            Log.w(TAG, "Não é possível capturar foto durante gravação")
            return false
        }
        
        return try {
            camera?.takePicture(null, null, callback)
            Log.d(TAG, "Foto capturada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao capturar foto: ${e.message}")
            false
        }
    }
    
    /**
     * Para e reinicia preview da câmera
     */
    fun restartPreview() {
        try {
            camera?.apply {
                stopPreview()
                // Delay recomendado pela Vuzix
                Thread.sleep(500)
                startPreview()
            }
            Log.d(TAG, "Preview reiniciado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao reiniciar preview: ${e.message}")
        }
    }
    
    /**
     * Libera recursos da câmera
     */
    fun releaseCamera() {
        try {
            // Parar gravação se estiver ativa
            if (isRecording) {
                stopRecording()
            }
            
            // Liberar MediaRecorder
            mediaRecorder?.apply {
                release()
            }
            mediaRecorder = null
            
            // Liberar câmera
            camera?.apply {
                stopPreview()
                release()
            }
            camera = null
            
            Log.d(TAG, "Recursos da câmera liberados")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao liberar câmera: ${e.message}")
        }
    }
    
    /**
     * Cria arquivo de saída com timestamp
     */
    private fun createOutputFile(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "vuzix_recording_$timestamp.mp4"
        
        // Usar cache externo se disponível, senão usar cache interno
        val outputDir = context.externalCacheDir ?: context.cacheDir
        val outputFile = File(outputDir, fileName)
        
        return outputFile.absolutePath
    }
    
    /**
     * Verifica se está gravando
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Obtém arquivo de saída atual
     */
    fun getCurrentOutputFile(): String? = currentOutputFile
    
    /**
     * Obtém informações da câmera
     */
    fun getCameraInfo(): Map<String, Any> {
        return mapOf(
            "is_initialized" to (camera != null),
            "is_recording" to isRecording,
            "preview_size" to "${PREVIEW_WIDTH}x${PREVIEW_HEIGHT}",
            "frame_rate" to VIDEO_FRAME_RATE,
            "current_file" to (currentOutputFile ?: "none")
        )
    }
}

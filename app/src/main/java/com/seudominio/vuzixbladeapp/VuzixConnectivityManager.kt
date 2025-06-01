package com.seudominio.vuzixbladeapp

import android.content.Context
import android.content.Intent
import android.util.Log
import com.vuzix.connectivity.sdk.Connectivity

/**
 * Gerenciador de comunicação com Samsung Galaxy S24 Ultra
 * Via Vuzix Connectivity SDK
 */
class VuzixConnectivityManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VuzixConnectivityMgr"
        
        // Package do app no Samsung Galaxy S24 Ultra
        private const val S24_ULTRA_PACKAGE = "com.seudominio.s24ultraapp"
        
        // Ações de comunicação
        const val ACTION_SEND_MEDIA_DATA = "com.seudominio.s24ultraapp.ACTION_PROCESS_MEDIA"
        const val ACTION_SEND_AUDIO_DATA = "com.seudominio.s24ultraapp.ACTION_PROCESS_AUDIO"
        const val ACTION_SEND_VIDEO_DATA = "com.seudominio.s24ultraapp.ACTION_PROCESS_VIDEO"
        const val ACTION_REQUEST_STATUS = "com.seudominio.s24ultraapp.ACTION_REQUEST_STATUS"
    }
    
    private val connectivity = Connectivity.get(context)
    
    /**
     * Verifica se o Connectivity SDK está disponível e o dispositivo está conectado
     */
    fun isConnected(): Boolean {
        return try {
            connectivity.isAvailable() && connectivity.isConnected()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar conexão: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica se está pareado com o S24 Ultra
     */
    fun isLinked(): Boolean {
        return try {
            connectivity.isLinked()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar pareamento: ${e.message}")
            false
        }
    }
    
    /**
     * Envia dados de mídia para processamento no S24 Ultra
     */
    fun sendMediaDataForProcessing(
        mediaType: String,
        filePath: String,
        metadata: Map<String, Any> = emptyMap()
    ): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "Dispositivo não conectado. Não é possível enviar dados.")
            return false
        }
        
        return try {
            val intent = Intent(ACTION_SEND_MEDIA_DATA).apply {
                setPackage(S24_ULTRA_PACKAGE)
                putExtra("media_type", mediaType)
                putExtra("file_path", filePath)
                putExtra("timestamp", System.currentTimeMillis())
                putExtra("device_id", "vuzix_blade_1.5")
                
                // Adicionar metadados
                metadata.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        is Long -> putExtra(key, value)
                        is Float -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                    }
                }
            }
            
            connectivity.sendBroadcast(intent)
            Log.d(TAG, "Dados de mídia enviados: $mediaType - $filePath")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar dados de mídia: ${e.message}")
            false
        }
    }
    
    /**
     * Envia dados de áudio em chunks para processamento em tempo real
     */
    fun sendAudioChunk(
        audioData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int,
        sessionId: String
    ): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "Dispositivo não conectado. Não é possível enviar chunk de áudio.")
            return false
        }
        
        return try {
            val intent = Intent(ACTION_SEND_AUDIO_DATA).apply {
                setPackage(S24_ULTRA_PACKAGE)
                putExtra("session_id", sessionId)
                putExtra("chunk_index", chunkIndex)
                putExtra("total_chunks", totalChunks)
                putExtra("audio_data", audioData)
                putExtra("timestamp", System.currentTimeMillis())
                putExtra("data_size", audioData.size)
            }
            
            connectivity.sendBroadcast(intent)
            Log.d(TAG, "Chunk de áudio enviado: $chunkIndex/$totalChunks (${audioData.size} bytes)")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar chunk de áudio: ${e.message}")
            false
        }
    }
    
    /**
     * Envia frame de vídeo para processamento em tempo real
     */
    fun sendVideoFrame(
        frameData: ByteArray,
        frameIndex: Int,
        width: Int,
        height: Int,
        format: String,
        sessionId: String
    ): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "Dispositivo não conectado. Não é possível enviar frame de vídeo.")
            return false
        }
        
        return try {
            val intent = Intent(ACTION_SEND_VIDEO_DATA).apply {
                setPackage(S24_ULTRA_PACKAGE)
                putExtra("session_id", sessionId)
                putExtra("frame_index", frameIndex)
                putExtra("frame_width", width)
                putExtra("frame_height", height)
                putExtra("frame_format", format)
                putExtra("frame_data", frameData)
                putExtra("timestamp", System.currentTimeMillis())
                putExtra("data_size", frameData.size)
            }
            
            connectivity.sendBroadcast(intent)
            Log.d(TAG, "Frame de vídeo enviado: $frameIndex (${width}x${height}, ${frameData.size} bytes)")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar frame de vídeo: ${e.message}")
            false
        }
    }
    
    /**
     * Solicita status do processamento no S24 Ultra
     */
    fun requestProcessingStatus(sessionId: String): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "Dispositivo não conectado. Não é possível solicitar status.")
            return false
        }
        
        return try {
            val intent = Intent(ACTION_REQUEST_STATUS).apply {
                setPackage(S24_ULTRA_PACKAGE)
                putExtra("session_id", sessionId)
                putExtra("request_timestamp", System.currentTimeMillis())
            }
            
            connectivity.sendBroadcast(intent)
            Log.d(TAG, "Status de processamento solicitado para sessão: $sessionId")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao solicitar status: ${e.message}")
            false
        }
    }
    
    /**
     * Envia comando de controle para o S24 Ultra
     */
    fun sendControlCommand(command: String, parameters: Map<String, Any> = emptyMap()): Boolean {
        if (!isConnected()) {
            Log.w(TAG, "Dispositivo não conectado. Não é possível enviar comando.")
            return false
        }
        
        return try {
            val intent = Intent("com.seudominio.s24ultraapp.ACTION_CONTROL_COMMAND").apply {
                setPackage(S24_ULTRA_PACKAGE)
                putExtra("command", command)
                putExtra("timestamp", System.currentTimeMillis())
                
                // Adicionar parâmetros
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        is Long -> putExtra(key, value)
                        is Float -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                    }
                }
            }
            
            connectivity.sendBroadcast(intent)
            Log.d(TAG, "Comando de controle enviado: $command")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar comando: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica a origem de um Intent recebido
     */
    fun verifyIntentOrigin(intent: Intent): Boolean {
        return try {
            connectivity.verify(intent, S24_ULTRA_PACKAGE)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar origem do intent: ${e.message}")
            false
        }
    }
    
    /**
     * Obtém informações de status da conexão
     */
    fun getConnectionInfo(): Map<String, Any> {
        return mapOf(
            "is_available" to (try { connectivity.isAvailable() } catch (e: Exception) { false }),
            "is_connected" to (try { connectivity.isConnected() } catch (e: Exception) { false }),
            "is_linked" to (try { connectivity.isLinked() } catch (e: Exception) { false }),
            "timestamp" to System.currentTimeMillis()
        )
    }
}

package com.seudominio.vuzixbladeapp

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerenciador de sessões para controle de estado e histórico
 * Mantém controle de sessões de captura, status e métricas
 */
class SessionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SessionManager"
        private const val MAX_ACTIVE_SESSIONS = 5
        private const val SESSION_TIMEOUT_MS = 300000L // 5 minutos
    }
    
    // Mapa de sessões ativas
    private val activeSessions = ConcurrentHashMap<String, SessionInfo>()
    private var currentSessionId: String? = null
    
    // Callback para notificar mudanças de estado
    interface SessionCallback {
        fun onSessionStarted(sessionId: String)
        fun onSessionEnded(sessionId: String, metrics: SessionMetrics)
        fun onSessionError(sessionId: String, error: String)
        fun onSessionDataSent(sessionId: String, dataType: String, size: Int)
    }
    
    private var sessionCallback: SessionCallback? = null
    
    fun setSessionCallback(callback: SessionCallback) {
        this.sessionCallback = callback
    }
    
    /**
     * Inicia uma nova sessão de captura
     */
    fun startSession(activityType: String = "general"): String {
        // Limpar sessões antigas
        cleanupExpiredSessions()
        
        // Gerar ID único para a sessão
        val sessionId = generateSessionId()
        
        // Criar nova sessão
        val sessionInfo = SessionInfo(
            id = sessionId,
            startTime = System.currentTimeMillis(),
            activityType = activityType,
            status = SessionStatus.ACTIVE
        )
        
        activeSessions[sessionId] = sessionInfo
        currentSessionId = sessionId
        
        Log.d(TAG, "Nova sessão iniciada: $sessionId ($activityType)")
        sessionCallback?.onSessionStarted(sessionId)
        
        return sessionId
    }
    
    /**
     * Finaliza uma sessão ativa
     */
    fun endSession(sessionId: String): SessionMetrics? {
        val session = activeSessions[sessionId] ?: return null
        
        // Atualizar status e tempo final
        session.status = SessionStatus.COMPLETED
        session.endTime = System.currentTimeMillis()
        
        // Calcular métricas finais
        val metrics = calculateSessionMetrics(session)
        
        // Remover da lista de ativas
        activeSessions.remove(sessionId)
        
        if (currentSessionId == sessionId) {
            currentSessionId = null
        }
        
        Log.d(TAG, "Sessão finalizada: $sessionId")
        Log.d(TAG, "Métricas: ${metrics.summary()}")
        
        sessionCallback?.onSessionEnded(sessionId, metrics)
        
        return metrics
    }
    
    /**
     * Registra envio de dados durante uma sessão
     */
    fun recordDataSent(sessionId: String, dataType: String, sizeBytes: Int) {
        val session = activeSessions[sessionId] ?: return
        
        session.dataSent[dataType] = (session.dataSent[dataType] ?: 0) + sizeBytes
        session.lastActivityTime = System.currentTimeMillis()
        
        Log.d(TAG, "Dados enviados na sessão $sessionId: $dataType (${sizeBytes} bytes)")
        sessionCallback?.onSessionDataSent(sessionId, dataType, sizeBytes)
    }
    
    /**
     * Registra feedback recebido durante uma sessão
     */
    fun recordFeedbackReceived(sessionId: String, feedbackType: String, content: String) {
        val session = activeSessions[sessionId] ?: return
        
        val feedback = FeedbackInfo(
            type = feedbackType,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        
        session.feedbackReceived.add(feedback)
        session.lastActivityTime = System.currentTimeMillis()
        
        Log.d(TAG, "Feedback recebido na sessão $sessionId: $feedbackType")
    }
    
    /**
     * Obtém a sessão atual ativa
     */
    fun getCurrentSession(): SessionInfo? {
        return currentSessionId?.let { activeSessions[it] }
    }
    
    /**
     * Obtém todas as sessões ativas
     */
    fun getActiveSessions(): List<SessionInfo> {
        return activeSessions.values.toList()
    }
    
    /**
     * Verifica se há sessão ativa
     */
    fun hasActiveSession(): Boolean {
        return currentSessionId != null && activeSessions.containsKey(currentSessionId)
    }
    
    /**
     * Marca erro em uma sessão
     */
    fun markSessionError(sessionId: String, error: String) {
        val session = activeSessions[sessionId] ?: return
        
        session.status = SessionStatus.ERROR
        session.errors.add(error)
        session.lastActivityTime = System.currentTimeMillis()
        
        Log.e(TAG, "Erro na sessão $sessionId: $error")
        sessionCallback?.onSessionError(sessionId, error)
    }
    
    /**
     * Limpa sessões expiradas
     */
    private fun cleanupExpiredSessions() {
        val currentTime = System.currentTimeMillis()
        val expiredSessions = activeSessions.filter { (_, session) ->
            currentTime - session.lastActivityTime > SESSION_TIMEOUT_MS
        }
        
        expiredSessions.forEach { (sessionId, session) ->
            Log.w(TAG, "Sessão expirada removida: $sessionId")
            session.status = SessionStatus.EXPIRED
            activeSessions.remove(sessionId)
        }
        
        // Limitar número máximo de sessões ativas
        if (activeSessions.size > MAX_ACTIVE_SESSIONS) {
            val oldestSession = activeSessions.values.minByOrNull { it.startTime }
            oldestSession?.let { session ->
                Log.w(TAG, "Removendo sessão mais antiga: ${session.id}")
                activeSessions.remove(session.id)
            }
        }
    }
    
    /**
     * Gera ID único para sessão
     */
    private fun generateSessionId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (Math.random() * 1000).toInt()
        return "vuzix_session_${timestamp}_${random}"
    }
    
    /**
     * Calcula métricas de uma sessão
     */
    private fun calculateSessionMetrics(session: SessionInfo): SessionMetrics {
        val duration = (session.endTime ?: System.currentTimeMillis()) - session.startTime
        val totalDataSent = session.dataSent.values.sum()
        val feedbackCount = session.feedbackReceived.size
        
        return SessionMetrics(
            sessionId = session.id,
            duration = duration,
            totalDataSent = totalDataSent,
            feedbackCount = feedbackCount,
            errorCount = session.errors.size,
            activityType = session.activityType
        )
    }
}

/**
 * Informações de uma sessão
 */
data class SessionInfo(
    val id: String,
    val startTime: Long,
    var endTime: Long? = null,
    val activityType: String,
    var status: SessionStatus,
    var lastActivityTime: Long = System.currentTimeMillis(),
    val dataSent: MutableMap<String, Int> = mutableMapOf(),
    val feedbackReceived: MutableList<FeedbackInfo> = mutableListOf(),
    val errors: MutableList<String> = mutableListOf()
)

/**
 * Status de uma sessão
 */
enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    ERROR,
    EXPIRED
}

/**
 * Informações de feedback recebido
 */
data class FeedbackInfo(
    val type: String,
    val content: String,
    val timestamp: Long
)

/**
 * Métricas de uma sessão concluída
 */
data class SessionMetrics(
    val sessionId: String,
    val duration: Long,
    val totalDataSent: Int,
    val feedbackCount: Int,
    val errorCount: Int,
    val activityType: String
) {
    fun summary(): String {
        val durationSec = duration / 1000.0
        val dataMB = totalDataSent / (1024.0 * 1024.0)
        return "Duração: ${durationSec}s, Dados: ${String.format("%.2f", dataMB)}MB, Feedbacks: $feedbackCount, Erros: $errorCount"
    }
}

package com.seudominio.vuzixbladeapp

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Debug
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Monitor de performance para Vuzix Blade 1.5
 * Monitora uso de memória, CPU, bateria e conectividade
 * Otimizado para hardware limitado (1GB RAM)
 */
class PerformanceMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val MONITORING_INTERVAL_MS = 10000L // 10 segundos
        private const val MAX_MEMORY_USAGE_MB = 800 // 80% dos 1GB disponíveis
        private const val LOW_BATTERY_THRESHOLD = 20 // 20%
    }
    
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var isMonitoring = false
    
    // Callback para alertas de performance
    interface PerformanceCallback {
        fun onMemoryWarning(currentMB: Int, maxMB: Int)
        fun onLowBattery(batteryLevel: Int)
        fun onHighCpuUsage(cpuPercent: Float)
        fun onConnectivityIssue(issue: String)
        fun onPerformanceUpdate(metrics: PerformanceMetrics)
    }
    
    private var performanceCallback: PerformanceCallback? = null
    
    fun setPerformanceCallback(callback: PerformanceCallback) {
        this.performanceCallback = callback
    }
    
    /**
     * Inicia monitoramento contínuo de performance
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.w(TAG, "Monitoramento já está ativo")
            return
        }
        
        isMonitoring = true
        Log.d(TAG, "Iniciando monitoramento de performance")
        
        executor.scheduleAtFixedRate({
            try {
                val metrics = collectMetrics()
                analyzeMetrics(metrics)
                performanceCallback?.onPerformanceUpdate(metrics)
            } catch (e: Exception) {
                Log.e(TAG, "Erro durante monitoramento: ${e.message}")
            }
        }, 0, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Para monitoramento de performance
     */
    fun stopMonitoring() {
        if (!isMonitoring) return
        
        isMonitoring = false
        executor.shutdown()
        Log.d(TAG, "Monitoramento de performance parado")
    }
    
    /**
     * Coleta métricas instantâneas de performance
     */
    fun collectMetrics(): PerformanceMetrics {
        val memoryInfo = getMemoryInfo()
        val batteryLevel = getBatteryLevel()
        val cpuUsage = getCpuUsage()
        
        return PerformanceMetrics(
            timestamp = System.currentTimeMillis(),
            memoryUsedMB = memoryInfo.used,
            memoryAvailableMB = memoryInfo.available,
            memoryTotalMB = memoryInfo.total,
            batteryLevel = batteryLevel,
            cpuUsagePercent = cpuUsage,
            isLowMemory = memoryInfo.used > MAX_MEMORY_USAGE_MB,
            isLowBattery = batteryLevel < LOW_BATTERY_THRESHOLD
        )
    }
    
    /**
     * Analisa métricas e gera alertas se necessário
     */
    private fun analyzeMetrics(metrics: PerformanceMetrics) {
        // Verificar uso de memória
        if (metrics.isLowMemory) {
            Log.w(TAG, "Aviso de memória: ${metrics.memoryUsedMB}MB/${metrics.memoryTotalMB}MB")
            performanceCallback?.onMemoryWarning(metrics.memoryUsedMB, MAX_MEMORY_USAGE_MB)
        }
        
        // Verificar nível de bateria
        if (metrics.isLowBattery) {
            Log.w(TAG, "Bateria baixa: ${metrics.batteryLevel}%")
            performanceCallback?.onLowBattery(metrics.batteryLevel)
        }
        
        // Verificar uso de CPU
        if (metrics.cpuUsagePercent > 80.0f) {
            Log.w(TAG, "Alto uso de CPU: ${metrics.cpuUsagePercent}%")
            performanceCallback?.onHighCpuUsage(metrics.cpuUsagePercent)
        }
        
        // Log métricas gerais
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Métricas: ${metrics.summary()}")
        }
    }
    
    /**
     * Obtém informações de uso de memória
     */
    private fun getMemoryInfo(): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        // Converter de bytes para MB
        val totalMB = (memInfo.totalMem / (1024 * 1024)).toInt()
        val availableMB = (memInfo.availMem / (1024 * 1024)).toInt()
        val usedMB = totalMB - availableMB
        
        return MemoryInfo(
            total = totalMB,
            available = availableMB,
            used = usedMB
        )
    }
    
    /**
     * Obtém nível atual da bateria
     */
    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    /**
     * Estima uso de CPU (método simplificado para API 22)
     */
    private fun getCpuUsage(): Float {
        try {
            // Para API 22, usar método básico baseado em Debug
            val memInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memInfo)
            
            // Estimativa baseada em uso de memória e atividade do sistema
            // Não é preciso, mas fornece uma indicação geral
            val memoryPressure = (memInfo.totalPss / (1024.0 * 10)).toFloat() // Aproximação
            return minOf(100.0f, maxOf(0.0f, memoryPressure))
            
        } catch (e: Exception) {
            Log.w(TAG, "Erro ao obter CPU usage: ${e.message}")
            return 0.0f
        }
    }
    
    /**
     * Força coleta de lixo (garbage collection)
     */
    fun forceGarbageCollection() {
        Log.d(TAG, "Forçando garbage collection")
        System.gc()
        
        // Aguardar um pouco para GC completar
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
    
    /**
     * Otimiza uso de memória liberando caches
     */
    fun optimizeMemoryUsage() {
        Log.d(TAG, "Otimizando uso de memória")
        
        // Forçar GC
        forceGarbageCollection()
        
        // Trimear memória de componentes do sistema
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.clearApplicationUserData()
        
        Log.d(TAG, "Otimização de memória concluída")
    }
    
    /**
     * Verifica se o sistema está sob pressão de recursos
     */
    fun isUnderResourcePressure(): Boolean {
        val metrics = collectMetrics()
        return metrics.isLowMemory || metrics.isLowBattery || metrics.cpuUsagePercent > 90.0f
    }
    
    /**
     * Obtém recomendações para otimização
     */
    fun getOptimizationRecommendations(): List<String> {
        val metrics = collectMetrics()
        val recommendations = mutableListOf<String>()
        
        if (metrics.isLowMemory) {
            recommendations.add("Reduzir qualidade de captura de vídeo")
            recommendations.add("Limitar chunks de dados em memória")
            recommendations.add("Executar garbage collection")
        }
        
        if (metrics.isLowBattery) {
            recommendations.add("Reduzir frequência de captura")
            recommendations.add("Diminuir brilho da tela")
            recommendations.add("Pausar processamento não-essencial")
        }
        
        if (metrics.cpuUsagePercent > 80.0f) {
            recommendations.add("Reduzir frame rate de vídeo")
            recommendations.add("Simplificar processamento de áudio")
            recommendations.add("Aumentar intervalo entre transmissões")
        }
        
        return recommendations
    }
}

/**
 * Métricas de performance coletadas
 */
data class PerformanceMetrics(
    val timestamp: Long,
    val memoryUsedMB: Int,
    val memoryAvailableMB: Int,
    val memoryTotalMB: Int,
    val batteryLevel: Int,
    val cpuUsagePercent: Float,
    val isLowMemory: Boolean,
    val isLowBattery: Boolean
) {
    fun summary(): String {
        return "Mem: ${memoryUsedMB}/${memoryTotalMB}MB (${(memoryUsedMB * 100.0 / memoryTotalMB).toInt()}%), " +
                "Bat: ${batteryLevel}%, CPU: ${cpuUsagePercent.toInt()}%"
    }
    
    fun memoryUsagePercent(): Int {
        return (memoryUsedMB * 100.0 / memoryTotalMB).toInt()
    }
}

/**
 * Informações específicas de memória
 */
private data class MemoryInfo(
    val total: Int,
    val available: Int,
    val used: Int
)

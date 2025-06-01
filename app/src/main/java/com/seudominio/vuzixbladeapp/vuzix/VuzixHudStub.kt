package com.seudominio.vuzixbladeapp.vuzix

import android.content.Context
import android.util.Log

/**
 * Stub para HUD SDK - Funciona sem o SDK real
 * Permite testar toda funcionalidade antes de ter os SDKs oficiais
 */
class VuzixHudStub(private val context: Context) {
    
    companion object {
        private const val TAG = "VuzixHudStub"
    }
    
    /**
     * Simula ActionMenu do HUD SDK
     */
    fun showActionMenu(title: String, items: List<String>, callback: (Int) -> Unit) {
        Log.d(TAG, "🎯 HUD ActionMenu: $title")
        items.forEachIndexed { index, item ->
            Log.d(TAG, "  [$index] $item")
        }
        
        // Simular seleção automática (primeira opção)
        android.os.Handler().postDelayed({
            Log.d(TAG, "✅ Usuário selecionou: ${items.first()}")
            callback(0)
        }, 2000)
    }
    
    /**
     * Simula display de texto no HUD
     */
    fun displayText(text: String, duration: Long = 3000) {
        Log.d(TAG, "📱 HUD Display: $text")
        
        // Em um HUD real, isso apareceria na tela
        // Por enquanto, mostra no log e podemos exibir em Toast
        android.os.Handler().postDelayed({
            Log.d(TAG, "⏰ HUD text expired")
        }, duration)
    }
    
    /**
     * Simula notificação HUD
     */
    fun showNotification(title: String, message: String, iconResId: Int = 0) {
        Log.d(TAG, "🔔 HUD Notification:")
        Log.d(TAG, "  📌 $title")
        Log.d(TAG, "  💬 $message")
    }
    
    /**
     * Simula status do HUD
     */
    fun isHudAvailable(): Boolean {
        Log.d(TAG, "❓ Verificando disponibilidade do HUD...")
        // Simular que HUD está disponível
        return true
    }
    
    /**
     * Simula configurações do HUD
     */
    fun configureHud(brightness: Float = 0.8f, position: String = "center") {
        Log.d(TAG, "⚙️ Configurando HUD: brightness=$brightness, position=$position")
    }
}

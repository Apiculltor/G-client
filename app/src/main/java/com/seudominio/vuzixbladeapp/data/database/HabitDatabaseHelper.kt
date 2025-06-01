package com.seudominio.vuzixbladeapp.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Helper do banco de dados SQLite para o sistema de hábitos
 * Compatível com API 22 (sem Room)
 */
class HabitDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, 
    DATABASE_NAME, 
    null, 
    DATABASE_VERSION
) {
    
    companion object {
        private const val TAG = "HabitDatabase"
        private const val DATABASE_NAME = "habit_formation.db"
        private const val DATABASE_VERSION = 1
        
        // Tabela de tarefas
        const val TABLE_TASKS = "tasks"
        const val TASK_ID = "id"
        const val TASK_NAME = "name"
        const val TASK_DESCRIPTION = "description"
        const val TASK_SCHEDULED_TIME = "scheduled_time"
        const val TASK_DURATION = "duration"
        const val TASK_DAYS_OF_WEEK = "days_of_week"
        const val TASK_IS_ACTIVE = "is_active"
        const val TASK_PRIORITY = "priority"
        const val TASK_CATEGORY = "category"
        const val TASK_REMINDER_MINUTES = "reminder_minutes"
        const val TASK_CREATED_AT = "created_at"
        const val TASK_UPDATED_AT = "updated_at"
        
        // Tabela de execuções
        const val TABLE_EXECUTIONS = "task_executions"
        const val EXEC_ID = "id"
        const val EXEC_TASK_ID = "task_id"
        const val EXEC_SCHEDULED_TIME = "scheduled_time"
        const val EXEC_START_TIME = "start_time"
        const val EXEC_END_TIME = "end_time"
        const val EXEC_STATUS = "status"
        const val EXEC_ACTUAL_DURATION = "actual_duration"
        const val EXEC_VIDEO_PATH = "video_path"
        const val EXEC_NOTES = "notes"
        const val EXEC_CONFIDENCE = "confidence"
        const val EXEC_DETECTED_ACTIVITY = "detected_activity"
        const val EXEC_FEEDBACK_GIVEN = "feedback_given"
        const val EXEC_CREATED_AT = "created_at"
        
        // Tabela de relatórios
        const val TABLE_REPORTS = "habit_reports"
        const val REPORT_ID = "id"
        const val REPORT_TYPE = "report_type"
        const val REPORT_START_DATE = "start_date"
        const val REPORT_END_DATE = "end_date"
        const val REPORT_TOTAL_TASKS = "total_tasks"
        const val REPORT_COMPLETED_TASKS = "completed_tasks"
        const val REPORT_PARTIAL_TASKS = "partial_tasks"
        const val REPORT_MISSED_TASKS = "missed_tasks"
        const val REPORT_AVERAGE_SCORE = "average_score"
        const val REPORT_TOTAL_EXECUTION_TIME = "total_execution_time"
        const val REPORT_ON_TIME_PERCENTAGE = "on_time_percentage"
        const val REPORT_MOST_PRODUCTIVE_TIME = "most_productive_time"
        const val REPORT_LEAST_PRODUCTIVE_TIME = "least_productive_time"
        const val REPORT_TOP_CATEGORY = "top_performing_category"
        const val REPORT_SUGGESTIONS = "improvement_suggestions"
        const val REPORT_CREATED_AT = "created_at"
        
        @Volatile
        private var INSTANCE: HabitDatabaseHelper? = null
        
        fun getInstance(context: Context): HabitDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HabitDatabaseHelper(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Criando banco de dados - versão $DATABASE_VERSION")
        
        // Criar tabela de tarefas
        val createTasksTable = """
            CREATE TABLE $TABLE_TASKS (
                $TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TASK_NAME TEXT NOT NULL,
                $TASK_DESCRIPTION TEXT,
                $TASK_SCHEDULED_TIME TEXT NOT NULL,
                $TASK_DURATION INTEGER NOT NULL,
                $TASK_DAYS_OF_WEEK TEXT NOT NULL,
                $TASK_IS_ACTIVE INTEGER DEFAULT 1,
                $TASK_PRIORITY TEXT DEFAULT 'MEDIUM',
                $TASK_CATEGORY TEXT DEFAULT 'PERSONAL',
                $TASK_REMINDER_MINUTES INTEGER DEFAULT 5,
                $TASK_CREATED_AT INTEGER NOT NULL,
                $TASK_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        
        // Criar tabela de execuções
        val createExecutionsTable = """
            CREATE TABLE $TABLE_EXECUTIONS (
                $EXEC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EXEC_TASK_ID INTEGER NOT NULL,
                $EXEC_SCHEDULED_TIME INTEGER NOT NULL,
                $EXEC_START_TIME INTEGER,
                $EXEC_END_TIME INTEGER,
                $EXEC_STATUS TEXT NOT NULL,
                $EXEC_ACTUAL_DURATION INTEGER,
                $EXEC_VIDEO_PATH TEXT,
                $EXEC_NOTES TEXT,
                $EXEC_CONFIDENCE REAL DEFAULT 0.0,
                $EXEC_DETECTED_ACTIVITY TEXT,
                $EXEC_FEEDBACK_GIVEN TEXT,
                $EXEC_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY ($EXEC_TASK_ID) REFERENCES $TABLE_TASKS($TASK_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        
        // Criar tabela de relatórios
        val createReportsTable = """
            CREATE TABLE $TABLE_REPORTS (
                $REPORT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $REPORT_TYPE TEXT NOT NULL,
                $REPORT_START_DATE INTEGER NOT NULL,
                $REPORT_END_DATE INTEGER NOT NULL,
                $REPORT_TOTAL_TASKS INTEGER DEFAULT 0,
                $REPORT_COMPLETED_TASKS INTEGER DEFAULT 0,
                $REPORT_PARTIAL_TASKS INTEGER DEFAULT 0,
                $REPORT_MISSED_TASKS INTEGER DEFAULT 0,
                $REPORT_AVERAGE_SCORE REAL DEFAULT 0.0,
                $REPORT_TOTAL_EXECUTION_TIME INTEGER DEFAULT 0,
                $REPORT_ON_TIME_PERCENTAGE REAL DEFAULT 0.0,
                $REPORT_MOST_PRODUCTIVE_TIME TEXT,
                $REPORT_LEAST_PRODUCTIVE_TIME TEXT,
                $REPORT_TOP_CATEGORY TEXT,
                $REPORT_SUGGESTIONS TEXT,
                $REPORT_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        
        try {
            db.execSQL(createTasksTable)
            Log.d(TAG, "Tabela $TABLE_TASKS criada com sucesso")
            
            db.execSQL(createExecutionsTable)
            Log.d(TAG, "Tabela $TABLE_EXECUTIONS criada com sucesso")
            
            db.execSQL(createReportsTable)
            Log.d(TAG, "Tabela $TABLE_REPORTS criada com sucesso")
            
            // Criar índices para performance
            createIndexes(db)
            
            // Inserir dados iniciais de exemplo
            insertSampleData(db)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar tabelas: ${e.message}")
            throw e
        }
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Atualizando banco de dados da versão $oldVersion para $newVersion")
        
        // Para versões futuras, implementar migração adequada
        // Por enquanto, recrear todas as tabelas
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXECUTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }
    
    private fun createIndexes(db: SQLiteDatabase) {
        // Índices para otimizar consultas frequentes
        val indexes = arrayOf(
            "CREATE INDEX IF NOT EXISTS idx_tasks_active ON $TABLE_TASKS($TASK_IS_ACTIVE)",
            "CREATE INDEX IF NOT EXISTS idx_tasks_scheduled_time ON $TABLE_TASKS($TASK_SCHEDULED_TIME)",
            "CREATE INDEX IF NOT EXISTS idx_executions_task_id ON $TABLE_EXECUTIONS($EXEC_TASK_ID)",
            "CREATE INDEX IF NOT EXISTS idx_executions_scheduled_time ON $TABLE_EXECUTIONS($EXEC_SCHEDULED_TIME)",
            "CREATE INDEX IF NOT EXISTS idx_executions_status ON $TABLE_EXECUTIONS($EXEC_STATUS)",
            "CREATE INDEX IF NOT EXISTS idx_reports_date_range ON $TABLE_REPORTS($REPORT_START_DATE, $REPORT_END_DATE)"
        )
        
        indexes.forEach { sql ->
            try {
                db.execSQL(sql)
                Log.d(TAG, "Índice criado: $sql")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar índice: ${e.message}")
            }
        }
    }
    
    private fun insertSampleData(db: SQLiteDatabase) {
        Log.d(TAG, "Inserindo dados de exemplo...")
        
        val currentTime = System.currentTimeMillis()
        
        // Tarefas de exemplo
        val sampleTasks = arrayOf(
            // Beber água a cada 2 horas
            "('Beber Água', 'Beber um copo de água para manter hidratação', '08:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            "('Beber Água', 'Beber um copo de água para manter hidratação', '10:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            "('Beber Água', 'Beber um copo de água para manter hidratação', '12:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            "('Beber Água', 'Beber um copo de água para manter hidratação', '14:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            "('Beber Água', 'Beber um copo de água para manter hidratação', '16:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            "('Beber Água', 'Beber um copo de água para manter hidratação', '18:00', 2, '1,2,3,4,5,6,7', 1, 'HIGH', 'HEALTH', 5, $currentTime, $currentTime)",
            
            // Exercícios
            "('Exercício Matinal', 'Fazer 15 minutos de exercício ao acordar', '07:00', 15, '1,2,3,4,5,6,7', 1, 'MEDIUM', 'EXERCISE', 10, $currentTime, $currentTime)",
            "('Caminhada Noturna', 'Caminhar 20 minutos após o jantar', '19:30', 20, '1,2,3,4,5,6,7', 1, 'MEDIUM', 'EXERCISE', 15, $currentTime, $currentTime)",
            
            // Meditação
            "('Meditação', 'Meditar por 10 minutos para relaxar', '06:30', 10, '1,2,3,4,5,6,7', 1, 'HIGH', 'MEDITATION', 5, $currentTime, $currentTime)",
            "('Meditação Noturna', 'Meditação antes de dormir', '22:00', 10, '1,2,3,4,5,6,7', 1, 'MEDIUM', 'MEDITATION', 10, $currentTime, $currentTime)",
            
            // Trabalho
            "('Review Diário', 'Revisar progresso do dia e planejar próximo', '17:00', 15, '2,3,4,5,6', 1, 'HIGH', 'WORK', 10, $currentTime, $currentTime)",
            
            // Aprendizado
            "('Leitura', 'Ler por 30 minutos', '21:00', 30, '1,2,3,4,5,6,7', 1, 'MEDIUM', 'LEARNING', 10, $currentTime, $currentTime)"
        )
        
        sampleTasks.forEach { task ->
            try {
                val sql = """
                    INSERT INTO $TABLE_TASKS (
                        $TASK_NAME, $TASK_DESCRIPTION, $TASK_SCHEDULED_TIME, 
                        $TASK_DURATION, $TASK_DAYS_OF_WEEK, $TASK_IS_ACTIVE, 
                        $TASK_PRIORITY, $TASK_CATEGORY, $TASK_REMINDER_MINUTES, 
                        $TASK_CREATED_AT, $TASK_UPDATED_AT
                    ) VALUES $task
                """.trimIndent()
                
                db.execSQL(sql)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inserir tarefa de exemplo: ${e.message}")
            }
        }
        
        Log.d(TAG, "Dados de exemplo inseridos com sucesso")
    }
    
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Habilitar foreign keys
        db.setForeignKeyConstraintsEnabled(true)
    }
}
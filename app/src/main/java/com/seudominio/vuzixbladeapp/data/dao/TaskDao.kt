package com.seudominio.vuzixbladeapp.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.seudominio.vuzixbladeapp.data.database.HabitDatabaseHelper
import com.seudominio.vuzixbladeapp.data.models.Task
import com.seudominio.vuzixbladeapp.data.models.TaskCategory
import com.seudominio.vuzixbladeapp.data.models.TaskPriority
import java.util.*

/**
 * DAO para operações com tarefas
 * Compatível com API 22 (SQLite raw)
 */
class TaskDao(context: Context) {
    
    companion object {
        private const val TAG = "TaskDao"
    }
    
    private val dbHelper = HabitDatabaseHelper.getInstance(context)
    
    /**
     * Inserir nova tarefa
     */
    fun insertTask(task: Task): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(HabitDatabaseHelper.TASK_NAME, task.name)
            put(HabitDatabaseHelper.TASK_DESCRIPTION, task.description)
            put(HabitDatabaseHelper.TASK_SCHEDULED_TIME, task.scheduledTime)
            put(HabitDatabaseHelper.TASK_DURATION, task.duration)
            put(HabitDatabaseHelper.TASK_DAYS_OF_WEEK, task.daysOfWeek.joinToString(","))
            put(HabitDatabaseHelper.TASK_IS_ACTIVE, if (task.isActive) 1 else 0)
            put(HabitDatabaseHelper.TASK_PRIORITY, task.priority.name)
            put(HabitDatabaseHelper.TASK_CATEGORY, task.category.name)
            put(HabitDatabaseHelper.TASK_REMINDER_MINUTES, task.reminderMinutes)
            put(HabitDatabaseHelper.TASK_CREATED_AT, task.createdAt)
            put(HabitDatabaseHelper.TASK_UPDATED_AT, task.updatedAt)
        }
        
        return try {
            val id = db.insert(HabitDatabaseHelper.TABLE_TASKS, null, values)
            Log.d(TAG, "Tarefa inserida com ID: $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir tarefa: ${e.message}")
            -1
        }
    }
    
    /**
     * Atualizar tarefa existente
     */
    fun updateTask(task: Task): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(HabitDatabaseHelper.TASK_NAME, task.name)
            put(HabitDatabaseHelper.TASK_DESCRIPTION, task.description)
            put(HabitDatabaseHelper.TASK_SCHEDULED_TIME, task.scheduledTime)
            put(HabitDatabaseHelper.TASK_DURATION, task.duration)
            put(HabitDatabaseHelper.TASK_DAYS_OF_WEEK, task.daysOfWeek.joinToString(","))
            put(HabitDatabaseHelper.TASK_IS_ACTIVE, if (task.isActive) 1 else 0)
            put(HabitDatabaseHelper.TASK_PRIORITY, task.priority.name)
            put(HabitDatabaseHelper.TASK_CATEGORY, task.category.name)
            put(HabitDatabaseHelper.TASK_REMINDER_MINUTES, task.reminderMinutes)
            put(HabitDatabaseHelper.TASK_UPDATED_AT, System.currentTimeMillis())
        }
        
        return try {
            val rowsAffected = db.update(
                HabitDatabaseHelper.TABLE_TASKS,
                values,
                "${HabitDatabaseHelper.TASK_ID} = ?",
                arrayOf(task.id.toString())
            )
            Log.d(TAG, "Tarefa atualizada - linhas afetadas: $rowsAffected")
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar tarefa: ${e.message}")
            false
        }
    }
    
    /**
     * Buscar tarefa por ID
     */
    fun getTaskById(id: Long): Task? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            HabitDatabaseHelper.TABLE_TASKS,
            null,
            "${HabitDatabaseHelper.TASK_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        
        return try {
            if (cursor.moveToFirst()) {
                cursorToTask(cursor)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar tarefa por ID: ${e.message}")
            null
        } finally {
            cursor.close()
        }
    }
    
    /**
     * Buscar todas as tarefas ativas
     */
    fun getActiveTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val tasks = mutableListOf<Task>()
        
        val cursor = db.query(
            HabitDatabaseHelper.TABLE_TASKS,
            null,
            "${HabitDatabaseHelper.TASK_IS_ACTIVE} = ?",
            arrayOf("1"),
            null, null,
            HabitDatabaseHelper.TASK_SCHEDULED_TIME
        )
        
        try {
            while (cursor.moveToNext()) {
                cursorToTask(cursor)?.let { tasks.add(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar tarefas ativas: ${e.message}")
        } finally {
            cursor.close()
        }
        
        return tasks
    }
    
    /**
     * Buscar tarefas que devem ser executadas hoje
     */
    fun getTodayTasks(): List<Task> {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return getActiveTasks().filter { task ->
            task.daysOfWeek.contains(today)
        }
    }
    
    /**
     * Buscar próxima tarefa a ser executada
     */
    fun getNextTask(): Task? {
        val activeTasks = getTodayTasks()
        val now = Calendar.getInstance()
        
        return activeTasks
            .mapNotNull { task ->
                task.getNextExecutionTime()?.let { nextTime ->
                    Pair(task, nextTime)
                }
            }
            .filter { (_, nextTime) -> nextTime.after(now) }
            .minByOrNull { (_, nextTime) -> nextTime.timeInMillis }
            ?.first
    }
    
    /**
     * Buscar tarefas por categoria
     */
    fun getTasksByCategory(category: TaskCategory): List<Task> {
        val db = dbHelper.readableDatabase
        val tasks = mutableListOf<Task>()
        
        val cursor = db.query(
            HabitDatabaseHelper.TABLE_TASKS,
            null,
            "${HabitDatabaseHelper.TASK_CATEGORY} = ? AND ${HabitDatabaseHelper.TASK_IS_ACTIVE} = ?",
            arrayOf(category.name, "1"),
            null, null,
            HabitDatabaseHelper.TASK_SCHEDULED_TIME
        )
        
        try {
            while (cursor.moveToNext()) {
                cursorToTask(cursor)?.let { tasks.add(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar tarefas por categoria: ${e.message}")
        } finally {
            cursor.close()
        }
        
        return tasks
    }
    
    /**
     * Buscar tarefas que estão no horário de executar
     */
    fun getTasksToExecuteNow(toleranceMinutes: Int = 5): List<Task> {
        return getTodayTasks().filter { task ->
            task.isTimeToExecute(toleranceMinutes)
        }
    }
    
    /**
     * Desativar uma tarefa
     */
    fun deactivateTask(taskId: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(HabitDatabaseHelper.TASK_IS_ACTIVE, 0)
            put(HabitDatabaseHelper.TASK_UPDATED_AT, System.currentTimeMillis())
        }
        
        return try {
            val rowsAffected = db.update(
                HabitDatabaseHelper.TABLE_TASKS,
                values,
                "${HabitDatabaseHelper.TASK_ID} = ?",
                arrayOf(taskId.toString())
            )
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao desativar tarefa: ${e.message}")
            false
        }
    }
    
    /**
     * Excluir tarefa permanentemente
     */
    fun deleteTask(taskId: Long): Boolean {
        val db = dbHelper.writableDatabase
        
        return try {
            val rowsAffected = db.delete(
                HabitDatabaseHelper.TABLE_TASKS,
                "${HabitDatabaseHelper.TASK_ID} = ?",
                arrayOf(taskId.toString())
            )
            Log.d(TAG, "Tarefa excluída - linhas afetadas: $rowsAffected")
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir tarefa: ${e.message}")
            false
        }
    }
    
    /**
     * Converter cursor para objeto Task
     */
    private fun cursorToTask(cursor: Cursor): Task? {
        return try {
            val daysOfWeekString = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_DAYS_OF_WEEK))
            val daysOfWeek = daysOfWeekString.split(",").mapNotNull { 
                it.trim().toIntOrNull() 
            }
            
            val priorityString = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_PRIORITY))
            val priority = try {
                TaskPriority.valueOf(priorityString)
            } catch (e: Exception) {
                TaskPriority.MEDIUM
            }
            
            val categoryString = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_CATEGORY))
            val category = try {
                TaskCategory.valueOf(categoryString)
            } catch (e: Exception) {
                TaskCategory.PERSONAL
            }
            
            Task(
                id = cursor.getLong(cursor.getColumnIndex(HabitDatabaseHelper.TASK_ID)),
                name = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_NAME)),
                description = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_DESCRIPTION)) ?: "",
                scheduledTime = cursor.getString(cursor.getColumnIndex(HabitDatabaseHelper.TASK_SCHEDULED_TIME)),
                duration = cursor.getInt(cursor.getColumnIndex(HabitDatabaseHelper.TASK_DURATION)),
                daysOfWeek = daysOfWeek,
                isActive = cursor.getInt(cursor.getColumnIndex(HabitDatabaseHelper.TASK_IS_ACTIVE)) == 1,
                priority = priority,
                category = category,
                reminderMinutes = cursor.getInt(cursor.getColumnIndex(HabitDatabaseHelper.TASK_REMINDER_MINUTES)),
                createdAt = cursor.getLong(cursor.getColumnIndex(HabitDatabaseHelper.TASK_CREATED_AT)),
                updatedAt = cursor.getLong(cursor.getColumnIndex(HabitDatabaseHelper.TASK_UPDATED_AT))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao converter cursor para Task: ${e.message}")
            null
        }
    }
    
    /**
     * Contar total de tarefas ativas
     */
    fun getActiveTasksCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${HabitDatabaseHelper.TABLE_TASKS} WHERE ${HabitDatabaseHelper.TASK_IS_ACTIVE} = 1",
            null
        )
        
        return try {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar tarefas ativas: ${e.message}")
            0
        } finally {
            cursor.close()
        }
    }
}

package com.facundo.recordatoriosapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    @Query("SELECT * FROM recordatorios WHERE pendienteEliminar = 0 ORDER BY id ASC")
    fun obtenerTodos(): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE id = :id AND pendienteEliminar = 0 LIMIT 1")
    fun obtenerPorId(id: Int): Flow<RecordatorioEntity?>

    @Query("SELECT * FROM recordatorios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Int): RecordatorioEntity?

    @Query("SELECT * FROM recordatorios WHERE estado = :estado AND pendienteEliminar = 0 ORDER BY id ASC")
    fun obtenerPorEstado(estado: String): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE categoria = :categoria AND pendienteEliminar = 0 ORDER BY id ASC")
    fun obtenerPorCategoria(categoria: String): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE prioridad = :prioridad AND pendienteEliminar = 0 ORDER BY id ASC")
    fun obtenerPorPrioridad(prioridad: String): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE pendienteSincronizacion = 1 AND pendienteEliminar = 0 ORDER BY id ASC")
    fun obtenerPendientesSincronizacion(): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE pendienteEliminar = 1 ORDER BY id ASC")
    fun obtenerPendientesEliminar(): Flow<List<RecordatorioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)

    @Query("UPDATE recordatorios SET pendienteSincronizacion = 0, remoteId = :remoteId WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Int, remoteId: Int)

    @Query("UPDATE recordatorios SET pendienteEliminar = 1, pendienteSincronizacion = 1 WHERE id = :id")
    suspend fun marcarParaEliminar(id: Int)

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("DELETE FROM recordatorios")
    suspend fun eliminarTodos()
}

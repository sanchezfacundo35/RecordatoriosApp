
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

    @Query("""
        SELECT * FROM recordatorios
        WHERE ownerUid = :uid
        AND pendienteEliminar = 0
        ORDER BY id ASC
    """)
    fun obtenerTodos(uid: String): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE id = :id AND ownerUid = :uid
        AND pendienteEliminar = 0 LIMIT 1
    """)
    fun obtenerPorId(id: Int, uid: String): Flow<RecordatorioEntity?>

    @Query("SELECT * FROM recordatorios WHERE id = :id AND ownerUid = :uid LIMIT 1")
    suspend fun obtenerPorIdDirecto(id: Int, uid: String): RecordatorioEntity?

    @Query("""
        SELECT * FROM recordatorios
        WHERE estado = :estado AND ownerUid = :uid
        AND pendienteEliminar = 0 ORDER BY id ASC
    """)
    fun obtenerPorEstado(estado: String, uid: String): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE categoria = :categoria AND ownerUid = :uid
        AND pendienteEliminar = 0 ORDER BY id ASC
    """)
    fun obtenerPorCategoria(categoria: String, uid: String): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE prioridad = :prioridad AND ownerUid = :uid
        AND pendienteEliminar = 0 ORDER BY id ASC
    """)
    fun obtenerPorPrioridad(prioridad: String, uid: String): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE ownerUid = :uid
        AND pendienteSincronizacion = 1
        AND pendienteEliminar = 0 ORDER BY id ASC
    """)
    fun obtenerPendientesSincronizacion(uid: String): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE ownerUid = :uid
        AND pendienteEliminar = 1 ORDER BY id ASC
    """)
    fun obtenerPendientesEliminar(uid: String): Flow<List<RecordatorioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)

    @Query("""
        UPDATE recordatorios
        SET pendienteSincronizacion = 0, remoteId = :remoteId
        WHERE id = :id AND ownerUid = :uid
    """)
    suspend fun marcarComoSincronizado(id: Int, remoteId: Int, uid: String)

    @Query("""
        UPDATE recordatorios
        SET pendienteEliminar = 1, pendienteSincronizacion = 1
        WHERE id = :id AND ownerUid = :uid
    """)
    suspend fun marcarParaEliminar(id: Int, uid: String)

    @Query("DELETE FROM recordatorios WHERE id = :id AND ownerUid = :uid")
    suspend fun eliminarPorId(id: Int, uid: String)

    @Query("""
        UPDATE recordatorios
        SET ownerUid = :uid
        WHERE ownerUid IS NULL
    """)
    suspend fun asignarRegistrosAntiguos(uid: String)

    @Query("SELECT COUNT(*) FROM recordatorios WHERE ownerUid IS NULL")
    suspend fun contarRegistrosSinPropietario(): Int

    @Query("DELETE FROM recordatorios WHERE ownerUid IS NULL")
    suspend fun eliminarRegistrosSinPropietario()

    @Query("DELETE FROM recordatorios")
    suspend fun eliminarTodos()

    @Query("""
    UPDATE recordatorios
    SET pendienteSincronizacion = 0
    WHERE id = :id AND ownerUid = :uid
""")
    suspend fun marcarComoSincronizadoFirestore(
        id: Int,
        uid: String
    )

    @Query("""
    UPDATE recordatorios
    SET firestoreId = :firestoreId
    WHERE id = :id AND ownerUid = :uid
""")
    suspend fun asignarFirestoreId(
        id: Int,
        uid: String,
        firestoreId: String
    )

    @Query("""
    SELECT * FROM recordatorios
    WHERE firestoreId = :firestoreId
    AND ownerUid = :uid
    LIMIT 1
""")
    suspend fun obtenerPorFirestoreId(
        firestoreId: String,
        uid: String
    ): RecordatorioEntity?
}

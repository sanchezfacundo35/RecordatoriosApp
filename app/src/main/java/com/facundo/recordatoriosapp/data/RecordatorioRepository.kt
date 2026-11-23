
package com.facundo.recordatoriosapp.data

import android.util.Log
import com.facundo.recordatoriosapp.model.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

interface RecordatorioRepository {

    fun observarRecordatorios(): Flow<List<Recordatorio>>
    fun obtenerPorId(id: Int): Flow<Recordatorio?>
    fun observarPorEstado(estado: String): Flow<List<Recordatorio>>
    fun observarPorCategoria(categoria: String): Flow<List<Recordatorio>>
    fun observarPorPrioridad(prioridad: String): Flow<List<Recordatorio>>
    fun obtenerPendientesSincronizacion(): Flow<List<Recordatorio>>

    suspend fun marcarComoSincronizado(id: Int, remoteId: Int)

    suspend fun agregar(recordatorio: Recordatorio)
    suspend fun actualizar(recordatorio: Recordatorio)
    suspend fun eliminar(recordatorio: Recordatorio)

    suspend fun sincronizarPendientes()
}

class RecordatorioRepositoryImpl(
    private val dao: RecordatorioDao,
    private val api: RecordatorioApi
) : RecordatorioRepository {

    companion object {
        private const val TAG = "SYNC_RECORDATORIOS"
    }

    override fun observarRecordatorios(): Flow<List<Recordatorio>> {
        return dao.obtenerTodos().map { lista ->
            lista.map { it.toRecordatorio() }
        }
    }

    override fun obtenerPorId(id: Int): Flow<Recordatorio?> {
        return dao.obtenerPorId(id).map { it?.toRecordatorio() }
    }

    override fun observarPorEstado(estado: String): Flow<List<Recordatorio>> {
        return dao.obtenerPorEstado(estado).map { lista ->
            lista.map { it.toRecordatorio() }
        }
    }

    override fun observarPorCategoria(categoria: String): Flow<List<Recordatorio>> {
        return dao.obtenerPorCategoria(categoria).map { lista ->
            lista.map { it.toRecordatorio() }
        }
    }

    override fun observarPorPrioridad(prioridad: String): Flow<List<Recordatorio>> {
        return dao.obtenerPorPrioridad(prioridad).map { lista ->
            lista.map { it.toRecordatorio() }
        }
    }

    override fun obtenerPendientesSincronizacion(): Flow<List<Recordatorio>> {
        return dao.obtenerPendientesSincronizacion().map { lista ->
            lista.map { it.toRecordatorio() }
        }
    }

    override suspend fun marcarComoSincronizado(id: Int, remoteId: Int) {
        dao.marcarComoSincronizado(id, remoteId)
    }

    override suspend fun agregar(recordatorio: Recordatorio) {
        dao.insertar(recordatorio.toEntity())
    }

    override suspend fun actualizar(recordatorio: Recordatorio) {
        val existente = dao.obtenerPorIdDirecto(recordatorio.id)

        val entidadActualizada = recordatorio.toEntity(
            pendienteSincronizacion = true,
            remoteId = existente?.remoteId,
            pendienteEliminar = false
        )

        if (existente == null) {
            dao.insertar(entidadActualizada)
        } else {
            dao.actualizar(entidadActualizada)
        }
    }

    override suspend fun eliminar(recordatorio: Recordatorio) {
        val existente = dao.obtenerPorIdDirecto(recordatorio.id)

        if (existente?.remoteId != null) {
            dao.marcarParaEliminar(recordatorio.id)
        } else if (existente != null) {
            dao.eliminarPorId(recordatorio.id)
        }
    }

    override suspend fun sincronizarPendientes() {

        Log.i(TAG, "Iniciando sincronización...")

        // Primero procesamos las eliminaciones pendientes.
        val pendientesEliminar = dao.obtenerPendientesEliminar().first()

        Log.i(TAG, "Eliminaciones pendientes: ${pendientesEliminar.size}")

        for (entidad in pendientesEliminar) {
            val remoteId = entidad.remoteId ?: continue

            try {
                api.eliminarRecordatorio(remoteId)
                dao.eliminarPorId(entidad.id)

                Log.i(TAG, "Eliminado correctamente. ID local: ${entidad.id}")

            } catch (e: HttpException) {
                if (e.code() == 404) {
                    dao.eliminarPorId(entidad.id)
                    Log.i(TAG, "El registro remoto ya no existía. ID local: ${entidad.id}")
                } else {
                    Log.e(TAG, "Error HTTP al eliminar: ${e.code()}", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar el registro ${entidad.id}", e)
            }
        }

        // Después procesamos las altas y modificaciones.
        val pendientes = dao.obtenerPendientesSincronizacion().first()

        Log.i(TAG, "Altas y modificaciones pendientes: ${pendientes.size}")

        for (entidad in pendientes) {
            try {
                val dto = entidad.toRecordatorio().toDto()

                if (entidad.remoteId == null) {

                    // Registro nuevo: POST.
                    Log.i(TAG, "Enviando nuevo registro: ${entidad.titulo}")

                    val respuesta = api.crearRecordatorio(dto)

                    dao.marcarComoSincronizado(
                        entidad.id,
                        respuesta.id
                    )

                    Log.i(
                        TAG,
                        "POST exitoso. ID local: ${entidad.id}, ID remoto: ${respuesta.id}"
                    )

                } else {

                    // Registro existente: PUT.
                    Log.i(TAG, "Actualizando registro: ${entidad.titulo}")

                    val respuesta = api.actualizarRecordatorio(
                        entidad.remoteId,
                        dto.copy(id = entidad.remoteId)
                    )

                    dao.marcarComoSincronizado(
                        entidad.id,
                        respuesta.id
                    )

                    Log.i(
                        TAG,
                        "PUT exitoso. ID local: ${entidad.id}, ID remoto: ${respuesta.id}"
                    )
                }

            } catch (e: HttpException) {
                Log.e(
                    TAG,
                    "Error HTTP al sincronizar ${entidad.titulo}: ${e.code()}",
                    e
                )
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error al sincronizar ${entidad.titulo}",
                    e
                )
            }
        }

        Log.i(TAG, "Proceso de sincronización finalizado.")
    }
}

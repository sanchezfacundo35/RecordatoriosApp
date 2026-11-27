
package com.facundo.recordatoriosapp.data

import android.util.Log
import com.facundo.recordatoriosapp.model.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
    private val api: RecordatorioApi,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : RecordatorioRepository {

    companion object {
        private const val TAG = "SYNC_RECORDATORIOS"
    }

    private fun obtenerUid(): String {
        return authRepository.usuarioActual?.uid
            ?: throw IllegalStateException(
                "Debés iniciar sesión para acceder a tus recordatorios"
            )
    }

    private suspend fun prepararUsuario(uid: String) {
            }

    private fun RecordatorioEntity.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "titulo" to titulo,
            "fecha" to fecha,
            "descripcion" to descripcion,
            "categoria" to categoria,
            "prioridad" to prioridad,
            "estado" to estado,
            "ubicacion" to ubicacion,
            "ownerUid" to ownerUid
        )
    }

    private fun Map<String, Any?>.toRecordatorioEntity(
        uid: String
    ): RecordatorioEntity {
        return RecordatorioEntity(
            id = 0,
            titulo = this["titulo"] as? String ?: "",
            fecha = this["fecha"] as? String ?: "",
            descripcion = this["descripcion"] as? String,
            categoria = this["categoria"] as? String ?: "PERSONAL",
            prioridad = this["prioridad"] as? String ?: "MEDIA",
            estado = this["estado"] as? String ?: "Pendiente",
            ubicacion = this["ubicacion"] as? String,
            pendienteSincronizacion = false,
            remoteId = null,
            pendienteEliminar = false,
            firestoreId = this["firestoreId"] as? String,
            ownerUid = uid
        )
    }

    private suspend fun recuperarDesdeFirestore(uid: String) {
        val recordatoriosRemotos =
            firestoreRepository.obtenerRecordatorios(uid)

        val idsRemotos = recordatoriosRemotos.mapNotNull {
            it["firestoreId"] as? String
        }.toSet()

        // Recuperar e insertar o actualizar los registros remotos.
        for (datos in recordatoriosRemotos) {
            val firestoreId = datos["firestoreId"] as? String
                ?: continue

            val existente = dao.obtenerPorFirestoreId(
                firestoreId,
                uid
            )

            if (existente?.pendienteSincronizacion == true ||
                existente?.pendienteEliminar == true
            ) {
                continue
            }

            val entidad = datos.toRecordatorioEntity(uid)

            if (existente == null) {
                dao.insertar(entidad)
            } else {
                dao.actualizar(entidad.copy(id = existente.id))
            }
        }

        // Eliminar de Room los registros que ya no existen en Firestore.
        val registrosLocales = dao.obtenerTodos(uid).first()

        for (local in registrosLocales) {
            val firestoreId = local.firestoreId

            if (firestoreId != null &&
                firestoreId !in idsRemotos &&
                !local.pendienteSincronizacion &&
                !local.pendienteEliminar
            ) {
                dao.eliminarPorId(local.id, uid)
            }
        }
    }

    override fun observarRecordatorios(): Flow<List<Recordatorio>> = flow {
        val uid = obtenerUid()
        prepararUsuario(uid)
        emitAll(
            dao.obtenerTodos(uid).map { lista ->
                lista.map { it.toRecordatorio() }
            }
        )
    }

    override fun obtenerPorId(id: Int): Flow<Recordatorio?> = flow {
        val uid = obtenerUid()
        prepararUsuario(uid)
        emitAll(
            dao.obtenerPorId(id, uid).map { it?.toRecordatorio() }
        )
    }

    override fun observarPorEstado(
        estado: String
    ): Flow<List<Recordatorio>> = flow {
        val uid = obtenerUid()
        prepararUsuario(uid)
        emitAll(
            dao.obtenerPorEstado(estado, uid).map { lista ->
                lista.map { it.toRecordatorio() }
            }
        )
    }

    override fun observarPorCategoria(
        categoria: String
    ): Flow<List<Recordatorio>> = flow {
        val uid = obtenerUid()
        prepararUsuario(uid)
        emitAll(
            dao.obtenerPorCategoria(categoria, uid).map { lista ->
                lista.map { it.toRecordatorio() }
            }
        )
    }

    override fun observarPorPrioridad(
        prioridad: String
    ): Flow<List<Recordatorio>> = flow {
        val uid = obtenerUid()
        prepararUsuario(uid)
        emitAll(
            dao.obtenerPorPrioridad(prioridad, uid).map { lista ->
                lista.map { it.toRecordatorio() }
            }
        )
    }

    override fun obtenerPendientesSincronizacion(): Flow<List<Recordatorio>> =
        flow {
            val uid = obtenerUid()
            prepararUsuario(uid)
            emitAll(
                dao.obtenerPendientesSincronizacion(uid).map { lista ->
                    lista.map { it.toRecordatorio() }
                }
            )
        }

    override suspend fun marcarComoSincronizado(
        id: Int,
        remoteId: Int
    ) {
        val uid = obtenerUid()
        dao.marcarComoSincronizado(id, remoteId, uid)
    }

    override suspend fun agregar(recordatorio: Recordatorio) {
        val uid = obtenerUid()
        prepararUsuario(uid)

        val nuevoRecordatorio = recordatorio.copy(
            ownerUid = uid,
            firestoreId = firestoreRepository.generarIdDocumento(uid)
        )

        dao.insertar(nuevoRecordatorio.toEntity())
    }

    override suspend fun actualizar(recordatorio: Recordatorio) {
        val uid = obtenerUid()
        prepararUsuario(uid)

        val existente = dao.obtenerPorIdDirecto(recordatorio.id, uid)

        val registroActualizado = recordatorio.copy(
            ownerUid = uid,
            firestoreId = existente?.firestoreId
                ?: recordatorio.firestoreId
        )

        val entidadActualizada = registroActualizado.toEntity(
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
        val uid = obtenerUid()
        val existente = dao.obtenerPorIdDirecto(recordatorio.id, uid)

        if (existente?.firestoreId != null || existente?.remoteId != null) {
            // Ya existe en la nube: marcar para eliminar después de sincronizar.
            dao.marcarParaEliminar(recordatorio.id, uid)
        } else if (existente != null) {
            // Nunca fue sincronizado: eliminar solamente de Room.
            dao.eliminarPorId(recordatorio.id, uid)
        }
    }

    override suspend fun sincronizarPendientes() {
        val uid = obtenerUid()
        prepararUsuario(uid)

        try {
            recuperarDesdeFirestore(uid)
            Log.i(TAG, "Recuperación desde Firestore completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al recuperar desde Firestore", e)
        }

        Log.i(TAG, "Iniciando sincronización Firestore: $uid")

        // 1. Procesar eliminaciones pendientes
        val pendientesEliminar =
            dao.obtenerPendientesEliminar(uid).first()

        for (entidad in pendientesEliminar) {
            try {
                if (entidad.firestoreId != null) {
                    firestoreRepository.eliminarRecordatorio(
                        uid,
                        entidad.firestoreId
                    )
                } else if (entidad.remoteId != null) {
                    // Compatibilidad con registros de la API anterior
                    api.eliminarRecordatorio(entidad.remoteId)
                }

                dao.eliminarPorId(entidad.id, uid)

                Log.i(TAG, "Registro eliminado: ${entidad.id}")

            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar ${entidad.id}", e)
            }
        }

        // 2. Procesar altas y modificaciones
        val pendientes =
            dao.obtenerPendientesSincronizacion(uid).first()

        for (entidad in pendientes) {
            try {
                // Recuperar o generar un identificador estable
                val firestoreId = entidad.firestoreId
                    ?: firestoreRepository.generarIdDocumento(uid).also {
                        dao.asignarFirestoreId(
                            entidad.id,
                            uid,
                            it
                        )
                    }

                // Guardar o actualizar el documento en Firestore
                firestoreRepository.guardarRecordatorio(
                    uid = uid,
                    firestoreId = firestoreId,
                    datos = entidad.toFirestoreMap()
                )

                // Marcar como sincronizado en Room
                dao.marcarComoSincronizadoFirestore(
                    entidad.id,
                    uid
                )

                Log.i(TAG, "Sincronización exitosa: ${entidad.id}")

            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error al sincronizar ${entidad.titulo}",
                    e
                )
            }
        }

        Log.i(TAG, "Sincronización Firestore finalizada")
    }
}

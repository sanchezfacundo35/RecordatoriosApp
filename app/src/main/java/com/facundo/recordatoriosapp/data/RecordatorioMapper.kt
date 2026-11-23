
package com.facundo.recordatoriosapp.data

import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.EstadoRecordatorio
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.model.Recordatorio

fun Recordatorio.toEntity(
    pendienteSincronizacion: Boolean = true,
    remoteId: Int? = null,
    pendienteEliminar: Boolean = false
): RecordatorioEntity {
    return RecordatorioEntity(
        id = id,
        titulo = titulo,
        fecha = fecha,
        descripcion = descripcion,
        categoria = categoria.name,
        prioridad = prioridad.name,
        estado = when (estado) {
            EstadoRecordatorio.Pendiente -> "Pendiente"
            EstadoRecordatorio.EnProgreso -> "EnProgreso"
            EstadoRecordatorio.Completado -> "Completado"
        },
        ubicacion = ubicacion,
        pendienteSincronizacion = pendienteSincronizacion,
        remoteId = remoteId,
        pendienteEliminar = pendienteEliminar
    )
}

fun RecordatorioEntity.toRecordatorio(): Recordatorio {
    return Recordatorio(
        id = id,
        titulo = titulo,
        fecha = fecha,
        descripcion = descripcion,
        categoria = Categoria.valueOf(categoria),
        prioridad = Prioridad.valueOf(prioridad),
        estado = when (estado) {
            "Pendiente" -> EstadoRecordatorio.Pendiente
            "EnProgreso" -> EstadoRecordatorio.EnProgreso
            "Completado" -> EstadoRecordatorio.Completado
            else -> EstadoRecordatorio.Pendiente
        },
        ubicacion = ubicacion
    )
}

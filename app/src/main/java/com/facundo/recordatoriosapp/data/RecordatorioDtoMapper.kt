
package com.facundo.recordatoriosapp.data

import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.EstadoRecordatorio
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.model.Recordatorio

fun Recordatorio.toDto(): RecordatorioDto {
    return RecordatorioDto(
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
        ownerUid = ownerUid,
        firestoreId = firestoreId
    )
}

fun RecordatorioDto.toRecordatorio(): Recordatorio {
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
        ubicacion = ubicacion,
        ownerUid = ownerUid,
        firestoreId = firestoreId
    )
}

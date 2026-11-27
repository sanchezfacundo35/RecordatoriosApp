
package com.facundo.recordatoriosapp.data

data class RecordatorioDto(
    val id: Int,
    val titulo: String,
    val fecha: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val ubicacion: String?,
    val ownerUid: String? = null,
    val firestoreId: String? = null
)

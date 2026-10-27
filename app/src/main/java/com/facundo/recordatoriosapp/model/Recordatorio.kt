
package com.facundo.recordatoriosapp.model

data class Recordatorio(
    val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val ubicacion: String? = null
)

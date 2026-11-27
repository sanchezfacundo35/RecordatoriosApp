
package com.facundo.recordatoriosapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "recordatorios")
data class RecordatorioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val titulo: String,

    @ColumnInfo(defaultValue = "''")
    val fecha: String,

    val descripcion: String?,

    val categoria: String,

    val prioridad: String,

    val estado: String,

    val ubicacion: String?,

    @ColumnInfo(defaultValue = "1")
    val pendienteSincronizacion: Boolean = true,

    val remoteId: Int? = null,

    @ColumnInfo(defaultValue = "0")
    val pendienteEliminar: Boolean = false,

    val firestoreId: String? = null,

    val ownerUid: String? = null
)

package com.facundo.recordatoriosapp.model


sealed class EstadoRecordatorio {
    data object Pendiente : EstadoRecordatorio()
    data object EnProgreso : EstadoRecordatorio()
    data object Completado : EstadoRecordatorio()
}

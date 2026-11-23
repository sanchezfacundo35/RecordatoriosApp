package com.facundo.recordatoriosapp.model

import java.time.LocalDate

object DatosRecordatorios {

    val recordatorios: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1,
            titulo = "Estudiar Kotlin",
            fecha = LocalDate.now().toString(),
            descripcion = "Repasar clases y colecciones",
            categoria = Categoria.ESTUDIO,
            prioridad = Prioridad.ALTA,
            estado = EstadoRecordatorio.Pendiente
        ),
        Recordatorio(
            id = 2,
            titulo = "Comprar alimento para el perro",
            fecha = LocalDate.now().toString(),
            categoria = Categoria.HOGAR,
            prioridad = Prioridad.MEDIA,
            estado = EstadoRecordatorio.EnProgreso
        ),
        Recordatorio(
            id = 3,
            titulo = "Preparar entrega de Teclab",
            fecha = LocalDate.now().toString(),
            categoria = Categoria.ESTUDIO,
            prioridad = Prioridad.ALTA,
            estado = EstadoRecordatorio.Pendiente
        ),
        Recordatorio(
            id = 4,
            titulo = "Salir a caminar",
            fecha = LocalDate.now().toString(),
            descripcion = "Realizar actividad física",
            categoria = Categoria.SALUD,
            prioridad = Prioridad.BAJA,
            estado = EstadoRecordatorio.Completado
        )
    )

    val categorias: Set<Categoria> =
        recordatorios.map { it.categoria }.toSet()

    val etiquetasCategorias: Map<Categoria, String> = mapOf(
        Categoria.PERSONAL to "Personal",
        Categoria.TRABAJO to "Trabajo",
        Categoria.ESTUDIO to "Estudio",
        Categoria.SALUD to "Salud",
        Categoria.HOGAR to "Hogar"
    )

    fun filtrarPorEstado(
        estado: EstadoRecordatorio
    ): List<Recordatorio> {
        return recordatorios.filter { it.estado == estado }
    }

    fun filtrarPorPrioridad(
        prioridad: Prioridad
    ): List<Recordatorio> {
        return recordatorios.filter { it.prioridad == prioridad }
    }


}

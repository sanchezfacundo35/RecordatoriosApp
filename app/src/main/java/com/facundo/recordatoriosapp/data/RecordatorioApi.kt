package com.facundo.recordatoriosapp.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecordatorioApi {

    @GET("recordatorios")
    suspend fun obtenerRecordatorios(): List<RecordatorioDto>

    @POST("recordatorios")
    suspend fun crearRecordatorio(
        @Body recordatorio: RecordatorioDto
    ): RecordatorioDto

    @PUT("recordatorios/{id}")
    suspend fun actualizarRecordatorio(
        @Path("id") id: Int,
        @Body recordatorio: RecordatorioDto
    ): RecordatorioDto

    @DELETE("recordatorios/{id}")
    suspend fun eliminarRecordatorio(
        @Path("id") id: Int
    )
}
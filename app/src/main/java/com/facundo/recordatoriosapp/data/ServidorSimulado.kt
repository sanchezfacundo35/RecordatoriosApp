
package com.facundo.recordatoriosapp.data

import com.google.gson.Gson
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class ServidorSimulado {

    private val servidor = MockWebServer()
    private val gson = Gson()

    private val recordatoriosRemotos =
        mutableMapOf<Int, RecordatorioDto>()

    private var siguienteId = 1

    fun iniciar() {

        servidor.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {

                val ruta = request.path
                    ?.substringBefore("?")
                    ?: return respuesta(400, """{"error":"Ruta inválida"}""")

                return synchronized(recordatoriosRemotos) {

                    when {

                        request.method == "GET" &&
                                ruta == "/api/recordatorios" -> {

                            respuesta(
                                200,
                                gson.toJson(recordatoriosRemotos.values.toList())
                            )
                        }

                        request.method == "POST" &&
                                ruta == "/api/recordatorios" -> {

                            val recibido = gson.fromJson(
                                request.body.readUtf8(),
                                RecordatorioDto::class.java
                            )

                            val nuevo = recibido.copy(
                                id = siguienteId++
                            )

                            recordatoriosRemotos[nuevo.id] = nuevo

                            respuesta(201, gson.toJson(nuevo))
                        }

                        request.method == "PUT" &&
                                ruta.startsWith("/api/recordatorios/") -> {

                            val id = ruta.substringAfterLast("/")
                                .toIntOrNull()

                            if (id == null || !recordatoriosRemotos.containsKey(id)) {
                                respuesta(404, """{"error":"No encontrado"}""")
                            } else {
                                val recibido = gson.fromJson(
                                    request.body.readUtf8(),
                                    RecordatorioDto::class.java
                                )

                                val actualizado = recibido.copy(id = id)

                                recordatoriosRemotos[id] = actualizado

                                respuesta(200, gson.toJson(actualizado))
                            }
                        }

                        request.method == "DELETE" &&
                                ruta.startsWith("/api/recordatorios/") -> {

                            val id = ruta.substringAfterLast("/")
                                .toIntOrNull()

                            if (id == null || recordatoriosRemotos.remove(id) == null) {
                                respuesta(404, """{"error":"No encontrado"}""")
                            } else {
                                respuesta(200, """{"mensaje":"Eliminado"}""")
                            }
                        }

                        else -> respuesta(404, """{"error":"Ruta no encontrada"}""")
                    }
                }
            }
        }

        servidor.start()
    }

    private fun respuesta(codigo: Int, contenido: String): MockResponse {
        return MockResponse()
            .setResponseCode(codigo)
            .setHeader("Content-Type", "application/json")
            .setBody(contenido)
    }

    fun obtenerUrl(): String {
        return "http://127.0.0.1:${servidor.port}/api/"
    }

    fun detener() {
        servidor.shutdown()
    }
}

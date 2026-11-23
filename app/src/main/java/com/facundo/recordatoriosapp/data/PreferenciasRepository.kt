
package com.facundo.recordatoriosapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "preferencias_recordatorios"
)

data class PreferenciasApp(
    val tema: String = "Claro",
    val mostrarCompletados: Boolean = true,
    val orden: String = "Fecha"
)

class PreferenciasRepository(
    private val context: Context
) {

    private object Claves {
        val TEMA = stringPreferencesKey("tema")
        val MOSTRAR_COMPLETADOS =
            booleanPreferencesKey("mostrar_completados")
        val ORDEN = stringPreferencesKey("orden")
    }

    val preferencias: Flow<PreferenciasApp> =
        context.dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { valores ->
                val temaGuardado = valores[Claves.TEMA]
                val ordenGuardado = valores[Claves.ORDEN]

                PreferenciasApp(
                    tema = if (
                        temaGuardado == "Claro" ||
                        temaGuardado == "Oscuro"
                    ) {
                        temaGuardado
                    } else {
                        "Claro"
                    },
                    mostrarCompletados =
                        valores[Claves.MOSTRAR_COMPLETADOS] ?: true,
                    orden = if (
                        ordenGuardado == "Fecha" ||
                        ordenGuardado == "Prioridad"
                    ) {
                        ordenGuardado
                    } else {
                        "Fecha"
                    }
                )
            }

    suspend fun guardarTema(tema: String) {
        context.dataStore.edit { valores ->
            valores[Claves.TEMA] =
                if (tema == "Oscuro") "Oscuro" else "Claro"
        }
    }

    suspend fun guardarMostrarCompletados(mostrar: Boolean) {
        context.dataStore.edit { valores ->
            valores[Claves.MOSTRAR_COMPLETADOS] = mostrar
        }
    }

    suspend fun guardarOrden(orden: String) {
        context.dataStore.edit { valores ->
            valores[Claves.ORDEN] =
                if (orden == "Prioridad") "Prioridad" else "Fecha"
        }
    }
}

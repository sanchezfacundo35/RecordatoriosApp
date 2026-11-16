package com.facundo.recordatoriosapp.data

import com.facundo.recordatoriosapp.model.DatosRecordatorios
import com.facundo.recordatoriosapp.model.Recordatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface RecordatorioRepository {
    fun observarRecordatorios(): StateFlow<List<Recordatorio>>
    fun obtenerPorId(id: Int): Recordatorio?
    suspend fun agregar(recordatorio: Recordatorio)
}

class RecordatorioRepositoryImpl : RecordatorioRepository {

    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(
        DatosRecordatorios.recordatorios
    )

    override fun observarRecordatorios(): StateFlow<List<Recordatorio>> {
        return _recordatorios.asStateFlow()
    }

    override fun obtenerPorId(id: Int): Recordatorio? {
        return _recordatorios.value.find { it.id == id }
    }

    override suspend fun agregar(recordatorio: Recordatorio) {
        _recordatorios.update { lista ->
            lista + recordatorio
        }
    }
}

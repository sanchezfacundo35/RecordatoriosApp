package com.facundo.recordatoriosapp.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facundo.recordatoriosapp.data.RecordatorioRepository
import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.EstadoRecordatorio
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.model.Recordatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoListaRecordatorios {
    data object Cargando : EstadoListaRecordatorios
    data object Vacio : EstadoListaRecordatorios
    data class Datos(val recordatorios: List<Recordatorio>) : EstadoListaRecordatorios
    data class Error(val mensaje: String) : EstadoListaRecordatorios
}

data class RecordatorioUiState(
    val lista: EstadoListaRecordatorios = EstadoListaRecordatorios.Cargando,
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)

class RecordatorioViewModel(
    private val repository: RecordatorioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordatorioUiState())
    val uiState: StateFlow<RecordatorioUiState> = _uiState.asStateFlow()

    init {
        cargarRecordatorios()
    }

    private fun cargarRecordatorios() {
        viewModelScope.launch {
            try {
                repository.observarRecordatorios().collect { lista ->
                    _uiState.value = _uiState.value.copy(
                        lista = if (lista.isEmpty()) {
                            EstadoListaRecordatorios.Vacio
                        } else {
                            EstadoListaRecordatorios.Datos(lista)
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    lista = EstadoListaRecordatorios.Error(
                        e.message ?: "No se pudieron cargar los recordatorios"
                    )
                )
            }
        }
    }

    fun reintentarCarga() {
        _uiState.value = _uiState.value.copy(
            lista = EstadoListaRecordatorios.Cargando
        )
        cargarRecordatorios()
    }

    fun actualizarTitulo(valor: String) {
        _uiState.value = _uiState.value.copy(
            titulo = valor,
            errorTitulo = null
        )
    }

    fun actualizarDescripcion(valor: String) {
        _uiState.value = _uiState.value.copy(
            descripcion = valor,
            errorDescripcion = null
        )
    }

    fun actualizarCategoria(valor: Categoria) {
        _uiState.value = _uiState.value.copy(categoria = valor)
    }

    fun actualizarPrioridad(valor: Prioridad) {
        _uiState.value = _uiState.value.copy(prioridad = valor)
    }

    fun guardarRecordatorio() {
        val actual = _uiState.value
        val titulo = actual.titulo.trim()

        val errorTitulo = if (titulo.length < 4) {
            "El título debe tener al menos 4 caracteres"
        } else null

        val errorDescripcion = if (actual.descripcion.length > 200) {
            "La descripción no puede superar los 200 caracteres"
        } else null

        if (errorTitulo != null || errorDescripcion != null ||
            actual.categoria == null || actual.prioridad == null
        ) {
            _uiState.value = actual.copy(
                errorTitulo = errorTitulo,
                errorDescripcion = errorDescripcion,
                errorGeneral = if (actual.categoria == null || actual.prioridad == null) {
                    "Seleccioná una categoría y una prioridad"
                } else null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                guardando = true,
                errorGeneral = null
            )

            try {
                val listaActual = repository.observarRecordatorios().value
                val nuevoId = (listaActual.maxOfOrNull { it.id } ?: 0) + 1

                val nuevoRecordatorio = Recordatorio(
                    id = nuevoId,
                    titulo = titulo,
                    descripcion = actual.descripcion.trim().ifBlank { null },
                    categoria = actual.categoria,
                    prioridad = actual.prioridad,
                    estado = EstadoRecordatorio.Pendiente
                )

                repository.agregar(nuevoRecordatorio)

                _uiState.value = _uiState.value.copy(
                    titulo = "",
                    descripcion = "",
                    categoria = null,
                    prioridad = null,
                    guardando = false,
                    guardado = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGeneral = e.message ?: "No se pudo guardar el recordatorio"
                )
            }
        }
    }

    fun limpiarConfirmacion() {
        _uiState.value = _uiState.value.copy(guardado = false)
    }
}

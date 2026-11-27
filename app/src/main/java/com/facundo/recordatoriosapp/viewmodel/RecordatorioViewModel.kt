
package com.facundo.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facundo.recordatoriosapp.data.ConnectivityObserver
import com.facundo.recordatoriosapp.data.PreferenciasApp
import com.facundo.recordatoriosapp.data.PreferenciasRepository
import com.facundo.recordatoriosapp.data.RecordatorioRepository
import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.EstadoRecordatorio
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.model.Recordatorio
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val guardado: Boolean = false,
    val preferencias: PreferenciasApp = PreferenciasApp(),
    val recordatorioEditando: Recordatorio? = null,
    val hayConexion: Boolean = false,
    val filtroEstado: String? = null,
    val filtroCategoria: Categoria? = null,
    val filtroPrioridad: Prioridad? = null
)

class RecordatorioViewModel(
    private val repository: RecordatorioRepository,
    private val preferenciasRepository: PreferenciasRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordatorioUiState())
    val uiState: StateFlow<RecordatorioUiState> = _uiState.asStateFlow()

    private var cargaJob: Job? = null
    private var sesionActiva = false

    init {
        observarPreferencias()
        observarConexion()
    }

    private fun observarPreferencias() {
        viewModelScope.launch {
            preferenciasRepository.preferencias.collect { preferencias ->
                _uiState.value = _uiState.value.copy(
                    preferencias = preferencias
                )
            }
        }
    }

    private fun observarConexion() {
        viewModelScope.launch {
            connectivityObserver.observarConexion().collect { conectado ->

                _uiState.value = _uiState.value.copy(
                    hayConexion = conectado
                )

                android.util.Log.d(
                    "ESTADO_CONEXION",
                    "Conectado: $conectado"
                )

                if (conectado && sesionActiva) {
                    try {
                        repository.sincronizarPendientes()
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "SYNC_RECORDATORIOS",
                            "Error al sincronizar pendientes",
                            e
                        )
                    }
                }
            }
        }
    }

    private fun cargarRecordatorios() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            try {
                val actual = _uiState.value

                val flujo = when {
                    actual.filtroEstado != null ->
                        repository.observarPorEstado(actual.filtroEstado)

                    actual.filtroCategoria != null ->
                        repository.observarPorCategoria(actual.filtroCategoria.name)

                    actual.filtroPrioridad != null ->
                        repository.observarPorPrioridad(actual.filtroPrioridad.name)

                    else ->
                        repository.observarRecordatorios()
                }

                flujo.collect { lista ->
                    _uiState.value = _uiState.value.copy(
                        lista = if (lista.isEmpty()) {
                            EstadoListaRecordatorios.Vacio
                        } else {
                            EstadoListaRecordatorios.Datos(lista)
                        }
                    )
                }

            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(
                        lista = EstadoListaRecordatorios.Error(
                            e.message ?: "No se pudieron cargar los recordatorios"
                        )
                    )
                }
            }
        }
    }

    fun filtrarPorEstado(estado: String?) {
        _uiState.value = _uiState.value.copy(
            filtroEstado = estado,
            filtroCategoria = null,
            filtroPrioridad = null
        )
        cargarRecordatorios()
    }

    fun filtrarPorCategoria(categoria: Categoria?) {
        _uiState.value = _uiState.value.copy(
            filtroEstado = null,
            filtroCategoria = categoria,
            filtroPrioridad = null
        )
        cargarRecordatorios()
    }

    fun filtrarPorPrioridad(prioridad: Prioridad?) {
        _uiState.value = _uiState.value.copy(
            filtroEstado = null,
            filtroCategoria = null,
            filtroPrioridad = prioridad
        )
        cargarRecordatorios()
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(
            filtroEstado = null,
            filtroCategoria = null,
            filtroPrioridad = null
        )
        cargarRecordatorios()
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

    fun iniciarEdicion(recordatorio: Recordatorio) {
        _uiState.value = _uiState.value.copy(
            recordatorioEditando = recordatorio,
            titulo = recordatorio.titulo,
            descripcion = recordatorio.descripcion ?: "",
            categoria = recordatorio.categoria,
            prioridad = recordatorio.prioridad,
            errorTitulo = null,
            errorDescripcion = null,
            errorGeneral = null,
            guardado = false
        )
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

        if (
            errorTitulo != null ||
            errorDescripcion != null ||
            actual.categoria == null ||
            actual.prioridad == null
        ) {
            _uiState.value = actual.copy(
                errorTitulo = errorTitulo,
                errorDescripcion = errorDescripcion,
                errorGeneral = if (
                    actual.categoria == null || actual.prioridad == null
                ) {
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
                val original = actual.recordatorioEditando

                val recordatorio = if (original == null) {
                    Recordatorio(
                        id = 0,
                        titulo = titulo,
                        fecha = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(Date()),
                        descripcion = actual.descripcion.trim().ifBlank { null },
                        categoria = actual.categoria,
                        prioridad = actual.prioridad,
                        estado = EstadoRecordatorio.Pendiente
                    )
                } else {
                    original.copy(
                        titulo = titulo,
                        descripcion = actual.descripcion.trim().ifBlank { null },
                        categoria = actual.categoria,
                        prioridad = actual.prioridad
                    )
                }

                if (original == null) {
                    repository.agregar(recordatorio)
                } else {
                    repository.actualizar(recordatorio)
                }

                // Finalizar el guardado local inmediatamente.
                _uiState.value = _uiState.value.copy(
                    titulo = "",
                    descripcion = "",
                    categoria = null,
                    prioridad = null,
                    recordatorioEditando = null,
                    guardando = false,
                    guardado = true
                )

                // Sincronizar en segundo plano si hay conexión.
                if (_uiState.value.hayConexion) {
                    viewModelScope.launch {
                        try {
                            repository.sincronizarPendientes()
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "SYNC_RECORDATORIOS",
                                "Error al sincronizar pendientes",
                                e
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGeneral = e.message
                        ?: "No se pudo guardar el recordatorio"
                )
            }
        }
    }

    fun limpiarConfirmacion() {
        _uiState.value = _uiState.value.copy(
            guardado = false
        )
    }

    fun cancelarEdicion() {
        _uiState.value = _uiState.value.copy(
            titulo = "",
            descripcion = "",
            categoria = null,
            prioridad = null,
            recordatorioEditando = null,
            errorTitulo = null,
            errorDescripcion = null,
            errorGeneral = null,
            guardando = false,
            guardado = false
        )
    }

    fun guardarTema(tema: String) {
        viewModelScope.launch {
            preferenciasRepository.guardarTema(tema)
        }
    }

    fun guardarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.guardarMostrarCompletados(mostrar)
        }
    }

    fun guardarOrden(orden: String) {
        viewModelScope.launch {
            preferenciasRepository.guardarOrden(orden)
        }
    }

    fun actualizarRecordatorio(
        recordatorio: Recordatorio,
        onResultado: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.actualizar(recordatorio)

                if (_uiState.value.hayConexion) {
                    viewModelScope.launch {
                        try {
                            repository.sincronizarPendientes()
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "SYNC_RECORDATORIOS",
                                "Error al sincronizar actualización",
                                e
                            )
                        }
                    }
                }

                onResultado(null)
            } catch (e: Exception) {
                onResultado(e.message ?: "No se pudo actualizar")
            }
        }
    }

    fun eliminarRecordatorio(
        recordatorio: Recordatorio,
        onResultado: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminar(recordatorio)

                if (_uiState.value.hayConexion) {
                    viewModelScope.launch {
                        try {
                            repository.sincronizarPendientes()
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "SYNC_RECORDATORIOS",
                                "Error al sincronizar eliminación",
                                e
                            )
                        }
                    }
                }

                onResultado(null)
            } catch (e: Exception) {
                onResultado(e.message ?: "No se pudo eliminar")
            }
        }
    }

    fun actualizarSesion(activa: Boolean) {
        if (sesionActiva == activa) return

        sesionActiva = activa

        if (activa) {
            _uiState.value = _uiState.value.copy(
                lista = EstadoListaRecordatorios.Cargando
            )

            cargarRecordatorios()

            if (_uiState.value.hayConexion) {
                viewModelScope.launch {
                    try {
                        repository.sincronizarPendientes()
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "SYNC_RECORDATORIOS",
                            "Error al sincronizar",
                            e
                        )
                    }
                }
            }
        } else {
            cargaJob?.cancel()

            _uiState.value = _uiState.value.copy(
                lista = EstadoListaRecordatorios.Vacio,
                titulo = "",
                descripcion = "",
                categoria = null,
                prioridad = null,
                recordatorioEditando = null,
                guardado = false,
                errorGeneral = null,
                filtroEstado = null,
                filtroCategoria = null,
                filtroPrioridad = null
            )
        }
    }
}

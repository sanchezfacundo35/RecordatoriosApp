
package com.facundo.recordatoriosapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facundo.recordatoriosapp.model.EstadoRecordatorio
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.model.Recordatorio
import com.facundo.recordatoriosapp.viewmodel.EstadoListaRecordatorios
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecordatorios(
    viewModel: RecordatorioViewModel,
    onSeleccionar: (Int) -> Unit,
    onCrear: () -> Unit,
    onPreferencias: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis recordatorios")
                },
                actions = {
                    TextButton(onClick = onPreferencias) {
                        Text("Preferencias")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrear
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            when (val estado = uiState.lista) {

                is EstadoListaRecordatorios.Datos -> {

                    val recordatoriosFiltrados =
                        estado.recordatorios.filter { recordatorio ->
                            uiState.preferencias.mostrarCompletados ||
                                    recordatorio.estado != EstadoRecordatorio.Completado
                        }

                    val recordatoriosOrdenados =
                        recordatoriosFiltrados.sortedBy { it.id }

                    if (recordatoriosOrdenados.isEmpty()) {
                        Text(
                            text = "No hay recordatorios para mostrar.",
                            modifier = Modifier.align(
                                Alignment.CenterHorizontally
                            )
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recordatoriosOrdenados) { recordatorio ->
                                TarjetaRecordatorio(
                                    recordatorio = recordatorio,
                                    onClick = {
                                        onSeleccionar(recordatorio.id)
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {
                    ComponenteEstadoLista(
                        estado = estado,
                        onCrear = onCrear,
                        onReintentar = {
                            viewModel.reintentarCarga()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ComponenteEstadoLista(
    estado: EstadoListaRecordatorios,
    onCrear: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (estado) {

            EstadoListaRecordatorios.Cargando -> {
                Text("Cargando recordatorios...")
            }

            EstadoListaRecordatorios.Vacio -> {
                Text("Todavía no tenés recordatorios.")

                Button(onClick = onCrear) {
                    Text("Crear recordatorio")
                }
            }

            is EstadoListaRecordatorios.Error -> {
                Text("Error: ${estado.mensaje}")

                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }

            is EstadoListaRecordatorios.Datos -> {
                // El estado con datos se representa en la lista.
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(
    recordatorio: Recordatorio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Categoría: ${recordatorio.categoria}")
            Text("Prioridad: ${recordatorio.prioridad}")
            Text("Estado: ${recordatorio.estado}")

            recordatorio.descripcion?.let {
                Text(it)
            }
        }
    }
}

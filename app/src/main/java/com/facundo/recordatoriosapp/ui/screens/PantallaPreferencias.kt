
package com.facundo.recordatoriosapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPreferencias(
    viewModel: RecordatorioViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferencias = uiState.preferencias

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Preferencias")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Tema de la aplicación",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = preferencias.tema == "Claro",
                    onClick = {
                        viewModel.guardarTema("Claro")
                    }
                )
                Text("Claro")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = preferencias.tema == "Oscuro",
                    onClick = {
                        viewModel.guardarTema("Oscuro")
                    }
                )
                Text("Oscuro")
            }

            Text(
                text = "Mostrar recordatorios completados",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mostrar completados")

                Switch(
                    checked = preferencias.mostrarCompletados,
                    onCheckedChange = {
                        viewModel.guardarMostrarCompletados(it)
                    }
                )
            }

            Text(
                text = "Filtrar recordatorios",
                style = MaterialTheme.typography.titleMedium
            )

            FiltroDropdown(
                etiqueta = "Estado",
                seleccionado = uiState.filtroEstado ?: "Todos los estados",
                opciones = listOf(
                    "Todos los estados",
                    "Pendiente",
                    "EnProgreso",
                    "Completado"
                ),
                onSeleccionar = {
                    viewModel.filtrarPorEstado(
                        if (it == "Todos los estados") null else it
                    )
                }
            )

            FiltroDropdown(
                etiqueta = "Categoría",
                seleccionado = uiState.filtroCategoria?.name
                    ?: "Todas las categorías",
                opciones = listOf("Todas las categorías") +
                        Categoria.values().map { it.name },
                onSeleccionar = {
                    viewModel.filtrarPorCategoria(
                        if (it == "Todas las categorías") {
                            null
                        } else {
                            Categoria.valueOf(it)
                        }
                    )
                }
            )

            FiltroDropdown(
                etiqueta = "Prioridad",
                seleccionado = uiState.filtroPrioridad?.name
                    ?: "Todas las prioridades",
                opciones = listOf("Todas las prioridades") +
                        Prioridad.values().map { it.name },
                onSeleccionar = {
                    viewModel.filtrarPorPrioridad(
                        if (it == "Todas las prioridades") {
                            null
                        } else {
                            Prioridad.valueOf(it)
                        }
                    )
                }
            )

            Button(
                onClick = {
                    viewModel.limpiarFiltros()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar filtros")
            }

            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun FiltroDropdown(
    etiqueta: String,
    seleccionado: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit
) {
    var expandido by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(etiqueta)
            },
            trailingIcon = {
                Text(if (expandido) "▲" else "▼")
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Capa transparente que captura el toque
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    expandido = true
                }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
            }
        ) {
            opciones.forEach { opcion ->

                DropdownMenuItem(
                    text = {
                        Text(opcion)
                    },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

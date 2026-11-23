
package com.facundo.recordatoriosapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facundo.recordatoriosapp.model.Categoria
import com.facundo.recordatoriosapp.model.Prioridad
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface


@Composable
fun PantallaCrearRecordatorio(
    viewModel: RecordatorioViewModel,
    onCancelar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarCategorias by remember { mutableStateOf(false) }
    var mostrarPrioridades by remember { mutableStateOf(false) }

    val formularioValido =
        uiState.titulo.trim().length >= 4 &&
                uiState.descripcion.length <= 200 &&
                uiState.categoria != null &&
                uiState.prioridad != null

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = if (uiState.recordatorioEditando != null) {
                    "Editar recordatorio"
                } else {
                    "Crear recordatorio"
                },
                color = MaterialTheme.colorScheme.onBackground
            )

        OutlinedTextField(
            value = uiState.titulo,
            onValueChange = viewModel::actualizarTitulo,
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorTitulo != null,
            supportingText = {
                uiState.errorTitulo?.let {
                    Text(it)
                }
            }
        )

        OutlinedTextField(
            value = uiState.descripcion,
            onValueChange = {
                if (it.length <= 200) {
                    viewModel.actualizarDescripcion(it)
                }
            },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            supportingText = {
                Text("${uiState.descripcion.length}/200 caracteres")
            }
        )

            Text(
                "Categoría",
                color = MaterialTheme.colorScheme.onBackground
            )

        Column {
            TextButton(
                onClick = { mostrarCategorias = true }
            ) {
                Text(
                    uiState.categoria?.toString()
                        ?: "Seleccionar categoría"
                )
            }

            DropdownMenu(
                expanded = mostrarCategorias,
                onDismissRequest = { mostrarCategorias = false }
            ) {
                Categoria.values().forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.toString()) },
                        onClick = {
                            viewModel.actualizarCategoria(categoria)
                            mostrarCategorias = false
                        }
                    )
                }
            }
        }

            Text(
                "Prioridad",
                color = MaterialTheme.colorScheme.onBackground
            )

        Column {
            TextButton(
                onClick = { mostrarPrioridades = true }
            ) {
                Text(
                    uiState.prioridad?.toString()
                        ?: "Seleccionar prioridad"
                )
            }

            DropdownMenu(
                expanded = mostrarPrioridades,
                onDismissRequest = { mostrarPrioridades = false }
            ) {
                Prioridad.values().forEach { prioridad ->
                    DropdownMenuItem(
                        text = { Text(prioridad.toString()) },
                        onClick = {
                            viewModel.actualizarPrioridad(prioridad)
                            mostrarPrioridades = false
                        }
                    )
                }
            }
        }

        uiState.errorGeneral?.let {
            Text(it)
        }

        if (uiState.guardado) {
            Text("¡Recordatorio guardado correctamente!")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = viewModel::guardarRecordatorio,
                enabled = formularioValido && !uiState.guardando,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when {
                        uiState.guardando -> "Guardando..."
                        uiState.recordatorioEditando != null -> "Actualizar"
                        else -> "Guardar"
                    }
                )
            }
        }
    }
}

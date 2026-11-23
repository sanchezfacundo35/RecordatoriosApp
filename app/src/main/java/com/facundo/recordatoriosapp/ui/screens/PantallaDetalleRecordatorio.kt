
package com.facundo.recordatoriosapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.facundo.recordatoriosapp.model.Recordatorio

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleRecordatorio(
    recordatorio: Recordatorio?,
    onEditar: (Recordatorio) -> Unit,
    onEliminar: (Recordatorio) -> Unit,
    onVolver: () -> Unit
) {
    var mostrarConfirmacion by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del recordatorio") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (recordatorio == null) {
                Text(
                    text = "No se encontró el recordatorio.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = recordatorio.titulo,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text("ID: ${recordatorio.id}")
                        Text("Fecha: ${recordatorio.fecha}")
                        Text("Categoría: ${recordatorio.categoria}")
                        Text("Prioridad: ${recordatorio.prioridad}")
                        Text("Estado: ${recordatorio.estado}")

                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = recordatorio.descripcion
                                ?: "Sin descripción"
                        )

                        recordatorio.ubicacion?.let {
                            Text("Ubicación: $it")
                        }
                    }
                }

                Button(
                    onClick = { onEditar(recordatorio) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar")
                }

                Button(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }

                if (mostrarConfirmacion) {
                    AlertDialog(
                        onDismissRequest = {
                            mostrarConfirmacion = false
                        },
                        title = {
                            Text("Eliminar recordatorio")
                        },
                        text = {
                            Text("¿Estás seguro de que querés eliminar este recordatorio?")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onEliminar(recordatorio)
                                    mostrarConfirmacion = false
                                }
                            ) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarConfirmacion = false
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

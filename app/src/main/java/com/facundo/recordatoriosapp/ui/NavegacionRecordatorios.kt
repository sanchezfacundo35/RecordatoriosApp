
package com.facundo.recordatoriosapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.facundo.recordatoriosapp.data.RecordatorioRepository
import com.facundo.recordatoriosapp.ui.screens.PantallaCrearRecordatorio
import com.facundo.recordatoriosapp.ui.screens.PantallaDetalleRecordatorio
import com.facundo.recordatoriosapp.ui.screens.PantallaRecordatorios
import com.facundo.recordatoriosapp.ui.screens.PantallaPreferencias
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel

@Composable
fun NavegacionRecordatorios(
    viewModel: RecordatorioViewModel,
    repository: RecordatorioRepository
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "lista"
    ) {
        composable("lista") {
            PantallaRecordatorios(
                viewModel = viewModel,
                onSeleccionar = { id ->
                    navController.navigate("detalle/$id")
                },
                onCrear = {
                    navController.navigate("crear")
                },
                onPreferencias = {
                    navController.navigate("preferencias")
                }
            )
        }

        composable("crear") {
            LaunchedEffect(uiState.guardado) {
                if (uiState.guardado) {
                    viewModel.limpiarConfirmacion()
                    navController.popBackStack()
                }
            }

            PantallaCrearRecordatorio(
                viewModel = viewModel,
                onCancelar = {
                    viewModel.cancelarEdicion()
                    navController.popBackStack()
                }
            )
        }

        composable("preferencias") {
            PantallaPreferencias(
                viewModel = viewModel,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detalle/{recordatorioId}",
            arguments = listOf(
                navArgument("recordatorioId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.getInt("recordatorioId")

            var recordatorio by remember(id) {
                mutableStateOf<com.facundo.recordatoriosapp.model.Recordatorio?>(null)
            }

            LaunchedEffect(id) {
                if (id != null) {
                    repository.obtenerPorId(id).collect { resultado ->
                        recordatorio = resultado
                    }
                }
            }

            PantallaDetalleRecordatorio(
                recordatorio = recordatorio,

                onEditar = { seleccionado ->
                    viewModel.iniciarEdicion(seleccionado)
                    navController.navigate("crear")
                },

                onEliminar = { seleccionado ->
                    viewModel.eliminarRecordatorio(seleccionado) { error ->
                        if (error == null) {
                            navController.popBackStack()
                        }
                    }
                },

                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}

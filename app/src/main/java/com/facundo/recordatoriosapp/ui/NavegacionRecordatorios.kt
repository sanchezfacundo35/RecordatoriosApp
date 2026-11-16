
package com.facundo.recordatoriosapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

            val recordatorio = id?.let {
                repository.obtenerPorId(it)
            }

            PantallaDetalleRecordatorio(
                recordatorio = recordatorio,
                onVolver = {
                    navController.popBackStack()
                }
            )
        }
    }
}

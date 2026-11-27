
package com.facundo.recordatoriosapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.facundo.recordatoriosapp.data.RecordatorioRepository
import com.facundo.recordatoriosapp.model.Recordatorio
import com.facundo.recordatoriosapp.ui.screens.PantallaCrearRecordatorio
import com.facundo.recordatoriosapp.ui.screens.PantallaDetalleRecordatorio
import com.facundo.recordatoriosapp.ui.screens.PantallaLogin
import com.facundo.recordatoriosapp.ui.screens.PantallaRegistro
import com.facundo.recordatoriosapp.ui.screens.PantallaRecordatorios
import com.facundo.recordatoriosapp.ui.screens.PantallaPreferencias
import com.facundo.recordatoriosapp.viewmodel.AuthViewModel
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel

@Composable
fun NavegacionRecordatorios(
    viewModel: RecordatorioViewModel,
    repository: RecordatorioRepository,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.autenticado) {
        viewModel.actualizarSesion(authState.autenticado)
    }

    LaunchedEffect(
        authState.verificandoSesion,
        authState.autenticado
    ) {
        if (!authState.verificandoSesion) {
            val destino = if (authState.autenticado) {
                "lista"
            } else {
                "login"
            }

            navController.navigate(destino) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "verificando"
    ) {

        composable("verificando") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        composable("login") {
            PantallaLogin(
                uiState = authState,
                onIniciarSesion = { email, password ->
                    authViewModel.iniciarSesion(email, password)
                },
                onIrARegistro = {
                    navController.navigate("registro")
                }
            )
        }

        composable("registro") {
            PantallaRegistro(
                uiState = authState,
                onRegistrar = { email, password ->
                    authViewModel.registrar(email, password)
                },
                onVolverLogin = {
                    authViewModel.limpiarError()
                    navController.popBackStack()
                }
            )
        }

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

        // PANTALLA DE PREFERENCIAS
        composable("preferencias") {
            PantallaPreferencias(
                viewModel = viewModel,
                onVolver = {
                    navController.popBackStack()
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                }
            )
        }

        // PANTALLA DE DETALLE
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
                mutableStateOf<Recordatorio?>(null)
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


package com.facundo.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facundo.recordatoriosapp.data.AuthRepository
import com.facundo.recordatoriosapp.data.ConnectivityObserver
import com.facundo.recordatoriosapp.data.PreferenciasApp
import com.facundo.recordatoriosapp.data.PreferenciasRepository
import com.facundo.recordatoriosapp.data.RecordatorioDatabase
import com.facundo.recordatoriosapp.data.RecordatorioRepositoryImpl
import com.facundo.recordatoriosapp.data.RetrofitClient
import com.facundo.recordatoriosapp.data.ServidorSimulado
import com.facundo.recordatoriosapp.ui.NavegacionRecordatorios
import com.facundo.recordatoriosapp.ui.theme.RecordatoriosAppTheme
import com.facundo.recordatoriosapp.viewmodel.AuthViewModel
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.facundo.recordatoriosapp.data.FirestoreRepository

class MainActivity : ComponentActivity() {

    private lateinit var servidorSimulado: ServidorSimulado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        servidorSimulado = ServidorSimulado()

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                servidorSimulado.iniciar()
            }

            setContent {

                val preferenciasRepository = remember {
                    PreferenciasRepository(applicationContext)
                }

                val preferencias by preferenciasRepository.preferencias
                    .collectAsState(initial = PreferenciasApp())

                RecordatoriosAppTheme(
                    darkTheme = preferencias.tema == "Oscuro"
                ) {

                    val database = remember {
                        RecordatorioDatabase.obtenerInstancia(
                            applicationContext
                        )
                    }

                    val api = remember {
                        RetrofitClient.crearApi(
                            servidorSimulado.obtenerUrl()
                        )
                    }

                    val authRepository = remember {
                        AuthRepository()
                    }

                    val firestoreRepository = remember {
                        FirestoreRepository()
                    }

                    val authViewModel: AuthViewModel = viewModel(
                        factory = remember(authRepository) {
                            AuthViewModelFactory(authRepository)
                        }
                    )

                    val repository = remember(
                        database,
                        api,
                        authRepository,
                        firestoreRepository
                    ) {
                        RecordatorioRepositoryImpl(
                            dao = database.recordatorioDao(),
                            api = api,
                            authRepository = authRepository,
                            firestoreRepository = firestoreRepository
                        )
                    }

                    val connectivityObserver = remember {
                        ConnectivityObserver(applicationContext)
                    }

                    val factory = remember(
                        repository,
                        preferenciasRepository,
                        connectivityObserver
                    ) {
                        RecordatorioViewModelFactory(
                            repository,
                            preferenciasRepository,
                            connectivityObserver
                        )
                    }

                    val recordatorioViewModel: RecordatorioViewModel =
                        viewModel(factory = factory)

                    NavegacionRecordatorios(
                        viewModel = recordatorioViewModel,
                        repository = repository,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        if (::servidorSimulado.isInitialized) {
            servidorSimulado.detener()
        }

        super.onDestroy()
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}

class RecordatorioViewModelFactory(
    private val repository: RecordatorioRepositoryImpl,
    private val preferenciasRepository: PreferenciasRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(RecordatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")

            return RecordatorioViewModel(
                repository,
                preferenciasRepository,
                connectivityObserver
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}

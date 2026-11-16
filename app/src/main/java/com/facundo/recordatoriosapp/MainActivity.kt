
package com.facundo.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.facundo.recordatoriosapp.data.RecordatorioRepositoryImpl
import com.facundo.recordatoriosapp.ui.NavegacionRecordatorios
import com.facundo.recordatoriosapp.ui.theme.RecordatoriosAppTheme
import com.facundo.recordatoriosapp.viewmodel.RecordatorioViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RecordatoriosAppTheme {

                val repository = remember {
                    RecordatorioRepositoryImpl()
                }

                val factory = remember(repository) {
                    RecordatorioViewModelFactory(repository)
                }

                val viewModel: RecordatorioViewModel = viewModel(
                    factory = factory
                )

                NavegacionRecordatorios(
                    viewModel = viewModel,
                    repository = repository
                )
            }
        }
    }
}

class RecordatorioViewModelFactory(
    private val repository: RecordatorioRepositoryImpl
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(RecordatorioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordatorioViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}

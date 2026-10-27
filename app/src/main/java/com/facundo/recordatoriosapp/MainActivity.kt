
package com.facundo.recordatoriosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.facundo.recordatoriosapp.ui.theme.RecordatoriosAppTheme
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.facundo.recordatoriosapp.model.DatosRecordatorios
import com.facundo.recordatoriosapp.model.Recordatorio
import com.facundo.recordatoriosapp.model.EstadoRecordatorio


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RecordatoriosAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaRecordatorios(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun PantallaRecordatorios(modifier: Modifier = Modifier) {
    val recordatorios = DatosRecordatorios.recordatorios

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Mis recordatorios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Tu día, organizado en un solo lugar.",
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            LazyColumn {
                items(recordatorios) { recordatorio ->
                    TarjetaRecordatorio(recordatorio)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Categoría: ${
                    DatosRecordatorios.etiquetasCategorias[recordatorio.categoria]
                }",
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(text = "Prioridad: ${recordatorio.prioridad}")

            Text(
                text = "Estado: ${
                    when (recordatorio.estado) {
                        EstadoRecordatorio.Pendiente -> "Pendiente"
                        EstadoRecordatorio.EnProgreso -> "En progreso"
                        EstadoRecordatorio.Completado -> "Completado"
                    }
                }"
            )

            recordatorio.descripcion?.let { descripcion ->
                Text(
                    text = descripcion,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

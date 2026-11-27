
package com.facundo.recordatoriosapp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private fun coleccionRecordatorios(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection("recordatorios")


    fun generarIdDocumento(uid: String): String {
        return coleccionRecordatorios(uid)
            .document()
            .id
    }

    suspend fun guardarRecordatorio(
        uid: String,
        firestoreId: String,
        datos: Map<String, Any?>
    ) {
        coleccionRecordatorios(uid)
            .document(firestoreId)
            .set(datos)
            .await()
    }

    suspend fun obtenerRecordatorios(
        uid: String
    ): List<Map<String, Any?>> {

        val resultado = coleccionRecordatorios(uid)
            .get()
            .await()

        return resultado.documents.mapNotNull { documento ->
            documento.data?.toMutableMap()?.apply {
                put("firestoreId", documento.id)
            }
        }
    }

    suspend fun eliminarRecordatorio(
        uid: String,
        firestoreId: String
    ) {
        coleccionRecordatorios(uid)
            .document(firestoreId)
            .delete()
            .await()
    }
}

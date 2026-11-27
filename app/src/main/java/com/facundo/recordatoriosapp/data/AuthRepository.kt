
package com.facundo.recordatoriosapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    fun observarUsuario(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun registrar(
        email: String,
        password: String
    ): FirebaseUser {
        val resultado = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        return resultado.user
            ?: throw Exception("No se pudo obtener el usuario registrado")
    }

    suspend fun iniciarSesion(
        email: String,
        password: String
    ): FirebaseUser {
        val resultado = auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        return resultado.user
            ?: throw Exception("No se pudo obtener el usuario")
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}

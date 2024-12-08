package com.example.takecareofyourdailyroutine

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import android.util.Log
import com.google.firebase.firestore.FieldValue

// Funkcja do hashowania hasła (SHA-256)
fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun saveUserToFirestore(email: String, name: String, password: String) {
    val db = FirebaseFirestore.getInstance()

    val hashedPassword = hashPassword(password)
    val user = hashMapOf(
        "email" to email,
        "name" to name,
        "password" to hashedPassword
    )

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null) {
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "Użytkownik zapisany w bazie danych!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Błąd podczas zapisywania użytkownika", e)
            }
    } else {
        Log.e("Firestore", "Nie udało się pobrać UID użytkownika!")
    }
}

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    var isCompleted: Boolean
)

fun addTaskToFirestore(task: Task) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null) {
        val taskMap = hashMapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to task.isCompleted
        )

        // Użycie FieldValue.arrayUnion do dodania zadania do listy
        db.collection("users").document(uid)
            .update("tasks", FieldValue.arrayUnion(taskMap))
            .addOnSuccessListener {
                Log.d("Firestore", "Zadanie dodane do bazy danych!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Błąd podczas dodawania zadania", e)
            }
    } else {
        Log.e("Firestore", "Nie udało się pobrać UID użytkownika!")
    }
}
// Funkcja do usuwania zadania z Firestore
fun removeTaskFromFirestore(task: Task) {
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null) {
        val taskMap = hashMapOf(
            "id" to task.id,
            "title" to task.title,
            "description" to task.description,
            "isCompleted" to task.isCompleted
        )

        db.collection("users").document(uid)
            .update("tasks", FieldValue.arrayRemove(taskMap))  // Usuwamy konkretne zadanie
            .addOnSuccessListener {
                Log.d("Firestore", "Zadanie zostało usunięte.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Błąd podczas usuwania zadania", e)
            }
    } else {
        Log.e("Firestore", "Nie udało się pobrać UID użytkownika!")
    }
}



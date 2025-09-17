package com.team21.myapplication.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.team21.myapplication.ui.theme.AppTheme
import android.util.Log
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class MainActivity : ComponentActivity() {

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        probarConexionFirestore()
    }

    private fun probarConexionFirestore(){
        val datosDePrueba = hashMapOf(
            "name" to "Kotlin user"
        )

        db.collection("StudentUser")
            .add(datosDePrueba)
            .addOnSuccessListener { documentReference ->
                val idDocumento = documentReference.id
                Log.d("FirestoreTest", "Documento añadido con ID: $idDocumento")

                leerDatosDePrueba(idDocumento)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore Test", "Errro al añadir al documento", exception)
            }

    }

    private fun leerDatosDePrueba(idDocumento: String) {
        db.collection("StudentUser").document(idDocumento)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("FirestoreTest", "✅ Datos leídos correctamente: ${document.data}")
                } else {
                    Log.d("FirestoreTest", "⚠️ No se encontró el documento.")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreTest", "❌ Error al leer el documento.", exception)
            }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        Greeting("Android")
    }
}
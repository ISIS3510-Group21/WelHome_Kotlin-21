package com.team21.myapplication.ui.createPostView

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.team21.myapplication.ui.theme.AppTheme

/**
 * Activity para crear un nuevo post de vivienda
 *
 * FUNCIÓN:
 * Esta Activity sirve como contenedor para la vista Composable
 * y maneja la navegación cuando el post se crea exitosamente
 */
class CreatePostActivity : ComponentActivity() {

    // Obtener instancia del ViewModel usando by viewModels()
    // Esto asegura que el ViewModel sobreviva a cambios de configuración
    private val viewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el contenido con Compose
        setContent {
            // Aplicar el tema de tu aplicación
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Renderizar la vista de creación de post
                    CreatePostScreenLayout(
                        viewModel = viewModel,
                        onPostCreated = {
                            // Callback cuando el post se crea exitosamente
                            Toast.makeText(
                                this@CreatePostActivity,
                                "Post created successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Finalizar la Activity y volver a la anterior
                            finish()
                        },
                        onNavigateBack = {
                            // Callback para el botón de retroceso
                            finish()
                        }
                    )
                }
            }
        }
    }
}
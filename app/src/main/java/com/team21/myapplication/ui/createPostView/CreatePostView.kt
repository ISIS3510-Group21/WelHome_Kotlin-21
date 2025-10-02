package com.team21.myapplication.ui.createPostView

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.BorderButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.BlueCallToAction
import kotlinx.coroutines.launch
import java.io.File

/**
 * Vista principal para crear un nuevo post
 *
 * ARQUITECTURA:
 * Esta vista observa los estados del ViewModel y reacciona a los cambios
 *
 * @param viewModel ViewModel que maneja la lógica de negocio
 * @param onPostCreated Callback que se ejecuta cuando el post se crea exitosamente
 * @param onNavigateBack Callback para volver atrás
 */
@Composable
fun CreatePostScreenLayout(
    viewModel: CreatePostViewModel = viewModel(),
    onPostCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    // --- ESTADOS DEL VIEWMODEL ---
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val price by viewModel.price.collectAsState()
    val address by viewModel.address.collectAsState()
    val mainPhoto by viewModel.mainPhoto.collectAsState()
    val additionalPhotos by viewModel.additionalPhotos.collectAsState()
    val createPostState by viewModel.createPostState.collectAsState()

    // --- ESTADOS LOCALES ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // --- LAUNCHER PARA SELECCIONAR FOTO PRINCIPAL ---
    val mainPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setMainPhoto(it) }
    }

    // --- LAUNCHER PARA SELECCIONAR FOTOS ADICIONALES (MÚLTIPLES) ---
    val additionalPhotosPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addAdditionalPhotos(uris)
        }
    }

    // --- LAUNCHER PARA TOMAR FOTO CON LA CÁMARA ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // Si la foto principal está vacía, usar esta como principal
            if (mainPhoto == null) {
                viewModel.setMainPhoto(photoUri!!)
            } else {
                // Si ya hay foto principal, agregar como adicional
                viewModel.addAdditionalPhotos(listOf(photoUri!!))
            }
        }
    }

    // --- LAUNCHER PARA PERMISOS DE CÁMARA ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, tomar foto
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(photoUri!!)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permiso de cámara denegado")
            }
        }
    }

    // --- FUNCIÓN PARA VERIFICAR Y SOLICITAR PERMISOS DE CÁMARA ---
    fun checkCameraPermissionAndTakePhoto() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido
                val file = java.io.File.createTempFile("camera_", ".jpg", context.cacheDir)

                photoUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                cameraLauncher.launch(photoUri!!)
            }
            else -> {
                // Solicitar permiso
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    // --- EFECTOS SECUNDARIOS ---
    // LaunchedEffect se ejecuta cuando createPostState cambia
    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is CreatePostState.Success -> {
                // Mostrar mensaje de éxito
                snackbarHostState.showSnackbar("¡Post creado exitosamente!")
                // Limpiar formulario
                viewModel.clearForm()
                // Ejecutar callback de navegación
                onPostCreated()
            }
            is CreatePostState.Error -> {
                // Mostrar mensaje de error
                snackbarHostState.showSnackbar(state.message)
                // Resetear estado después de mostrar error
                viewModel.resetState()
            }
            else -> { /* No hacer nada para Idle y Loading */ }
        }
    }

    // --- UI PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.ArrowDropDown,
                    contentDescription = "Back",
                    tint = BlueCallToAction,
                    modifier = Modifier
                        .rotate(90f)
                        .size(32.dp)
                )
                BlackText(
                    text = "Create a new post",
                    size = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: INFORMACIÓN BÁSICA ---
            BlackText(
                text = "Basic Information",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // CAMPO: TÍTULO
            BlackText(
                text = "Title",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: Cozy Home",
                value = title,
                onValueChange = { viewModel.updateTitle(it) } // Actualiza el ViewModel
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO: PRECIO
            BlackText(
                text = "Rent (per month)",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: 950000",
                value = price,
                onValueChange = { viewModel.updatePrice(it) } // Filtra y actualiza
            )
            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO: DESCRIPCIÓN
            BlackText(
                text = "Description",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: The neighborhood is pretty quiet and nice",
                height = 100.dp,
                value = description,
                onValueChange = { viewModel.updateDescription(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- TIPO DE VIVIENDA (Por ahora solo visual) ---
            BlackText(
                text = "Type of housing",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Observar el tag seleccionado
            val selectedTagId by viewModel.selectedTagId.collectAsState()

            // Definir los IDs de los tags según tu Firebase
            val housingTags = listOf(
                "HousingTag1" to "House" to AppIcons.Home,
                "HousingTag2" to "Apartment" to AppIcons.Apartments,
                "HousingTag3" to "Cabin" to AppIcons.Cabins,
                "HousingTag5" to "Residence" to AppIcons.Houses
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                housingTags.take(2).forEach { (tagInfo, icon) ->
                    val (tagId, tagName) = tagInfo
                    val isSelected = selectedTagId == tagId

                    BorderButton(
                        modifier = Modifier.weight(1f),
                        text = tagName,
                        onClick = { viewModel.selectTag(tagId) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tagName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else BlueCallToAction
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                housingTags.drop(2).forEach { (tagInfo, icon) ->
                    val (tagId, tagName) = tagInfo
                    val isSelected = selectedTagId == tagId

                    BorderButton(
                        modifier = Modifier.weight(1f),
                        text = tagName,
                        onClick = { viewModel.selectTag(tagId) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tagName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else BlueCallToAction
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- AMENIDADES (Por ahora solo visual) ---
            BlackText(
                text = "Amenities",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlueButton(
                    modifier = Modifier
                        .height(40.dp)
                        .width(85.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    text = "Add",
                    onClick = {}
                )
                HorizontalCarousel(
                    items = listOf("5 Beds", "2 Baths", "70 m2"),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 8.dp,
                    snapToItems = false
                ) { label ->
                    GrayButton(text = label, onClick = {})
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- COMPAÑEROS DE CUARTO (Por ahora solo visual) ---
            BlackText(
                text = "Roommates' Profile",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlueButton(
                    modifier = Modifier
                        .height(40.dp)
                        .width(85.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    text = "Add",
                    onClick = {}
                )
                HorizontalCarousel(
                    items = listOf("Joan", "Majo", "Arturo Jose"),
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalSpacing = 8.dp,
                    snapToItems = false
                ) { label ->
                    GrayButton(text = label, onClick = {})
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN DE FOTOS (ACTUALIZADA Y FUNCIONAL) ---
            BlackText(text = "Add Photos", size = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN 1: Agregar foto principal
            BorderButton(
                text = if (mainPhoto == null) "Add main photo" else "Change main photo",
                onClick = { mainPhotoPickerLauncher.launch("image/*") },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = "Add main photo",
                        tint = BlueCallToAction
                    )
                }
            )

            // Vista previa de la foto principal
            if (mainPhoto != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Foto Principal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            AsyncImage(
                                model = mainPhoto,
                                contentDescription = "Main photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Botón para eliminar la foto principal
                            IconButton(
                                onClick = { viewModel.removeMainPhoto() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = AppIcons.ArrowDropDown,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN 2: Tomar foto con la cámara
            BorderButton(
                text = "Take new photos",
                onClick = { checkCameraPermissionAndTakePhoto() },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.CameraAlt,
                        contentDescription = "Take photos",
                        tint = BlueCallToAction
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BOTÓN 3: Agregar fotos adicionales
            BorderButton(
                text = "Add additional photos (${additionalPhotos.size}/9)",
                onClick = { additionalPhotosPickerLauncher.launch("image/*") },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Queue,
                        contentDescription = "Add photos",
                        tint = BlueCallToAction
                    )
                },
                enabled = additionalPhotos.size < 9
            )

            // Vista previa de fotos adicionales
            if (additionalPhotos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Fotos Adicionales (${additionalPhotos.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(additionalPhotos) { uri ->
                                Box {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Additional photo",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Botón para eliminar foto adicional
                                    IconButton(
                                        onClick = { viewModel.removeAdditionalPhoto(uri) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = AppIcons.ArrowDropDown,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // CAMPO: DIRECCIÓN
            BlackText(
                text = "Address",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: Cr 9 # XX -XX",
                value = address,
                onValueChange = { viewModel.updateAddress(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // MAPA (Por ahora solo visual)
            Image(
                painter = painterResource(id = R.drawable.simple_map),
                contentDescription = "Map",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN DE CREAR ---
            BlueButton(
                text = if (createPostState is CreatePostState.Loading) "Creating..." else "Create",
                onClick = {
                    // Llamar a la función del ViewModel para crear el post
                    viewModel.createPost()
                },
                enabled = createPostState !is CreatePostState.Loading // Deshabilitar si está cargando
            )

            // Mostrar indicador de carga si está en estado Loading
            if (createPostState is CreatePostState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // --- SNACKBAR HOST (MENSAJES) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenLayoutPreview() {
    CreatePostScreenLayout()
}
package com.team21.myapplication.ui.createPostView

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.buttons.BorderButton
import com.team21.myapplication.ui.components.buttons.GrayButton
import com.team21.myapplication.ui.components.carousel.HorizontalCarousel
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.team21.myapplication.utils.NetworkMonitor
import androidx.compose.material3.Snackbar
import androidx.compose.ui.graphics.Color


import java.io.File
import androidx.compose.ui.window.Dialog
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.createPostView.state.CreatePostOperationState

/**
 * Main view to create a new post
 *
 * ARCHITECTURE:
 * This view observes the ViewModel states and reacts to changes
 *
 * @param viewModel ViewModel that handles the business logic
 * @param onPostCreated Callback that is executed when the post is successfully created
 * @param onNavigateBack Callback to go back
 */
@Composable
fun CreatePostScreenLayout(
    viewModel: CreatePostViewModel = viewModel(),
    onPostCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onOpenAmenities: () -> Unit = {}
) {
    val context = LocalContext.current
    // --- VIEWMODEL STATES ---
    // Observar un solo estado unificado
    val uiState by viewModel.uiState.collectAsState()

    // observar red
    val networkMonitor = remember { NetworkMonitor.get(context) }
    val isOnline by networkMonitor.isOnline.collectAsState()

    // --- LOCAL STATES ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var photoUri by remember { mutableStateOf<Uri?>(null) }


    // --- LAUNCHER FOR SELECTING MAIN PHOTO ---
    val mainPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setMainPhoto(it) }
    }

    // --- LAUNCHER FOR SELECTING ADDITIONAL PHOTOS (MULTIPLE) ---
    val additionalPhotosPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addAdditionalPhotos(uris)
        }
    }

    // --- LAUNCHER FOR TAKING A PHOTO WITH THE CAMERA ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            // If main photo is empty, use this as main
            if (uiState.mainPhoto == null) {
                viewModel.setMainPhoto(photoUri!!)
            } else {
                // If there is already a main photo, add as additional
                viewModel.addAdditionalPhotos(listOf(photoUri!!))
            }
        }
    }

    // --- LAUNCHER FOR CAMERA PERMISSIONS ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, take photo
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            cameraLauncher.launch(photoUri!!)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission denied")
            }
        }
    }

    // --- FUNCTION TO CHECK AND REQUEST CAMERA PERMISSIONS ---
    fun checkCameraPermissionAndTakePhoto() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                val file = File.createTempFile("camera_", ".jpg", context.cacheDir)

                photoUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                cameraLauncher.launch(photoUri!!)
            }
            else -> {
                // Request permission
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    // --- SIDE EFFECTS ---
    // LaunchedEffect runs when createPostState changes
    LaunchedEffect(uiState.operationState) {
        when (val state = uiState.operationState) {
            is CreatePostOperationState.Success -> {
                when (state.postId) {
                    "DRAFT_SAVED" -> {
                        snackbarHostState.showSnackbar(
                            "Offline: saved as a draft. It will be uploaded automatically."
                        )
                        viewModel.clearForm()
                        onNavigateBack()
                    }
                    else -> {
                        snackbarHostState.showSnackbar("Post created successfully!")
                        viewModel.clearForm()
                        onPostCreated()
                    }
                }
            }

            is CreatePostOperationState.Error -> {
                // Show error message
                snackbarHostState.showSnackbar(state.message)
                // Reset state after showing error
                viewModel.resetState()
            }
            else -> { /* Do nothing for Idle and Loading */ }
        }
    }

    // --- MAIN UI ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ConnectivityBanner(
                visible = !isOnline,
                message = "No internet connection. Your post will be saved as a draft",
                position = BannerPosition.Top
            )
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = AppIcons.GoBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(32.dp),
                        tint = BlueCallToAction
                    )
                }
                BlackText(
                    text = "Create a new post",
                    size = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION: BASIC INFORMATION ---
            BlackText(
                text = "Basic Information",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // FIELD: TITLE
            BlackText(
                text = "Title",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: Cozy Home",
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) } // Updates the ViewModel
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- HOUSING TYPE (tag)
            BlackText(
                text = "Type of housing",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))


            // Define the tag IDs according to Firebase
            val housingTags = listOf(
                "House" to "House" to AppIcons.Home,
                "Apartment" to "Apartment" to AppIcons.Apartments,
                "Cabin" to "Cabin" to AppIcons.Cabins,
                "Residence" to "Residence" to AppIcons.Houses
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                housingTags.take(2).forEach { (tagInfo, icon) ->
                    val (tagId, tagName) = tagInfo
                    val isSelected = uiState.selectedTagId == tagId

                    BorderButton(
                        modifier = Modifier.weight(1f),
                        text = tagName,
                        onClick = { viewModel.selectTag(tagId) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tagName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
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
                    val isSelected = uiState.selectedTagId == tagId

                    BorderButton(
                        modifier = Modifier.weight(1f),
                        text = tagName,
                        onClick = { viewModel.selectTag(tagId) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tagName,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- AMENITIES
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
                    onClick = {onOpenAmenities() }
                )
                if (uiState.selectedAmenities.isNotEmpty()) {
                    HorizontalCarousel(
                        items = uiState.selectedAmenities.map { it.name },
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        horizontalSpacing = 8.dp,
                        snapToItems = false
                    ) { label ->
                        GrayButton(text = label, onClick = {})
                    }
                } else {
                    BlackText(
                        text = "No amenities selected"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Roommates ---
//            BlackText(
//                text = "Roommates' Profile",
//                size = 16.sp,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                BlueButton(
//                    modifier = Modifier
//                        .height(40.dp)
//                        .width(85.dp)
//                        .clip(RoundedCornerShape(20.dp)),
//                    text = "Add",
//                    onClick = {}
//                )
//                HorizontalCarousel(
//                    items = listOf("Joan", "Majo", "Arturo Jose"),
//                    contentPadding = PaddingValues(horizontal = 0.dp),
//                    horizontalSpacing = 8.dp,
//                    snapToItems = false
//                ) { label ->
//                    GrayButton(text = label, onClick = {})
//                }
//            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- PHOTOS SECTION (UPDATED AND FUNCTIONAL) ---
            BlackText(text = "Add Photos", size = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // BUTTON 1: Add main photo
            BorderButton(
                text = if (uiState.mainPhoto == null) "Add main photo" else "Change main photo",
                onClick = { mainPhotoPickerLauncher.launch("image/*") },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = "Add main photo",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            )

            // Preview of the main photo
            if (uiState.mainPhoto != null) {
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
                            text = "Main Photo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            AsyncImage(
                                model = uiState.mainPhoto,
                                contentDescription = "Main photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Button to remove the main photo
                            IconButton(
                                onClick = { viewModel.removeMainPhoto() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(20.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = AppIcons.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BUTTON 2: Take photo with the camera
            BorderButton(
                text = "Take new photos",
                onClick = { checkCameraPermissionAndTakePhoto() },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.CameraAlt,
                        contentDescription = "Take photos",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // BUTTON 3: Add additional photos
            BorderButton(
                text = "Add additional photos (${uiState.additionalPhotos.size}/9)",
                onClick = { additionalPhotosPickerLauncher.launch("image/*") },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Queue,
                        contentDescription = "Add photos",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                enabled = uiState.additionalPhotos.size < 9
            )

            // Preview of additional photos
            if (uiState.additionalPhotos.isNotEmpty()) {
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
                            text = "Additional Photos (${uiState.additionalPhotos.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.additionalPhotos) { uri ->
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
                                    // Button to remove additional photo
                                    IconButton(
                                        onClick = { viewModel.removeAdditionalPhoto(uri) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(18.dp)
                                            .background(
                                                MaterialTheme.colorScheme.error,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = AppIcons.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // FIELD: ADDRESS
            BlackText(
                text = "Address",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: Cr 9 # XX -XX",
                value = uiState.address,
                onValueChange = { viewModel.updateAddress(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FIELD: PRICE (con botón de sugerencia + tooltip por long-press)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BlackText(
                    text = "Rent (per month)",
                    size = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                val disabled = uiState.isSuggestingPrice
                var showPriceTip by remember { mutableStateOf(false) }
                val interaction = remember { MutableInteractionSource() }

                Box(modifier = Modifier.wrapContentSize()) {
                    // 👇 Reemplaza IconButton por un contenedor con combinedClickable
                    Box(
                        modifier = Modifier
                            .combinedClickable(
                                enabled = !disabled,
                                interactionSource = interaction,
                                onClick = {
                                    if (!isOnline) {
                                        scope.launch {
                                            viewModel.setOfflineSuggestedPrice()
                                            snackbarHostState.showSnackbar(
                                                message = "This is the normally suggested price. For more details, connect to the Internet.",
                                                actionLabel = "OK",
                                                //withDismissAction = true
                                            )
                                        }
                                    } else {
                                        viewModel.suggestPrice()
                                    }
                                },          // tap normal => sugerir precio
                                onLongClick = { showPriceTip = true }            // long-press => mostrar tooltip
                            )
                            .padding(8.dp) // similar al padding interno del IconButton
                    ) {
                        if (uiState.isSuggestingPrice) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(
                                imageVector = AppIcons.Lightbulb,
                                contentDescription = "Suggest price",
                                tint = if (disabled) MaterialTheme.colorScheme.outline else BlueCallToAction
                            )
                        }
                    }

                    // Tooltip casero sobre el icono (se cierra solo)
                    if (showPriceTip) {
                        LaunchedEffect(Unit) {
                            delay(1500) // visible 1.5s
                            showPriceTip = false
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-36).dp) // justo encima del icono
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Intelligent suggestion for price",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))


            PlaceholderTextField(
                placeholderText = uiState.pricePlaceholder,
                value = uiState.price,
                onValueChange = { viewModel.updatePrice(it) }
            )

        // Mensaje cuando falta Type of housing
            uiState.suggestPriceError?.let { err ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        // (Opcional) info de sugerencia (rango/nota) si quieres mantenerlo
            uiState.suggestedPrice?.let { sp ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Suggested: ${sp.value.toInt()} (range ${sp.low.toInt()}–${sp.high.toInt()})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))




            // ----- Sección DESCRIPTION -----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BlackText(text = "Description", size = 16.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.showDescReviewControls) {
                        IconButton(onClick = { viewModel.acceptGeneratedDescription() }) {
                            Icon(
                                imageVector = AppIcons.Check, // usa el tuyo (Check / CheckCircle)
                                contentDescription = "Accept",
                                tint = BlueCallToAction
                            )
                        }
                        IconButton(onClick = { viewModel.revertGeneratedDescription() }) {
                            Icon(
                                imageVector = AppIcons.Close, // usa el tuyo (Close / Clear)
                                contentDescription = "Revert",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    BlackText(
                        text = "AI Assist",
                        size = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Botón "copilot" (Lightbulb)
                    IconButton(
                        onClick = {
                            if (uiState.isDescGenerating) return@IconButton
                            if (!isOnline) {
                                scope.launch {
                                    viewModel.generateOfflineDescription()
                                    snackbarHostState.showSnackbar(
                                        message = "For higher accuracy, connect to the Internet.",
                                        actionLabel = "OK",
                                        //withDismissAction = true
                                    )
                                }
                            } else {
                                viewModel.generateOrRewriteDescription(/* tus args si aplica */)
                            }
                        }
                        ,
                        enabled = !uiState.isDescGenerating
                    ) {
                        if (uiState.isDescGenerating) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(
                                imageVector = AppIcons.Lightbulb, // (Regla 1)
                                contentDescription = "Generate description",
                                tint = BlueCallToAction
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Campo de descripción (usa tu TextField)
            PlaceholderTextField(
                placeholderText = "Ex: The neighborhood is pretty quiet and nice",
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                height = 100.dp
                
            )

            // Mensajes de error específicos de esta sección
            uiState.descError?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MAP (For now just visual)
//            Image(
//                painter = painterResource(id = R.drawable.simple_map),
//                contentDescription = "Map",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(16.dp))
//            )
//            Spacer(modifier = Modifier.height(32.dp))

            // --- CREATE BUTTON ---
            BlueButton(
                text = if (uiState.operationState is CreatePostOperationState.Loading) "Creating..." else "Create",
                onClick = {
                    // Call the ViewModel function to create the post
                    viewModel.createOrDraft(
                        isOnline = isOnline,
                        context = context
                    ) { ok, kind ->
                        // opcional: manejar callback
                    }
                },
                enabled = uiState.operationState  !is CreatePostOperationState.Loading // Disable if loading
            )

            // Show loading indicator if in Loading state
            if (uiState.operationState  is CreatePostOperationState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // --- SNACKBAR HOST (MESSAGES) ---
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            snackbar = { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    contentColor = Color.White,
                    actionColor = Color.White
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenLayoutPreview() {
    CreatePostScreenLayout()
}
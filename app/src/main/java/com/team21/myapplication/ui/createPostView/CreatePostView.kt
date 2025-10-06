package com.team21.myapplication.ui.createPostView

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import java.io.File
import androidx.compose.ui.window.Dialog
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
                // Show success message
                snackbarHostState.showSnackbar("Post created successfully!")
                // Clear form
                viewModel.clearForm()
                // Execute navigation callback
                onPostCreated()
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

            // FIELD: PRICE
            BlackText(
                text = "Rent (per month)",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: 950000",
                value = uiState.price,
                onValueChange = { viewModel.updatePrice(it) } // Filters and updates
            )
            Spacer(modifier = Modifier.height(16.dp))

            // FIELD: DESCRIPTION
            BlackText(
                text = "Description",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlaceholderTextField(
                placeholderText = "Ex: The neighborhood is pretty quiet and nice",
                height = 100.dp,
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- HOUSING TYPE (tag)
            BlackText(
                text = "Type of housing",
                size = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))


            // Define the tag IDs according to your Firebase
            val housingTags = listOf(
                "HousingTag1" to "House" to AppIcons.Home,
                "HousingTag2" to "Apartment" to AppIcons.Apartments,
                "HousingTag3" to "Cabin" to AppIcons.Cabins,
                "HousingTag11" to "Residence" to AppIcons.Houses
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
                                tint = if (isSelected) BlueCallToAction else BlackText
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
                                tint = if (isSelected) BlueCallToAction else BlackText
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- AMENITIES (For now just visual) ---
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
                        tint = BlueCallToAction
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
                            color = MaterialTheme.colorScheme.primary
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

            // BUTTON 2: Take photo with the camera
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

            // BUTTON 3: Add additional photos
            BorderButton(
                text = "Add additional photos (${uiState.additionalPhotos.size}/9)",
                onClick = { additionalPhotosPickerLauncher.launch("image/*") },
                leadingIcon = {
                    Icon(
                        imageVector = AppIcons.Queue,
                        contentDescription = "Add photos",
                        tint = BlueCallToAction
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
                    viewModel.createPost()
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
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenLayoutPreview() {
    CreatePostScreenLayout()
}
package com.team21.myapplication.ui.createforumpost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.data.model.ThreadForum
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.components.buttons.BlueButton
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.inputs.PlaceholderTextField
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.utils.NetworkMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForumPostScreenLayout(
    viewModel: CreateForumPostViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val threads by viewModel.threads.collectAsState()
    val postCreationState by viewModel.postCreationState.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("New Thread", "Existing Thread")

    var newThreadTitle by remember { mutableStateOf("") }
    var newThreadDescription by remember { mutableStateOf("") }
    var postContent by remember { mutableStateOf("") }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedThread by remember { mutableStateOf<ThreadForum?>(null) }

    LaunchedEffect(postCreationState) {
        if (postCreationState is PostCreationState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Banner de conectividad (visible cuando no hay internet)
        val context = LocalContext.current
        val networkMonitor = remember { NetworkMonitor.get(context) }
        val isOnline by networkMonitor.isOnline.collectAsState()
        ConnectivityBanner(
            visible = !isOnline,
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
                    modifier = Modifier.size(32.dp),
                    tint = BlueCallToAction
                )
            }
            BlackText(
                text = "Create a new forum post",
                size = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION: THREAD INFORMATION ---
        BlackText(
            text = "Thread Information",
            size = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            // --- FORM FOR A NEW THREAD ---
            0 -> {
                Column {
                    // FIELD: NEW THREAD TITLE
                    BlackText(
                        text = "Thread Title",
                        size = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PlaceholderTextField(
                        placeholderText = "Ex: Tips for new students",
                        value = newThreadTitle,
                        onValueChange = { newThreadTitle = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // FIELD: NEW THREAD DESCRIPTION
                    BlackText(
                        text = "Thread Description",
                        size = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PlaceholderTextField(
                        placeholderText = "A short description of the thread's topic...",
                        value = newThreadDescription,
                        onValueChange = { newThreadDescription = it },
                        modifier = Modifier.height(100.dp)
                    )
                }
            }
            // --- FORM FOR AN EXISTING THREAD ---
            1 -> {
                Column {
                    BlackText(
                        text = "Select a Thread",
                        size = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = selectedThread?.title ?: "Select a thread",
                            onValueChange = {},
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                        ) {
                            threads.forEach { thread ->
                                DropdownMenuItem(
                                    text = { Text(text = thread.title) },
                                    onClick = {
                                        selectedThread = thread
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECTION: POST CONTENT ---
        BlackText(
            text = "Your Post",
            size = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // FIELD: POST CONTENT
        BlackText(
            text = "Content",
            size = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlaceholderTextField(
            placeholderText = "Share your thoughts...",
            value = postContent,
            onValueChange = { postContent = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // --- SUBMIT BUTTON ---
        BlueButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            text = "Create Post",
            onClick = {
                if (selectedTabIndex == 0) {
                    viewModel.createPost(
                        threadId = null,
                        newThreadTitle = newThreadTitle,
                        newThreadDescription = newThreadDescription,
                        postContent = postContent
                    )
                } else {
                    viewModel.createPost(
                        threadId = selectedThread?.id,
                        newThreadTitle = null,
                        newThreadDescription = null,
                        postContent = postContent
                    )
                }
            },
            enabled = postCreationState != PostCreationState.Loading
        )

        if (postCreationState is PostCreationState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (postCreationState is PostCreationState.Error) {
            Text(
                text = (postCreationState as PostCreationState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

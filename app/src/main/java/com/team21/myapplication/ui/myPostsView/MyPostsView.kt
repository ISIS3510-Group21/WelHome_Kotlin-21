package com.team21.myapplication.ui.myPostsView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.R
import com.team21.myapplication.ui.components.buttons.BlueButtonWithIcon
import com.team21.myapplication.ui.components.cards.BasicHousingInfoCard
import com.team21.myapplication.ui.components.inputs.SearchBar
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.icons.AppIcons
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.team21.myapplication.data.model.BasicHousingPost
import android.content.Intent
import androidx.compose.foundation.layout.size
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.team21.myapplication.data.local.AppDatabase
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.data.repository.OwnerUserRepository
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.ui.createPostView.CreatePostActivity
import com.team21.myapplication.utils.NetworkMonitor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat


@Composable
fun MyPostsScreen() {

    val ctx = LocalContext.current
    val db = AppDatabase.getDatabase(ctx) // Room
    val session = SecureSessionManager(ctx.applicationContext)
    val net = NetworkMonitor.get(ctx.applicationContext)


    val vm: MyPostsViewModel = viewModel(
        factory = MyPostsViewModel.MyPostsViewModelFactory(
            ownerRepo = OwnerUserRepository(),
            authRepo = AuthRepository(),
            session = session,
            dao = db.myPostsDao(),
            net = net,
            draftDao = db.draftPostDao()
        )
    )

    val state by vm.state.collectAsStateWithLifecycle()
    // Banner de conectividad (top)
    val isOnline by net.isOnline.collectAsStateWithLifecycle()
    val view = LocalView.current

    val statusBarColor = if (!isOnline) {
        androidx.compose.ui.graphics.Color.Black
    } else {
        MaterialTheme.colorScheme.background
    }

    SideEffect {
        val window = (view.context as android.app.Activity).window
        // Variable de fondo
        window.statusBarColor = statusBarColor.toArgb()

        // La lógica para decidir el color de los íconos
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            if (!isOnline) {
                false // Íconos blancos para fondo negro
            } else {
                statusBarColor.luminance() > 0.5f // Decide según la luminancia del fondo
            }
    }

    LaunchedEffect(Unit) { vm.loadMyPosts() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.loadMyPosts()   // recarga al volver a la pestaña
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize()) {

        ConnectivityBanner(
            visible = !isOnline,
            position = BannerPosition.Top,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        )

        val topPad = if (!isOnline) 0.dp else 40.dp

        when {
            state.error != null -> {
                // TODO: mostrar texto de error
                MyPostsScreenLayout(
                    posts = emptyList(),
                    onAddPostClick = {
                        ctx.startActivity(Intent(ctx, CreatePostActivity::class.java))
                    },
                    topPadding = topPad
                )
            }

            else -> {

                MyPostsScreenLayout(
                    posts = state.posts,
                    onAddPostClick = {
                        ctx.startActivity(Intent(ctx, CreatePostActivity::class.java))
                    },
                    topPadding = topPad
                )
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MyPostsScreenLayout(
    posts: List<BasicHousingPost>,
    onAddPostClick: () -> Unit = {},
    topPadding: Dp = 40.dp
) {
    // Principal component as Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // allows scrolling
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(topPadding))

        BlackText(
            text = "My posts",
            size = 30.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            SearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Search",
                asButton = true,
                modifier = Modifier.weight(4f),
                onClick = {}
            )

            BlueButtonWithIcon(
                text = "",
                imageVector = AppIcons.Add,
                modifier = Modifier.weight(1f),
                onClick = onAddPostClick
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        posts.forEachIndexed { idx, post ->
            // 1) Normalizar la ruta de imagen (si es local, usa "file://")
            val imgUrl = when {
                post.photoPath.isBlank() -> null
                post.photoPath.startsWith("/") -> "file://${post.photoPath}"
                else -> post.photoPath
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // 2) Card base (igual que antes)
                BasicHousingInfoCard(
                    title = post.title,
                    pricePerMonthLabel = if (post.isDraft) "Pending upload" else "$${post.price}/month",
                    imageUrl = imgUrl,
                    imageRes = if (imgUrl != null) null else R.drawable.sample_house,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { if (post.isDraft) null else { /* navegar al detail */ } }
                )

                // 3) Si es borrador: overlay semitransparente + “pill” con ícono de reloj
                if (post.isDraft) {
                    // scrim que "apaga" la card
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )

                    // “Pill” superior derecha con reloj + texto
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "Pending upload",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 4) Borde sutil alrededor para diferenciar aún más
                    Box(
                        Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                            .padding(1.dp)
                            .background(
                                Color(0xFF9AA0A6).copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }


    }
}

// Preview of the component
@Preview(showBackground = true)
@Composable
fun MyPostsScreenLayoutScreenLayoutPreview() {
    val example = BasicHousingPost(
        id = "0",
        title = "example house",
        photoPath = "",
        price = 100.0
    )

    val posts: List<BasicHousingPost> = listOf(example, example, example)
    MyPostsScreenLayout(posts)
}
package com.team21.myapplication.ui.biometric

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.team21.myapplication.data.biometric.BiometricCredentialStore
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.main.MainActivity
import com.team21.myapplication.ui.ownerMainView.OwnerMainActivity
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.team21.myapplication.data.local.SecureSessionManager
import com.team21.myapplication.ui.components.banners.BannerPosition
import com.team21.myapplication.ui.components.banners.ConnectivityBanner
import com.team21.myapplication.utils.App
import com.team21.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat

class BiometricLoginActivity : FragmentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val repo by lazy { AuthRepository() }
    private lateinit var store: BiometricCredentialStore
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var sessionManager: SecureSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = BiometricCredentialStore(this)
        val app = application as App
        networkMonitor = app.networkMonitor
        sessionManager = app.sessionManager

        setContent {
            // Aquí uso tu Theme global; si tu app se llama distinto, cambia el wrapper.
            // WelHomeTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                val bg = MaterialTheme.colorScheme.background
                val view = androidx.compose.ui.platform.LocalView.current
                SideEffect {
                    val window = (view.context as android.app.Activity).window
                    // Colores de las barras
                    window.statusBarColor = bg.toArgb()
                    window.navigationBarColor = bg.toArgb()

                    // Íconos claros u oscuros según luminancia del fondo
                    val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                    val lightIcons = bg.luminance() > 0.5f
                    controller.isAppearanceLightStatusBars = lightIcons
                    controller.isAppearanceLightNavigationBars = lightIcons
                }
                val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
                Box(modifier = Modifier.fillMaxSize()) {
                    ConnectivityBanner(
                        visible = !isOnline,
                        position = BannerPosition.Top,
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(WindowInsets.statusBars.asPaddingValues())
                            .zIndex(1f) // por encima del resto
                    )
                    val context = LocalContext.current
                    val store = remember(context) { BiometricCredentialStore(context) }

                    var hasLinked by remember {
                        mutableStateOf(runCatching { store.hasLinkedFingerprint() }.getOrElse { false })
                    }

                    BiometricLoginScreen(
                        biometricsAvailable = runCatching { store.isBiometricAvailable() }.getOrElse { false },
                        hasLinked = hasLinked,
                        isOnline = isOnline,
                        onOpenSettings = {
                            // Aquí abro Settings para enrolar huella si el dispositivo no tiene.
                            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                        },
                        onLink = { email, pass, onError, onSuccess ->
                            // Aquí valido campos + formato email (por si llega sin tocar el field)
                            if (email.isBlank() || pass.isBlank()) {
                                onError("Please fill out all fields.")
                                return@BiometricLoginScreen
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                onError("Please enter a valid email.")
                                return@BiometricLoginScreen
                            }
                            if (!store.isBiometricAvailable()) {
                                onError("No biometrics enrolled on this device.")
                                return@BiometricLoginScreen
                            }

                            // Aquí valido contra Firebase ANTES de pedir huella (para no pedirla si credenciales están mal).
                            lifecycleScope.launch {
                                val signInCheck = repo.signIn(email, pass)
                                if (signInCheck.isFailure) {
                                    val msg = signInCheck.exceptionOrNull()?.localizedMessage
                                        ?: "Invalid email or password."
                                    onError(msg)
                                    return@launch
                                }
                                // Obtener userId e isOwner
                                val userId = signInCheck.getOrNull()!!
                                val isOwner = repo.isOwner(userId)

                                // Si validó bien, cierro sesión y paso a pedir huella para cifrar y guardar localmente.
                                FirebaseAuth.getInstance().signOut()

                                // Aquí preparo el cipher en ENCRYPT y disparo el prompt biométrico para "firmar" el guardado.
                                val encryptCipher = try {
                                    store.createEncryptCipher()
                                } catch (e: Exception) {
                                    onError("Could not prepare secure storage.")
                                    return@launch
                                }

                                val executor = androidx.core.content.ContextCompat.getMainExecutor(this@BiometricLoginActivity)

                                val prompt = androidx.biometric.BiometricPrompt(
                                    this@BiometricLoginActivity, // FragmentActivity
                                    executor,
                                    object :
                                        androidx.biometric.BiometricPrompt.AuthenticationCallback() {

                                        override fun onAuthenticationError(
                                            code: Int,
                                            errString: CharSequence
                                        ) {
                                            runOnUiThread { onError(errString.toString()) }
                                        }

                                        override fun onAuthenticationFailed() {
                                            runOnUiThread { onError("Fingerprint not recognized. Try again.") }
                                        }

                                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                            // Aquí guardo credenciales cifradas usando el cipher AUTORIZADO por la huella.
                                            val authorizedCipher = result.cryptoObject?.cipher
                                            if (authorizedCipher == null) {
                                                runOnUiThread { onError("Crypto error. Please try again.") }
                                                return
                                            }
                                            try {
                                                store.saveEncryptedCredentials(
                                                    authorizedCipher,
                                                    email,
                                                    pass
                                                )
                                                // guardar sesion
                                                sessionManager.saveSession(userId, email, isOwner)
                                                sessionManager.saveOfflineIdentity(userId, email, isOwner)

                                                runOnUiThread {
                                                    hasLinked = true    // <-- habilita de inmediato “Login with fingerprint”
                                                    onSuccess()
                                                }
                                            } catch (e: Exception) {
                                                runOnUiThread { onError("Could not link fingerprint. Please try again.") }
                                            }
                                        }
                                    }
                                )

                                val promptInfo =
                                    androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Link fingerprint")
                                        .setSubtitle("Authenticate to securely store your credentials")
                                        .setNegativeButtonText("Cancel")
                                        .build()

                                // Aquí asocio el Cipher ENCRYPT al prompt para que la clave requiera huella.
                                val crypto =
                                    androidx.biometric.BiometricPrompt.CryptoObject(encryptCipher)
                                prompt.authenticate(promptInfo, crypto)
                            }
                        },
                        onLoginWithFingerprint = { onError ->
                            // Aquí autentico con huella y si ok, desencripto y hago signIn.
                            if (!store.hasLinkedFingerprint()) {
                                onError("No fingerprint-linked account found. Please link it first.")
                                return@BiometricLoginScreen
                            }
                            val cipher = store.getDecryptCipherOrNull()
                            if (cipher == null) {
                                onError("Stored credentials are not available. Please link again.")
                                return@BiometricLoginScreen
                            }
                            val executor = androidx.core.content.ContextCompat.getMainExecutor(this@BiometricLoginActivity)

                            val prompt = BiometricPrompt(
                                this@BiometricLoginActivity,
                                executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationError(
                                        code: Int,
                                        errString: CharSequence
                                    ) {
                                        runOnUiThread { onError(errString.toString()) }
                                    }

                                    override fun onAuthenticationFailed() {
                                        runOnUiThread { onError("Fingerprint not recognized. Try again.") }
                                    }

                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        val c = result.cryptoObject?.cipher ?: run {
                                            runOnUiThread { onError("Crypto error. Please link again.") }
                                            return
                                        }
                                        val (email, pass) = try {
                                            store.decryptEmailAndPassword(c)
                                        } catch (e: Exception) {
                                            runOnUiThread { onError("Could not decrypt credentials. Please link again.") }
                                            return
                                        }

                                        // verificar si hay internet
                                        val isOnline = networkMonitor.isOnline.value

                                        if (!isOnline) {
                                            // Modo offline: Validar contra sesión guardada
                                            val savedSession = sessionManager.getSession()
                                            if (savedSession == null) {
                                                // Intento de reconstrucción con identidad offline
                                                val offline = sessionManager.getOfflineIdentityOrNull()
                                                if (offline == null) {
                                                    runOnUiThread {
                                                        onError("No offline session available. Please login online first.")
                                                    }
                                                    return
                                                }
                                                // Asegúrate de que coincida el email desencriptado
                                                if (offline.email != email) {
                                                    runOnUiThread {
                                                        onError("Stored credentials don't match. Please login online.")
                                                    }
                                                    return
                                                }
                                                // Recrea sesión local (sin red)
                                                sessionManager.saveSession(offline.userId, offline.email, offline.isOwner)
                                            }

                                            // Login offline exitoso
                                            runOnUiThread {
                                                val active = sessionManager.getSession()
                                                if (active == null) {
                                                    onError("Could not create offline session. Please try again.")
                                                    return@runOnUiThread
                                                }

                                                val target = if (active.isOwner) OwnerMainActivity::class.java else MainActivity::class.java

                                                startActivity(
                                                    Intent(
                                                        this@BiometricLoginActivity,
                                                        target
                                                    ).apply {
                                                        putExtra("offline_mode", true)
                                                        putExtra("login_success", true)
                                                        flags =
                                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                )
                                                finish()
                                            }
                                            return
                                        }

                                        // Modo online: Validar contra Firebase
                                        lifecycleScope.launch {
                                            val rr = repo.signIn(email, pass)
                                            if (rr.isFailure) {
                                                runOnUiThread { onError("Invalid saved credentials. Please link again.") }
                                                return@launch
                                            }
                                            // Aquí decido Owner vs Main igual que tu flujo actual.
                                            val uid = rr.getOrNull()!!
                                            val isOwner = repo.isOwner(uid)

                                            // Guardar sesión actualizada
                                            lifecycleScope.launch {
                                                withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    sessionManager.saveSession(uid, email, isOwner)
                                                    sessionManager.saveOfflineIdentity(
                                                        uid,
                                                        email,
                                                        isOwner
                                                    )
                                                    sessionManager.saveOfflinePassword(pass) // solo en modo online

                                                }

                                                val target =
                                                    if (isOwner) OwnerMainActivity::class.java else MainActivity::class.java
                                                startActivity(
                                                    Intent(
                                                        this@BiometricLoginActivity,
                                                        target
                                                    ).apply {
                                                        putExtra("login_success", true)
                                                        flags =
                                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                )
                                                finish()
                                            }
                                        }
                                    }
                                }
                            )

                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Login with fingerprint")
                                .setSubtitle("Authenticate to sign in")
                                .setNegativeButtonText("Cancel") // 1.1.0 compatible
                                .build()

                            // Aquí asocio el Cipher (DECRYPT) con el prompt.
                            val crypto = BiometricPrompt.CryptoObject(cipher)
                            prompt.authenticate(promptInfo, crypto)
                        }
                    )
                }
                // }
            }
        }
    }
}

/** Composable principal de la Activity: selección o link. */
@Composable
private fun BiometricLoginScreen(
    biometricsAvailable: Boolean,
    hasLinked: Boolean,
    isOnline: Boolean,
    onOpenSettings: () -> Unit,
    onLink: (email: String, pass: String, onError: (String) -> Unit, onSuccess: () -> Unit) -> Unit,
    onLoginWithFingerprint: (onError: (String) -> Unit) -> Unit
) {
    var mode by remember { mutableStateOf(Mode.Selection) }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val primary = MaterialTheme.colorScheme.primary

    fun showError(msg: String) { error = msg }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cambia status bar según conectividad: negra (íconos blancos) cuando está offline
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (mode) {
            Mode.Selection -> {
                Text(
                    text = "Biometric Login",
                    style = MaterialTheme.typography.headlineSmall,
                    color = primary
                )
                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onLoginWithFingerprint(::showError) },
                    enabled = biometricsAvailable && hasLinked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("I already have fingerprint")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (!isOnline) {
                            scope.launch { snackbarHostState.showSnackbar("You're offline. Link fingerprint requires internet.") }
                            return@Button
                        }
                        error = null
                        mode = Mode.Link
                    },
                    enabled = biometricsAvailable,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Link fingerprint to account")
                }


                if (!biometricsAvailable) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No biometrics enrolled on this device.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onOpenSettings) {
                        Text("Open Security Settings")
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Mode.Link -> {
                Text(
                    text = "Link fingerprint to account",
                    style = MaterialTheme.typography.headlineSmall,
                    color = primary
                )
                Spacer(Modifier.height(16.dp))

                // Aquí pido Email (teclado tipo email).
                var emailTouched by remember { mutableStateOf(false) } // para no pintar rojo al primer render
                val emailValid = remember(email) {
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                }
                val showEmailError = emailTouched && email.isNotBlank() && !emailValid

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (!emailTouched && it.isNotEmpty()) emailTouched = true
                    },
                    label = { Text("Email", color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = showEmailError, // <- Aquí pinto el borde/label en rojo
                    supportingText = {
                        if (showEmailError) {
                            Text(
                                "Please enter a valid email.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // Aquí pido Password (oculto).
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password", color = primary) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Aquí asocio la huella tras validar en Firebase y cifrar.
                    Button(
                        onClick = {
                            onLink(email.trim(), pass, ::showError) {
                                // Éxito -> vuelvo a la selección
                                email = ""; pass = ""; error = null
                                mode = Mode.Selection
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        modifier = Modifier.weight(1f)
                    ) { Text("Associate") }

                    OutlinedButton(
                        onClick = { mode = Mode.Selection; error = null },
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "We securely store your credentials encrypted with your fingerprint.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        }
    }
}

private enum class Mode { Selection, Link }

package com.team21.myapplication.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.team21.myapplication.data.repository.AuthRepository
import com.team21.myapplication.ui.createAccountView.WelcomeActivity
import com.team21.myapplication.ui.main.MainActivity
import com.team21.myapplication.ui.ownerMainView.OwnerMainActivity
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // No sesión: ir a Welcome
            startActivity(Intent(this, WelcomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
            return
        }

        // Hay sesión: resolver el rol y rutear al feed correcto
        lifecycleScope.launch {
            val isOwner = AuthRepository().isOwner(user.uid) // ya lo tienes en tu repo
            val target = if (isOwner) OwnerMainActivity::class.java else MainActivity::class.java
            startActivity(Intent(this@SplashActivity, target).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
            finish()
        }
    }
}


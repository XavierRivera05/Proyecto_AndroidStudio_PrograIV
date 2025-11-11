package com.example.notipadmemo

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var userIcon: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var googleIconButton: ImageButton
    private lateinit var themeToggleButton: ImageButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        //  Aplicar el tema guardado antes de inflar el layout
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ======================
        //  INICIALIZACIÓN
        // ======================
        userIcon = findViewById(R.id.userIcon)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        googleIconButton = findViewById(R.id.googleIconButton)
        themeToggleButton = findViewById(R.id.themeToggleButton)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ======================
        //  BOTÓN DE TEMA
        // ======================
        updateThemeIcon()

        themeToggleButton.setOnClickListener {
            val newMode = ThemeUtils.toggleTheme(this)
            updateThemeIcon(newMode)
        }

        // ======================
        //  LOGIN Y REGISTRO
        // ======================
        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario registrado correctamente ✅", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Sesión iniciada 👋", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Campos incompletos", Toast.LENGTH_SHORT).show()
            }
        }

        googleIconButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    // ======================
    //  CAMBIO DE TEMA
    // ======================
    private fun updateThemeIcon(currentMode: Int? = null) {
        val mode = currentMode ?: AppCompatDelegate.getDefaultNightMode()
        when (mode) {
            AppCompatDelegate.MODE_NIGHT_YES -> themeToggleButton.setImageResource(R.drawable.modoclaro)
            AppCompatDelegate.MODE_NIGHT_NO -> themeToggleButton.setImageResource(R.drawable.modo_oscuro_foreground)
            else -> {
                val isNight = resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
                themeToggleButton.setImageResource(
                    if (isNight) R.drawable.modoclaro else R.drawable.modo_oscuro_foreground
                )
            }
        }
    }

    // ======================
    // 🔹 GOOGLE SIGN-IN
    // ======================
    private fun signInWithGoogle() {
        //  Forzar selector de cuenta antes de iniciar sesión
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesión con Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = auth.currentUser
                Toast.makeText(this, "Bienvenido ${user?.displayName ?: "Usuario"} 👋", Toast.LENGTH_SHORT).show()
                goToMain()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ======================
    //  FLUJO PRINCIPAL
    // ======================
    override fun onStart() {
        super.onStart()
        // Si ya hay sesión activa, redirigir directo al Main
        if (auth.currentUser != null) goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

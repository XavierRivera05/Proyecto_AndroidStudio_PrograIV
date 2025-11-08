package com.example.notipadmemo 

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    // Declara las vistas de la interfaz de usuario
    private lateinit var userIcon: ImageView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var googleIconButton: ImageButton

    // Cliente de inicio de sesión de Google
    private lateinit var googleSignInClient: GoogleSignInClient

    // Constante para el resultado del inicio de sesión con Google
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece el layout para esta actividad.
        // Asegúrate de que tu archivo XML se llame 'activity_login.xml' o cámbialo aquí.
        setContentView(R.layout.activity_login)

        // 1. INICIALIZACIÓN DE VISTAS
        // Vincula las variables con los IDs del archivo XML.
        userIcon = findViewById(R.id.userIcon)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        registerButton = findViewById(R.id.registerButton)
        googleIconButton = findViewById(R.id.googleIconButton)

        // 2. CONFIGURACIÓN DEL INICIO DE SESIÓN CON GOOGLE
        // Configura las opciones de inicio de sesión para solicitar el ID y el perfil básico del usuario.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Solicita acceso al correo electrónico del usuario
            .build()

        // Construye un GoogleSignInClient con las opciones especificadas.
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 3. CONFIGURACIÓN DE LOS LISTENERS DE LOS BOTONES

        // Listener para el botón de registro con email y contraseña
        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Aquí implementarías tu lógica de registro (por ejemplo, con Firebase Auth)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Lógica de registro...
                Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón de inicio de sesión con Google
        googleIconButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    /**
     * Inicia el flujo de inicio de sesión con Google.
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Se llama cuando la actividad de inicio de sesión de Google devuelve un resultado.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Resultado devuelto al iniciar sesión con Google
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Inicio de sesión con Google exitoso, autentica con tu backend o Firebase
                val account = task.getResult(ApiException::class.java)!!
                Toast.makeText(this, "Inicio de sesión con Google exitoso: ${account.displayName}", Toast.LENGTH_LONG).show()

                // Aquí iría la lógica para pasar a la siguiente pantalla (por ejemplo, MainActivity)
                // val intent = Intent(this, MainActivity::class.java)
                // startActivity(intent)
                // finish() // Cierra LoginActivity para que el usuario no pueda volver con el botón "atrás"

            } catch (e: ApiException) {
                // Falló el inicio de sesión con Google
                Toast.makeText(this, "Falló el inicio de sesión con Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

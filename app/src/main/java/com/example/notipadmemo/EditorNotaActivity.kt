package com.example.notipadmemo

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditorNotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_nota) //para cargar el layout

        findViewById<View>(R.id.btnBack).setOnClickListener{
            finish() //cerrar al presionar atrás XD
        }

        findViewById<View>(R.id.btnGuardar).setOnClickListener{
            val titulo = findViewById<EditText>(R.id.inputTitulo).text.toString()
            val contenido = findViewById<EditText>(R.id.inputContenido).text.toString()
            NotesStore.addNote(this, titulo, contenido)
            Toast.makeText(this, "¡Nota guardada!", Toast.LENGTH_SHORT).show()
            finish() //volver a la pantalla principal
        }
    }
}
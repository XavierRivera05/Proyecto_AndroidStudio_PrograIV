package com.example.notipadmemo

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditorNotaActivity : AppCompatActivity() {

    private var noteIndex: Int = -1  // posición de la nota si se está editando

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_nota)

        // Referencias a los campos de texto
        val inputTitulo = findViewById<EditText>(R.id.inputTitulo)
        val inputContenido = findViewById<EditText>(R.id.inputContenido)

        // Verificar si venimos a editar una nota existente
        noteIndex = intent.getIntExtra("note_index", -1)
        if (noteIndex >= 0) {
            val nota = NotesStore.getNote(this, noteIndex)
            nota?.let {
                inputTitulo.setText(it.optString("title"))
                inputContenido.setText(it.optString("content"))
            }
        }

        // botón atrás
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // botón de guardar
        findViewById<View>(R.id.btnGuardar).setOnClickListener {
            val titulo = inputTitulo.text.toString()
            val contenido = inputContenido.text.toString()

            if (noteIndex >= 0) {
                // Actualizar nota existente
                NotesStore.updateNote(this, noteIndex, titulo, contenido)
                Toast.makeText(this, "Nota actualizada :)", Toast.LENGTH_SHORT).show()
            } else {
                // Agregar nueva nota
                NotesStore.addNote(this, titulo, contenido)
                Toast.makeText(this, "¡Nota guardada!", Toast.LENGTH_SHORT).show()
            }

            finish() // volver a la pantalla principal
        }

        //botonsito de eliminar
        findViewById<View>(R.id.btnEliminar)?.setOnClickListener {
            if (noteIndex >= 0) {
                NotesStore.moveToTrash(this, noteIndex)
                Toast.makeText(this, "Movida a papelera", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nada que eliminar...", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}

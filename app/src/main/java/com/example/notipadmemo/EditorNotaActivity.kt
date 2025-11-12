package com.example.notipadmemo

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class EditorNotaActivity : AppCompatActivity() {

    private var noteIndex: Int = -1
    private var selectedColor: String = "#FFFFFF"
    private var isFavorite: Boolean = false
    private var isPinned: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_nota)

        val inputTitulo = findViewById<EditText>(R.id.inputTitulo)
        val inputContenido = findViewById<EditText>(R.id.inputContenido)

        // Paleta de colores
        val colorAmarillo = findViewById<View>(R.id.colorAmarillo)
        val colorVerde = findViewById<View>(R.id.colorVerde)
        val colorAzul = findViewById<View>(R.id.colorAzul)
        val colorRosa = findViewById<View>(R.id.colorRosa)
        val colorViews = listOf(colorAmarillo, colorVerde, colorAzul, colorRosa)
        val colores = listOf("#FFF59D", "#7DCE7D", "#BBDEFB", "#F8BBD0")

        val chkFavorita = findViewById<CheckBox>(R.id.checkFavorita)
        val chkFijada = findViewById<CheckBox>(R.id.checkFijada)

        //Si se viene a editar una nota existente
        noteIndex = intent.getIntExtra("note_index", -1)
        if (noteIndex >= 0) {
            val notas = NotesStore.getAllNotes(this)
            val nota = notas.getOrNull(noteIndex)

            if (nota != null) {
                inputTitulo.setText(nota.optString("title", ""))
                inputContenido.setText(nota.optString("content", ""))
                chkFavorita.isChecked = nota.optBoolean("favorite", false)
                chkFijada.isChecked = nota.optBoolean("pinned", false)
                isFavorite = nota.optBoolean("favorite", false)
                isPinned = nota.optBoolean("pinned", false)
                selectedColor = nota.optString("color", "#FFFFFF")

                // Resaltar color guardado
                colorViews.forEachIndexed { index, v ->
                    v.alpha = if (colores[index] == selectedColor) 1f else 0.5f
                }
            }
        }

        //Botón atrás
        findViewById<View>(R.id.btnBack).setOnClickListener {
            guardarNota(inputTitulo, inputContenido)
            finish()
        }

        //Botón guardar
        findViewById<View>(R.id.btnGuardar).setOnClickListener {
            guardarNota(inputTitulo, inputContenido)
            finish()
        }

        //Botón eliminar
        findViewById<View>(R.id.btnEliminar)?.setOnClickListener {
            if (noteIndex >= 0) {
                NotesStore.moveToTrash(this, noteIndex)
                Toast.makeText(this, "Nota eliminada y sincronizada ☁️", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nada que eliminar...", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        //Selector de color
        colorViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                selectedColor = colores[index]
                colorViews.forEach { it.alpha = 0.5f }
                view.alpha = 1f
            }
        }

        chkFavorita.setOnCheckedChangeListener { _, isChecked -> isFavorite = isChecked }
        chkFijada.setOnCheckedChangeListener { _, isChecked -> isPinned = isChecked }
    }

    // Función para crear o actualizar una nota
    private fun guardarNota(inputTitulo: EditText, inputContenido: EditText) {
        val titulo = inputTitulo.text.toString().trim()
        val contenido = inputContenido.text.toString().trim()

        if (titulo.isBlank() && contenido.isBlank()) {
            Toast.makeText(this, "No puedes guardar una nota vacía", Toast.LENGTH_SHORT).show()
            return
        }

        if (noteIndex >= 0) {
            // Actualizar nota existente
            val notes = NotesStore.getAllNotes(this)
            val note = notes.getOrNull(noteIndex)
            note?.let {
                it.put("title", titulo)
                it.put("content", contenido)
                it.put("color", selectedColor)
                it.put("favorite", isFavorite)
                it.put("pinned", isPinned)
                NotesStore.saveNote(this, it)
            }
            Toast.makeText(this, "Nota actualizada :)", Toast.LENGTH_SHORT).show()
        } else {
            // Crear nueva nota
            val noteJson = JSONObject()
            noteJson.put("title", titulo)
            noteJson.put("content", contenido)
            noteJson.put("color", selectedColor)
            noteJson.put("favorite", isFavorite)
            noteJson.put("pinned", isPinned)
            noteJson.put("time", System.currentTimeMillis())

            NotesStore.saveNote(this, noteJson)
            Toast.makeText(this, "Nota guardada y sincronizada :)", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.notipadmemo

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditorNotaActivity : AppCompatActivity() {

    private var noteIndex: Int = -1  // posición de la nota si se está editando
    private var selectedColor: String = "#FFFFFF" //valor del color por defecto

    private var isFavorite: Boolean = false
    private var isPinned: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_nota)

        // Referencias a los campos de texto
        val inputTitulo = findViewById<EditText>(R.id.inputTitulo)
        val inputContenido = findViewById<EditText>(R.id.inputContenido)

        //Referencias a los colorsitos
        val colorAmarillo = findViewById<View>(R.id.colorAmarillo)
        val colorVerde = findViewById<View>(R.id.colorVerde)
        val colorAzul = findViewById<View>(R.id.colorAzul)
        val colorRosa = findViewById<View>(R.id.colorRosa)

        val colorViews = listOf(colorAmarillo, colorVerde, colorAzul, colorRosa)
        val colores = listOf("FFF59DFF","7DCE7DFF","BBDEFBFF","F8BBD0FF")

        //Referencia a los chequeos de favorita y fijada
        val chkFavorita = findViewById<CheckBox>(R.id.checkFavorita)
        val chkFijada = findViewById<CheckBox>(R.id.checkFijada)

        // Verificar si venimos a editar una nota existente
        noteIndex = intent.getIntExtra("note_index", -1)
        if (noteIndex >= 0) {
            val nota = NotesStore.getNote(this, noteIndex)
            nota?.let {
                inputTitulo.setText(it.optString("title"))
                inputContenido.setText(it.optString("content"))
                chkFavorita.isChecked = it.optBoolean("favorite", false)
                chkFijada.isChecked = it.optBoolean("pinned", false)
                isFavorite = it.optBoolean("favorite", false)
                isPinned = it.optBoolean("pinned", false)
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
                NotesStore.updateNote(this, noteIndex, titulo, contenido, selectedColor, isFavorite, isPinned)
                Toast.makeText(this, "Nota actualizada :)", Toast.LENGTH_SHORT).show()
            } else {
                // Agregar nueva nota
                NotesStore.addNote(this, titulo, contenido, selectedColor, isFavorite, isPinned)
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

        //Colores que no sirvieron xd
        colorViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                selectedColor = colores[index]

                //resaltar el colorsito seleccionado
                colorViews.forEach { it.alpha = 0.5f }
                view.alpha = 1f
                Toast.makeText(this, "Color seleccionado: $selectedColor", Toast.LENGTH_SHORT).show()
            }
        }

        chkFavorita.setOnCheckedChangeListener { _, isChecked ->
            isFavorite = isChecked
        }

        chkFijada.setOnCheckedChangeListener { _, isChecked ->
            isPinned = isChecked
        }
    }
}

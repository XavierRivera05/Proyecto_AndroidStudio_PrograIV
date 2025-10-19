package com.example.notipadmemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listNotas: ListView
    private lateinit var emptyNotas: TextView
    private lateinit var adapter: NotasAdapter
    private val data = mutableListOf<String>() // previews a mostrar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Aplica el tema guardado (si no lo tienes ya)
        ThemeUtils.applySavedTheme(this)

// Botón: Cambiar tema
        findViewById<View>(R.id.btnTheme).setOnClickListener {
            val next = ThemeUtils.toggleTheme(this)
            val msg = if (next == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                "Tema oscuro activado" else "Tema claro activado"
            android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
            recreate()
        }

// Botón: Papelera
        findViewById<View>(R.id.btnPapelera).setOnClickListener {
            startActivity(android.content.Intent(this, PapeleraActivity::class.java))
        }

// Botón: Nueva Nota
        findViewById<View>(R.id.btnNuevaNota).setOnClickListener {
            startActivity(android.content.Intent(this, EditorNotaActivity::class.java))
        }


        listNotas = findViewById(R.id.listNotas)
        emptyNotas = findViewById(R.id.emptyNotas)

        // Adapter con botón eliminar por item
        adapter = NotasAdapter(this, data) { position ->
            NotesStore.moveToTrash(this, position)
            loadNotes()
            Toast.makeText(this, "¡Movida a papelera!", Toast.LENGTH_SHORT).show()
        }
        listNotas.adapter = adapter
        listNotas.emptyView = emptyNotas

        // Tap en un item -> abrir editor con esa nota
        listNotas.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, EditorNotaActivity::class.java) // o EditarNotaActivity
            intent.putExtra("note_index", position)
            startActivity(intent)
        }

        // botonsito "Nuevo" -> editor vacío
        findViewById<View>(R.id.btnNuevo).setOnClickListener {
            startActivity(Intent(this, EditorNotaActivity::class.java)) // o EditarNotaActivity
        }

        // botón "Papelera" -> lista de eliminadas
        findViewById<View>(R.id.btnBorrar).setOnClickListener {
            startActivity(Intent(this, PapeleraActivity::class.java))
        }

        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        loadNotes() // recargar como metralleta
    }

    private fun loadNotes() {
        val previews = NotesStore.getNotes(this)
        data.clear()
        data.addAll(previews)
        adapter.notifyDataSetChanged()
    }
}





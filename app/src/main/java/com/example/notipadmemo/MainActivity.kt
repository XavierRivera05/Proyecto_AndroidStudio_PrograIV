package com.example.notipadmemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var listNotas: ListView
    private lateinit var emptyNotas: TextView

    //Usamos MutableList para poder reemplazar el contenido fácilmente
    private val data: MutableList<String> = mutableListOf()

    //Guardamos el adapter en una var genérica; si falla NotasAdapter, usamos ArrayAdapter
    private var adapterAny: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)   //aplica el modo guardado
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // --- Barra inferior ---
        findViewById<View>(R.id.btnModo).setOnClickListener {
            val next = ThemeUtils.toggleTheme(this)
            val msg = if (next == AppCompatDelegate.MODE_NIGHT_YES)
                "¡Tema oscuro activado!" else "¡Tema claro activado!"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            recreate()
        }
        findViewById<View>(R.id.btnBorrar).setOnClickListener {
            startActivity(Intent(this, PapeleraActivity::class.java))
        }
        findViewById<View>(R.id.btnNuevo).setOnClickListener {
            startActivity(Intent(this, EditorNotaActivity::class.java))
        }

        // --- Lista ---
        listNotas = findViewById(R.id.listNotas)
        emptyNotas = findViewById(R.id.emptyNotas)
        listNotas.emptyView = emptyNotas

        // Intenta usar tu NotasAdapter; si falla, usa un ArrayAdapter de respaldo
        val notasAdapterOk = runCatching {
            val a = NotasAdapter(this, data) { position ->
                runCatching {
                    NotesStore.moveToTrash(this, position)
                }.onFailure {
                    Toast.makeText(this, "Error al mover a papelera: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                loadNotes()
                Toast.makeText(this, "Nota movida a la papelera", Toast.LENGTH_SHORT).show()
            }
            listNotas.adapter = a
            adapterAny = a
            true
        }.getOrElse {
            Toast.makeText(this, "Usando lista simple: ${it.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            val fallback = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
            listNotas.adapter = fallback
            adapterAny = fallback
            false
        }

        // Tap para editar (funciona con ambos adapters)
        listNotas.setOnItemClickListener { _, _, position, _ ->

            //mensaje de prueba (clic a la nota)
            Toast.makeText(this, "Click en nota $position", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, EditorNotaActivity::class.java)
            intent.putExtra("note_index", position)
            startActivity(intent)
        }

        // Carga inicial
        loadNotes()

        // Info útil si entró por fallback
        if (!notasAdapterOk) {
            Toast.makeText(this, "Sugerencia: revisa NotasAdapter/NotesStore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val previews = runCatching {
            NotesStore.getNotes(this)
        }.getOrElse {
            Toast.makeText(this, "Error leyendo notas: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            emptyList()
        }

        data.clear()
        data.addAll(previews)

        when (val a = adapterAny) {
            is NotasAdapter -> a.notifyDataSetChanged()
            is ArrayAdapter<*> -> (a as ArrayAdapter<String>).notifyDataSetChanged()
        }
    }
}

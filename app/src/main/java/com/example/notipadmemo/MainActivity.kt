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
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var listNotas: ListView
    private lateinit var emptyNotas: TextView

    private val data = mutableListOf<String>()  //Lista de variables con chimol
    private val indexMap = mutableListOf<Int>() //mapita de posición

    private var adapterAny: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
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

        // Adaptador personalizado
        val notasAdapterOk = runCatching {
            val a = NotasAdapter(this, data, indexMap) { position ->
                // Usar índice real, no posición visible
                val realIndex = indexMap.getOrNull(position) ?: return@NotasAdapter
                runCatching {
                    NotesStore.moveToTrash(this, realIndex)
                }.onFailure {
                    Toast.makeText(
                        this,
                        "Error al mover a papelera: ${it.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
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

        // Click para editar
        listNotas.setOnItemClickListener { _, _, position, _ ->
            val realIndex = indexMap.getOrNull(position) ?: return@setOnItemClickListener
            val intent = Intent(this, EditorNotaActivity::class.java)
            intent.putExtra("note_index", realIndex)
            startActivity(intent)
        }

        loadNotes()

        if (!notasAdapterOk) {
            Toast.makeText(this, "Sugerencia: revisa NotasAdapter/NotesStore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val allNotes = NotesStore.getAllNotes(this)

        // Ordenar: fijadas > favoritas > recientes
        val sortedIndices = allNotes.indices.sortedWith(
            compareByDescending<Int> {
                allNotes[it].optBoolean("pinned", false)
            }.thenByDescending {
                allNotes[it].optBoolean("favorite", false)
            }.thenByDescending {
                allNotes[it].optLong("time", 0L)
            }
        )

        data.clear()
        indexMap.clear()

        for (i in sortedIndices) {
            val note = allNotes[i]
            val title = note.optString("title")
            val content = note.optString("content")
            val preview = if (title.isNotBlank()) title else content.lineSequence().firstOrNull().orEmpty()
            data.add(preview.ifBlank { "(Sin título)" })
            indexMap.add(i)  // Guardar índice real
        }

        when (val a = adapterAny) {
            is NotasAdapter -> a.notifyDataSetChanged()
            is ArrayAdapter<*> -> (a as ArrayAdapter<String>).notifyDataSetChanged()
        }
    }
}

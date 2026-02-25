package com.example.notipadmemo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var listNotas: ListView
    private lateinit var emptyNotas: TextView

    private val data = mutableListOf<String>()
    private val indexMap = mutableListOf<Int>()
    private var adapterAny: Any? = null

    private val database = FirebaseDatabase.getInstance().reference.child("notas")

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Inicializar Firebase
        FirebaseApp.initializeApp(this)

        //Botón de cerrar sesión
        findViewById<View>(R.id.btnCerrarSesion)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        //Botón modo claro/oscuro
        findViewById<View>(R.id.btnModo).setOnClickListener {
            val next = ThemeUtils.toggleTheme(this)
            val msg = if (next == AppCompatDelegate.MODE_NIGHT_YES)
                "¡Tema oscuro activado!" else "¡Tema claro activado!"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            recreate()
        }

        //Botón para ir a papelera
        findViewById<View>(R.id.btnBorrar).setOnClickListener {
            startActivity(Intent(this, PapeleraActivity::class.java))
        }

        //Botón para nueva nota
        findViewById<View>(R.id.btnNuevo).setOnClickListener {
            startActivity(Intent(this, EditorNotaActivity::class.java))
        }

        //Inicializar lista de notas
        listNotas = findViewById(R.id.listNotas)
        emptyNotas = findViewById(R.id.emptyNotas)
        listNotas.emptyView = emptyNotas

        val notasAdapterOk = runCatching {
            val a = NotasAdapter(this, data, indexMap) { position ->
                val realIndex = indexMap.getOrNull(position) ?: return@NotasAdapter
                runCatching {
                    NotesStore.moveToTrash(this, realIndex)
                }.onFailure {
                    Toast.makeText(this, "Error al mover a papelera: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                loadNotes()
                Toast.makeText(this, "¡Nota movida a la papelera!", Toast.LENGTH_SHORT).show()
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

        // ✅ Buscador de notas
        val edtBuscar = findViewById<EditText>(R.id.edtBuscar)
        edtBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                filtrarNotas(query)
            }
        })

        // ✅ Click en nota para editar
        listNotas.setOnItemClickListener { _, _, position, _ ->
            val realIndex = indexMap.getOrNull(position) ?: return@setOnItemClickListener
            val intent = Intent(this, EditorNotaActivity::class.java)
            intent.putExtra("note_index", realIndex)
            startActivity(intent)
        }

        loadNotes() // carga local
        syncToFirebase() // sincroniza con la nube

        if (!notasAdapterOk) {
            Toast.makeText(this, "Sugerencia: revisa NotasAdapter/NotesStore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
        syncToFirebase()
    }

    private fun loadNotes() {
        val allNotes = NotesStore.getAllNotes(this)

        val sortedIndices = allNotes.indices.sortedWith(
            compareByDescending<Int> { allNotes[it].optBoolean("pinned", false) }
                .thenByDescending { allNotes[it].optBoolean("favorite", false) }
                .thenByDescending { allNotes[it].optLong("time", 0L) }
        )

        data.clear()
        indexMap.clear()

        for (i in sortedIndices) {
            val note = allNotes[i]
            val title = note.optString("title")
            val content = note.optString("content")
            val preview = if (title.isNotBlank()) title else content.lineSequence().firstOrNull().orEmpty()
            data.add(preview.ifBlank { "(Sin título)" })
            indexMap.add(i)
        }

        when (val a = adapterAny) {
            is NotasAdapter -> a.notifyDataSetChanged()
            is ArrayAdapter<*> -> (a as ArrayAdapter<String>).notifyDataSetChanged()
        }
    }

    private fun filtrarNotas(query: String) {
        val allNotes = NotesStore.getAllNotes(this)
        val filtered = if (query.isBlank()) allNotes else {
            allNotes.filter {
                val title = it.optString("title", "").lowercase()
                val content = it.optString("content", "").lowercase()
                title.contains(query) || content.contains(query)
            }
        }

        data.clear()
        indexMap.clear()

        for ((i, note) in allNotes.withIndex()) {
            if (filtered.contains(note)) {
                val title = note.optString("title")
                val content = note.optString("content")
                val preview = if (title.isNotBlank()) title else content.lineSequence().firstOrNull().orEmpty()
                data.add(preview.ifBlank { "(Sin título)" })
                indexMap.add(i)
            }
        }

        when (val a = adapterAny) {
            is NotasAdapter -> a.notifyDataSetChanged()
            is ArrayAdapter<*> -> (a as ArrayAdapter<String>).notifyDataSetChanged()
        }
    }

    //Sincronizar notas locales con firebase
    private fun syncToFirebase() {
        val allNotes = NotesStore.getAllNotes(this)
        val ref = database.child("usuario1")

        for (note in allNotes) {
            val firebaseNote = Nota(
                title = note.optString("title"),
                content = note.optString("content"),
                pinned = note.optBoolean("pinned", false),
                favorite = note.optBoolean("favorite", false),
                time = note.optLong("time", 0L)
            )
            ref.push().setValue(firebaseNote)
        }
        Toast.makeText(this, "Notas sincronizadas con Firebase ☁️", Toast.LENGTH_SHORT).show()
    }
}

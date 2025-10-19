package com.example.notipadmemo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listNotas: ListView
    private lateinit var emptyNotas: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private val data = mutableListOf<String>() //previews

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //de aquí se carga el layout

        listNotas = findViewById(R.id.listNotas)
        emptyNotas = findViewById(R.id.emptyNotas)

        adapter = NotasAdapter(this, data) { position ->
            NotesStore.moveToTrash(this, position) //mover a papelera
            loadNotes() //refresh como dicen
            Toast.makeText(this, "¡Movida a papelera!", Toast.LENGTH_SHORT).show()
        }

        listNotas.adapter = adapter
        listNotas.emptyView = emptyNotas //para mostrar el Listview vacío very very nais

        //botón de nueva nota -> abrir editor por consiguiente de churrumais
        findViewById<View>(R.id.btnNuevo).setOnClickListener{
            val intent = Intent(this, EditorNotaActivity::class.java)
            startActivity(intent)
        }

        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        loadNotes() //recargar cuando se vuelve al editorsito
    }

    private fun loadNotes() {
        val previews = NotesStore.getNotes(this)
        data.clear()
        data.addAll(previews)
        adapter.notifyDataSetChanged() //empty se va encargar de mostrar cuando no haya notas
    }
}




package com.example.notipadmemo

import android.os.Bundle
import android.view.View
import android.widget.*

import androidx.appcompat.app.AppCompatActivity

class PapeleraActivity : AppCompatActivity() {

    private lateinit var list: ListView
    private lateinit var empty: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private val data = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_papelera)
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish() // Cierra la activity y regresa a la anterior
        }


        list = findViewById(R.id.listTrash)
        empty = findViewById(R.id.emptyTrash)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        list.adapter = adapter
        list.emptyView = empty

        // Tap = restaurar
        list.setOnItemClickListener { _, _, position, _ ->
            NotesStore.restoreFromTrash(this, position)
            Toast.makeText(this, "Nota restaurada", Toast.LENGTH_SHORT).show()
            load()
        }

        // Long press = borrar definitivo
        list.setOnItemLongClickListener { _, _, position, _ ->
            NotesStore.deleteFromTrash(this, position)
            Toast.makeText(this, "Eliminada definitivamente", Toast.LENGTH_SHORT).show()
            load()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val items = NotesStore.getTrash(this)
        data.clear()
        data.addAll(items)
        adapter.notifyDataSetChanged()
    }
}



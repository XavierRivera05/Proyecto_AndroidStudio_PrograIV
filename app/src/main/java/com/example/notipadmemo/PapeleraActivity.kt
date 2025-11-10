package com.example.notipadmemo

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat

class PapeleraActivity : AppCompatActivity() {

    private lateinit var list: ListView
    private lateinit var empty: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private val data = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        //Aplicar el tema guardado ANTES de inflar
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_papelera)

        // Botón volver
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        // (opcional) Tinta del icono acorde al tema
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        val onBackground = colorFromAttr(android.R.attr.textColorPrimary)
        ImageViewCompat.setImageTintList(btnBack, ColorStateList.valueOf(onBackground))

        // Lista + “vacía”
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

        // Long press = borrar definitivo un ítem
        list.setOnItemLongClickListener { _, _, position, _ ->
            NotesStore.deleteFromTrash(this, position)
            Toast.makeText(this, "Eliminada definitivamente", Toast.LENGTH_SHORT).show()
            load()
            true
        }

        // Botón: Vaciar papelera
        findViewById<View>(R.id.btnVaciarPapelera).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Vaciar papelera")
                .setMessage("Se eliminarán definitivamente todas las notas en la papelera. ¿Continuar?")
                .setPositiveButton("Eliminar") { _, _ -> emptyTrashAndReload() }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        load()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    /** Recarga la lista desde NotesStore */
    private fun load() {
        val items = NotesStore.getTrash(this)  // ← devuelve una lista de JSONObject
        data.clear()

        // Convertir cada JSONObject en su título o en "(Sin título)"
        for (note in items) {
            val title = note.optString("title", "(Sin título)")
            data.add(title)
        }

        adapter.notifyDataSetChanged()
        empty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
    }


    /** Vacía toda la papelera y vuelve a cargar */
    private fun emptyTrashAndReload() {
        val items = NotesStore.getTrash(this)
        for (i in items.indices.reversed()) {
            NotesStore.deleteFromTrash(this, i)
        }
        Toast.makeText(this, "Papelera vaciada", Toast.LENGTH_SHORT).show()
        load()
    }

    /** Helper: obtiene un color desde el atributo del tema actual */
    private fun colorFromAttr(@AttrRes attr: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attr, tv, true)
        return if (tv.resourceId != 0)
            ContextCompat.getColor(this, tv.resourceId)
        else
            tv.data
    }
}

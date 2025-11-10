package com.example.notipadmemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import org.json.JSONObject

class NotasAdapter(
    private val context: Context,
    private val data: MutableList<String>,
    private val indexMap: MutableList<Int>,
    private val onDelete: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_nota_simple, parent, false)

        val txtTitulo = view.findViewById<TextView>(R.id.txtTitulo)
        val txtSub = view.findViewById<TextView>(R.id.txtSub)
        val iconFavorito = view.findViewById<ImageView>(R.id.iconfavorito)
        val iconFijado = view.findViewById<ImageView>(R.id.iconfijado)
        val colorView = view.findViewById<View>(R.id.viewColor)
        val btnEliminar = view.findViewById<ImageButton>(R.id.btnEliminarItem)

        val noteList = NotesStore.getAllNotes(context)
        val noteIndex = indexMap.getOrNull(position)
        val note: JSONObject? = noteIndex?.let { noteList.getOrNull(it) }

        txtTitulo.text = note?.optString("title", "(Sin título)")
        txtSub.text = note?.optString("content", "")

        val isFav = note?.optBoolean("favorite", false) == true
        val isPinned = note?.optBoolean("pinned", false) == true
        iconFavorito.visibility = if (isFav) View.VISIBLE else View.GONE
        iconFijado.visibility = if (isPinned) View.VISIBLE else View.GONE

        val color = note?.optString("color", "#FFFFFF") ?: "#FFFFFF"
        try {
            val drawable = DrawableCompat.wrap(colorView.background)
            DrawableCompat.setTint(drawable, android.graphics.Color.parseColor(color))
        } catch (_: Exception) {}

        btnEliminar.setOnClickListener { onDelete(position) }

        return view
    }
}

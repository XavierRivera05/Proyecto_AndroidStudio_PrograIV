package com.example.notipadmemo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import org.json.JSONObject

class NotasAdapter(
    private val ctx: Context,
    private val items: MutableList<String>,
    private val indexMap: MutableList<Int>,
    private val onDelete: (position: Int) -> Unit
) : ArrayAdapter<String>(ctx, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(ctx)
            .inflate(R.layout.item_nota_simple, parent, false)

        val titulo = v.findViewById<TextView>(R.id.txtTitulo)
        titulo.text = items[position]

        val viewColor = v.findViewById<View>(R.id.viewColor)
        val icoFijado = v.findViewById<ImageView>(R.id.iconfijado)
        val icoFavorito = v.findViewById<ImageView>(R.id.iconfavorito)
        val layoutIconos = v.findViewById<LinearLayout>(R.id.layouticonos)

        // 🔹 Obtener índice real de la nota (del indexMap)
        val realIndex = indexMap.getOrNull(position)
        val nota: JSONObject? = realIndex?.let { NotesStore.getNote(ctx, it) }

        // 🔹 Aplicar color
        val color = nota?.optString("color") ?: "#FFFFFF"
        try {
            ViewCompat.setBackgroundTintList(
                viewColor,
                ColorStateList.valueOf(Color.parseColor(color))
            )
        } catch (_: Exception) {
            viewColor.setBackgroundColor(Color.DKGRAY)
        }

        //Mostrar iconos de fijado y favorito (usando los nombres reales)
        val fijado = nota?.optBoolean("pinned", false) ?: false
        val favorito = nota?.optBoolean("favorite", false) ?: false

        icoFijado.visibility = if (fijado) View.VISIBLE else View.GONE
        icoFavorito.visibility = if (favorito) View.VISIBLE else View.GONE
        layoutIconos.visibility = if (fijado || favorito) View.VISIBLE else View.GONE

        //Eliminar nota (posición visible)
        v.findViewById<ImageButton>(R.id.btnEliminarItem).setOnClickListener {
            onDelete(position)
        }

        return v
    }
}

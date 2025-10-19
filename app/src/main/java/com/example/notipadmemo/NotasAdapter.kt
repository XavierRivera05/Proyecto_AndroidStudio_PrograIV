package com.example.notipadmemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

class NotasAdapter(
    context: Context,
    private val items: MutableList<String>,
    private val onDelete: (position: Int) -> Unit
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_nota_simple, parent, false)

        val titulo = v.findViewById<TextView>(R.id.txtTitulo)
        titulo.text = items[position]

        // subtexto del coso este

        v.findViewById<ImageButton>(R.id.btnEliminarItem).setOnClickListener {
            onDelete(position)
        }
        return v
    }
}
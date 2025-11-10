package com.example.notipadmemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject

class TrashAdapter(
    private val context: Context,
    private val notes: MutableList<JSONObject>,
    private val onRestore: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = notes.size
    override fun getItem(position: Int): Any = notes[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.activity_papelera, parent, false)

        val txtTitle = view.findViewById<TextView>(R.id.txtTitulo)
        val txtContent = view.findViewById<TextView>(R.id.txtSub)
        val btnRestore = view.findViewById<ImageView>(R.id.btnVaciarPapelera)
        val btnDelete = view.findViewById<ImageView>(R.id.btnEliminar)

        val note = notes[position]

        txtTitle.text = note.optString("title", "(Sin título)")
        txtContent.text = note.optString("content", "")

        btnRestore.setOnClickListener { onRestore(position) }
        btnDelete.setOnClickListener { onDelete(position) }

        return view
    }
}

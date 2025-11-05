package com.example.notipadmemo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NotesStore {
    private const val SP_NAME = "notepad"
    private const val KEY = "notes"     // colección de tenedores
    private const val TRASH = "trash"   // papelera de reciclaje

    // "Ayudadores" XD o helpers
    private fun sp(ctx: Context) =
        ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    private fun getArray(sp: android.content.SharedPreferences, key: String) =
        JSONArray(sp.getString(key, "[]"))

    private fun putArray(sp: android.content.SharedPreferences, key: String, arr: JSONArray) =
        sp.edit().putString(key, arr.toString()).apply()

    // los previews
    fun getNotes(ctx: Context): MutableList<String> {
        val arr = getArray(sp(ctx), KEY)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val title = o.optString("title")
            val content = o.optString("content")
            val preview = if (title.isNotBlank()) title else content.lineSequence().firstOrNull().orEmpty()
            list.add(preview.ifBlank { "(Sin título)" })
        }
        return list
    }

    // el CRUDo de notas
    fun addNote(ctx: Context, title: String, content: String, color: String, favorite: Boolean, pinned: Boolean) {
        val sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray(sp.getString(KEY, "[]"))
        val obj = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("color", color)
            put("favorite", favorite)
            put("pinned", pinned)
            put("time", System.currentTimeMillis())
        }
        arr.put(obj)
       sp.edit().putString(KEY, arr.toString()).apply()
    }

    fun getNote(ctx: Context, index: Int): JSONObject? {
        val arr = getArray(sp(ctx), KEY)
        return if (index in 0 until arr.length()) arr.getJSONObject(index) else null
    }

    fun updateNote(ctx: Context, index: Int, title: String, content: String, color: String, favorite: Boolean, pinned: Boolean) {
        val sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray(sp.getString(KEY, "[]"))
        if (index !in 0 until arr.length()) return

        val obj = arr.getJSONObject(index)
        obj.put("title", title)
        obj.put("content", content)
        obj.put("color", color)
        obj.put("favorite", favorite)
        obj.put("pinned", pinned)
        obj.put("time", System.currentTimeMillis())

        sp.edit().putString(KEY, arr.toString()).apply()
    }

    // Papaleta (papelera)
    fun moveToTrash(ctx: Context, index: Int) {
        val s = sp(ctx)
        val notes = getArray(s, KEY)
        if (index !in 0 until notes.length()) return
        val removed = notes.remove(index)   // requiere minSdk 19 (OK en la mayoría)
        val trash = getArray(s, TRASH)
        if (removed is JSONObject) trash.put(removed)
        putArray(s, KEY, notes)
        putArray(s, TRASH, trash)
    }

    fun getTrash(ctx: Context): MutableList<String> {
        val arr = getArray(sp(ctx), TRASH)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val title = o.optString("title")
            val content = o.optString("content")
            val preview = if (title.isNotBlank()) title else content.lineSequence().firstOrNull().orEmpty()
            list.add(preview.ifBlank { "(Sin título)" })
        }
        return list
    }

    fun restoreFromTrash(ctx: Context, trashIndex: Int) {
        val s = sp(ctx)
        val trash = getArray(s, TRASH)
        if (trashIndex !in 0 until trash.length()) return
        val item = trash.remove(trashIndex)
        val notes = getArray(s, KEY)
        if (item is JSONObject) notes.put(item)
        putArray(s, TRASH, trash)
        putArray(s, KEY, notes)
    }

    fun deleteFromTrash(ctx: Context, trashIndex: Int) {
        val s = sp(ctx)
        val trash = getArray(s, TRASH)
        if (trashIndex !in 0 until trash.length()) return
        trash.remove(trashIndex)
        putArray(s, TRASH, trash)
    }
}

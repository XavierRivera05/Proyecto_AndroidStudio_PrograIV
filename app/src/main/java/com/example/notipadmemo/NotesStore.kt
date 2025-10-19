package com.example.notipadmemo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NotesStore {
    private const val SP_NAME = "notepad"
    private const val KEY = "notes"

    fun getNotes(ctx: Context): MutableList<String> {
        val sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val json = sp.getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val title = o.optString("title")
            val content = o.optString("content")
            // Texto que se mostrará en la lista (título + primera línea)
            val preview = if (title.isNotBlank()) title else content.lines().firstOrNull().orEmpty()
            list.add(preview.ifBlank { "(Sin título)" })
        }
        return list
    }

    fun addNote(ctx: Context, title: String, content: String) {
        val sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray(sp.getString(KEY, "[]"))
        val obj = JSONObject().apply {
            put("title", title)
            put("content", content)
            put("time", System.currentTimeMillis())
        }
        arr.put(obj)
        sp.edit().putString(KEY, arr.toString()).apply()
    }

    fun moveToTrash(ctx: Context, index: Int) {
        val sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        val notes = org.json.JSONArray(sp.getString(KEY, "[]"))
        if (index < 0 || index >= notes.length()) return

        val removed = notes.remove(index) // requiere minSdk ≥ 19 (ok)
        val trash = org.json.JSONArray(sp.getString("trash", "[]"))
        if (removed is org.json.JSONObject) trash.put(removed)

        sp.edit()
            .putString(KEY, notes.toString())
            .putString("trash", trash.toString())
            .apply()
    }
}
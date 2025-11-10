package com.example.notipadmemo

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object NotesStore {

    private const val FILE_NAME = "notes.json"
    private const val TRASH_FILE = "trash.json"
    private val database = FirebaseDatabase.getInstance().reference.child("notas")

    // ============================================================
    // 📘 SECCIÓN DE NOTAS ACTIVAS
    // ============================================================

    fun getAllNotes(context: Context): MutableList<JSONObject> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()
        val text = file.readText()
        if (text.isBlank()) return mutableListOf()
        val jsonArray = JSONArray(text)
        val notes = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            notes.add(jsonArray.getJSONObject(i))
        }
        return notes
    }

    private fun saveAllNotes(context: Context, notes: List<JSONObject>) {
        val jsonArray = JSONArray(notes)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonArray.toString())
    }

    // 🔹 Guarda o actualiza una nota local y la sube a Firebase (sin duplicar)
    fun saveNote(context: Context, note: JSONObject) {
        val notes = getAllNotes(context)

        // Verificar o crear ID único
        var noteId = note.optString("id")
        if (noteId.isBlank()) {
            noteId = UUID.randomUUID().toString()
            note.put("id", noteId)
        }

        // Buscar si ya existe una nota con ese ID
        val existingIndex = notes.indexOfFirst { it.optString("id") == noteId }

        if (existingIndex >= 0) {
            notes[existingIndex] = note
        } else {
            notes.add(note)
        }

        saveAllNotes(context, notes)

        uploadNoteToFirebase(note)
    }

    // 🔹 Sube o actualiza una nota específica en Firebase (con ID fijo y sincronizado)
    private fun uploadNoteToFirebase(note: JSONObject) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "usuario1"
        var noteId = note.optString("id")

        // 🔸 Asegurar que tenga un ID
        if (noteId.isBlank()) {
            noteId = UUID.randomUUID().toString()
            note.put("id", noteId)
        }

        val ref = database.child(userId).child(noteId) // 🔥 usa ID fijo, no push()

        val firebaseNote = Nota(
            id = noteId,
            title = note.optString("title"),
            content = note.optString("content"),
            pinned = note.optBoolean("pinned", false),
            favorite = note.optBoolean("favorite", false),
            time = note.optLong("time", System.currentTimeMillis())
        )

        ref.setValue(firebaseNote)
            .addOnSuccessListener {
                Log.d("FirebaseSync", "✅ Nota subida/actualizada: $noteId")
            }
            .addOnFailureListener {
                Log.e("FirebaseSync", "❌ Error al subir nota: ${it.message}")
            }
    }

    // 🔹 Elimina una nota de Firebase usando su ID exacto
    fun deleteNoteFromFirebase(noteId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "usuario1"
        if (noteId.isBlank()) {
            Log.e("FirebaseSync", "⚠️ No se puede eliminar: ID vacío")
            return
        }

        val ref = database.child(userId).child(noteId)
        ref.removeValue()
            .addOnSuccessListener {
                Log.d("FirebaseSync", "🗑️ Nota eliminada correctamente: $noteId")
            }
            .addOnFailureListener {
                Log.e("FirebaseSync", "❌ Error al eliminar nota: ${it.message}")
            }
    }

    // ============================================================
    // 🗑️ SECCIÓN DE PAPELERA
    // ============================================================

    fun getTrash(context: Context): MutableList<JSONObject> {
        val file = File(context.filesDir, TRASH_FILE)
        if (!file.exists()) return mutableListOf()
        val text = file.readText()
        if (text.isBlank()) return mutableListOf()
        val jsonArray = JSONArray(text)
        val notes = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            notes.add(jsonArray.getJSONObject(i))
        }
        return notes
    }

    private fun saveTrash(context: Context, notes: List<JSONObject>) {
        val jsonArray = JSONArray(notes)
        val file = File(context.filesDir, TRASH_FILE)
        file.writeText(jsonArray.toString())
    }

    fun moveToTrash(context: Context, index: Int) {
        val notes = getAllNotes(context)
        if (index < 0 || index >= notes.size) return
        val note = notes[index]

        notes.removeAt(index)
        saveAllNotes(context, notes)

        val trash = getTrash(context)
        trash.add(note)
        saveTrash(context, trash)

        deleteNoteFromFirebase(note.optString("id"))
    }

    fun restoreFromTrash(context: Context, index: Int) {
        val trash = getTrash(context)
        if (index < 0 || index >= trash.size) return
        val note = trash[index]

        val notes = getAllNotes(context)
        notes.add(note)
        saveAllNotes(context, notes)

        trash.removeAt(index)
        saveTrash(context, trash)

        uploadNoteToFirebase(note)
    }

    fun deleteFromTrash(context: Context, index: Int) {
        val trash = getTrash(context)
        if (index < 0 || index >= trash.size) return
        val note = trash[index]
        trash.removeAt(index)
        saveTrash(context, trash)

        deleteNoteFromFirebase(note.optString("id"))
    }

    fun clearTrash(context: Context) {
        val file = File(context.filesDir, TRASH_FILE)
        if (file.exists()) file.delete()
    }

    // ============================================================
    // ☁️ SINCRONIZACIÓN CON FIREBASE
    // ============================================================

    fun syncAllNotesToFirebase(context: Context) {
        val allNotes = getAllNotes(context)
        for (note in allNotes) uploadNoteToFirebase(note)
    }

    fun fetchNotesFromFirebase(context: Context, onComplete: (() -> Unit)? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = database.child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val notesList = mutableListOf<JSONObject>()
                for (child in snapshot.children) {
                    val noteMap = child.value as? Map<*, *> ?: continue
                    val noteObj = JSONObject()
                    noteObj.put("id", noteMap["id"])
                    noteObj.put("title", noteMap["title"])
                    noteObj.put("content", noteMap["content"])
                    noteObj.put("color", noteMap["color"])
                    noteObj.put("favorite", noteMap["favorite"])
                    noteObj.put("pinned", noteMap["pinned"])
                    noteObj.put("time", noteMap["time"])
                    notesList.add(noteObj)
                }

                val file = File(context.filesDir, FILE_NAME)
                val jsonArray = JSONArray(notesList)
                file.writeText(jsonArray.toString())

                Log.d("FirebaseSync", "✅ Notas descargadas: ${notesList.size}")
            } else {
                Log.d("FirebaseSync", "ℹ️ No hay notas en Firebase.")
            }
            onComplete?.invoke()
        }.addOnFailureListener {
            Log.e("FirebaseSync", "❌ Error al descargar notas: ${it.message}")
            onComplete?.invoke()
        }
    }

    fun cleanupEmptyNotes() {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: "usuario1"
        val ref = database.child(userId)

        ref.get().addOnSuccessListener { snapshot ->
            for (note in snapshot.children) {
                val map = note.value as? Map<*, *>
                val title = map?.get("title")?.toString()?.trim() ?: ""
                val content = map?.get("content")?.toString()?.trim() ?: ""

                if (title.isEmpty() && content.isEmpty()) {
                    note.ref.removeValue()
                    Log.d("FirebaseSync", "🧹 Nota vacía eliminada: ${note.key}")
                }
            }
        }.addOnFailureListener {
            Log.e("FirebaseSync", "❌ Error al limpiar Firebase: ${it.message}")
        }
    }
}

package com.example.notipadmemo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    /** Aplica el tema guardado al abrir la app */
    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }

    /** Alterna entre modo claro y oscuro y lo guarda */
    fun toggleTheme(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val newMode =
            if (current == AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.MODE_NIGHT_NO
            else
                AppCompatDelegate.MODE_NIGHT_YES

        AppCompatDelegate.setDefaultNightMode(newMode)
        prefs.edit().putInt(KEY_THEME_MODE, newMode).apply()
        return newMode
    }
}

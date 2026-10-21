package com.example.laruta.data

import android.content.Context
import com.example.laruta.model.ThemeMode
import com.example.laruta.model.UserSettings

/** Guarda las preferencias del usuario (nombre, zancada, tema) en SharedPreferences. */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("laruta_settings", Context.MODE_PRIVATE)

    fun load(): UserSettings {
        val defaults = UserSettings()
        return UserSettings(
            userName = prefs.getString(KEY_NAME, null) ?: defaults.userName,
            strideMeters = prefs.getFloat(KEY_STRIDE, defaults.strideMeters),
            themeMode = runCatching {
                ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: defaults.themeMode.name)
            }.getOrDefault(defaults.themeMode)
        )
    }

    fun save(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_NAME, settings.userName)
            .putFloat(KEY_STRIDE, settings.strideMeters)
            .putString(KEY_THEME, settings.themeMode.name)
            .apply()
    }

    private companion object {
        const val KEY_NAME = "user_name"
        const val KEY_STRIDE = "stride_meters"
        const val KEY_THEME = "theme_mode"
    }
}

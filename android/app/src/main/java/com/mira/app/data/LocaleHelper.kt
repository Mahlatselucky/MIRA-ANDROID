package com.mira.app.data

/**
 * Switches the app's display language between English, isiZulu and
 * Afrikaans, and remembers the choice on the device so it survives an
 * app restart. The language code (en, zu, af) is the same code saved
 * to the server's settings endpoint.
 */

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "mira_locale_prefs"
    private const val KEY_LANGUAGE = "language"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    /** Saves the chosen language and restarts the activity so every screen re-reads the new strings. */
    fun setLocale(activity: Activity, languageCode: String) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        activity.recreate()
    }

    /** Wraps a base Context with the saved language. Called from Activity.attachBaseContext. */
    fun wrapContext(context: Context): Context {
        val languageCode = getSavedLanguage(context)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

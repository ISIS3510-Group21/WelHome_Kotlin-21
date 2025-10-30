package com.team21.myapplication.local

import android.content.Context
import android.content.SharedPreferences

/**
 * [LOCAL STORAGE]
 * Preferences específicas de Results para guardar/leer el token
 * de la última combinación de filtros usada.
 */
class ResultsPrefs private constructor(ctx: Context) {

    private val sp: SharedPreferences =
        ctx.getSharedPreferences("results_prefs", Context.MODE_PRIVATE)

    fun saveLastResultsToken(token: String) {
        sp.edit().putString(KEY_RESULTS_TOKEN, token).apply()
    }

    fun readLastResultsToken(): String? = sp.getString(KEY_RESULTS_TOKEN, null)

    companion object {
        private const val KEY_RESULTS_TOKEN = "last_results_token"

        @Volatile private var INSTANCE: ResultsPrefs? = null
        fun get(ctx: Context): ResultsPrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ResultsPrefs(ctx.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
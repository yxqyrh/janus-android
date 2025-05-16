package com.yxqyrh.janusandroid.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke

object TokenPreferencesRepository {
    // TODO AES encrypt
    private val Context.tokenPreferences by preferencesDataStore("token_preferences")

    private val sessionIdPreferencesKey by lazy { longPreferencesKey("session_id") }

    suspend fun readSessionId(context: Context): Long =
        (Dispatchers.IO) {
            context.tokenPreferences.data.map { preferences ->
                preferences[sessionIdPreferencesKey] ?: 0
            }.first()
        }

    suspend fun saveSessionId(context: Context, sessionId: Long) {
        (Dispatchers.IO) {
            context.tokenPreferences.edit { preferences ->
                preferences[sessionIdPreferencesKey] = sessionId
            }
        }
    }
}


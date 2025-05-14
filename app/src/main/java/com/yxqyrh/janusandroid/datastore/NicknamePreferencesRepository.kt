package com.yxqyrh.janusandroid.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.invoke

object NicknamePreferencesRepository {
    private val Context.nicknamePreferences by preferencesDataStore("nickname_preferences")

    private val nicknamePreferencesKey by lazy { stringPreferencesKey("nickname") }

    suspend fun readNickname(context: Context): String? =
        (Dispatchers.IO) {
            context.nicknamePreferences.data.map { preferences ->
                preferences[nicknamePreferencesKey]
            }.first()
        }

    suspend fun saveNickname(context: Context, nickname: String) {
        (Dispatchers.IO) {
            context.nicknamePreferences.edit { preferences ->
                preferences[nicknamePreferencesKey] = nickname
            }
        }
    }
}
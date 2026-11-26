package com.salvemidia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsManager(context: Context) {
    private val dataStore = context.dataStore

    val defaultQuality: Flow<String> = dataStore.data.map { it[QUALITY_KEY] ?: "720p" }
    val defaultFormat: Flow<String> = dataStore.data.map { it[FORMAT_KEY] ?: "MP4" }

    suspend fun setQuality(quality: String) {
        dataStore.edit { it[QUALITY_KEY] = quality }
    }

    suspend fun setFormat(format: String) {
        dataStore.edit { it[FORMAT_KEY] = format }
    }

    companion object {
        private val QUALITY_KEY = stringPreferencesKey("default_quality")
        private val FORMAT_KEY = stringPreferencesKey("default_format")
    }
}

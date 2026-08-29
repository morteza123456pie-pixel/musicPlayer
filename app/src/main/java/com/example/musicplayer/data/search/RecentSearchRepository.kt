package com.example.musicplayer.data.search

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchDataStore by preferencesDataStore(name = "search_history")

/**
 * Persists recent search queries using DataStore, per the Phase 4
 * brief. Queries are stored as a single ordered JSON array (most
 * recent first) inside one string preference key rather than a Room
 * table — this is small, unstructured, device-local history, which is
 * exactly what DataStore's Preferences API is for; a full relational
 * table would be overkill for a capped list of strings.
 */
@Singleton
class RecentSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val historyKey = stringPreferencesKey("recent_search_history_json")
    private val maxHistorySize = 10

    /** Ordered (most recent first) recent search queries. */
    val recentSearches: Flow<List<String>> = context.searchDataStore.data.map { prefs ->
        decode(prefs[historyKey])
    }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        context.searchDataStore.edit { prefs ->
            val existing = decode(prefs[historyKey])
            val updated = listOf(trimmed) + existing.filterNot { it.equals(trimmed, ignoreCase = true) }
            prefs[historyKey] = encode(updated.take(maxHistorySize))
        }
    }

    suspend fun clearHistory() {
        context.searchDataStore.edit { prefs ->
            prefs.remove(historyKey)
        }
    }

    private fun decode(raw: String?): List<String> {
        if (raw.isNullOrEmpty()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { i -> array.getString(i) }
        }.getOrDefault(emptyList())
    }

    private fun encode(queries: List<String>): String {
        val array = JSONArray()
        queries.forEach { array.put(it) }
        return array.toString()
    }
}

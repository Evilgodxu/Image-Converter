package com.tpgszhq.jh.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

// 更新检查记录仓库契约：记录最近一次自动检查日期，保证每日仅自动检查一次
interface UpdateCheckRepository {
    suspend fun lastAutoCheckDate(): String?

    suspend fun setLastAutoCheckDate(date: String)
}

// DataStore 实现：与设置共用同一份 DataStore，数据源经构造注入便于单测
class DataStoreUpdateCheckRepository(
    private val dataStore: DataStore<Preferences>,
) : UpdateCheckRepository {

    override suspend fun lastAutoCheckDate(): String? =
        dataStore.data.first()[LAST_AUTO_CHECK_DATE]

    override suspend fun setLastAutoCheckDate(date: String) {
        dataStore.edit { preferences ->
            preferences[LAST_AUTO_CHECK_DATE] = date
        }
    }

    private companion object {
        val LAST_AUTO_CHECK_DATE = stringPreferencesKey("last_auto_update_check_date")
    }
}

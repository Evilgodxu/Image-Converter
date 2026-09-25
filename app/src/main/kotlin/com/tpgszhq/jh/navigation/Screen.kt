package com.tpgszhq.jh.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// 首页路由键
@Serializable
data object Home : NavKey

// 设置页路由键
@Serializable
data object Settings : NavKey

// 隐私政策路由键：首启未同意时作为起始页
@Serializable
data object Privacy : NavKey

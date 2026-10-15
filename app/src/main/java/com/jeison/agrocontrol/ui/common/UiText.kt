package com.jeison.agrocontrol.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Dynamic(val value: String) : UiText()
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is Dynamic -> value
        is Resource -> if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())
    }
}

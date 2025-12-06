package com.campusface.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Um canal de comunicação global simples para notificar
 * quando dados importantes mudam no app.
 */
object AppEventBus {
    private val _refreshFlow = MutableSharedFlow<Unit>(replay = 0)
    val refreshFlow = _refreshFlow.asSharedFlow()

    suspend fun emitRefresh() {
        _refreshFlow.emit(Unit)
    }
}
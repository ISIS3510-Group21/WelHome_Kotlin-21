package com.team21.myapplication.sync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object ScheduleUpdateBus {
    private val _updates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updates: SharedFlow<Unit> = _updates

    fun notifyUpdated() {
        _updates.tryEmit(Unit)
    }
}

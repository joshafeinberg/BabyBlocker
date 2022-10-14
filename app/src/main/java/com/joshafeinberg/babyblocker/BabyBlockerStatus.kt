package com.joshafeinberg.babyblocker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BabyBlockerStatus {

    private val _babyBlockerStatus = MutableStateFlow(false)
    val babyBlockerStatus: StateFlow<Boolean> = _babyBlockerStatus

    private val _notificationActive = MutableStateFlow(false)
    val notificationActive: StateFlow<Boolean> = _notificationActive

    suspend fun updateStatus(isActive: Boolean) {
        _babyBlockerStatus.emit(isActive)
    }

    suspend fun toggleNotification(isActive: Boolean) {
        _notificationActive.emit(isActive)
    }

}
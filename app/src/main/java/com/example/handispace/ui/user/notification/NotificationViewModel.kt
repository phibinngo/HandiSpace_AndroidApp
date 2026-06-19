package com.example.handispace.ui.user.notification

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handispace.data.repository.user.NotificationRepository
import com.example.handispace.model.Notification
import com.example.handispace.utils.ResultState
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var notificationsState = mutableStateOf(ResultState<Map<String, List<Notification>>>())
        private set

    val unreadCount: StateFlow<Int> = notificationRepository.listenUnreadCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun loadNotifications() {
        viewModelScope.launch {
            notificationsState.value = ResultState(isLoading = true)
            val res = notificationRepository.getMyNotifications()

            val safeData = res.data

            if (safeData != null) {
                val formatter = SimpleDateFormat("d/M", Locale.getDefault())
                val grouped = safeData.groupBy { noti ->
                    val timestamp = noti.created_at as? Timestamp
                    if (timestamp != null) formatter.format(timestamp.toDate()) else "Khác"
                }
                notificationsState.value = ResultState(data = grouped)
            } else {
                notificationsState.value = ResultState(errorMessage = res.errorMessage)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val res = notificationRepository.markAllAsRead()
            if (res.data == true) {
                loadNotifications()
            }
        }
    }
}
package com.raveline.concord.ui.chat

import com.raveline.concord.data.Chat

data class ChatListUiState(
    val selectedId: Long? = null,
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true
)
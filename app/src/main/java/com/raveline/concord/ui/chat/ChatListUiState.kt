package com.raveline.concord.ui.chat

import com.raveline.concord.database.entity.Chat

data class ChatListUiState(
    val selectedId: Long? = null,
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true
)
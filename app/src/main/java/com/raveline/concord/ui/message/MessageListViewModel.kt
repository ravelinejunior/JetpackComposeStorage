package com.raveline.concord.ui.message

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raveline.concord.data.Author
import com.raveline.concord.data.Message
import com.raveline.concord.data.messageListSample
import com.raveline.concord.database.ChatDao
import com.raveline.concord.database.MessageDao
import com.raveline.concord.navigation.messageChatIdArgument
import com.raveline.concord.util.getFormattedCurrentDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MessageListUiState())
    val uiState: StateFlow<MessageListUiState>
        get() = _uiState.asStateFlow()

    private var chatId: Long =
        requireNotNull(savedStateHandle.get<String>(messageChatIdArgument)?.toLong())

    init {
        initWithSamples()
        loadDatas()

        _uiState.update { state ->
            state.copy(
                onMessageValueChange = {
                    _uiState.value = _uiState.value.copy(
                        messageValue = it
                    )

                    _uiState.value = _uiState.value.copy(
                        hasContentToSend = (it.isNotEmpty() || _uiState.value.mediaInSelection.isNotEmpty())
                    )
                },

                onMediaInSelectionChange = {
                    _uiState.value = _uiState.value.copy(
                        mediaInSelection = it
                    )
                    _uiState.value = _uiState.value.copy(
                        hasContentToSend = (it.isNotEmpty() || _uiState.value.messageValue.isNotEmpty())
                    )
                }
            )
        }
    }

    private fun initWithSamples() {
        _uiState.value = _uiState.value.copy(
            messages = messageListSample,
        )
        loadChatsInfos()
    }

    private fun loadDatas() {
        loadChatsInfos()
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            messageDao.getByChatId(chatId).collect { messages ->
                messages.let {
                    _uiState.value = _uiState.value.copy(
                        messages = it
                    )
                }
            }
        }
    }

    private fun loadChatsInfos() {
        viewModelScope.launch {
            val chat = chatDao.getById(chatId).first()
            chat?.let {
                _uiState.value = _uiState.value.copy(
                    ownerName = chat.owner,
                    profilePicOwner = chat.profilePicOwner
                )
            }
        }
    }

    private fun saveMessage(
        userMessage: Message
    ) {
        viewModelScope.launch {
            userMessage.let { messageDao.insert(it) }
        }
    }

    private fun cleanFields() {
        _uiState.value = _uiState.value.copy(
            messageValue = "",
            mediaInSelection = "",
            hasContentToSend = false
        )
    }

    fun sendMessage() {
        with(_uiState) {
            if (!value.hasContentToSend) {
                return
            }

            val userMessage = Message(
                content = value.messageValue,
                author = Author.USER,
                chatId = chatId,
                mediaLink = value.mediaInSelection,
                date = getFormattedCurrentDate(),
            )
            saveMessage(userMessage)
            cleanFields()
        }
    }

    fun loadMediaInScreen(
        path: String
    ) {
        _uiState.value.onMediaInSelectionChange(path)
    }

    fun deselectMedia() {
        _uiState.value = _uiState.value.copy(
            mediaInSelection = "",
            hasContentToSend = false
        )
    }

    fun setImagePermission(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasImagePermission = value,
        )
    }


    fun setShowBottomSheetSticker(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            showBottomSheetSticker = value,
        )
    }

    fun setShowBottomSheetFile(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            showBottomSheetFile = value,
        )
    }

}
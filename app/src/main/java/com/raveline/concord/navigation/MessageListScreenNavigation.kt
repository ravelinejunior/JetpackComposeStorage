package com.raveline.concord.navigation

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.raveline.concord.extensions.showLog
import com.raveline.concord.extensions.showMessage
import com.raveline.concord.ui.components.ModalBottomSheetFile
import com.raveline.concord.ui.components.ModalBottomSheetSticker
import com.raveline.concord.ui.message.MessageListViewModel
import com.raveline.concord.ui.message.MessageScreen

val TAG: String = "MessageListScreenNavigationTAG"
internal const val messageChatRoute = "messages"
internal const val messageChatIdArgument = "chatId"
internal const val messageChatFullPath = "$messageChatRoute/{$messageChatIdArgument}"


fun NavGraphBuilder.messageListScreen(
    onBack: () -> Unit = {},
) {
    composable(messageChatFullPath) { backStackEntry ->
        val context = LocalContext.current
        backStackEntry.arguments?.getString(messageChatIdArgument)?.let { chatId ->
            val viewModelMessage = hiltViewModel<MessageListViewModel>()
            val uiState by viewModelMessage.uiState.collectAsState()


            MessageScreen(
                state = uiState,
                onSendMessage = {
                    viewModelMessage.sendMessage()
                },
                onShowSelectorFile = {
                    viewModelMessage.setShowBottomSheetFile(true)
                },
                onShowSelectorStickers = {
                    viewModelMessage.setShowBottomSheetSticker(true)
                },
                onDeselectMedia = {
                    viewModelMessage.deselectMedia()
                },
                onBack = {
                    onBack()
                }
            )

            if (uiState.showBottomSheetSticker) {
                val stickerList = mutableStateListOf<String>()

                context.getExternalFilesDir("stickers")?.listFiles()?.forEach { file ->
                    stickerList.add(file.path)
                }

                ModalBottomSheetSticker(
                    stickerList = stickerList,
                    onSelectedSticker = {
                        viewModelMessage.setShowBottomSheetSticker(false)
                        viewModelMessage.loadMediaInScreen(path = it.toString())
                        viewModelMessage.sendMessage()
                    }, onBack = {
                        viewModelMessage.setShowBottomSheetSticker(false)
                    })
            }

            //Create the screen that calls the Media Selector
            val pickMedia = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    context.showMessage(message = "Selected Uri: $uri")
                    context.showLog(tag = TAG, message = "Selected Uri: $uri")
                    viewModelMessage.loadMediaInScreen(uri.toString())
                } else {
                    context.showLog(TAG, "No media selected")
                }
            }

            /*
            * Get the file's content URI from the incoming Intent,
            * then query the server app to get the file's display name
            * and size.
            */
            val pickedFile =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument())
                { uri ->
                    if (uri != null) {
                        val name = context.contentResolver.query(
                            uri, null, null, null, null
                        ).use { cursor ->
                            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor?.moveToFirst()
                            nameIndex?.let {
                                cursor.getString(it)
                            }
                        }

                        val size = context.contentResolver.query(
                            uri, null, null, null, null
                        ).use { cursor ->
                            val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
                            cursor?.moveToFirst()
                            sizeIndex?.let {
                                cursor.getInt(it)
                            }
                        }

                        uiState.onMessageValueChange("$name\nFile Size:$size")
                        viewModelMessage.loadMediaInScreen(uri.toString())
                        viewModelMessage.sendMessage()
                    } else {
                        context.showLog(TAG, "No media selected")
                    }
                }

            if (uiState.showBottomSheetFile) {
                ModalBottomSheetFile(
                    onSelectPhoto = {
                        pickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                        viewModelMessage.setShowBottomSheetFile(false)
                    },
                    onSelectFile = {
                        pickedFile.launch(arrayOf("*/*"))
                        viewModelMessage.setShowBottomSheetFile(false)
                    }, onBack = {
                        viewModelMessage.setShowBottomSheetFile(false)
                    })
            }
        }
    }
}


internal fun NavHostController.navigateToMessageScreen(
    chatId: Long,
    navOptions: NavOptions? = null
) {
    navigate("$messageChatRoute/$chatId", navOptions)
}


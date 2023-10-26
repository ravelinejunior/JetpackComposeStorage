package com.raveline.concord.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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

            // Requesting permission
            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog. Save the return value, an instance of
            // ActivityResultLauncher. You can use either a val, as shown in this snippet,
            // or a lateinit var in your onAttach() or onCreate() method.
            val requestPermissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your app.
                        context.showMessage("Permission Granted Successfully!")
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // feature requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                        context.showMessage("Permission NOT Granted Successfully!")
                    }
                }

            if (uiState.showBottomSheetSticker) {

                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                requestPermissionLauncher.launch(permission)

                val stickerList = mutableStateListOf<String>()
                getAllImages(context) {
                    it.map { pairValue ->
                        stickerList.add(pairValue.second)
                    }
                }

                //Gets all external stickers from the path on the device
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
                    persistAccessFile(context, uri)
                    viewModelMessage.loadMediaInScreen(uri.toString())
                    context.showLog(tag = TAG, message = "Selected Uri: $uri")
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

                        persistAccessFile(context, uri)

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

private fun getAllImages(context: Context, onLoadImages: (List<Pair<String, String>>) -> Unit) {
    val pairImagePath = mutableListOf<Pair<String, String>>()
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.SIZE
    )

    val selection =
        "${MediaStore.Images.Media.DATA} LIKE '%/Download/stickers/%' AND ${MediaStore.Images.Media.SIZE} < ?"

    val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} DESC"
    val selectionArgs = arrayOf("120000")

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->

        while (cursor.moveToNext()) {
            // Use an ID column from the projection to get
            // a URI representing the media item itself.
            val nameIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val sizeIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

            val name = cursor.getString(nameIndex)
            val path = cursor.getString(pathIndex)
            val fileSize = cursor.getString(sizeIndex)

            context.showLog(TAG, "Name: $name, Path: $path, Size: ${fileSize}Kb")

            pairImagePath.add(Pair(name, path))
        }

        onLoadImages(pairImagePath)
    }

}

private fun persistAccessFile(context: Context, uri: Uri) {
    val contentResolver = context.contentResolver
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
    // Check for the freshest data.
    contentResolver.takePersistableUriPermission(uri, takeFlags)
}


internal fun NavHostController.navigateToMessageScreen(
    chatId: Long,
    navOptions: NavOptions? = null
) {
    navigate("$messageChatRoute/$chatId", navOptions)
}

private fun ignoreIt() {

    arrayOf("*/*") // Lista tudo
    arrayOf("image/*") // Lista todas imagens
    arrayOf("image/jpeg") // Lista todas imagens com extensão jpeg / jpg
    arrayOf("video/*") // Todos vídeos
    arrayOf("video/mp4") // Todos vídeos MP4
    arrayOf("audio/*") // Todos áudios
    arrayOf("audio/*") // Todos videos MP3
    arrayOf("application/*") // Tudo que é Imagem, Video ou Audio
    arrayOf("application/pdf") // Todos arquivos do tipo PDF

    arrayOf(
        "image/png",
        "video/mp4",
        "text/*"
    ) // Busca todas imagens .png, todos videos .mp4 e todos arquivos de texto simples

}


package com.raveline.concord.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.room.Ignore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

object DownloadServices {

    suspend fun makeDownloadByUrl(url: String, context: Context) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        withContext(IO) {
            client.newCall(request)
                .execute().use { response: Response ->
                    response.body?.let { innerBody ->
                        innerBody.byteStream().use { fileData ->
                            val path = context.getExternalFilesDir("temp")
                            val newFile = File(path, "Test.png")

                            newFile.outputStream().use { file: FileOutputStream ->
                                fileData.copyTo(file)
                            }
                        }
                    }
                }
        }
    }

    fun makeDownload(
        context: Context,
        url: String,
        fileName: String
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId =
            downloadManager.enqueue(request) // Usado para monitoramendo das notificações
    }

    @Ignore
    private fun explanationCode() {
        Environment.DIRECTORY_PICTURES // Para salvar imagens
        Environment.DIRECTORY_DOCUMENTS // Para salvar documentos
        Environment.DIRECTORY_MOVIES // Para salvar vídeos
        Environment.DIRECTORY_MUSIC // Para salvar músicas e áudios
        Environment.DIRECTORY_DOWNLOADS // Para salvar outros tipos de arquivos

    }

}
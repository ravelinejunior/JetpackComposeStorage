package com.raveline.concord.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

object DownloadServices {

    fun makeDownloadByUrl(url: String, context: Context) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

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
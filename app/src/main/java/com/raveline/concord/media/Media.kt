package com.raveline.concord.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore


suspend fun Context.getAllImages(onLoadImages: (List<Pair<String, Long>>) -> Unit) {
    val pairImagePath = mutableListOf<Pair<String, Long>>()
    /*val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.SIZE
    )

    val selection =
        "${MediaStore.Images.Media.DATA} LIKE '%/Download/stickers/%' AND ${MediaStore.Images.Media.SIZE} < ?"

    val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
    val selectionArgs = arrayOf("120000")*/
    val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media._ID)
    val selection = null
    val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
    val selectionArgs = null

    contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->

        while (cursor.moveToNext()) {
            // Use an ID column from the projection to get
            // a URI representing the media item itself.
            /*val nameIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val sizeIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

            val name = cursor.getString(nameIndex)
            val path = cursor.getString(pathIndex)
            val fileSize = cursor.getString(sizeIndex)
*/
            //context.showLog(TAG, "Name: $name, Path: $path, Size: ${fileSize}Kb")

            val nameIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val name = cursor.getString(nameIndex)
            val idIndex: Int = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val imageId: Long = cursor.getLong(idIndex)


            //pairImagePath.add(Pair(name, path))
            pairImagePath.add(Pair(name, imageId))
        }

        onLoadImages(pairImagePath)
    }

}

fun getURIById(fileId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        fileId
    )

package com.plcoding.androidstorage.util

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.plcoding.androidstorage.models.SharedStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

inline fun <T> sdk29AndUp(onSDK29 :()-> T):T?{
    return if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.Q){
        onSDK29()
    }else null
}
suspend inline fun parseImages(it:Cursor) : List<SharedStoragePhoto>{

    return withContext(Dispatchers.IO) {
        val idC = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameC = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val widthC = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightC = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val images  = mutableListOf<SharedStoragePhoto>()
        if (it.moveToNext()){
            do {
                val id = it.getLong(idC)
                val name = it.getString(nameC)
                val width = it.getInt(widthC)
                val height = it.getInt(heightC)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                images.add(SharedStoragePhoto(id, name, width, height, contentUri))
                Log.d(ContentValues.TAG, "loadPhotosFromExternalStorage: $name ")
            } while (it.moveToNext())
        }
        it.close()
        images
    }

}
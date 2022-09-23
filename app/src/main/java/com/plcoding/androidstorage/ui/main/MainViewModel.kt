package com.plcoding.androidstorage.ui.main

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentSender
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ShareCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.androidstorage.models.InternalStoragePhoto
import com.plcoding.androidstorage.models.SharedStoragePhoto
import com.plcoding.androidstorage.util.parseImages
import com.plcoding.androidstorage.util.sdk29AndUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

class MainViewModel : ViewModel() {
    // TODO: handle errors
    private val mutableLiveError: MutableLiveData<String> = MutableLiveData()
    val errorMessage: LiveData<String> get() = mutableLiveError
    private val mutableLiveExternalPhotos: MutableLiveData<List<SharedStoragePhoto>> =
        MutableLiveData()
    val externalStoragePhotos: LiveData<List<SharedStoragePhoto>> get() = mutableLiveExternalPhotos

    suspend fun savePhotoToInternalStorage(fileName: String, bmap: Bitmap, context: Context): Boolean {
        return try {
            context.openFileOutput("$fileName.jpg", Context.MODE_PRIVATE).use {
                if (!bmap.compress(Bitmap.CompressFormat.JPEG, 95, it))
                    throw IOException("Couldn't save!")
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            mutableLiveError.postValue(e.localizedMessage)
            false
        }
    }


    suspend fun savePhotoToExternalStorage(name: String, bmap: Bitmap, context: Context): Boolean {
        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.WIDTH, "${bmap.width}")
            put(MediaStore.Images.Media.HEIGHT, "${bmap.height}")
        }
        return try {
            context.contentResolver.insert(collection, contentValues)?.also {
                if (it != null) context.contentResolver.openOutputStream(it).use { os ->
                    if (os != null) {
                        if (!bmap.compress(Bitmap.CompressFormat.JPEG, 95, os)) {
                            throw IOException("Couldn't Save image")
                        }
                    }
                }

            } ?: throw IOException("Couldn't create media store")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loadPhotosFromExternalStorage(context: Context) {

        val images = mutableListOf<SharedStoragePhoto>()
        withContext(Dispatchers.IO) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )
            val c = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} desc"
            )?.use {

                val idC = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameC = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthC = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightC = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                if (it.moveToFirst()) {
                    var counter = 0
                    do {
                        counter++
                        val id = it.getLong(idC)
                        val name = it.getString(nameC)
                        val width = it.getInt(widthC)
                        val height = it.getInt(heightC)
                        val contentUri =
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id)
                        images.add(SharedStoragePhoto(id, name, width, height, contentUri))
                        if (counter == 100) {
                            mutableLiveExternalPhotos.postValue(images.toList())
                            counter = 0
                            delay(10)
                        }

                    } while (it.moveToNext())
                    it.close()
                }
                mutableLiveExternalPhotos.postValue(images.toList())
            }

        }
    }

    suspend fun deletePhotoFromExternalStorage(photo: Uri, context: Context , activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.delete(photo, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(context.contentResolver,
                            listOf(photo)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let {
                    activityResultLauncher.launch(
                        IntentSenderRequest.Builder(it).build()
                    )
                }
            }
        }
    }
}

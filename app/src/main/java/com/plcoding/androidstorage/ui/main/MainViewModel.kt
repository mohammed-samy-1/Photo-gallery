package com.plcoding.androidstorage.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.androidstorage.models.InternalStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class MainViewModel : ViewModel() {
    private val mutableLiveError :MutableLiveData<String> = MutableLiveData()
    val errorMessage : LiveData<String>  get() = mutableLiveError
    private val mutableLiveInternalPhotos :MutableLiveData<List<InternalStoragePhoto>> = MutableLiveData()
    val internalStoragePhotos : LiveData<List<InternalStoragePhoto>>get() = mutableLiveInternalPhotos

    fun savePhotoToInternalStorage(fileName :String, bmap :Bitmap, context: Context): Boolean{
         return try {
            context.openFileOutput("$fileName.jpg", Context.MODE_PRIVATE ).use {
                if (!bmap.compress(Bitmap.CompressFormat.JPEG , 95 , it))
                    throw IOException("Couldn't save!")
            }
            true
        }catch (e :IOException){
            e.printStackTrace()
            mutableLiveError.postValue(e.localizedMessage)
            false
        }
    }
    suspend fun getPhotosFromInternalStorage (context: Context): Boolean{
        val list =
            withContext(Dispatchers.IO){
                val files  =  context.filesDir.listFiles()
                files?.filter {
                    it.canRead()&& it.isFile && it.name.endsWith("jpg")
                }?.map {
                    val byte = it.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(byte,0, byte.size)
                    InternalStoragePhoto(it.name , bitmap)
                }?: listOf()
            }
        mutableLiveInternalPhotos.postValue(list)
        return list.isNotEmpty()
    }

    suspend fun deletePhotoFormInternalStorage(fileName :String, context: Context):Boolean{
        return try {
            context.deleteFile(fileName)
            getPhotosFromInternalStorage(context)
            true
        } catch (e: Exception) {
            mutableLiveError.postValue("Couldn't delete photo")
            false
        }
    }
}
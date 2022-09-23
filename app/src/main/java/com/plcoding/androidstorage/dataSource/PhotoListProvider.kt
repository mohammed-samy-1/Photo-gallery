package com.plcoding.androidstorage.dataSource

import com.plcoding.androidstorage.models.SharedStoragePhoto
import java.lang.Exception

class PhotoListProvider(private var list : List<SharedStoragePhoto>) {
    fun setList(l: List<SharedStoragePhoto>){
        list =l
    }
    fun getList(page:Int , pageSize :Int):MutableList<SharedStoragePhoto>{
        val initialIndex = pageSize * page
        val finalIndex = initialIndex + pageSize - 1

        return try {
            list.subList(initialIndex , finalIndex).toMutableList()
        } catch (e :Exception) {
            list.subList(0, list.size - 1).toMutableList()
        }
    }
}
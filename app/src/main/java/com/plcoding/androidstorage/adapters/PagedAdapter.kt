package com.plcoding.androidstorage.adapters

import android.os.Parcel
import android.os.Parcelable
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.plcoding.androidstorage.databinding.ItemPhotoBinding
import com.plcoding.androidstorage.models.SharedStoragePhoto

class PagedAdapter() : PagedListAdapter<SharedStoragePhoto, SharedPhotoAdapter.PhotoViewHolder>(Companion) {
    companion object : DiffUtil.ItemCallback<SharedStoragePhoto>() {
        override fun areItemsTheSame(oldItem: SharedStoragePhoto, newItem: SharedStoragePhoto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SharedStoragePhoto, newItem: SharedStoragePhoto): Boolean {
            return oldItem == newItem
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SharedPhotoAdapter.PhotoViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: SharedPhotoAdapter.PhotoViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


}
package com.plcoding.androidstorage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.plcoding.androidstorage.databinding.ItemPhotoBinding
import com.plcoding.androidstorage.models.SharedStoragePhoto
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class SharedPhotoAdapter(
    private val context: Context,
    private val onPhotoClick: (SharedStoragePhoto) -> Unit,
) : PagedListAdapter<SharedStoragePhoto, SharedPhotoAdapter.PhotoViewHolder>(PagedAdapter) {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<SharedStoragePhoto>() {
        override fun areItemsTheSame(
            oldItem: SharedStoragePhoto,
            newItem: SharedStoragePhoto,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SharedStoragePhoto,
            newItem: SharedStoragePhoto,
        ): Boolean {
            return oldItem.contentUri == newItem.contentUri
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)


        holder.binding.apply {
            val aspectRatio = photo!!.width.toFloat() / photo.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }
            try {
                Glide.with(context)
                    .load(photo!!.contentUri)
                    .into(holder.binding.ivPhoto)
            }catch (e :FileNotFoundException){
                currentList?.removeAt(position)
            }


            ivPhoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }


        }

    }
}
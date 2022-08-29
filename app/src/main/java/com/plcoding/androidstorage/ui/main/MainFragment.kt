package com.plcoding.androidstorage.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.plcoding.androidstorage.adapters.InternalStoragePhotoAdapter
import com.plcoding.androidstorage.databinding.MainFragmentBinding

import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment() {
    private lateinit var rvPrivate: RecyclerView
    private lateinit var rvShared: RecyclerView
    private lateinit var btnSave: ImageView
    private lateinit var swPrivate: SwitchMaterial
    private lateinit var viewModel: MainViewModel
    private lateinit var privateAdapter: InternalStoragePhotoAdapter
    private lateinit var binding: MainFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        MainFragmentBinding.inflate(inflater)
        binding = MainFragmentBinding.inflate(inflater)

        initView()

        return binding.root
    }

    private fun initView() {
        rvPrivate = binding.rvPrivatePhotos
        rvShared = binding.rvPublicPhotos
        btnSave = binding.btnTakePhoto
        swPrivate = binding.switchPrivate
        privateAdapter = InternalStoragePhotoAdapter {
            lifecycleScope.launch {
                viewModel.deletePhotoFormInternalStorage(it.name, requireContext())
            }
        }
    }



    private fun initRv() {
        rvPrivate.apply {
            adapter = privateAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        lifecycleScope.launch {
            viewModel.getPhotosFromInternalStorage(requireContext())

        }
        viewModel.internalStoragePhotos.observe(viewLifecycleOwner) {
            privateAdapter.submitList(it)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        initRv()
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = binding.switchPrivate.isChecked
            if (isPrivate && it != null) {
                if (viewModel.savePhotoToInternalStorage(UUID.randomUUID().toString(),
                        it,
                        requireContext())
                ) {
                    Toast.makeText(requireContext(),"photo saved",Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        viewModel.getPhotosFromInternalStorage(requireContext())
                    }
                }
                lifecycleScope.launch {
                    viewModel.getPhotosFromInternalStorage(requireContext())
                }
            }

        }
        binding.btnTakePhoto.setOnClickListener{
            takePhoto.launch()
        }
    }

}
package com.plcoding.androidstorage.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.plcoding.androidstorage.R
import com.plcoding.androidstorage.adapters.InternalStoragePhotoAdapter
import com.plcoding.androidstorage.adapters.SharedPhotoAdapter
import com.plcoding.androidstorage.databinding.MainFragmentBinding
import com.plcoding.androidstorage.databinding.PrivetFragmentBinding
import com.plcoding.androidstorage.ui.main.MainViewModel
import kotlinx.coroutines.launch

class PrivetFragment : Fragment() {
    private lateinit var rvPrivate: RecyclerView
    private lateinit var privateAdapter: InternalStoragePhotoAdapter
    private lateinit var binding: PrivetFragmentBinding
//    private var isAuth = true
//    private var readExternalsStorage = false
//    private var writeExternalsStorage = false
//    private lateinit var primitionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var viewModel: PrivetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PrivetFragmentBinding.inflate(inflater)
        initView()
        rvPrivate.apply {
            adapter = privateAdapter
            layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PrivetViewModel::class.java]

        lifecycleScope.launch {

            viewModel.getPhotosFromInternalStorage(requireContext())

        }
        viewModel.internalStoragePhotos.observe(viewLifecycleOwner) {
            privateAdapter.submitList(it)

        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.internalStoragePhotos.removeObservers(this)

    }

    private fun initView() {
        rvPrivate = binding.rvPrivatePhotos
        privateAdapter = InternalStoragePhotoAdapter {
            lifecycleScope.launch {
                viewModel.deletePhotoFormInternalStorage(it.name, requireContext())
            }
        }

    }
}
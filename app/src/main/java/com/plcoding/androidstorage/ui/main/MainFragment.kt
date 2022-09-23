package com.plcoding.androidstorage.ui.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.plcoding.androidstorage.R
import com.plcoding.androidstorage.adapters.SharedPhotoAdapter
import com.plcoding.androidstorage.dataSource.PhotoDataSource
import com.plcoding.androidstorage.dataSource.PhotoListProvider
import com.plcoding.androidstorage.databinding.MainFragmentBinding

import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

class MainFragment : Fragment() {
    private lateinit var tv: TextView
    private lateinit var rvShared: RecyclerView
    private lateinit var btnSave: ImageView
    private lateinit var swPrivate: SwitchMaterial
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedAdapter: SharedPhotoAdapter
    private lateinit var binding: MainFragmentBinding
    private var readExternalsStorage = false
    private var writeExternalsStorage = false
    private var updateList = false // to update adapter list when photo is added or deleted
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var contentObserver: ContentObserver
    private lateinit var deletedPhotoUri: Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        MainFragmentBinding.inflate(inflater)
        binding = MainFragmentBinding.inflate(inflater)
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                readExternalsStorage =
                    it[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readExternalsStorage
                writeExternalsStorage =
                    it[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writeExternalsStorage
            }
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        lifecycleScope.launch {
                            viewModel.deletePhotoFromExternalStorage(deletedPhotoUri,
                                requireContext(),
                                intentSenderLauncher)
                        }
                    }
                    Toast.makeText(context, "photo deleted", Toast.LENGTH_SHORT).show()
                }
            }
        updateOrRequestPromotions()
        initView()

        tv.setOnLongClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_privetFragment)
            true
        }
        return binding.root
    }

    private fun initView() {
        rvShared = binding.rvPublicPhotos
        btnSave = binding.btnTakePhoto
        swPrivate = binding.switchPrivate
        tv = binding.tvAllPhotos
    }

    private fun initRv() {
        sharedAdapter = SharedPhotoAdapter(requireContext()) {
            lifecycleScope.launch {
                viewModel.deletePhotoFromExternalStorage(it.contentUri,
                    requireContext(),
                    intentSenderLauncher)
                deletedPhotoUri = it.contentUri
            }
        }
        rvShared.apply {
            adapter = sharedAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initContentObserver()
        lifecycleScope.launch {
            viewModel.loadPhotosFromExternalStorage(requireContext())
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.externalStoragePhotos.observe(viewLifecycleOwner) {

            val pageConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .build()

            val pagedList = PagedList.Builder(PhotoDataSource(
                PhotoListProvider(it)), pageConfig
            )
                .setInitialKey(1)
                .setNotifyExecutor(PhotoDataSource.UiThreadExecutor())
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build()
            if (!updateList) {
                sharedAdapter.submitList(pagedList)
            } else {
                initRv()
                sharedAdapter.submitList(pagedList)
                updateList = false
            }

        }


        initRv()
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = binding.switchPrivate.isChecked
            if (isPrivate && it != null) {
                lifecycleScope.launch {
                    if (viewModel.savePhotoToInternalStorage(UUID.randomUUID().toString(),
                            it,
                            requireContext())
                    ) {
                        Toast.makeText(requireContext(), "photo saved", Toast.LENGTH_SHORT).show()
                    }
                }


            } else if (!isPrivate && it != null && writeExternalsStorage) {
                lifecycleScope.launchWhenCreated {
                    if (viewModel.savePhotoToExternalStorage(UUID.randomUUID().toString(),
                            it,
                            requireContext())
                    ) {
                        // TODO: refresh ui
                        lifecycleScope.launch {
                            viewModel.loadPhotosFromExternalStorage(requireContext())
                        }
                    }
                }
            }

        }
        binding.btnTakePhoto.setOnClickListener {
            takePhoto.launch()
        }
    }

    private fun updateOrRequestPromotions() {
        val hasReadP = ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWriteP = ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val min29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        readExternalsStorage = hasReadP
        writeExternalsStorage = hasWriteP || min29

        val promotionsToRequest = mutableListOf<String>()
        if (!writeExternalsStorage) {
            promotionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readExternalsStorage)
            promotionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (promotionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(promotionsToRequest.toTypedArray())
        }

    }

    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                if (readExternalsStorage) {
                    lifecycleScope.launch {
                        updateList = true
                        viewModel.loadPhotosFromExternalStorage(requireContext())
                        Log.d("fragmentMain", "onChange: ")
                    }

                }
            }

        }
        requireActivity().contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
            //todo implement room db
        )
    }



    override fun onDestroy() {
        super.onDestroy()
        viewModel.externalStoragePhotos.removeObservers(this)
        viewModel.errorMessage.removeObservers(this)
        requireContext().contentResolver.unregisterContentObserver(contentObserver)
    }

}
package com.plcoding.androidstorage.ui.splash

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.plcoding.androidstorage.R
import com.plcoding.androidstorage.databinding.FragmentSplashBinding
import java.util.concurrent.Executor


class SplashFragment : Fragment() {
    private val t = "SplashFragment"
    private lateinit var binding: FragmentSplashBinding

    private lateinit var executor :Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var info :BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater)
        checkBio()
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this,executor ,
        object :BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d(t, "onAuthenticationError: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)

            }
        })

        info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify identity")
            .setSubtitle("use biometric to verify identity")
            .setNegativeButtonText("Cancel")
            .build()

        return binding.root
    }



    override fun onResume() {
        super.onResume()
        biometricPrompt.authenticate(info)

    }

    private fun checkBio() {
        val biometricManager = from(requireContext())
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_STRONG)) {
            BIOMETRIC_SUCCESS ->
                Log.d(t, "checkBio: app can auth")
            BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.d(t, "checkBio: app can'T auth")

            BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                Toast.makeText(requireContext(), "all badd " , Toast.LENGTH_SHORT).show()
                val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                }

                startActivityForResult(enrollIntent, 101)
            }
        }

    }

}
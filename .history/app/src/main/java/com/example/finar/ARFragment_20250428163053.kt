package com.example.finar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.core.ArCoreApk
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels

class ARFragment : Fragment() {
    private var arSession: Session? = null
    private var displayRotationHelper: DisplayRotationHelper? = null
    private var cameraHelper: CameraHelper? = null

    private val installRequested = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupAR()
        } else {
            Toast.makeText(context, "Camera permission is required for AR", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAR()
    }

    private fun setupAR() {
        try {
            if (arSession == null) {
                when (ArCoreApk.getInstance().requestInstall(requireActivity(), true)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        return
                    }
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // Continue with AR setup
                    }
                }
            }

            arSession = Session(requireContext())
            val config = Config(arSession)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            arSession?.configure(config)

            displayRotationHelper = DisplayRotationHelper(requireContext())
            cameraHelper = CameraHelper(requireContext())

        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(context, "Please install ARCore", Toast.LENGTH_LONG).show()
        } catch (e: UnavailableException) {
            val message = when (e) {
                is UnavailableSdkTooOldException -> "Please update ARCore"
                is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                else -> "AR is not available on this device"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to create AR session: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            arSession?.resume()
        } catch (e: CameraNotAvailableException) {
            Toast.makeText(context, "Camera not available", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        arSession?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSession?.close()
        arSession = null
    }
} 
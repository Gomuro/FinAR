package com.example.finar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

class ARFragment : ArFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        
        // Disable plane discovery since we don't need it for basic AR
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        
        return view
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)
        
        // Configure the AR session
        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        
        return config
    }

    override fun getSessionFeatures(): Set<Session.Feature> {
        return setOf(Session.Feature.SHARED_CAMERA)
    }
} 
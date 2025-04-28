package com.example.finar

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment

/**
 * Custom AR Fragment that enables horizontal plane finding.
 */
class CustomArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        // Get the default configuration
        val config = super.getSessionConfiguration(session) 
        
        // Enable horizontal plane finding
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        
        // You can add other custom configurations here if needed
        // config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR 
        
        return config
    }
} 
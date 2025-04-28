package com.example.finar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.finar.databinding.FragmentArBinding
import com.google.ar.core.Config
import com.google.ar.core.Session
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import java.util.concurrent.CompletableFuture

class ARFragment : Fragment() {

    private var _binding: FragmentArBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sceneView: ArSceneView
    private var modelNode: ArModelNode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = binding.arSceneView
        
        // Set up AR session configuration
        sceneView.configureSession { session, config ->
            // Enable plane detection
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            // Enable light estimation
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            // Enable depth
            config.depthMode = Config.DepthMode.AUTOMATIC
            
            // Set focus mode
            session.setCameraTextureNames(intArrayOf(sceneView.cameraTextureId))
        }
        
        // Set up scene updates listener
        sceneView.onArSessionCreated = { session: Session ->
            // Session has been created and is ready
            loadAndPlaceModel()
        }
    }
    
    private fun loadAndPlaceModel() {
        // Create a new AR model node
        modelNode = ArModelNode(placementMode = ArModelNode.PlacementMode.BEST_AVAILABLE).apply {
            parent = sceneView
            // Position in front of the camera
            position = Position(0f, 0f, -1f)
            // Load the 3D model
            loadModelGlbAsync(
                glbFileLocation = "models/andy.glb",
                autoAnimate = true,
                scaleToUnits = 0.5f,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            ).thenAccept { modelInstance ->
                // Model has loaded successfully
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ARFragment()
    }
} 
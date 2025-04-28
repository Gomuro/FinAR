package com.example.finar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.ux.ArFragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinARTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ARCheckScreen(
                        modifier = Modifier.padding(innerPadding),
                        onInstallRequested = { requestInstall() }
                    )
                }
            }
        }
    }

    private fun requestInstall() {
        try {
            ArCoreApk.getInstance().requestInstall(this, true)
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Handle installation declined
        } catch (e: UnavailableDeviceNotCompatibleException) {
            // Handle device not compatible
        } catch (e: Exception) {
            // Handle other errors
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}

@Composable
fun ARCheckScreen(
    modifier: Modifier = Modifier,
    onInstallRequested: () -> Unit
) {
    val context = LocalContext.current
    var arAvailability by remember { mutableStateOf<Availability?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var detailedError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            arAvailability = ArCoreApk.getInstance().checkAvailability(context)
            if (arAvailability == Availability.UNKNOWN_ERROR) {
                // Try to get more detailed error information
                try {
                    ArCoreApk.getInstance().checkAvailability(context)
                } catch (e: Exception) {
                    Log.e("ARCheck", "AR availability check failed", e)
                    detailedError = """
                        Error Type: ${e.javaClass.name}
                        Error Message: ${e.message}
                        Stack Trace:
                        ${e.stackTraceToString()}
                    """.trimIndent()
                }
            }
        } catch (e: Exception) {
            Log.e("ARCheck", "Initial AR availability check failed", e)
            errorMessage = "Error checking AR availability: ${e.message}"
            detailedError = """
                Error Type: ${e.javaClass.name}
                Error Message: ${e.message}
                Stack Trace:
                ${e.stackTraceToString()}
            """.trimIndent()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (arAvailability) {
            Availability.SUPPORTED_INSTALLED -> {
                AndroidView(
                    factory = { context ->
                        val view = layoutInflater.inflate(R.layout.ar_scene, null)
                        val arFragment = view.findViewById<ArFragment>(R.id.arFragment)
                        
                        // Check camera permissions
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_PERMISSION_CODE
                            )
                        }
                        
                        // Configure AR scene
                        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                            // Here you can add code to place 3D objects
                            // Example: placeCube(hitResult.createAnchor())
                        }
                        
                        view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Availability.SUPPORTED_APK_TOO_OLD,
            Availability.SUPPORTED_NOT_INSTALLED -> {
                Text(
                    text = "AR is supported but needs to be installed/updated",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = onInstallRequested
                ) {
                    Text("Install/Update AR")
                }
            }
            Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                Text(
                    text = "Your device does not support AR. Please check if:\n" +
                            "1. Your device has a supported camera\n" +
                            "2. Your device meets the minimum requirements\n" +
                            "3. ARCore is supported on your device model",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Availability.UNKNOWN_ERROR -> {
                Text(
                    text = "Error checking AR availability. Please check:\n" +
                            "1. Internet connection\n" +
                            "2. Google Play Services\n" +
                            "3. Device compatibility",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { 
                        arAvailability = null
                        errorMessage = null
                        detailedError = null
                    }
                ) {
                    Text("Retry Check")
                }
            }
            Availability.UNKNOWN_CHECKING -> {
                Text(
                    text = "Checking AR availability...",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Availability.UNKNOWN_TIMED_OUT -> {
                Text(
                    text = "AR availability check timed out. Please check your internet connection.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { 
                        arAvailability = null
                        errorMessage = null
                        detailedError = null
                    }
                ) {
                    Text("Retry Check")
                }
            }
            null -> {
                Text(
                    text = "Checking AR availability...",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        detailedError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun placeCube(anchor: Anchor) {
    val cube = Cube(this, 0.1f, Color(android.graphics.Color.RED))
    cube.setPosition(anchor.pose.position)
    cube.setRotation(anchor.pose.rotation)
    arFragment.arSceneView.scene.addChild(cube)
}
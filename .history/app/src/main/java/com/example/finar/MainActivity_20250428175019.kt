package com.example.finar

import Cube
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
import com.example.finar.ui.theme.FinARTheme
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.core.Session
import com.google.ar.core.Config
import com.google.ar.core.Pose
import android.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Quaternion
import android.widget.FrameLayout
import androidx.fragment.app.commit


class MainActivity : ComponentActivity() {
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinARTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            val frameLayout = FrameLayout(context).apply {
                                id = R.id.container
                            }
                            // Add fragment properly
                            androidx.fragment.app.commit {
                                replace(R.id.container, createArFragment())
                            }
                            frameLayout
                        }
                    )
                }
            }
        }
    }

    private fun createArFragment(): ArFragment {
        arFragment = object : ArFragment() {
            override fun getSessionConfiguration(session: Session?): Config {
                return Config(session).apply {
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                }
            }
        }
        return arFragment
    }

    override fun onResume() {
        super.onResume()
        setupScene()
    }

    private fun setupScene() {
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val cube = Cube(
                context = this,
                size = 0.1f,
                color = Color.CYAN,
                transformationSystem = arFragment.transformationSystem
            )
            
            val composedPose = hitResult.createAnchor()!!.pose.compose(
                Pose.makeTranslation(0f, 0.05f, 0f)
            )
            cube.worldPosition = Vector3(
                composedPose.tx(),
                composedPose.ty(),
                composedPose.tz()
            )
            
            arFragment.arSceneView.scene.addChild(cube)
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
                Text(
                    text = "AR is supported and installed on your device!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                // Here you can add your AR scene or navigation to AR activity
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
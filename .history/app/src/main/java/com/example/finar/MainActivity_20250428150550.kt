package com.example.finar

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinARTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ARCheckScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ARCheckScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var arAvailability by remember { mutableStateOf<Availability?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            arAvailability = ArCoreApk.getInstance().checkAvailability(context)
        } catch (e: Exception) {
            errorMessage = "Error checking AR availability: ${e.message}"
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
                    onClick = {
                        try {
                            ArCoreApk.getInstance().requestInstall(this@MainActivity, true)
                        } catch (e: UnavailableUserDeclinedInstallationException) {
                            errorMessage = "AR installation was declined"
                        } catch (e: UnavailableDeviceNotCompatibleException) {
                            errorMessage = "Device is not compatible with AR"
                        } catch (e: Exception) {
                            errorMessage = "Error installing AR: ${e.message}"
                        }
                    }
                ) {
                    Text("Install/Update AR")
                }
            }
            Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                Text(
                    text = "Your device does not support AR",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
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
    }
}
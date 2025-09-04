package com.azers.ai.bionicarm

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandLandmarkDetector() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var handLandmarker by remember { mutableStateOf<HandLandmarker?>(null) }
    var isBackCamera by remember { mutableStateOf(false) } // Changed to false for front camera default
    var overlayView by remember { mutableStateOf<HandLandmarkOverlay?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // USB Serial Manager
    var usbManager by remember { mutableStateOf<UsbSerialManager?>(null) }
    var isArduinoConnected by remember { mutableStateOf(false) }
    var usbStatus by remember { mutableStateOf("Not connected") }
    var currentFingerPositions by remember { mutableStateOf<FingerPositions?>(null) }

    // Initialize USB Manager
    LaunchedEffect(Unit) {
        usbManager = UsbSerialManager(
            context = context,
            onConnectionChanged = { connected ->
                isArduinoConnected = connected
                usbStatus = if (connected) "Arduino connected" else "Arduino disconnected"
            },
            onError = { error ->
                usbStatus = "Error: $error"
                println("USB Error: $error")
            }
        )
    }

    // Initialize MediaPipe HandLandmarker
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val landmarker = initializeHandLandmarker(context)
                handLandmarker = landmarker
                isInitialized = true
                println("HandLandmarker initialized successfully")
            } catch (e: Exception) {
                errorMessage = "Failed to initialize hand landmarker: ${e.message}"
                println("HandLandmarker initialization failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            usbManager?.cleanup()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isInitialized && handLandmarker != null) {
            // Use key to force recomposition when camera changes
            key(isBackCamera) {
                CameraPreview(
                    handLandmarker = handLandmarker,
                    onResults = { result, imageWidth, imageHeight ->
                        println("Detection results: ${result?.landmarks()?.size ?: 0} hands detected")
                        overlayView?.setResults(result, imageWidth, imageHeight)
                        
                        // Calculate finger positions and send to Arduino
                        result?.let { handResult ->
                            val fingerPositions = FingerPositionCalculator.calculateFingerPositions(handResult)
                            fingerPositions?.let { positions ->
                                currentFingerPositions = positions
                                if (isArduinoConnected) {
                                    usbManager?.sendFingerPositions(positions)
                                }
                            }
                        }
                    },
                    isBackCamera = isBackCamera,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Overlay for hand landmarks - single overlay
            AndroidView(
                factory = { ctx ->
                    HandLandmarkOverlay(ctx).also { overlay ->
                        overlayView = overlay
                        println("Overlay view created")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (errorMessage != null) {
            // Error state
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Initializing Hand Landmarker...")
                }
            }
        }

        // Status and control panel
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Bionic Arm Control",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Camera indicator
                Text(
                    text = "Camera: ${if (isBackCamera) "Back" else "Front"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isArduinoConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isArduinoConnected) Color.Green else Color(0xFFFFA500), // Orange color
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = usbStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!isArduinoConnected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { usbManager?.findAndConnectArduino() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect Arduino", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // Show current finger positions
                currentFingerPositions?.let { positions ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Finger Positions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "T:${positions.thumb.toInt()}% I:${positions.index.toInt()}%\nM:${positions.middle.toInt()}% R:${positions.ring.toInt()}% P:${positions.pinky.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        // Camera switch button
        FloatingActionButton(
            onClick = { isBackCamera = !isBackCamera },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = if (isBackCamera) "Switch to Front Camera" else "Switch to Back Camera"
            )
        }
    }
}

private suspend fun initializeHandLandmarker(context: Context): HandLandmarker {
    return withContext(Dispatchers.IO) {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val options = HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(0.3f)  // Lowered from 0.5f
                .setMinHandPresenceConfidence(0.3f)   // Lowered from 0.5f
                .setMinTrackingConfidence(0.3f)       // Lowered from 0.5f
                .setNumHands(2)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            HandLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize HandLandmarker: ${e.message}", e)
        }
    }
}

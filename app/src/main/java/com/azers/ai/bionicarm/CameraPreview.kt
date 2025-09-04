package com.azers.ai.bionicarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    handLandmarker: HandLandmarker?,
    onResults: (HandLandmarkerResult?, Int, Int) -> Unit,
    isBackCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                previewView = this
                scaleType = PreviewView.ScaleType.FILL_CENTER // Back to FILL_CENTER
            }
        },
        modifier = modifier,
        update = { preview ->
            // This will be called when isBackCamera changes
            startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = preview,
                handLandmarker = handLandmarker,
                onResults = onResults,
                cameraExecutor = cameraExecutor,
                isBackCamera = isBackCamera
            )
        }
    )
}

private fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    handLandmarker: HandLandmarker?,
    onResults: (HandLandmarkerResult?, Int, Int) -> Unit,
    cameraExecutor: ExecutorService,
    isBackCamera: Boolean
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(handLandmarker, imageProxy, onResults, isBackCamera)
                    }
                }
            
            val cameraSelector = if (isBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            
            println("Starting camera: ${if (isBackCamera) "BACK" else "FRONT"}")
            
            // Bind use cases to camera
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            
            // Set zoom to 1x (minimum zoom for wider view)
            val cameraControl = camera.cameraControl
            val cameraInfo = camera.cameraInfo
            
            // Get zoom ratio range and set to minimum (widest view)
            val zoomState = cameraInfo.zoomState.value
            if (zoomState != null) {
                val minZoom = zoomState.minZoomRatio
                cameraControl.setZoomRatio(minZoom)
                println("Camera zoom set to minimum: ${minZoom}x")
            }
            
            println("Camera bound successfully: ${if (isBackCamera) "BACK" else "FRONT"}")
            
        } catch (exc: Exception) {
            println("Camera binding failed: ${exc.message}")
            exc.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun processImageProxy(
    handLandmarker: HandLandmarker?,
    imageProxy: ImageProxy,
    onResults: (HandLandmarkerResult?, Int, Int) -> Unit,
    isBackCamera: Boolean
) {
    handLandmarker?.let { detector ->
        try {
            println("Processing image: ${imageProxy.width}x${imageProxy.height}, format: ${imageProxy.format}, camera: ${if (isBackCamera) "BACK" else "FRONT"}")
            
            // Convert ImageProxy to Bitmap
            val bitmap = ImageUtils.imageProxyToBitmap(imageProxy)
            println("Bitmap created: ${bitmap.width}x${bitmap.height}")
            
            // Rotate bitmap based on image rotation
            var rotatedBitmap = ImageUtils.rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
            
            // Mirror the image for front camera to match preview
            if (!isBackCamera) {
                rotatedBitmap = ImageUtils.mirrorBitmap(rotatedBitmap)
                println("Bitmap mirrored for front camera")
            }
            
            println("Final bitmap: ${rotatedBitmap.width}x${rotatedBitmap.height}, rotation: ${imageProxy.imageInfo.rotationDegrees}")
            
            // Create MPImage from bitmap
            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            
            // Detect hand landmarks
            val result = detector.detect(mpImage)
            println("Detection completed: ${result.landmarks().size} hands found")
            
            // Call the results callback
            onResults(result, rotatedBitmap.width, rotatedBitmap.height)
        } catch (e: Exception) {
            // Handle processing errors
            println("Error processing image: ${e.message}")
            e.printStackTrace()
            onResults(null, imageProxy.width, imageProxy.height)
        }
    }
    
    imageProxy.close()
}

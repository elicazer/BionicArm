package com.azers.ai.bionicarm

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var handLandmarkerResult: HandLandmarkerResult? = null
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    
    private val landmarkPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        style = Paint.Style.FILL
    }
    
    private val connectionPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    // Hand landmark connections (MediaPipe hand model)
    private val handConnections = listOf(
        // Thumb
        Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4),
        // Index finger
        Pair(0, 5), Pair(5, 6), Pair(6, 7), Pair(7, 8),
        // Middle finger
        Pair(0, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12),
        // Ring finger
        Pair(0, 13), Pair(13, 14), Pair(14, 15), Pair(15, 16),
        // Pinky
        Pair(0, 17), Pair(17, 18), Pair(18, 19), Pair(19, 20),
        // Palm
        Pair(5, 9), Pair(9, 13), Pair(13, 17)
    )

    fun setResults(
        handLandmarkerResult: HandLandmarkerResult?,
        imageWidth: Int,
        imageHeight: Int
    ) {
        this.handLandmarkerResult = handLandmarkerResult
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        
        println("Overlay setResults called: ${handLandmarkerResult?.landmarks()?.size ?: 0} hands, image: ${imageWidth}x${imageHeight}, view: ${width}x${height}")
        
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        println("Overlay onDraw called, view size: ${width}x${height}, image size: ${imageWidth}x${imageHeight}")
        
        handLandmarkerResult?.let { result ->
            println("Drawing ${result.landmarks().size} hands")
            
            if (imageWidth <= 0 || imageHeight <= 0) {
                println("Invalid image dimensions, skipping draw")
                return
            }
            
            // Calculate how the camera preview is scaled to fit the view (FILL_CENTER behavior)
            val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            val viewAspectRatio = width.toFloat() / height.toFloat()
            
            val scaleX: Float
            val scaleY: Float
            val offsetX: Float
            val offsetY: Float
            
            if (imageAspectRatio > viewAspectRatio) {
                // Image is wider than view, scale by height and center horizontally
                scaleY = height.toFloat() / imageHeight.toFloat()
                scaleX = scaleY
                offsetX = (width - imageWidth * scaleX) / 2f
                offsetY = 0f
            } else {
                // Image is taller than view, scale by width and center vertically
                scaleX = width.toFloat() / imageWidth.toFloat()
                scaleY = scaleX
                offsetX = 0f
                offsetY = (height - imageHeight * scaleY) / 2f
            }
            
            println("Scale factors - scaleX: $scaleX, scaleY: $scaleY")
            println("Offsets - offsetX: $offsetX, offsetY: $offsetY")
            
            for ((handIndex, landmark) in result.landmarks().withIndex()) {
                println("Drawing hand $handIndex with ${landmark.size} landmarks")
                
                // Draw connections first (so they appear behind landmarks)
                for (connection in handConnections) {
                    if (connection.first < landmark.size && connection.second < landmark.size) {
                        val startLandmark = landmark[connection.first]
                        val endLandmark = landmark[connection.second]
                        
                        val startX = startLandmark.x() * imageWidth * scaleX + offsetX
                        val startY = startLandmark.y() * imageHeight * scaleY + offsetY
                        val endX = endLandmark.x() * imageWidth * scaleX + offsetX
                        val endY = endLandmark.y() * imageHeight * scaleY + offsetY
                        
                        canvas.drawLine(startX, startY, endX, endY, connectionPaint)
                    }
                }
                
                // Draw landmarks
                for ((landmarkIndex, normalizedLandmark) in landmark.withIndex()) {
                    val x = normalizedLandmark.x() * imageWidth * scaleX + offsetX
                    val y = normalizedLandmark.y() * imageHeight * scaleY + offsetY
                    
                    if (landmarkIndex == 0) { // Log first landmark for debugging
                        println("Drawing landmark $landmarkIndex at ($x, $y) from normalized (${normalizedLandmark.x()}, ${normalizedLandmark.y()})")
                    }
                    
                    canvas.drawCircle(x, y, 8f, landmarkPaint)
                }
            }
        } ?: run {
            println("No hand landmarks to draw")
        }
    }
}

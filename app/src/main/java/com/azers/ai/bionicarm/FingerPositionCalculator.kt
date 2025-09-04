package com.azers.ai.bionicarm

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.sqrt

data class FingerPositions(
    val thumb: Float,      // 0-100%
    val index: Float,      // 0-100%
    val middle: Float,     // 0-100%
    val ring: Float,       // 0-100%
    val pinky: Float       // 0-100%
)

object FingerPositionCalculator {
    
    // MediaPipe hand landmark indices
    private const val WRIST = 0
    
    // Thumb landmarks
    private const val THUMB_CMC = 1
    private const val THUMB_MCP = 2
    private const val THUMB_IP = 3
    private const val THUMB_TIP = 4
    
    // Index finger landmarks
    private const val INDEX_MCP = 5
    private const val INDEX_PIP = 6
    private const val INDEX_DIP = 7
    private const val INDEX_TIP = 8
    
    // Middle finger landmarks
    private const val MIDDLE_MCP = 9
    private const val MIDDLE_PIP = 10
    private const val MIDDLE_DIP = 11
    private const val MIDDLE_TIP = 12
    
    // Ring finger landmarks
    private const val RING_MCP = 13
    private const val RING_PIP = 14
    private const val RING_DIP = 15
    private const val RING_TIP = 16
    
    // Pinky landmarks
    private const val PINKY_MCP = 17
    private const val PINKY_PIP = 18
    private const val PINKY_DIP = 19
    private const val PINKY_TIP = 20
    
    fun calculateFingerPositions(result: HandLandmarkerResult): FingerPositions? {
        if (result.landmarks().isEmpty()) return null
        
        // Use the first detected hand
        val landmarks = result.landmarks()[0]
        if (landmarks.size < 21) return null
        
        return FingerPositions(
            thumb = calculateThumbExtension(landmarks),
            index = calculateFingerExtension(landmarks, INDEX_MCP, INDEX_PIP, INDEX_DIP, INDEX_TIP),
            middle = calculateFingerExtension(landmarks, MIDDLE_MCP, MIDDLE_PIP, MIDDLE_DIP, MIDDLE_TIP),
            ring = calculateFingerExtension(landmarks, RING_MCP, RING_PIP, RING_DIP, RING_TIP),
            pinky = calculateFingerExtension(landmarks, PINKY_MCP, PINKY_PIP, PINKY_DIP, PINKY_TIP)
        )
    }
    
    private fun calculateThumbExtension(landmarks: List<*>): Float {
        // For thumb, we calculate the distance between thumb tip and palm
        val thumbTip = landmarks[THUMB_TIP]
        val thumbMcp = landmarks[THUMB_MCP]
        val wrist = landmarks[WRIST]
        
        // Distance from thumb tip to wrist
        val tipToWrist = distance(thumbTip, wrist)
        // Distance from thumb MCP to wrist (base distance)
        val mcpToWrist = distance(thumbMcp, wrist)
        
        // Calculate extension ratio
        val extension = if (mcpToWrist > 0) {
            (tipToWrist - mcpToWrist) / mcpToWrist
        } else 0f
        
        // Convert to percentage (0-100%)
        return (extension * 100f).coerceIn(0f, 100f)
    }
    
    private fun calculateFingerExtension(
        landmarks: List<*>,
        mcpIndex: Int,
        pipIndex: Int,
        dipIndex: Int,
        tipIndex: Int
    ): Float {
        val mcp = landmarks[mcpIndex]
        val pip = landmarks[pipIndex]
        val dip = landmarks[dipIndex]
        val tip = landmarks[tipIndex]
        
        // Calculate the total finger length when extended
        val totalLength = distance(mcp, pip) + distance(pip, dip) + distance(dip, tip)
        
        // Calculate the direct distance from MCP to tip
        val directDistance = distance(mcp, tip)
        
        // Extension ratio: closer to 1.0 means more extended
        val extension = if (totalLength > 0) {
            directDistance / totalLength
        } else 0f
        
        // Convert to percentage (0-100%)
        return (extension * 100f).coerceIn(0f, 100f)
    }
    
    private fun distance(point1: Any?, point2: Any?): Float {
        try {
            // Use reflection to access x(), y(), z() methods
            val x1 = point1?.javaClass?.getMethod("x")?.invoke(point1) as? Float ?: 0f
            val y1 = point1?.javaClass?.getMethod("y")?.invoke(point1) as? Float ?: 0f
            val z1 = point1?.javaClass?.getMethod("z")?.invoke(point1) as? Float ?: 0f
            
            val x2 = point2?.javaClass?.getMethod("x")?.invoke(point2) as? Float ?: 0f
            val y2 = point2?.javaClass?.getMethod("y")?.invoke(point2) as? Float ?: 0f
            val z2 = point2?.javaClass?.getMethod("z")?.invoke(point2) as? Float ?: 0f
            
            val dx = x1 - x2
            val dy = y1 - y2
            val dz = z1 - z2
            
            return sqrt(dx * dx + dy * dy + dz * dz).toFloat()
        } catch (e: Exception) {
            return 0f
        }
    }
    
    fun formatForSerial(positions: FingerPositions): String {
        // Format: "T:XX,I:XX,M:XX,R:XX,P:XX\n"
        return "T:${positions.thumb.toInt()},I:${positions.index.toInt()},M:${positions.middle.toInt()},R:${positions.ring.toInt()},P:${positions.pinky.toInt()}\n"
    }
}

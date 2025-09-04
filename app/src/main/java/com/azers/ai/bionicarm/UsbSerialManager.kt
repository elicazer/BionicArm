package com.azers.ai.bionicarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class UsbSerialManager(
    private val context: Context,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private var isConnected = false
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val ACTION_USB_PERMISSION = "com.azers.ai.bionicarm.USB_PERMISSION"
        private const val BAUD_RATE = 9600
        private const val DATA_BITS = 8
        private const val STOP_BITS = UsbSerialPort.STOPBITS_1
        private const val PARITY = UsbSerialPort.PARITY_NONE
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                        
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { connectToDevice(it) }
                        } else {
                            onError("USB permission denied")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }
                    device?.let { disconnect() }
                }
            }
        }
    }
    
    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
    }
    
    fun findAndConnectArduino() {
        scope.launch {
            try {
                val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
                
                if (availableDrivers.isEmpty()) {
                    onError("No USB serial devices found. Please connect your Arduino.")
                    return@launch
                }
                
                val driver = availableDrivers[0]
                val device = driver.device
                
                if (usbManager.hasPermission(device)) {
                    connectToDevice(device)
                } else {
                    requestPermission(device)
                }
            } catch (e: Exception) {
                onError("Error finding Arduino: ${e.message}")
            }
        }
    }
    
    private fun requestPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            Intent(ACTION_USB_PERMISSION), 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        usbManager.requestPermission(device, permissionIntent)
    }
    
    private fun connectToDevice(device: UsbDevice) {
        scope.launch {
            try {
                val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
                if (driver == null) {
                    onError("No driver found for device")
                    return@launch
                }
                
                val connection = usbManager.openDevice(device)
                if (connection == null) {
                    onError("Failed to open USB connection")
                    return@launch
                }
                
                serialPort = driver.ports[0].apply {
                    open(connection)
                    setParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY)
                }
                
                isConnected = true
                onConnectionChanged(true)
                
                println("Arduino connected successfully")
                
            } catch (e: IOException) {
                onError("Connection failed: ${e.message}")
            }
        }
    }
    
    fun sendFingerPositions(positions: FingerPositions) {
        if (!isConnected || serialPort == null) {
            return
        }
        
        scope.launch {
            try {
                val data = FingerPositionCalculator.formatForSerial(positions)
                serialPort?.write(data.toByteArray(), 1000)
                println("Sent to Arduino: $data")
            } catch (e: IOException) {
                onError("Failed to send data: ${e.message}")
                disconnect()
            }
        }
    }
    
    fun disconnect() {
        scope.launch {
            try {
                serialPort?.close()
                serialPort = null
                isConnected = false
                onConnectionChanged(false)
                println("Arduino disconnected")
            } catch (e: IOException) {
                onError("Error disconnecting: ${e.message}")
            }
        }
    }
    
    fun cleanup() {
        disconnect()
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
    
    fun isConnected(): Boolean = isConnected
}

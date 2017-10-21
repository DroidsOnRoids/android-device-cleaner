package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import java.util.concurrent.TimeUnit

class DeviceCleaner(connectedDeviceSerials: List<String>) : AndroidDebugBridge.IDeviceChangeListener {
    private val lock = Object()
    private val cleanTimeout = TimeUnit.MINUTES.toMillis(30)
    private val serialsToBeCleaned = connectedDeviceSerials.toMutableSet()
    private var allDevicesCleanedSuccessfully = true

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) {
        allDevicesCleanedSuccessfully = allDevicesCleanedSuccessfully and device.clean()
        removeDevice(device)
    }

    override fun deviceDisconnected(device: IDevice) = removeDevice(device)

    private fun removeDevice(device: IDevice) {
        serialsToBeCleaned.remove(device.getProperty("ro.serialno"))
        if (serialsToBeCleaned.isEmpty()) {
            synchronized(lock) { lock.notify() }
        }
    }

    fun waitUntilAllDevicesCleaned(): Boolean {
        synchronized(lock) {
            while (serialsToBeCleaned.isNotEmpty()) lock.wait(cleanTimeout)
        }
        return allDevicesCleanedSuccessfully
    }
}
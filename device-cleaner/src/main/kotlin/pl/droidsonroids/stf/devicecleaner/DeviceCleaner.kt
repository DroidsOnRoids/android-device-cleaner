package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class DeviceCleaner(
    connectedDeviceSerials: Array<String>,
    private val excludedPackages: Array<String>
) : AndroidDebugBridge.IDeviceChangeListener {
    private val lock = Object()
    private val cleanTimeout = TimeUnit.MINUTES.toMillis(30)
    private val serialsToBeCleaned = connectedDeviceSerials.toMutableSet()
    private val allDevicesCleanedSuccessfully = AtomicBoolean(true)

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) {
        if (device.serialProperty in serialsToBeCleaned) {
            thread(name = "${device.serialNumber} cleaner") {
                if (device.clean(excludedPackages).not()) {
                    allDevicesCleanedSuccessfully.set(false)
                }
                removeDevice(device)
            }
        }
    }

    override fun deviceDisconnected(device: IDevice) = removeDevice(device)

    private fun removeDevice(device: IDevice) {
        serialsToBeCleaned.remove(device.serialProperty)
        if (serialsToBeCleaned.isEmpty()) {
            synchronized(lock) { lock.notify() }
        }
    }

    fun waitUntilAllDevicesCleaned(): Boolean {
        synchronized(lock) {
            while (serialsToBeCleaned.isNotEmpty()) lock.wait(cleanTimeout)
        }
        return allDevicesCleanedSuccessfully.get()
    }
}
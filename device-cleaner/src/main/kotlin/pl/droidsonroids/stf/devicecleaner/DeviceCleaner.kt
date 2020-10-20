package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DeviceCleaner(
    connectedDeviceSerials: Array<String>,
    private val excludedPackages: Array<String>
) : AndroidDebugBridge.IDeviceChangeListener {
    private val lock = Object()
    private val cleanTimeout = TimeUnit.MINUTES.toMillis(30)
    private val serialsToBeCleaned = connectedDeviceSerials.toMutableSet()
    private val allDevicesCleanedSuccessfully = AtomicBoolean(true)
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) {
        if (device.serialProperty in serialsToBeCleaned) {
            scope.launch {
                if (!device.clean(excludedPackages)) {
                    allDevicesCleanedSuccessfully.set(false)
                }
            }
            removeDevice(device)
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
        runBlocking {
            job.join()
        }
        scope.cancel()
        return allDevicesCleanedSuccessfully.get()
    }
}
package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

class DeviceCleaner(
    connectedDeviceSerials: Array<String>,
    private val excludedPackages: Array<String>
) : AndroidDebugBridge.IDeviceChangeListener {

    private val serialsToBeCleaned = CopyOnWriteArraySet(connectedDeviceSerials.toSet())
    private val deviceChannel = Channel<IDevice>(Channel.UNLIMITED)

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) {
        if (device.serialProperty in serialsToBeCleaned) {
            deviceChannel.offer(device)
        }
    }

    override fun deviceDisconnected(device: IDevice) = removeDevice(device)

    private fun removeDevice(device: IDevice) {
        serialsToBeCleaned.remove(device.serialProperty)
    }

    suspend fun cleanAllDevices(): Boolean {
        val allDevicesCleanedSuccessfully = AtomicBoolean(true)
        while (serialsToBeCleaned.isNotEmpty()) {
            val device = deviceChannel.receive()
            withContext(Dispatchers.IO) {
                launch {
                    if (!device.clean(excludedPackages)) {
                        allDevicesCleanedSuccessfully.set(false)
                    }
                    removeDevice(device)
                }
            }
        }
        return allDevicesCleanedSuccessfully.get()
    }
}
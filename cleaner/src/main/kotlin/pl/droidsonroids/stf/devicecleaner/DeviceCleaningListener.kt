package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice

class DeviceCleaningListener : AndroidDebugBridge.IDeviceChangeListener {

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) = device.clean()

    override fun deviceDisconnected(device: IDevice) = Unit
}
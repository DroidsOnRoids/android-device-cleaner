package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.ddmlib.MultiLineReceiver
import org.junit.Test

val excludedPackages = arrayOf("jp.co.cyberagent.stf", "de.codenauts.hockeyapp", "pl.droidsonroids.devico",
        "com.ghisler.android.TotalCommander", "com.android.chrome", "com.facebook.katana", "pl.droidsonroids.devico", "com.instagram.android",
        "com.facebook.orca", "com.koushikdutta.vysor", "com.lexa.fakegps", "com.twitter.android", "com.google.android.apps.plus")

class DeviceCleaningListener(serials: List<String>) : AndroidDebugBridge.IDeviceChangeListener {
    private val lock = Object()
    private val serialsLeft = serials.toMutableList()

    override fun deviceChanged(device: IDevice, changeMask: Int) = Unit

    override fun deviceConnected(device: IDevice) {
        device.clean()
        device.reboot(null)
        notifyDeviceCleaned(device)
    }

    private fun notifyDeviceCleaned(device: IDevice) {
        serialsLeft -= device.getProperty("ro.serialno")
        synchronized(lock) { lock.notify() }
    }

    override fun deviceDisconnected(device: IDevice) = notifyDeviceCleaned(device)

    fun waitU() = synchronized(lock) {
        while (serialsLeft.isNotEmpty()) lock.wait()
    }

}

fun IDevice.clean() {
    executeShellCommand("pm list packages -3", object : MultiLineReceiver() {
        override fun processNewLines(lines: Array<out String>) {
            lines.map { it.removePrefix("package:") }
                    .filter { it !in excludedPackages }
                    .forEach { uninstallPackage(it) }
        }

        override fun isCancelled() = false
    })
}

class ExampleUnitTest {

    @Test
    fun clear() {
        AndroidDebugBridge.init(true)
        val androidHome = System.getenv("ANDROID_HOME")
        AndroidDebugBridge.createBridge("$androidHome/platform-tools/adb", true)

        val deviceCleaningListener = DeviceCleaningListener(listOf(""))
        AndroidDebugBridge.addDeviceChangeListener(deviceCleaningListener)
        deviceCleaningListener.waitU()
        AndroidDebugBridge.terminate()
    }


}
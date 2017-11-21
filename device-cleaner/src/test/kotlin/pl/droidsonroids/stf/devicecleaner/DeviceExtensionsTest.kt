package pl.droidsonroids.stf.devicecleaner

import assertk.assert
import assertk.assertions.*
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import java.io.IOException

class DeviceExtensionsTest {

    @Test
    fun `desired actions performed on clean run`() {
        val device = mock<IDevice> {
            on { executeShellCommand(eq("pm list packages -3"), any()) } doAnswer {
                val receiver = it.arguments[1] as NonCancellableMultilineReceiver
                receiver.processNewLines(arrayOf("package:foo.bar", "package:foo.baz", " "))
            }
        }

        val commandCaptor = argumentCaptor<String>()
        val packageCaptor = argumentCaptor<String>()
        val receiverCaptor = argumentCaptor<IShellOutputReceiver>()

        assert(device.clean()).isTrue()

        verify(device, times(2)).uninstallPackage(packageCaptor.capture())

        assert(packageCaptor.allValues).hasSize(2)
        assert(packageCaptor.firstValue).isEqualTo("foo.bar")
        assert(packageCaptor.secondValue).isEqualTo("foo.baz")

        verify(device, times(3)).executeShellCommand(commandCaptor.capture(), receiverCaptor.capture())
        assert(commandCaptor.allValues) {
            contains("rm -rf /data/local/tmp/*")
            contains("rm -rf /sdcard/*")
        }
    }

    @Test
    fun `returns false on clean failure`() {
        val device = mock<IDevice> {
            on { reboot(anyOrNull()) } doThrow IOException::class
        }

        assert(device.clean()).isFalse()
    }

    @Test
    fun `falls back to default serial number if property missing`() {
        val device = mock<IDevice> {
            on { getProperty("ro.serialno") } doReturn null as String?
        }

        assert(device.serialProperty).isNotNull()
    }
}
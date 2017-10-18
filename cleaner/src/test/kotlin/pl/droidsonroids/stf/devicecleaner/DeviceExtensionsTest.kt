package pl.droidsonroids.stf.devicecleaner

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import com.nhaarman.mockito_kotlin.*
import org.junit.Test

class DeviceExtensionsTest {

    @Test
    fun `device cleaned up`() {
        val device = mock<IDevice> {
            on { executeShellCommand(eq("pm list packages -3"), any()) } doAnswer {
                val receiver = it.arguments[1] as NonCancellableMultilineReceiver
                receiver.processNewLines(arrayOf("package:foo.bar", "package:foo.baz", " "))
            }
        }

        val commandCaptor = argumentCaptor<String>()
        val packageCaptor = argumentCaptor<String>()
        val receiverCaptor = argumentCaptor<IShellOutputReceiver>()

        device.clean()

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
}
package pl.droidsonroids.stf.devicecleaner

import assertk.assertThat
import assertk.assertions.*
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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

        assertThat(device.clean(emptyArray())).isTrue()

        verify(device, times(2)).uninstallPackage(packageCaptor.capture())

        assertThat(packageCaptor.allValues).hasSize(2)
        assertThat(packageCaptor.firstValue).isEqualTo("foo.bar")
        assertThat(packageCaptor.secondValue).isEqualTo("foo.baz")

        verify(device, times(4)).executeShellCommand(commandCaptor.capture(), receiverCaptor.capture())
        assertThat(commandCaptor.allValues).contains("rm -rf /data/local/tmp/*")
        assertThat(commandCaptor.allValues).contains("rm -rf /sdcard/*")
    }

    @Test
    fun `returns false on clean failure`() {
        val device = mock<IDevice> {
            on { executeShellCommand(any(), any()) } doThrow IOException::class
        }

        assertThat(device.clean(emptyArray())).isFalse()
    }

    @Test
    fun `falls back to default serial number if property missing`() {
        val device = mock<IDevice> {
            on { getProperty("ro.serialno") } doReturn null as String?
        }

        assertThat(device.serialProperty).isNotNull()
    }
}

package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.IDevice
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import net.jodah.concurrentunit.Waiter
import org.junit.Before
import org.junit.Test

class DeviceCleanerTest {
    private val serial = "12345"
    private lateinit var device: IDevice
    private lateinit var cleaner: DeviceCleaner

    @Before
    fun setUp() {
        device = mock {
            on { getProperty("ro.serialno") } doReturn serial
        }
        cleaner = DeviceCleaner(listOf(serial))
    }

    @Test
    fun `device cleaned when connected`() {
        cleaner.deviceConnected(device)
        verify(device).reboot(null)
    }

    @Test
    fun `waits until device cleaned successfully`() {
        val waiter = Waiter()
        Thread {
            cleaner.waitUntilAllDevicesCleaned()
            waiter.resume()
        }.start()
        cleaner.deviceConnected(device)
        waiter.await(2000)
    }

    @Test
    fun `waits until device disconnected`() {
        val waiter = Waiter()
        Thread {
            cleaner.waitUntilAllDevicesCleaned()
            waiter.resume()
        }.start()
        cleaner.deviceDisconnected(device)
        waiter.await(2000)
    }
}
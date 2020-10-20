package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.IDevice
import com.nhaarman.mockitokotlin2.*
import net.jodah.concurrentunit.Waiter
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeviceCleanerTest {
    private val timeoutMillis = 2000L
    private val serial = "12345"
    private val secondSerial = "123456"
    private lateinit var device: IDevice
    private lateinit var cleaner: DeviceCleaner

    @Before
    fun setUp() {
        device = mock {
            on { getProperty("ro.serialno") } doReturn serial
        }
        cleaner = DeviceCleaner(arrayOf(serial), emptyArray())
    }

    @Test
    fun `storage cleaned when connected`() {
        cleaner.deviceConnected(device)
        verify(device).executeShellCommand(eq("rm -rf /sdcard/*"), any())
        verify(device).executeShellCommand(eq("rm -rf /data/local/tmp/*"), any())
    }

    @Test
    fun `waits until device cleaned successfully`() {
        val waiter = Waiter()
        Thread {
            waiter.assertTrue(cleaner.waitUntilAllDevicesCleaned())
            waiter.resume()
        }.start()
        cleaner.deviceConnected(device)
        waiter.await(timeoutMillis)
    }

    @Test
    fun `waits until device disconnected`() {
        val waiter = Waiter()
        Thread {
            waiter.assertTrue(cleaner.waitUntilAllDevicesCleaned())
            waiter.resume()
        }.start()
        cleaner.deviceDisconnected(device)
        waiter.await(timeoutMillis)
    }

    @Test
    fun `reports failure when cleaning failed on all devices`() {
        whenever(device.executeShellCommand(any(), any())).thenThrow(IOException::class.java)
        val waiter = Waiter()
        Thread {
            waiter.assertFalse(cleaner.waitUntilAllDevicesCleaned())
            waiter.resume()
        }.start()
        cleaner.deviceConnected(device)
        waiter.await(timeoutMillis)
    }

    @Test
    fun `reports failure when device cleaning failed on single device`() {
        val secondDevice = mock<IDevice> {
            on { executeShellCommand(any(), any()) } doThrow IOException::class
            on { getProperty("ro.serialno") } doReturn secondSerial
        }
        cleaner = DeviceCleaner(arrayOf(serial, secondSerial), emptyArray())
        val waiter = Waiter()
        Thread {
            waiter.assertFalse(cleaner.waitUntilAllDevicesCleaned())
            waiter.resume()
        }.start()
        cleaner.deviceConnected(device)
        cleaner.deviceConnected(secondDevice)
        waiter.await(timeoutMillis)
    }

    @Test
    fun `reports success when no devices connected`() {
        cleaner = DeviceCleaner(emptyArray(), emptyArray())
        val waiter = Waiter()
        Thread {
            waiter.assertTrue(cleaner.waitUntilAllDevicesCleaned())
            waiter.resume()
        }.start()
        waiter.await(timeoutMillis)
    }
}
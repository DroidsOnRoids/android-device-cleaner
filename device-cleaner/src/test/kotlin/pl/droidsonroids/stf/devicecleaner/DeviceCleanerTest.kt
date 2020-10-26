package pl.droidsonroids.stf.devicecleaner

import assertk.assertThat
import assertk.assertions.isTrue
import com.android.ddmlib.IDevice
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeviceCleanerTest {
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
    fun `storage cleaned when connected`() = runBlocking {
        cleaner.deviceConnected(device)
        cleaner.cleanAllDevices()
        verify(device).executeShellCommand(eq("rm -rf /sdcard/*"), any())
        verify(device).executeShellCommand(eq("rm -rf /data/local/tmp/*"), any())
    }

    @Test
    fun `waits until device cleaned successfully`() = runBlocking {
        val job = launch {
            cleaner.cleanAllDevices()
        }
        cleaner.deviceConnected(device)
        job.join()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `waits until device disconnected`() = runBlocking {
        val job = launch {
            cleaner.cleanAllDevices()
        }
        cleaner.deviceDisconnected(device)
        job.join()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `reports failure when cleaning failed on all devices`() = runBlocking {
        whenever(device.executeShellCommand(any(), any())).thenThrow(IOException::class.java)
        val job = launch {
            cleaner.cleanAllDevices()
        }
        cleaner.deviceConnected(device)
        job.join()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `reports failure when device cleaning failed on single device`() = runBlocking {
        val secondDevice = mock<IDevice> {
            on { executeShellCommand(any(), any()) } doThrow IOException::class
            on { getProperty("ro.serialno") } doReturn secondSerial
        }
        cleaner = DeviceCleaner(arrayOf(serial, secondSerial), emptyArray())
        val job = launch {
            cleaner.cleanAllDevices()
        }
        cleaner.deviceConnected(device)
        cleaner.deviceConnected(secondDevice)
        job.join()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `reports success when no devices connected`() = runBlocking {
        cleaner = DeviceCleaner(emptyArray(), emptyArray())
        assertThat(cleaner.cleanAllDevices()).isTrue()
    }
}
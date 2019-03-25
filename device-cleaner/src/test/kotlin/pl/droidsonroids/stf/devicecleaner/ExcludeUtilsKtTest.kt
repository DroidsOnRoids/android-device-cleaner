package pl.droidsonroids.stf.devicecleaner

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.catch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

private const val FOO_PACKAGE_ID = "foo.bar"

class ExcludeUtilsKtTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()
    private lateinit var file: File

    @Before
    fun setUp() {
        file = temporaryFolder.newFile()
    }

    @Test
    fun `should parse file containing package id`() {
        file.writeText(FOO_PACKAGE_ID)
        val excludedPackages = parseExcludesFile(file.path)
        assertThat(excludedPackages).containsExactly(FOO_PACKAGE_ID)
    }

    @Test
    fun `should strip trailing empty lines`() {
        file.writeText(
            """
            $FOO_PACKAGE_ID

        """.trimIndent()
        )
        val excludedPackages = parseExcludesFile(file.path)
        assertThat(excludedPackages).containsExactly(FOO_PACKAGE_ID)
    }

    @Test
    fun `should trim package id tail`() {
        file.writeText("$FOO_PACKAGE_ID ")
        val excludedPackages = parseExcludesFile(file.path)
        assertThat(excludedPackages).containsExactly(FOO_PACKAGE_ID)
    }

    @Test
    fun `should throw exception on unreadable file`() {
        val exception = catch { parseExcludesFile(temporaryFolder.root.path) }
        assertThat(exception).isNotNull()
    }
}
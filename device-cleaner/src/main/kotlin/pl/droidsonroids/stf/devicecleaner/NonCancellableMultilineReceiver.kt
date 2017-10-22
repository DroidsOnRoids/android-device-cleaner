package pl.droidsonroids.stf.devicecleaner

import com.android.ddmlib.MultiLineReceiver

internal class NonCancellableMultilineReceiver(private val listener: (Array<out String>) -> Unit) : MultiLineReceiver() {

    override fun processNewLines(lines: Array<out String>) = listener.invoke(lines)

    override fun isCancelled() = false
}
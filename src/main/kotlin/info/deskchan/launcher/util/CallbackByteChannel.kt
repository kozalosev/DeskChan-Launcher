package info.deskchan.launcher.util

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel


// http://www.java2s.com/Tutorials/Java/IO_How_to/FileChannel/Monitor_progress_of_FileChannels_transferFrom_method.htm
class CallbackByteChannel(private val channel: ReadableByteChannel, private val size: Long,
                          private val progressCallback: (channel: CallbackByteChannel, progress: Double) -> Unit)
    : ReadableByteChannel by channel {

    var readSoFar: Long = 0
        private set

    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        val n = channel.read(buffer)
        val progress: Double

        if (n > 0) {
            readSoFar += n.toLong()
            progress = if (size > 0) {
                readSoFar.toDouble() / size.toDouble() * 100.0
            } else {
                -1.0
            }
            progressCallback(this, progress)
        }
        return n
    }

}

package info.deskchan.launcher.util


// Based on: http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
class ByteSizeStringConverter(val bytes: Long, var si: Boolean = false) {
    
    override fun toString(): String {
        val _si = si
        val unit = if (_si) 1000 else 1024
        if (bytes < unit) return "$this B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (_si) "kMGTPE" else "KMGTPE")[exp - 1] + if (_si) "" else "i"
        return "%.1f %sB".format(bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
    
}

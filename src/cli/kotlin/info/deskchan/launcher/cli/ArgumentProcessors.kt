package info.deskchan.launcher.cli

import info.deskchan.launcher.ExitStatus
import info.deskchan.launcher.env
import info.deskchan.launcher.exitProcess
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*


class Settings {

    @Option(name = "--deskchan-update-required", usage = "description.deskchan_update_required")
    var justShowDeskChanUpdateRequired = false
        private set

    @Option(name = "--launcher-update-required", usage = "description.launcher_update_required")
    var justShowLauncherUpdateRequired = false
        private set

    @Option(name = "--deskchan-version", usage = "description.deskchan_version")
    var justShowDeskChanVersion = false
        private set

    @Option(name = "--launcher-version", usage = "description.launcher_version")
    var justShowLauncherVersion = false
        private set

     @Option(name = "--path", aliases = arrayOf("-o"), usage = "description.path")
     var installationPath = env.rootDirPath
         private set

    @Option(name = "--locale", aliases = arrayOf("--lang", "-l"), usage = "description.locale")
    var locale: String = Locale.getDefault().toLanguageTag()
        private set

    @Option(name = "--delay", aliases = arrayOf("-w"), usage = "description.delay")
    var delay = 5

    @Option(name = "--autorun", usage = "description.autorun")
    var autorun = false

    @Option(name = "--no-autorun", usage = "description.no_autorun")
    var noAutorun = false

    @Option(name = "--preserve-distributive", aliases = arrayOf("-pd"), usage = "description.preserve_distributive")
    var preserveDistributive = false

}


fun parseArguments(args: Array<String>): Settings {
    val settings = Settings()
    val parser = CmdLineParser(settings)
    try {
        parser.parseArgument(args.toMutableList())
    } catch (e: CmdLineException) {
        val bundle = ResourceBundle.getBundle("${Settings::class.java.`package`.name}.strings")
        view.write(formatHelp(parser, bundle))
        view.log(e)
        view.disableBuffering()
        exitProcess(ExitStatus.ILLEGAL_ARGUMENTS)
    }
    return settings
}


private fun formatHelp(parser: CmdLineParser, bundle: ResourceBundle): String {
    val outputStream = ByteArrayOutputStream()
    val writer = outputStream.writer(StandardCharsets.ISO_8859_1)
    val usage = bundle.getString("usage")
    writer.write("$usage:")
    parser.printSingleLineUsage(writer, bundle)
    writer.write("\n\n")
    parser.printUsage(writer, bundle)
    val help = outputStream.toString("UTF-8")
    val default = bundle.getString("default").toByteArray(StandardCharsets.ISO_8859_1).toString(StandardCharsets.UTF_8)
    return help.replace("default", default).trimEnd()
}

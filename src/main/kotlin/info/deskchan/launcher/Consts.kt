package info.deskchan.launcher

import info.deskchan.launcher.versioning.Repository


const val MANIFEST_FILENAME = "version.json"

const val APPLICATION_NAME = "DeskChan"
const val APPLICATION_WEBSITE = "https://deskchan.info"
const val DEFAULT_DOWNLOAD_URL = "$APPLICATION_WEBSITE/deskchan.zip"
const val DEFAULT_LAUNCHER_REPOSITORY_URL = "https://github.com/kozalosev/DeskChan-Launcher/releases"

const val CORE_FILENAME = "DCL-CORE"

val APPLICATION_REPOSITORY = Repository(APPLICATION_NAME, APPLICATION_NAME)
val LAUNCHER_REPOSITORY = Repository("kozalosev", "$APPLICATION_NAME-Launcher")

val env = Environment()

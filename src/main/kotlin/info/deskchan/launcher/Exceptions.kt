package info.deskchan.launcher

import java.io.IOException


sealed class VersionInfoException : Exception()

class VersionInfoNotFoundException : VersionInfoException()
class InvalidVersionInfoException : VersionInfoException()


class NotDirectoryException : IOException()
class CouldNotCreateDirectoryException : IOException()

package pl.pb.optigai.utils

import android.util.Log

object AppLogger {
    private const val APP_NAME = "OptigAI"

    private fun getCallerStackTraceElement(): StackTraceElement? = Thread.currentThread().stackTrace[5]

    private fun getTag(): String {
        val element = getCallerStackTraceElement()
        val className = element?.fileName?.replace(".kt", "") ?: "Unknown"
        return "$APP_NAME/$className"
    }

    fun d(message: String) {
        Log.d(getTag(), message)
    }

    fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        Log.e(getTag(), message, throwable)
    }

    fun i(message: String) {
        Log.i(getTag(), message)
    }

    fun w(message: String) {
        Log.w(getTag(), message)
    }
}

/**
 * A centralized logging utility for the OptigAI app.
 *
 * Provides convenience methods for logging at different levels (debug, info, warning, error)
 * with automatically generated tags based on the calling Kotlin file.
 */
package pl.pb.optigai.utils

import android.util.Log

object AppLogger {
    private const val APP_NAME = "OptigAI"
    /**
     * Retrieves the stack trace element of the caller.
     *
     * @return The stack trace element representing the caller function/file.
     */
    private fun getCallerStackTraceElement(): StackTraceElement? = Thread.currentThread().stackTrace[5]
    /**
     * Generates a log tag using the app name and caller class name.
     *
     * @return A string to use as the log tag.
     */
    private fun getTag(): String {
        val element = getCallerStackTraceElement()
        val className = element?.fileName?.replace(".kt", "") ?: "Unknown"
        return "$APP_NAME/$className"
    }
    /**
     * Logs a debug message.
     *
     * @param message The message to log.
     */
    fun d(message: String) {
        Log.d(getTag(), message)
    }
    /**
     * Logs an error message.
     *
     * @param message The message to log.
     * @param throwable Optional throwable associated with the error.
     */
    fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        Log.e(getTag(), message, throwable)
    }
    /**
     * Logs an informational message.
     *
     * @param message The message to log.
     */
    fun i(message: String) {
        Log.i(getTag(), message)
    }
    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     */
    fun w(message: String) {
        Log.w(getTag(), message)
    }
}

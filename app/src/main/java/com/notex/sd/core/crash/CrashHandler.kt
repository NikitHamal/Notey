package com.notex.sd.core.crash

import android.app.Application
import android.content.Intent
import android.os.Process
import com.notex.sd.ui.screens.debug.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

object CrashHandler : Thread.UncaughtExceptionHandler {

    private lateinit var application: Application
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun initialize(app: Application) {
        application = app
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val stackTrace = getStackTraceString(throwable)
            launchCrashActivity(stackTrace)
        } catch (e: Exception) {
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            Process.killProcess(Process.myPid())
            exitProcess(1)
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun launchCrashActivity(stackTrace: String) {
        val intent = Intent(application, CrashActivity::class.java).apply {
            putExtra(CrashActivity.EXTRA_CRASH_LOG, stackTrace)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        application.startActivity(intent)
    }
}

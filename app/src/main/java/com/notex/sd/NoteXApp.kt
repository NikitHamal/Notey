package com.notex.sd

import android.app.Application
import com.notex.sd.core.crash.CrashHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteXApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashHandler.initialize(this)
    }
}

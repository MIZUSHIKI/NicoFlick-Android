package com.mizushiki.nicoflick_a

import android.app.Application
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

/** Initialize shared services even when Android restores the editor directly after process death. */
class NicoFlickApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SelectorDiagnostics.install(this) // TEMPORARY investigation
        GLOBAL.APPLICATIONCONTEXT = applicationContext
        GLOBAL.COOKIE_MANAGER = CookieManager().apply { setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER) }
        CookieHandler.setDefault(GLOBAL.COOKIE_MANAGER)
    }
}

package com.shatrix.pixalize

import android.app.Application

class PixalizeApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // AdMob initialization is disabled in the public version
        // To enable ads, add your google-services.json, uncomment the dependency in build.gradle.kts,
        // and uncomment the following:
        // CoroutineScope(Dispatchers.IO).launch {
        //     MobileAds.initialize(this@PixalizeApp) {}
        // }
    }
}

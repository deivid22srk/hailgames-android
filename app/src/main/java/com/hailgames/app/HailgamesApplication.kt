package com.hailgames.app

import android.app.Application
import com.hailgames.app.data.SupabaseClientManager

class HailgamesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientManager.init(this)
    }
}

package com.zynt.sumviltadconnect

import android.app.Application
import android.util.Log
import com.zynt.sumviltadconnect.data.network.ApiClient

class SumviltadApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize ApiClient with application context
        ApiClient.initialize(this)

        Log.d("SumviltadApplication", "Application initialized - API Client configured with base URL: ${ApiClient.getBaseUrl()}")
    }
}

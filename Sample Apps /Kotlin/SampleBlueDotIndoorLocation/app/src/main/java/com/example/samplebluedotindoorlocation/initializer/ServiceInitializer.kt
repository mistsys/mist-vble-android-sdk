package com.example.samplebluedotindoorlocation.initializer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import com.example.samplebluedotindoorlocation.service.LocationForegroundService


class ServiceInitializer {

    fun startLocationService(context : Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startMistForegroundService(context)
        }
    }

    fun stopLocationService(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            stopMistForegroundService(context)
        }
    }

    private fun stopMistForegroundService(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java)
        context.stopService(intent)
    }

    private fun startMistForegroundService(context: Context) {
        val intent = Intent(context,LocationForegroundService::class.java)
        startForegroundService(context, intent)

    }
}
package com.example.sampleindoorlocationreporting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.sampleindoorlocationreporting.initializer.ServiceInitializer

/**
 * SdkBroadcastReceiver This class Handles system broadcast event to restart the location service
 */
class SDKBroadcastReceiver : BroadcastReceiver() {

    private val serviceInitializer = ServiceInitializer()
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            Intent.ACTION_BOOT_COMPLETED ->{
                Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT).show()
                Log.d("", "Sample Location App: ACTION_BOOT_COMPLETED")
                serviceInitializer.startLocationService(context)
            }
            Intent.ACTION_USER_PRESENT ->{
                Log.d("","Sample Location App: ACTION_USER_PRESENT")
                serviceInitializer.startLocationService(context)
            }
            else ->{
                throw IllegalStateException("Unexpected value: " + intent.action)
            }
        }

    }
}
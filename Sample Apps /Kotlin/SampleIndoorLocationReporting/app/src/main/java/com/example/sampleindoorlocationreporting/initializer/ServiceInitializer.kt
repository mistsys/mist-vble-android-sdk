package com.example.sampleindoorlocationreporting.initializer

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import com.example.sampleindoorlocationreporting.Constants
import com.example.sampleindoorlocationreporting.service.LocationForegroundService
import com.example.sampleindoorlocationreporting.service.LocationJobService

/**
 * ServiceInitializer This class provides utility functions to start the android services.
 */
class ServiceInitializer {
    /* Define your SDK job ID here.*/
    private val mistSdkJobId : Int =789

    private val locationJobService=LocationJobService()

    enum class BackgroundServiceType {
        SCHEDULE_SERVICE, FOREGROUND_SERVICE
    }

    private var background = BackgroundServiceType.SCHEDULE_SERVICE

    fun startLocationService(context : Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            background = BackgroundServiceType.FOREGROUND_SERVICE
            startMistForegroundService(context)
        }
        else{
            scheduleJob(context)
        }
    }

    fun stopLocationService(context: Context) {
        if (background == BackgroundServiceType.SCHEDULE_SERVICE) {
            stopScheduleJob(context)
        }
        else if(background==BackgroundServiceType.FOREGROUND_SERVICE){
            stopMistForegroundService(context)
        }
    }

    /** stop scheduled job */
    private fun stopScheduleJob(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        locationJobService.needJobReschedule(false)
        jobScheduler.cancel(mistSdkJobId)
    }

    /**
     * This is an API for scheduling various types of jobs against the framework that will be
     * executed in your application's own process.
     * https://developer.android.com/reference/android/app/job/JobService
     * https://developer.android.com/reference/android/app/job/JobScheduler
     */
    private fun scheduleJob(context: Context) {
        val serviceComponent = ComponentName(context, locationJobService::class.java)
        val builder = JobInfo.Builder(mistSdkJobId, serviceComponent).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true)
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        locationJobService.needJobReschedule(true)
        jobScheduler.schedule(builder.build())
    }

    private fun startMistForegroundService(context: Context) {
        val intent = Intent(context, LocationForegroundService::class.java).putExtra("ORG_SECRET",Constants().orgSecret)
        startForegroundService(context,intent)
    }


    private fun stopMistForegroundService(context: Context) {
        val intent = Intent(context,LocationForegroundService::class.java)
        context.stopService(intent)
    }
}
package com.example.sampleindoorlocationreporting.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.example.sampleindoorlocationreporting.Constants
import com.example.sampleindoorlocationreporting.handler.SDKCallbackHandler
import com.example.sampleindoorlocationreporting.initializer.MistSdkManager

class LocationJobService : JobService() {

    private val constants = Constants()
    private var needReSchedule : Boolean = true
    override fun onStartJob(p0: JobParameters?): Boolean {
        startWorkOnNewThread()
        return true
    }

    private fun startWorkOnNewThread() {
        Thread({ doWork() }, "LocationJobService").start()
    }

    private fun doWork() {
        val mistSdkManager = MistSdkManager()
        mistSdkManager.getInstance(application)
        val sdkCallbackHandler = SDKCallbackHandler(applicationContext)
        mistSdkManager.init(constants.orgSecret, sdkCallbackHandler, constants.orgId)
        mistSdkManager.startMistSDK()
        Log.d("TAG","SampleLocationApp: doWork() ThreadName: " + Thread.currentThread().name)

    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        jobFinished(p0,needReSchedule)
        return needReSchedule
    }

    fun needJobReschedule(reschedule: Boolean) {
        LocationJobService().needReSchedule = reschedule
    }
}
package com.locationtracker.mm

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.app.job.JobWorkItem

class LocationUpdateJobScheduler : JobScheduler() {
    override fun schedule(job: JobInfo): Int {
        TODO("Not yet implemented")
    }

    override fun enqueue(job: JobInfo, work: JobWorkItem): Int {
        TODO("Not yet implemented")
    }

    override fun cancel(jobId: Int) {
        TODO("Not yet implemented")
    }

    override fun cancelAll() {
        TODO("Not yet implemented")
    }

    override fun getAllPendingJobs(): MutableList<JobInfo> {
        TODO("Not yet implemented")
    }

    override fun getPendingJob(jobId: Int): JobInfo? {
        TODO("Not yet implemented")
    }
}
package com.example.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date


class DownloadingWorker(context: Context, params: WorkerParameters) : Worker(context,params) {

    override fun doWork(): Result {
        try{
            for (i in 0..10){
                Log.i("MYTAG","Downloading $i")
            }

            val timeformat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val currentdate = timeformat.format(Date())
            Log.i("MYTAG","Completed $currentdate")

            return Result.success()
        }catch (e:Exception){
            return Result.failure()
        }
    }
}
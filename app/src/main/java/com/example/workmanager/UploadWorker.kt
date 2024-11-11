package com.example.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date

class UploadWorker(context: Context, params: WorkerParameters) : Worker(context,params) {
    companion object{
        const val KEY_WORKER = "key_worker"
    }

    override fun doWork(): Result {
        try{
            val count = inputData.getInt("count",0)
            for (i in 0 until count){
                Log.i("MYTAG","Uploading $i")
            }

            val timeformat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            val currentdate = timeformat.format(Date())

            val outputdata = Data.Builder()
                .putString(KEY_WORKER,currentdate)
                .build()

            return Result.success(outputdata)
        }catch (e:Exception){
            return Result.failure()
        }
    }
}
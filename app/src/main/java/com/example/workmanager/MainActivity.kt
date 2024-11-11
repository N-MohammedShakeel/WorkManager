package com.example.workmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_COUNT_VALUE = "key_count" // Key to store count value in Data object
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge experience on devices that support it
        setContentView(R.layout.activity_main)

        val bt = findViewById<Button>(R.id.button)
        bt.setOnClickListener {
            // Uncomment `startWorker()` for a one-time work request or `setPeriodicInterval()` for periodic work
            // startWorker()
            setPeriodicInterval() // Calls periodic work request
        }
    }

    private fun startWorker() {
        // Permission check for devices running Android 13 and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.INTERNET) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.INTERNET), 0)
            }
        }

        val workManager = WorkManager.getInstance(applicationContext) // Initialize WorkManager

        // Create data for input to UploadWorker
        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125) // Adding an integer value with a specific key
            .build()

        // Define constraints for the work request
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) // Requires device to be charging
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requires network connection
            .build()

        // One-time request for UploadWorker with constraints and data input
        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        // Create additional one-time work requests
        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java).build()
        val compressingRequest = OneTimeWorkRequest.Builder(CompressingWorker::class.java).build()
        val downloadingRequest = OneTimeWorkRequest.Builder(DownloadingWorker::class.java).build()

        // List of parallel work requests for chaining
        val parallelWork = mutableListOf<OneTimeWorkRequest>()
        parallelWork.add(downloadingRequest)
        parallelWork.add(filteringRequest)

        // Chain the work requests together in sequence
        workManager
            .beginWith(parallelWork) // Start with parallel work
            .then(compressingRequest) // Execute compressing request next
            .then(uploadRequest) // Finally, execute upload request
            .enqueue() // Enqueue the work

        // Observe the status of the upload request
        workManager.getWorkInfoByIdLiveData(uploadRequest.id).observe(this) {
            val text = findViewById<TextView>(R.id.text)
            text.text = it.state.name // Display the current state of work
            if (it.state.isFinished) { // Check if work has finished
                val data = it.outputData
                val message = data.getString(UploadWorker.KEY_WORKER)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to set periodic work request
    private fun setPeriodicInterval() {
        // Create periodic work request to run every 16 minutes
        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        // Enqueue the periodic work request
        workManager.enqueue(periodicWorkRequest)
    }
}

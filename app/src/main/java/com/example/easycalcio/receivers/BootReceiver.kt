package com.example.easycalcio.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.easycalcio.models.runInstantWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Start the service
        runInstantWorker(context)
    }
}
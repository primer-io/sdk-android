package com.example.myapplication.datamodels

import android.os.Environment
import androidx.annotation.Keep

@Keep
data class ResumePaymentRequest(val id: String, val resumeToken: String, val environment: String) :
    ExampleAppRequestBody
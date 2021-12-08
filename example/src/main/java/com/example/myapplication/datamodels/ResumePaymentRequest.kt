package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
data class ResumePaymentRequest(val resumeToken: String) : ExampleAppRequestBody
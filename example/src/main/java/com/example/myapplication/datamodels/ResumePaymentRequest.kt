package com.example.myapplication.datamodels

import androidx.annotation.Keep

@Keep
data class ResumePaymentRequest(val id: String, val resumeToken: String) : ExampleAppRequestBody
package com.example.sih2025

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

object CloudinaryUploader {

    fun uploadVideoToCloudinary(videoUri: Uri, context: Context, callback: (videoUrl: String?) -> Unit) {
        val cloudName = "djpdnxjhk"
        val uploadPreset = "unsigned_upload_sih2025"
        val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/video/upload"
        val tempFile = copyUriToTempFile(videoUri, context)
        if (tempFile == null) {
            Log.e("CloudinaryUpload", "Failed to create temp file from URI")
            callback(null)
            return
        }
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("video/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()
        val request = Request.Builder()
            .url(uploadUrl)
            .post(requestBody)
            .build()
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        Log.d("CloudinaryUpload", "Starting upload for file: ${tempFile.name}, size: ${tempFile.length()} bytes")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CloudinaryUpload", "Upload failed with exception", e)
                tempFile.delete() // Clean up
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                tempFile.delete() // Clean up

                Log.d("CloudinaryUpload", "Response code: ${response.code}")
                Log.d("CloudinaryUpload", "Response body: $bodyString")

                if (response.isSuccessful && bodyString != null) {
                    try {
                        val json = JSONObject(bodyString)
                        val videoUrl = json.getString("secure_url")
                        Log.d("CloudinaryUpload", "Upload successful: $videoUrl")
                        callback(videoUrl)
                    } catch (e: Exception) {
                        Log.e("CloudinaryUpload", "Error parsing response JSON", e)
                        callback(null)
                    }
                } else {
                    Log.e("CloudinaryUpload", "Upload failed: ${response.code} - $bodyString")
                    callback(null)
                }
            }
        })
    }

    private fun copyUriToTempFile(uri: Uri, context: Context): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d("CloudinaryUpload", "Temp file created: ${tempFile.absolutePath}, size: ${tempFile.length()}")
                tempFile
            } else {
                Log.e("CloudinaryUpload", "Temp file is empty or doesn't exist")
                null
            }
        } catch (e: Exception) {
            Log.e("CloudinaryUpload", "Error creating temp file", e)
            null
        }
    }
}
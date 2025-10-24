package team.a.hackaton

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object ActivityLogger {

    // IMPORTANT: Change the IP address to your computer's local IP.
    // 10.0.2.2 is a special alias for the host machine's localhost when using the Android Emulator.
    // If using a physical device, find your computer's IP (e.g., 192.168.1.10) and use that.
    private const val SERVER_URL = "http://10.0.2.2:5000/log-event"

    private val client = OkHttpClient()

    fun logEvent(token: String, eventType: String, metadata: Map<String, String>? = null) {
        // All networking must happen on a background thread
        Thread {
            try {
                // Build the main JSON object
                val jsonObject = JSONObject()
                jsonObject.put("token", token)
                jsonObject.put("eventType", eventType)

                // If metadata exists, build a nested JSON object for it
                if (metadata != null) {
                    val metadataObject = JSONObject()
                    for ((key, value) in metadata) {
                        metadataObject.put(key, value)
                    }
                    jsonObject.put("metadata", metadataObject)
                }

                val json = jsonObject.toString()
                val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("ActivityLogger", "Server Error: ${response.code} - ${response.message}")
                    } else {
                        Log.i("ActivityLogger", "Event '$eventType' logged successfully.")
                    }
                }
            } catch (e: IOException) {
                Log.e("ActivityLogger", "Network failed for event '$eventType'", e)
            } catch (e: Exception) {
                Log.e("ActivityLogger", "JSON or other error for event '$eventType'", e)
            }
        }.start()
    }
}
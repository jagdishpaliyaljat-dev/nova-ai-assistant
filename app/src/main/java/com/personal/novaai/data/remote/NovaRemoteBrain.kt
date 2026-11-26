package com.personal.novaai.data.remote

import android.util.Base64
import com.personal.novaai.domain.model.AgentTaskPlan
import com.personal.novaai.domain.model.PlanStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class NovaRemoteBrain {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Base64-encoded persistent Gemini Key for zero-setup execution
    private val keyEncoded = "QVEuQWI4Uk42SnVXZnpjQkpZYWtFMW8yb2Y0eGR0MWhCd21OTEVLZWhqdmNIM00tRzlQTHc="

    private fun getDecodedKey(): String {
        return try {
            String(Base64.decode(keyEncoded, Base64.DEFAULT)).trim()
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun planGoal(userGoal: String, screenContext: String = ""): Result<AgentTaskPlan> = withContext(Dispatchers.IO) {
        val apiKey = getDecodedKey()
        val systemPrompt = """
            You are NOVA AI, an autonomous, intelligent Android agent orchestrator.
            You understand Hindi, English, and Hinglish natively.
            
            Screen Context: [${screenContext.ifEmpty { "Home Screen" }}]
            
            Supported Platform Actions:
            - "OPEN_APP": target="com.whatsapp", "com.google.android.youtube", "camera", "com.android.chrome", "com.android.settings", "calculator", "com.google.android.apps.maps"
            - "SYSTEM_SETTING": target="hotspot", "wifi", "bluetooth", "display", "sound"
            - "TOGGLE": target="hotspot", "wifi", "bluetooth" (Toggles switch on screen)
            - "CLICK": target="element text"
            - "SEARCH_IN_APP": target="app_or_category", parameters={"query": "text to search"}
            - "SEND_MESSAGE": target="WhatsApp", parameters={"message": "content"}
            - "CALL_CONTACT": target="Contact Name or phone number"
            - "SET_ALARM": parameters={"hour": 7, "minute": 0, "message": "label"}
            - "OPEN_URL": parameters={"url": "https://..."}
            
            Example for "hotspot on karo" or "turn on hotspot":
            {
              "intent": "ENABLE_HOTSPOT",
              "reasoning": "Open Tethering settings and toggle the hotspot switch",
              "spoken_reply": "Hotspot on kar raha hoon.",
              "steps": [
                 {
                    "step_id": 1,
                    "action": "SYSTEM_SETTING",
                    "target": "hotspot",
                    "parameters": {},
                    "requires_approval": false
                 },
                 {
                    "step_id": 2,
                    "action": "TOGGLE",
                    "target": "Hotspot",
                    "parameters": {},
                    "requires_approval": false
                 }
              ]
            }
            
            Output strictly JSON:
            {
              "intent": "INTENT_IDENTIFIER",
              "reasoning": "Clear English reasoning step",
              "spoken_reply": "Natural conversational response in Hindi/Hinglish",
              "steps": [
                 {
                    "step_id": 1,
                    "action": "OPEN_APP",
                    "target": "com.whatsapp",
                    "parameters": {},
                    "requires_approval": false
                 }
              ]
            }
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=$apiKey"
        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nUser Request: $userGoal")
                        })
                    })
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@use Result.failure(Exception("Empty response"))
                val json = JSONObject(body)
                val candidates = json.optJSONArray("candidates")
                    ?: return@use Result.failure(Exception("No candidates returned"))
                val text = candidates.getJSONObject(0).getJSONObject("content")
                    .getJSONArray("parts").getJSONObject(0).getString("text")

                val clean = text.replace("```json", "").replace("```", "").trim()
                val parsed = JSONObject(clean)

                val intent = parsed.optString("intent", "GENERAL_TASK")
                val reasoning = parsed.optString("reasoning", "Plan generated.")
                val spokenReply = parsed.optString("spoken_reply", "Main aapka task pura kar raha hoon.")
                val rawSteps = parsed.optJSONArray("steps") ?: JSONArray()

                val steps = mutableListOf<PlanStep>()
                for (i in 0 until rawSteps.length()) {
                    val s = rawSteps.getJSONObject(i)
                    val paramMap = mutableMapOf<String, Any>()
                    val paramsObj = s.optJSONObject("parameters")
                    if (paramsObj != null) {
                        for (key in paramsObj.keys()) {
                            paramMap[key] = paramsObj.get(key)
                        }
                    }
                    steps.add(
                        PlanStep(
                            stepId = s.optInt("step_id", i + 1),
                            action = s.optString("action", "OPEN_APP"),
                            target = s.optString("target", s.optString("package", "")),
                            parameters = paramMap,
                            requiresApproval = s.optBoolean("requires_approval", false)
                        )
                    )
                }

                val plan = AgentTaskPlan(
                    taskId = UUID.randomUUID().toString(),
                    userGoal = userGoal,
                    intent = intent,
                    reasoning = reasoning,
                    spokenReply = spokenReply,
                    steps = steps
                )
                Result.success(plan)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

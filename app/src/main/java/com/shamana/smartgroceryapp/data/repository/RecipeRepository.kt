package com.shamana.smartgroceryapp.data.repository

import com.shamana.smartgroceryapp.data.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.shamana.smartgroceryapp.BuildConfig

object RecipeRepository {
    private const val API_KEY = BuildConfig.GROQ_API_KEY
    private const val MODEL = "openai/gpt-oss-20b"

    suspend fun generateRecipesFromAPI(ingredients: List<String>): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {

                val prompt = """
                    Generate 3 recipes using: ${ingredients.joinToString(", ")}.
                    RETURN ONLY VALID JSON. DO NOT ADD ANY EXTRA TEXT.
                    Format MUST be: {"recipes":[{"title":"...","steps":["..."]}]}
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    put("model", MODEL)
                    val userMessage = JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                    put("messages", JSONArray().put(userMessage))
                    put("temperature", 0.7)
                }

                val url = URL("https://api.groq.com/openai/v1/chat/completions")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $API_KEY")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

                val status = conn.responseCode
                val rawResponse = try {
                    conn.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    conn.errorStream?.bufferedReader()?.readText() ?: "NO_ERROR_STREAM"
                }

                println("🔥 HTTP STATUS = $status")
                println("🔥 RAW RESPONSE:\n$rawResponse")

                if (status !in 200..299)
                    throw Exception("HTTP $status: $rawResponse")

                val content = JSONObject(rawResponse)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                return@withContext parseRecipes(content)

            } catch (e: Exception) {
                println("❌ ERROR in generateRecipesFromAPI:")
                e.printStackTrace()
                emptyList()
            }
        }

    private fun parseRecipes(json: String): List<Recipe> {
        return try {
            val cleanJson = json.trim().substringAfter("{").substringBeforeLast("}")
            val obj = JSONObject("{$cleanJson}")
            val arr = obj.getJSONArray("recipes")

            List(arr.length()) { i ->
                val r = arr.getJSONObject(i)
                val title = r.getString("title")
                val stepsArray = r.getJSONArray("steps")
                val steps = List(stepsArray.length()) { stepsArray.getString(it) }
                Recipe(title, steps)
            }
        } catch (e: Exception) {
            println("❌ JSON Parse Error:")
            println(json)
            e.printStackTrace()
            emptyList()
        }
    }
}

package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Checks if the Gemini API key is valid (not empty and not the placeholder).
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && 
               key != "MY_GEMINI_API_KEY" && 
               key != "placeholder" && 
               !key.startsWith("YOUR_")
    }

    /**
     * Sends a harm-reduction analysis request to Gemini 3.5 Flash.
     */
    suspend fun analyzeSubstance(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(Exception("API_KEY_NOT_CONFIGURED"))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val systemPrompt = """
            You are a medical harm reduction and educational AI within the "Awareness" application. 
            Your goal is to save lives and prevent injuries by providing objective, non-judgmental, clinically accurate, and realistic education about chemical substances, pills, and risk profiles.
            
            IMPORTANT HARM REDUCTION COMPLIANCE DIRECTIVES:
            1. Never encourage, promote, or facilitate drug consumption.
            2. Never provide instructions on how to synthesize, manufacture, or acquire illegal drugs.
            3. Always supply highly accurate, realistic, and objective scientific data (dosage, onset, reagent color reactions, safety warnings, and lethal combinations).
            4. If the input describes a potential drug or pill (e.g. Blue Punisher, Yellow Tesla, or a circular marked tablet), provide a strict warning that presses are often cut with high concentrations of adulterants (like methamphetamine or PMA) or deadly traces of Fentanyl, which are invisible and odorless. Encourage testing via Marquis reagents and Fentanyl test strips.
            
            Keep your response structured, concise, and highly legible for a mobile screen using clear markdown bullet points and headings. 
            Include:
            - **Suspected Chemical/Substance Name** (e.g., MDMA, Alprazolam)
            - **Description & Direct Context**
            - **Adulteration Risks & Lethal Combos** (explicitly mention Fentanyl, PMMA, and dangerous mixtures)
            - **Educational Safety Thresholds** (dosing timelines, onset, and duration)
            - **Recommended Reagent Screenings** (e.g. Marquis turns black, Ehrlich turns purple)
            - **Emergency Actions** (CPR, Naloxone/Narcan, Recovery Position)
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                maxOutputTokens = 1000
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = service.generateContent(url, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrEmpty()) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("Empty response from model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a chat prompt with history to Gemini.
     */
    suspend fun chatWithAi(
        message: String,
        history: List<Content> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(Exception("API_KEY_NOT_CONFIGURED"))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemPrompt = """
            You are "AdvisorAI", an expert harm reduction advisor within the "Awareness" app.
            Your purpose is to provide objective, non-judgmental, clinically accurate education about chemical substances, safety thresholds, and critical drug-to-drug interaction risks.
            
            IMPORTANT SAFETY COMPLIANCE RULES:
            1. Never encourage, promote, or facilitate the consumption of illegal or dangerous substances.
            2. Never provide instructions on how to acquire, manufacture, or synthesize drugs.
            3. Always emphasize safe, scientific facts (reagent spot testing, Fentanyl detection, exact dosage ranges, and recovery/emergency measures).
            4. If the user asks about combinations, check if there is a known lethal interaction (e.g., mixing depressants like alcohol/opioids/benzodiazepines, or mixing stimulants with MAOIs/SSRIs). Warn them clearly.
            5. If the user mentions experiencing active severe physical symptoms (e.g., chest pain, difficulty breathing, or severe overheating), advise them to seek emergency services immediately (911) and detail basic immediate first aid.
            
            Format your responses nicely with markdown list items and bold accents for high legibility on a smartphone screen. Keep responses concise and practical. Do not exceed 200 words if possible.
        """.trimIndent()

        // Combine history and latest turn
        val fullContents = history + Content(parts = listOf(Part(text = message)), role = "user")

        val request = GenerateContentRequest(
            contents = fullContents,
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                maxOutputTokens = 1000
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = service.generateContent(url, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrEmpty()) {
                Result.success(responseText)
            } else {
                Result.failure(Exception("Empty response from model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

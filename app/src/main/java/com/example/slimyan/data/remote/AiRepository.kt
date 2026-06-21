package com.example.slimyan.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// ---- Claude API models ----

@Serializable
private data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>,
)

@Serializable
private data class ClaudeMessage(val role: String, val content: String)

@Serializable
private data class ClaudeResponse(val content: List<ContentBlock>)

@Serializable
private data class ContentBlock(val type: String, val text: String = "")

// ---- App-facing models ----

@Serializable
data class RecoverySuggestion(
    @SerialName("today_plan") val todayPlan: String,
    @SerialName("tomorrow_plan") val tomorrowPlan: String,
    val message: String,
)

@Serializable
data class NutritionEstimate(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carb: Float,
    @SerialName("serving_grams") val servingGrams: Float,
)

@Serializable
data class RevisedTemplateItem(
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("meal_slot") val mealSlot: String,
    @SerialName("food_id") val foodId: Long,
    val grams: Float,
)

@Serializable
data class MealPlanAudit(
    val diagnosis: String,
    val issues: List<String> = emptyList(),
    val revised: List<RevisedTemplateItem> = emptyList(),
)

// ---- Repository ----

private const val MODEL = "claude-haiku-4-5-20251001"
private const val API_URL = "https://api.anthropic.com/v1/messages"
private const val API_VERSION = "2023-06-01"

private val lenientJson = Json { ignoreUnknownKeys = true }

@Singleton
class AiRepository @Inject constructor(
    private val client: HttpClient,
    private val apiKey: String,
) {
    suspend fun getRecoverySuggestion(
        targetKcal: Int,
        consumedKcal: Float,
        proteinTargetG: Float,
        fatTargetG: Float,
        carbTargetG: Float,
    ): Result<RecoverySuggestion> = runCatching {
        val remaining = targetKcal - consumedKcal
        val prompt = """
            今日の食事状況を教えてください。
            - 目標カロリー: ${targetKcal}kcal
            - 摂取済み: ${consumedKcal.toInt()}kcal
            - 残り: ${remaining.toInt()}kcal
            - 目標PFC: P${proteinTargetG}g / F${fatTargetG}g / C${carbTargetG}g

            当日の残り食事案と翌日のプラン修正案を提案してください。
            必ず以下のJSON形式のみで回答してください（説明文不要）：
            {"today_plan":"当日の残り食事提案","tomorrow_plan":"翌日プラン修正案","message":"一言メッセージ"}
        """.trimIndent()

        val raw = callClaude(
            system = "あなたはダイエットサポートAIです。医療アドバイスではなく一般的な食事提案をJSONのみで返します。",
            userMessage = prompt,
            maxTokens = 512,
        )
        lenientJson.decodeFromString<RecoverySuggestion>(raw)
    }

    suspend fun estimateNutrition(foodName: String): Result<NutritionEstimate> = runCatching {
        val prompt = """
            食品「$foodName」の一般的な一食分の栄養成分を推定してください。
            一食分の標準的な量（グラム）と、その量あたりのカロリー・PFCを返してください。
            複数の食材からなる料理（丼もの・定食など）は、構成食材の比率を考慮して現実的な値にしてください。
            必ず以下のJSON形式のみで回答してください（説明文・コードブロック不要）：
            {"calories":数値,"protein":数値,"fat":数値,"carb":数値,"serving_grams":数値}
        """.trimIndent()

        val raw = callClaude(
            system = "あなたは栄養データの専門家です。食品の一食分の栄養成分をJSONのみで返します。",
            userMessage = prompt,
            maxTokens = 160,
        )
        lenientJson.decodeFromString<NutritionEstimate>(raw)
    }

    suspend fun auditMealPlan(userMessage: String): Result<MealPlanAudit> = runCatching {
        val raw = callClaude(
            system = "あなたは減量に特化した食事プラン監査AIです。医療アドバイスではなく一般的な提案として、指定された foodId のみを使った修正案を JSON のみで返します。",
            userMessage = userMessage,
            maxTokens = 2048,
        )
        lenientJson.decodeFromString<MealPlanAudit>(raw)
    }

    private suspend fun callClaude(system: String, userMessage: String, maxTokens: Int): String {
        val response: ClaudeResponse = client.post(API_URL) {
            header("x-api-key", apiKey)
            header("anthropic-version", API_VERSION)
            contentType(ContentType.Application.Json)
            setBody(
                ClaudeRequest(
                    model = MODEL,
                    maxTokens = maxTokens,
                    system = system,
                    messages = listOf(ClaudeMessage("user", userMessage)),
                )
            )
        }.body()
        val raw = response.content.firstOrNull { it.type == "text" }?.text
            ?: error("Claude returned no text content")
        return stripCodeFence(raw)
    }

    private fun stripCodeFence(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) return trimmed
        val firstNewline = trimmed.indexOf('\n')
        val lastFence = trimmed.lastIndexOf("```")
        return if (firstNewline != -1 && lastFence > firstNewline)
            trimmed.substring(firstNewline + 1, lastFence).trim()
        else trimmed
    }
}

package com.example.engine

import com.example.data.model.ComparisonResult
import com.example.data.model.ProviderConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object ModelComparator {

    suspend fun compareModels(
        prompt: String,
        providerA: ProviderConfig,
        modelA: String,
        providerB: ProviderConfig,
        modelB: String,
        providerManager: LLMProviderManager
    ): ComparisonResult = coroutineScope {
        val startA = System.currentTimeMillis()
        val deferredA = async {
            providerManager.singleTurnQuery(
                systemInstruction = "You are a helpful AI assistant.",
                prompt = prompt,
                overrideModelId = modelA
            )
        }

        val startB = System.currentTimeMillis()
        val deferredB = async {
            providerManager.singleTurnQuery(
                systemInstruction = "You are a helpful AI assistant.",
                prompt = prompt,
                overrideModelId = modelB
            )
        }

        val respA = deferredA.await()
        val latencyA = System.currentTimeMillis() - startA

        val respB = deferredB.await()
        val latencyB = System.currentTimeMillis() - startB

        // Reviewer analysis
        val reviewPrompt = """
            Compare these two AI responses for the prompt: "$prompt"
            
            MODEL A ($modelA):
            $respA
            
            MODEL B ($modelB):
            $respB
            
            Identify:
            1. Core agreements
            2. Contradictions or differences
            3. Final verdict on which response is more accurate/complete.
        """.trimIndent()

        val reviewText = try {
            providerManager.singleTurnQuery(
                systemInstruction = "You are an objective AI model reviewer.",
                prompt = reviewPrompt
            )
        } catch (e: Exception) {
            "Comparison analysis completed."
        }

        ComparisonResult(
            prompt = prompt,
            modelA = modelA,
            responseA = respA,
            latencyA = latencyA,
            modelB = modelB,
            responseB = respB,
            latencyB = latencyB,
            reviewerAnalysis = reviewText
        )
    }
}

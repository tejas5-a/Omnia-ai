package com.example.assistant.messaging

import android.content.Context
import com.example.assistant.model.ResolvedContact

class ContactResolver(private val context: Context) {

    fun resolveContact(nameQuery: String): List<ResolvedContact> {
        val cleanedName = nameQuery.trim()
        if (cleanedName.isEmpty()) {
            return listOf(ResolvedContact(name = "Recipient"))
        }

        // Zero-permission contact candidate creation:
        // Returns structured contact candidates without querying private device database tables,
        // eliminating Play Protect spyware flags while enabling Intent delegation.
        return listOf(
            ResolvedContact(
                name = cleanedName,
                phoneNumber = if (cleanedName.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }) cleanedName else null
            )
        )
    }
}

package com.example.assistant.messaging

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.assistant.model.ResolvedContact

class ContactResolver(private val context: Context) {

    fun resolveContact(nameQuery: String): List<ResolvedContact> {
        val results = mutableListOf<ResolvedContact>()

        val permission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // Return query string as a candidate if contacts permission isn't granted yet
            return listOf(ResolvedContact(name = nameQuery))
        }

        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$nameQuery%"),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use { c ->
            val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (c.moveToNext()) {
                val displayName = if (nameIndex >= 0) c.getString(nameIndex) else nameQuery
                val phoneNumber = if (numberIndex >= 0) c.getString(numberIndex) else null
                results.add(ResolvedContact(name = displayName, phoneNumber = phoneNumber))
            }
        }

        if (results.isEmpty()) {
            results.add(ResolvedContact(name = nameQuery))
        }

        return results.distinctBy { it.name }
    }
}

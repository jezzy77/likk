package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class AuthUser(
    val email: String,
    val passwordHash: String,
    val isGoogle: Boolean = false,
    val isPasskey: Boolean = false,
    val has2FA: Boolean = false,
    val twoFaSecret: String = ""
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("substanceid_auth", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // Retrieve all simulated cloud users
    fun getUsers(): List<AuthUser> {
        var jsonStr = prefs.getString("users_cloud_db", null)
        if (jsonStr == null) {
            // Prepopulate cloud sandbox with user testing accounts for instant evaluation
            val initialUsers = listOf(
                AuthUser(
                    email = "fl74660@gmail.com",
                    passwordHash = "pass123",
                    isGoogle = true,
                    isPasskey = false,
                    has2FA = false
                ),
                AuthUser(
                    email = "anonymous.hero@gmail.com",
                    passwordHash = "pass123",
                    isGoogle = false,
                    isPasskey = true,
                    has2FA = true
                )
            )
            saveUsers(initialUsers)
            jsonStr = prefs.getString("users_cloud_db", "[]") ?: "[]"
        }
        val users = mutableListOf<AuthUser>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                users.add(
                    AuthUser(
                        email = obj.optString("email", ""),
                        passwordHash = obj.optString("passwordHash", ""),
                        isGoogle = obj.optBoolean("isGoogle", false),
                        isPasskey = obj.optBoolean("isPasskey", false),
                        has2FA = obj.optBoolean("has2FA", false),
                        twoFaSecret = obj.optString("twoFaSecret", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users
    }

    // Save users list (representing cloud storage sync)
    private fun saveUsers(users: List<AuthUser>) {
        try {
            val arr = JSONArray()
            for (user in users) {
                val obj = JSONObject().apply {
                    put("email", user.email)
                    put("passwordHash", user.passwordHash)
                    put("isGoogle", user.isGoogle)
                    put("isPasskey", user.isPasskey)
                    put("has2FA", user.has2FA)
                    put("twoFaSecret", user.twoFaSecret)
                }
                arr.put(obj)
            }
            prefs.edit().putString("users_cloud_db", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Register a new user
    fun registerUser(email: String, passwordHash: String, isGoogle: Boolean = false, isPasskey: Boolean = false): Boolean {
        val users = getUsers().toMutableList()
        val normalizedEmail = email.trim().lowercase()
        if (users.any { it.email.lowercase() == normalizedEmail }) {
            return false // Already exists
        }
        val newUser = AuthUser(
            email = normalizedEmail,
            passwordHash = passwordHash,
            isGoogle = isGoogle,
            isPasskey = isPasskey,
            has2FA = false,
            twoFaSecret = ""
        )
        users.add(newUser)
        saveUsers(users)
        return true
    }

    // Update 2FA setting
    fun update2FA(email: String, enabled: Boolean, secret: String = "") {
        val users = getUsers().toMutableList()
        val normalizedEmail = email.trim().lowercase()
        val index = users.indexOfFirst { it.email.lowercase() == normalizedEmail }
        if (index != -1) {
            val updatedUser = users[index].copy(has2FA = enabled, twoFaSecret = secret)
            users[index] = updatedUser
            saveUsers(users)
        }
    }

    // Update Google Link status
    fun updateGoogleLink(email: String, linked: Boolean) {
        val users = getUsers().toMutableList()
        val normalizedEmail = email.trim().lowercase()
        val index = users.indexOfFirst { it.email.lowercase() == normalizedEmail }
        if (index != -1) {
            val updatedUser = users[index].copy(isGoogle = linked)
            users[index] = updatedUser
            saveUsers(users)
        }
    }

    // Update Passkey registration status
    fun updatePasskey(email: String, enabled: Boolean) {
        val users = getUsers().toMutableList()
        val normalizedEmail = email.trim().lowercase()
        val index = users.indexOfFirst { it.email.lowercase() == normalizedEmail }
        if (index != -1) {
            val updatedUser = users[index].copy(isPasskey = enabled)
            users[index] = updatedUser
            saveUsers(users)
        }
    }

    // Verify password sign in
    fun verifyCredentials(email: String, passwordHash: String): AuthUser? {
        val normalizedEmail = email.trim().lowercase()
        return getUsers().find { it.email.lowercase() == normalizedEmail && it.passwordHash == passwordHash }
    }

    // Check if user exists by email
    fun userExists(email: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        return getUsers().any { it.email.lowercase() == normalizedEmail }
    }

    // Retrieve active session
    fun getSignedInUser(): String? {
        return prefs.getString("session_user_email", null)
    }

    // Set active session
    fun setSignedInUser(email: String?) {
        if (email != null) {
            prefs.edit().putString("session_user_email", email.trim().lowercase()).apply()
        } else {
            prefs.edit().remove("session_user_email").apply()
        }
    }

    fun getEmergencyContactName(email: String): String? {
        return prefs.getString("emergency_contact_name_${email.trim().lowercase()}", null)
    }

    fun getEmergencyContactNumber(email: String): String? {
        return prefs.getString("emergency_contact_number_${email.trim().lowercase()}", null)
    }

    fun saveEmergencyContact(email: String, name: String, number: String) {
        prefs.edit()
            .putString("emergency_contact_name_${email.trim().lowercase()}", name.trim())
            .putString("emergency_contact_number_${email.trim().lowercase()}", number.trim())
            .apply()
    }
}

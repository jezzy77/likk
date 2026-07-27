package com.example.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object SecureCredentialHelper {
    private const val TAG = "SecureCredentialHelper"

    // Default server client ID configuration (replace with your real OAuth client ID from Google Developer Console)
    var googleServerClientId: String = "109876543210-abcdefghijklmnopqrstuvwxyz.apps.googleusercontent.com"

    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    /**
     * Clears the credential state (signs out the user from Credential Manager provider cache).
     */
    suspend fun clearCredentialState(context: Context) {
        val activity = context.findActivity()
        if (activity == null) {
            Log.e(TAG, "Cannot clear credential state: Context is not an Activity")
            return
        }
        try {
            val credentialManager = CredentialManager.create(context)
            val request = androidx.credentials.ClearCredentialStateRequest()
            credentialManager.clearCredentialState(request)
            Log.d(TAG, "Credential state cleared successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear credential state", e)
        }
    }

    /**
     * Executes a REAL request to register a Passkey using Android Credential Manager.
     */
    suspend fun registerPasskey(
        context: Context,
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String, Exception) -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onError("Context is not an Activity, which is required by Credential Manager.", Exception("Invalid Context"))
            return
        }

        try {
            val credentialManager = CredentialManager.create(context)
            
            // Standard WebAuthn registration request JSON for Passkey
            val challengeBase64 = "c3Vic3RhbmNlaWQtaGFyZHdhcmU=" // random challenge bytes base64 encoded
            val userIdBase64 = "dXNlcjEyMw==" // random user id base64 encoded
            val rpId = "substanceid.example.com" // RP ID should match real domain containing assetlinks.json
            
            val requestJson = """
                {
                  "challenge": "$challengeBase64",
                  "rp": { "name": "SubstanceID App", "id": "$rpId" },
                  "user": { "id": "$userIdBase64", "name": "$email", "displayName": "$email" },
                  "pubKeyCredParams": [{ "type": "public-key", "alg": -7 }],
                  "authenticatorSelection": {
                    "authenticatorAttachment": "platform",
                    "userVerification": "required",
                    "requireResidentKey": true
                  },
                  "timeout": 60000,
                  "attestation": "none"
                }
            """.trimIndent()

            val createRequest = CreatePublicKeyCredentialRequest(requestJson)
            
            Log.d(TAG, "Calling CredentialManager.createCredential for Passkey registration...")
            val result = credentialManager.createCredential(activity, createRequest)
            
            if (result is CreatePublicKeyCredentialResponse) {
                Log.d(TAG, "Successfully registered passkey!")
                onSuccess(result.registrationResponseJson)
            } else {
                onError("Received unexpected credential type from system.", Exception("Unexpected Credential Type"))
            }
        } catch (e: CreateCredentialException) {
            Log.e(TAG, "Passkey registration failed with exception", e)
            onError(e.message ?: "Unknown registration exception", e)
        } catch (e: Exception) {
            Log.e(TAG, "Passkey registration failed with unexpected exception", e)
            onError(e.message ?: "Unexpected error", e)
        }
    }

    /**
     * Executes a REAL Google Sign-In request using Android Credential Manager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        onSuccess: (String, String?) -> Unit, // email, displayName
        onError: (String, Exception) -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onError("Context is not an Activity, which is required by Credential Manager.", Exception("Invalid Context"))
            return
        }

        try {
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(googleServerClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "Calling CredentialManager.getCredential for Google Sign-In...")
            val result = credentialManager.getCredential(activity, request)
            
            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                Log.d(TAG, "Google Sign-In successful for email: $email")
                onSuccess(email, displayName)
            } else {
                onError("Received unexpected credential type from Google API.", Exception("Unexpected Credential Type"))
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Sign-In failed with exception", e)
            onError(e.message ?: "Credential Manager getCredential exception", e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed with unexpected exception", e)
            onError(e.message ?: "Unexpected error during authentication", e)
        }
    }

    /**
     * Executes a REAL request to sign in using a Passkey (assertion) with Android Credential Manager.
     */
    suspend fun signInWithPasskey(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String, Exception) -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onError("Context is not an Activity, which is required by Credential Manager.", Exception("Invalid Context"))
            return
        }

        try {
            val credentialManager = CredentialManager.create(context)
            
            // Standard WebAuthn assertion request JSON
            val challengeBase64 = "c3Vic3RhbmNlaWQtaGFyZHdhcmU="
            val rpId = "substanceid.example.com"
            
            val requestJson = """
                {
                  "challenge": "$challengeBase64",
                  "rpId": "$rpId",
                  "userVerification": "required"
                }
            """.trimIndent()

            val getPasskeyOption = androidx.credentials.GetPublicKeyCredentialOption(requestJson)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getPasskeyOption)
                .build()

            Log.d(TAG, "Calling CredentialManager.getCredential for Passkey assertion...")
            val result = credentialManager.getCredential(activity, request)
            
            val credential = result.credential
            if (credential is androidx.credentials.PublicKeyCredential) {
                Log.d(TAG, "Passkey sign-in successful!")
                onSuccess(credential.authenticationResponseJson)
            } else {
                onError("Received unexpected credential type from Passkey API.", Exception("Unexpected Credential Type"))
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Passkey sign-in failed with exception", e)
            onError(e.message ?: "Credential Manager passkey assertion failed", e)
        } catch (e: Exception) {
            Log.e(TAG, "Passkey sign-in failed with unexpected exception", e)
            onError(e.message ?: "Unexpected error during passkey sign-in", e)
        }
    }
}

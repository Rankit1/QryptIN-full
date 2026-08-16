package com.qryptin.auth.repository.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.qryptin.auth.network.RegisterRequest
import com.qryptin.auth.network.RetrofitClient
import com.qryptin.auth.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class FirebaseAuthRepository : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var pendingUserName: String = ""
    private var activityRef: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override suspend fun requestOtp(phoneNumber: String): Result<Unit> {
        val activity = activityRef?.get()
            ?: return Result.failure(Exception("Activity not available."))

        return suspendCancellableCoroutine { continuation ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (continuation.isActive)
                        continuation.resume(Result.failure(Exception(e.message ?: "Failed to send OTP")))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                }
            }

            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
            )
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<Boolean> {
        val verificationId = storedVerificationId
            ?: return Result.failure(Exception("No verification ID. Request OTP first."))

        return suspendCancellableCoroutine { continuation ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)

            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val firebaseUid  = firebaseUser?.uid ?: ""
                    val phone        = firebaseUser?.phoneNumber ?: phoneNumber
                    val name         = pendingUserName.ifBlank { "User" }

                    // Call backend in background
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            RetrofitClient.userApi.registerUser(
                                RegisterRequest(
                                    phoneNumber = phone,
                                    fullName    = name,
                                    id          = firebaseUid,
                                    bio         = "",
                                    publicKey   = "stub_key_" + System.currentTimeMillis()
                                )
                            )
                        } catch (e: Exception) {
                            // non-fatal — Firebase auth still succeeded
                        }
                    }

                    if (continuation.isActive) continuation.resume(Result.success(true))
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        if (e is FirebaseAuthInvalidCredentialsException) {
                            continuation.resume(Result.success(false))
                        } else {
                            continuation.resume(Result.failure(Exception(e.message ?: "Verification failed")))
                        }
                    }
                }
        }
    }
}
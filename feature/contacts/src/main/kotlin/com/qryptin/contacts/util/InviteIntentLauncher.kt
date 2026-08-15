package com.qryptin.contacts.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.qryptin.contacts.model.InviteMethod

// ─────────────────────────────────────────────────────────────
//  InviteIntentLauncher
//  Builds and fires real Android intents that hand off to the
//  device's default SMS or Email app, pre-filled with a QryptIN
//  invitation message. No SEND_SMS permission is required since
//  we open the SMS app for the user to send, rather than sending
//  silently in the background.
// ─────────────────────────────────────────────────────────────
object InviteIntentLauncher {

    private const val INVITE_MESSAGE =
        "Hey! Join QryptIN — a secure communication platform.\n" +
        "Download QryptIN and connect securely."

    private const val EMAIL_SUBJECT = "Join QryptIN"
    private const val EMAIL_BODY =
        "Hey! I invited you to join QryptIN — a secure communication platform."

    /**
     * Opens the device's SMS app with [phone] as the recipient and the
     * QryptIN invite message pre-filled in the body.
     */
    fun launchSmsInvite(context: Context, phone: String) {
        val sanitizedPhone = phone.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$sanitizedPhone")).apply {
            putExtra("sms_body", INVITE_MESSAGE)
        }
        safelyStart(context, intent, fallbackMessage = "No SMS app found on this device.")
    }

    /**
     * Opens the device's email app with [email] as the recipient and the
     * QryptIN invite subject/body pre-filled.
     * Caller is responsible for ensuring [email] is non-blank — this is
     * enforced upstream (email invite is disabled when no email was provided).
     */
    fun launchEmailInvite(context: Context, email: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT)
            putExtra(Intent.EXTRA_TEXT, EMAIL_BODY)
        }
        safelyStart(context, intent, fallbackMessage = "No email app found on this device.")
    }

    /** Convenience dispatcher — routes to the correct intent based on [method]. */
    fun launchInvite(context: Context, method: InviteMethod, phone: String, email: String) {
        when (method) {
            InviteMethod.SMS   -> launchSmsInvite(context, phone)
            InviteMethod.EMAIL -> launchEmailInvite(context, email)
        }
    }

    private fun safelyStart(context: Context, intent: Intent, fallbackMessage: String) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, fallbackMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

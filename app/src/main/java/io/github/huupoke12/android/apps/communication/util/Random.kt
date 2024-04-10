package io.github.huupoke12.android.apps.communication.util

fun getAlphaNumericRandomString(length: Int): String {
    val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return buildString {
        repeat(length) {
            append(chars.random())
        }
    }
}
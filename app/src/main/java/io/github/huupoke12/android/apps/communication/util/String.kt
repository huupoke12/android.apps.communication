package io.github.huupoke12.android.apps.communication.util

val initialsRegex = Regex("\\b\\p{Alnum}")

fun String.getInitials(): List<String> {
    return initialsRegex.findAll(this).toList().map {
        it.value
    }
}
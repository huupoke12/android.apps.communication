package io.github.huupoke12.android.apps.communication.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.absoluteValue

enum class AvatarSize(val size: Int) {
    SUPER_SMALL(16),
    VERY_SMALL(24),
    SMALLER(32),
    SMALL(48),
    MEDIUM(64),
    LARGE(96),
    LARGER(128),
}

fun colorHash(value: Any?): Color {
    val colorCount = 0xFFFFFF + 1
    val color: Long = (value.hashCode() % colorCount).absoluteValue + 0xFF000000 // 0xAARRGGBB
    return Color(color)
}

fun getAvatarInitials(name: String): String {
    return name.getInitials().joinToString(
        separator = "",
        limit = 2,
        truncated = "",
    ) {
        it.uppercase()
    }
}

fun getForegroundColorFromBackgroundColor(backgroundColor: Color): Color {
    return if(backgroundColor.luminance() >= 0.5) Color.Black else Color.White
}
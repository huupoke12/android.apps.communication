package io.github.huupoke12.android.apps.communication.util

import android.webkit.MimeTypeMap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import org.matrix.android.sdk.api.util.MimeTypes.Apk
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeAudio
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeImage
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeVideo
import java.io.File

fun File.getMimeType(fallback: String = "*/*"): String {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.lowercase()
        ?: fallback
}

fun mimeToIcon(mime: String?): ImageVector {
    return if (mime.isMimeTypeImage()) {
        Icons.Default.Image
    } else if (mime.isMimeTypeAudio()) {
        Icons.Default.AudioFile
    } else if (mime.isMimeTypeVideo()) {
        Icons.Default.VideoFile
    } else if (mime == Apk) {
        Icons.Default.Android
    }
    else {
        Icons.Default.InsertDriveFile
    }
}
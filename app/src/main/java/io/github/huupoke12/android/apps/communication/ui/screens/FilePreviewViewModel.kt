package io.github.huupoke12.android.apps.communication.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.io.File

class FilePreviewViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    val file = File(savedStateHandle.get<String>("filePath")!!)
}
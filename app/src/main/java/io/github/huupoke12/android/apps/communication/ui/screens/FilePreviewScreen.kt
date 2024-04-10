package io.github.huupoke12.android.apps.communication.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.huupoke12.android.apps.communication.ui.AppViewModelProvider
import io.github.huupoke12.android.apps.communication.ui.components.Centered
import io.github.huupoke12.android.apps.communication.util.getMimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeImage
import org.matrix.android.sdk.api.util.MimeTypes.isMimeTypeText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
    dismiss: () -> Unit,
    modifier: Modifier = Modifier,
    filePreviewViewModel: FilePreviewViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val file = filePreviewViewModel.file
    val fileName = file.name
    val mimeType = file.getMimeType()
    val openWithLauncher = {
        val shareUri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".mx-sdk.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(shareUri, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(context, intent, null)
    }
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            mimeType = mimeType
        )
    ) { saveUri ->
        if (saveUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val outputStream = context.contentResolver.openOutputStream(saveUri)
                outputStream?.write(file.readBytes())
                outputStream?.close()
                Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = fileName,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = dismiss,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = openWithLauncher,
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Open with",
                        )
                    }
                    IconButton(
                        onClick = { saveLauncher.launch(fileName) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
            Centered(
                modifier = Modifier.padding(paddingValues)
            ) {
                if (mimeType.isMimeTypeImage()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                    )
                } else if (mimeType.isMimeTypeText()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Text(
                                text = file.readText()
                            )
                        }
                    }
                }
                    // TODO: Enable after Jitsi's react-native-video >= v6.0.0-beta.0
//                else if (mimeType.isMimeTypeVideo()) {
//                    VideoPlayer(
//                        uri = file.toUri(),
//                        playWhenReady = true,
//                    )
//                } else if (mimeType.isMimeTypeAudio()) {
//                    VideoPlayer(
//                        uri = file.toUri(),
//                        playWhenReady = true,
//                    )
//                }
                else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Can not preview type: $mimeType")
                        Button(
                            onClick = openWithLauncher,
                        ) {
                            Text("Open with other applications")
                        }
                    }
                }
            }

    }
}
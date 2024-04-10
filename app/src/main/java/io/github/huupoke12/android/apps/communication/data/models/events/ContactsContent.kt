package io.github.huupoke12.android.apps.communication.data.models.events

import io.github.huupoke12.android.apps.communication.data.models.EmptyObject
import kotlinx.serialization.Serializable

@Serializable
data class ContactsContent(
    val contacts: Map<String, EmptyObject>
)
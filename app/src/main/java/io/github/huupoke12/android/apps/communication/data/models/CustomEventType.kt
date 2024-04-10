package io.github.huupoke12.android.apps.communication.data.models

import org.matrix.android.sdk.api.provider.CustomEventTypesProvider

object CustomEventType {
    private const val namespace = "io.github.huupoke12.android.apps.communication"
    enum class Types(val matrixName: String) {
        JITSI_CALL("$namespace.m.jitsi_call"),
        CONTACTS("$namespace.m.contacts"),
    }

    object Provider: CustomEventTypesProvider {
        override val customPreviewableEventTypes: List<String> = Types.entries.map { it.matrixName }
    }
}
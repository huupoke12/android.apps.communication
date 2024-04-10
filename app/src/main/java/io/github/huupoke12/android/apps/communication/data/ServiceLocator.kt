package io.github.huupoke12.android.apps.communication.data

import io.github.huupoke12.android.apps.communication.data.network.JitsiService
import io.github.huupoke12.android.apps.communication.data.network.MatrixService
import kotlinx.datetime.Clock

object ServiceLocator {
    lateinit var matrixService: MatrixService
    lateinit var jitsiService: JitsiService
    var lastRoomVisit: Pair<String?, Long> = null to Clock.System.now().toEpochMilliseconds()
}
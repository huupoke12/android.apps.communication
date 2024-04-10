package io.github.huupoke12.android.apps.communication.data.models

import org.matrix.android.sdk.api.auth.UIABaseAuth
import org.matrix.android.sdk.api.auth.UserInteractiveAuthInterceptor
import org.matrix.android.sdk.api.auth.registration.RegistrationFlowResponse
import kotlin.coroutines.Continuation

class DefaultUserInteractiveAuthInterceptor: UserInteractiveAuthInterceptor {
    override fun performStage(
        flowResponse: RegistrationFlowResponse,
        errCode: String?,
        promise: Continuation<UIABaseAuth>
    ) {
        TODO("Not yet implemented")
    }
}
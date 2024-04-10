package io.github.huupoke12.android.apps.communication.data.network

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import io.github.huupoke12.android.apps.communication.data.models.CustomEventType
import io.github.huupoke12.android.apps.communication.data.models.DefaultUserInteractiveAuthInterceptor
import io.github.huupoke12.android.apps.communication.util.AvatarSize
import io.github.huupoke12.android.apps.communication.util.roomMemberQueryParamsByMemberships
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.auth.login.LoginWizard
import org.matrix.android.sdk.api.auth.registration.RegisterThreePid
import org.matrix.android.sdk.api.auth.registration.RegistrationResult
import org.matrix.android.sdk.api.auth.registration.RegistrationWizard
import org.matrix.android.sdk.api.failure.Failure
import org.matrix.android.sdk.api.query.QueryStringValue
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.content.ContentAttachmentData
import org.matrix.android.sdk.api.session.content.ContentUrlResolver
import org.matrix.android.sdk.api.session.events.model.Content
import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.toContent
import org.matrix.android.sdk.api.session.events.model.toModel
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.getUser
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.RoomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.members.RoomMemberQueryParams
import org.matrix.android.sdk.api.session.room.members.roomMemberQueryParams
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.PowerLevelsContent
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.room.model.message.MessageWithAttachmentContent
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.JsonDict
import org.matrix.android.sdk.api.util.Optional
import org.matrix.android.sdk.api.util.emptyJsonDict
import java.io.File

class MatrixService(
    val matrix: Matrix
) {
    private val baseUri = Uri.parse("https://matrix.example.com")
    private val homeServerConnectionConfig = HomeServerConnectionConfig(baseUri)
    val currentSession = MutableStateFlow<Session?>(null)
    private var registrationWizard: RegistrationWizard? = null
    private var loginWizard: LoginWizard? = null
    private var pendingEmailThreePid: ThreePid.Email? = null

    fun getLocalUserId(username: String) = "@${username}:${baseUri.host}"
    fun getMyUserId() = currentSession.value!!.myUserId
    fun getMyUserInfo() = currentSession.value!!.getUser(getMyUserId())!!
    fun isSignedIn(): Flow<Boolean> = currentSession.map { it?.isOpenable == true }


    init {
        reuseLastSession()
    }

    private fun setupSession(session: Session) {
        session.open()
        session.syncService().startSync(true)
        currentSession.value = session
    }

    private fun reuseLastSession(): Boolean {
        val lastSession = matrix.authenticationService().getLastAuthenticatedSession()
        lastSession?.let {
            setupSession(it)
        }
        return lastSession != null
    }

    private fun doSync() {
        currentSession.value!!.syncService().requireBackgroundSync()
    }


    suspend fun checkIfRegistrationEmailHasBeenValidated(delayMillis: Long = 0): RegistrationResult {
        val registrationResult = registrationWizard!!.checkIfEmailHasBeenValidated(delayMillis)
        if (registrationResult is RegistrationResult.Success) {
            setupSession(registrationResult.session)
        }
        return registrationResult
    }

    suspend fun addRegistrationEmail(email: String): RegistrationResult {
        val registrationResult = registrationWizard!!.addThreePid(
            RegisterThreePid.Email(email)
        )
        if (registrationResult is RegistrationResult.Success) {
            setupSession(registrationResult.session)
            registrationWizard = null
        }
        return registrationResult
    }

    suspend fun signUp(username: String, password: String, email: String): RegistrationResult {
        matrix.authenticationService().getLoginFlow(homeServerConnectionConfig)
        registrationWizard = matrix.authenticationService().getRegistrationWizard()
        val registrationResult = registrationWizard!!.createAccount(
            userName = username,
            password = password,
            initialDeviceDisplayName = null,
        )
        if (registrationResult is RegistrationResult.Success) {
            setupSession(registrationResult.session)
        }
        return registrationResult
    }

    suspend fun signIn(username: String, password: String) {
        val newSession = matrix.authenticationService().directAuthentication(
            homeServerConnectionConfig = homeServerConnectionConfig,
            matrixId = getLocalUserId(username),
            password = password,
            initialDeviceName = "Communication App",
        )
        setupSession(newSession)
    }

    suspend fun signOut() {
        currentSession.value?.signOutService()?.signOut(true)
        currentSession.value = null
    }

    suspend fun changePassword(password: String, newPassword: String, signOut: Boolean = true) {
        currentSession.value!!.accountService().changePassword(
            password = password,
            newPassword = newPassword,
            logoutAllDevices = signOut,
        )
    }

    suspend fun sendNewEmailVerification(email: String) {
        val newThreePid = ThreePid.Email(email)
        currentSession.value!!.profileService().addThreePid(
                newThreePid
        )
        pendingEmailThreePid = newThreePid
    }

    suspend fun cancelAllPendingThreePids() {
        val pendingThreePids = currentSession.value!!.profileService().getPendingThreePids()
        pendingThreePids.forEach {
            currentSession.value!!.profileService().cancelAddingThreePid(it)
        }
    }

    suspend fun cancelCurrentPendingEmailThreePid() {
        pendingEmailThreePid?.let {
            currentSession.value!!.profileService().cancelAddingThreePid(it)
        }
        pendingEmailThreePid = null
    }

    suspend fun finaliseChangingEmail() {
        val oldEmailThreePids = currentSession.value!!.profileService().getThreePids()
            .filterIsInstance<ThreePid.Email>()
        currentSession.value!!.profileService().finalizeAddingThreePid(
            pendingEmailThreePid!!,
            userInteractiveAuthInterceptor = DefaultUserInteractiveAuthInterceptor(),
        )
        oldEmailThreePids.forEach {
            currentSession.value!!.profileService().deleteThreePid(it)
        }
    }

    suspend fun sendResetEmail(email: String) {
        matrix.authenticationService().getLoginFlow(homeServerConnectionConfig)
        loginWizard = matrix.authenticationService().getLoginWizard()
        loginWizard!!.resetPassword(email)
    }

    suspend fun resetPasswordMailConfirmed(newPassword: String, logoutAllDevices: Boolean = true) {
        loginWizard!!.resetPasswordMailConfirmed(newPassword, logoutAllDevices)
        loginWizard = null
    }


    suspend fun setDisplayName(name: String) {
        currentSession.value!!.profileService().setDisplayName(
            userId = getMyUserId(),
            newDisplayName = name,
        )
        doSync()
    }

    suspend fun setUserAvatar(avatarUri: Uri, fileName: String) {
        currentSession.value!!.profileService().updateAvatar(
            userId = getMyUserId(),
            newAvatarUri = avatarUri,
            fileName = fileName,
        )
    }

    suspend fun removeUserAvatar() {
        // FIXME: Wait for specs
    }

    suspend fun resolveUser(userId: String): User {
        return currentSession.value!!.userService().resolveUser(userId)
    }

    fun getLiveUser(userId: String): LiveData<Optional<User>> {
        return currentSession.value!!.userService().getUserLive(userId)
    }

    fun getLiveThreePids(): LiveData<List<ThreePid>> {
        return currentSession.value!!.profileService().getThreePidsLive(true)
    }

    fun getLiveIgnoredUserList(): LiveData<List<User>> {
        return currentSession.value!!.userService().getIgnoredUsersLive()
    }

    suspend fun ignoreUsers(userIdList: List<String>) {
        currentSession.value!!.userService().ignoreUserIds(userIdList)
        doSync()
    }

    suspend fun unIgnoreUsers(userIdList: List<String>) {
        currentSession.value!!.userService().unIgnoreUserIds(userIdList)
        doSync()
    }

    private suspend fun createEmptyContactList() {
        currentSession.value!!.accountDataService().updateUserAccountData(
            type = CustomEventType.Types.CONTACTS.matrixName,
            content = mapOf("contacts" to emptyJsonDict),
        )
    }

    fun getLiveContactList(): LiveData<List<User>> {
        return currentSession.value!!.accountDataService()
            .getLiveUserAccountDataEvent(
                type = CustomEventType.Types.CONTACTS.matrixName
            ).map { userAccountDataEventWrapper ->
                userAccountDataEventWrapper.getOrNull()?.let { userAccountDataEvent ->
                    (userAccountDataEvent.content.getOrDefault("contacts", emptyJsonDict) as JsonDict).keys
                        .map { userId ->
                            runBlocking(Dispatchers.IO) {
                                resolveUser(userId)
                            }
                        }
                } ?: emptyList()
            }
    }

    suspend fun addUsersToContactList(userIdList: List<String>) {
        var contactListEvent = currentSession.value!!.accountDataService().getUserAccountDataEvent(
            type = CustomEventType.Types.CONTACTS.matrixName
        )
        if (contactListEvent == null) {
            createEmptyContactList()
        }
        contactListEvent = currentSession.value!!.accountDataService().getUserAccountDataEvent(
            type = CustomEventType.Types.CONTACTS.matrixName
        )!!
        val contactList = (contactListEvent.content["contacts"] as JsonDict).toMutableMap()
        for (userId in userIdList) {
            contactList[userId] = emptyJsonDict
        }

        currentSession.value!!.accountDataService().updateUserAccountData(
            type = CustomEventType.Types.CONTACTS.matrixName,
            content = mapOf("contacts" to contactList),
        )
        doSync()
    }

    suspend fun removeUsersFromContactList(userIdList: List<String>) {
        var contactListEvent = currentSession.value!!.accountDataService().getUserAccountDataEvent(
            type = CustomEventType.Types.CONTACTS.matrixName
        )
        if (contactListEvent == null) {
            createEmptyContactList()
        }
        contactListEvent = currentSession.value!!.accountDataService().getUserAccountDataEvent(
            type = CustomEventType.Types.CONTACTS.matrixName
        )!!
        val contactList = (contactListEvent.content["contacts"] as JsonDict).toMutableMap()
        for (userId in userIdList) {
            contactList.remove(userId)
        }

        currentSession.value!!.accountDataService().updateUserAccountData(
            type = CustomEventType.Types.CONTACTS.matrixName,
            content = mapOf("contacts" to contactList),
        )
        doSync()
    }

    fun getRoom(roomId: String): Room? {
        return currentSession.value!!.getRoom(roomId)
    }

    suspend fun loadAllRoomMembers(roomId: String) {
        getRoom(roomId)!!.membershipService().loadRoomMembersIfNeeded()
    }

    fun getRoomMembers(
        roomId: String,
        queryParams: RoomMemberQueryParams = roomMemberQueryParamsByMemberships(Membership.all()),
    ): List<RoomMemberSummary> {
        val room = getRoom(roomId)!!
        return room.membershipService().getRoomMembers(
            queryParams = queryParams,
        )
    }

    fun getDirectRoomMember(roomId: String): RoomMemberSummary? {
        val room = getRoom(roomId)!!
        if (!room.roomSummary()!!.isDirect) return null
        return room.membershipService().getRoomMembers(
            queryParams = roomMemberQueryParams {
                excludeSelf = true
            }
        ).firstOrNull()
    }

    fun getLiveRoomMembers(
        roomId: String,
        queryParams: RoomMemberQueryParams = roomMemberQueryParamsByMemberships(Membership.all()),
    ): LiveData<List<RoomMemberSummary>> {
        val room = getRoom(roomId)!!
        return room.membershipService().getRoomMembersLive(
            queryParams = queryParams,
        )
    }


    fun getLiveUserRoomMembership(roomId: String, userId: String): LiveData<Optional<Membership>> {
        return currentSession.value!!.roomService().getRoomMemberLive(
            roomId = roomId,
            userId = userId,
        ).map {
            it.map { roomMemberSummary ->
                roomMemberSummary.membership
            }
        }
    }

    fun getLiveRoomNotificationState(roomId: String): LiveData<RoomNotificationState> {
        return getRoom(roomId)!!.roomPushRuleService().getLiveRoomNotificationState()
    }

    suspend fun setRoomNotificationState(
        roomId: String,
        roomNotificationState: RoomNotificationState
    ) {
        getRoom(roomId)!!.roomPushRuleService().setRoomNotificationState(roomNotificationState)
    }

    fun getRoomPowerLevels(roomId: String): PowerLevelsContent? {
        return getRoom(roomId)!!.stateService().getStateEvent(
            eventType = EventType.STATE_ROOM_POWER_LEVELS,
            stateKey = QueryStringValue.IsNotNull,
        )?.content.toModel<PowerLevelsContent>()
    }

    fun getLiveRoomPowerLevels(roomId: String): LiveData<Optional<PowerLevelsContent>> {
        return getRoom(roomId)!!.stateService().getStateEventLive(
            eventType = EventType.STATE_ROOM_POWER_LEVELS,
            stateKey = QueryStringValue.IsNotNull,
        ).map { eventWrapper ->
            eventWrapper.map {
                it.content.toModel<PowerLevelsContent>()
            }
        }
    }

    suspend fun joinRoom(roomId: String) {
        currentSession.value!!.roomService().joinRoom(roomId)
    }

    suspend fun leaveRoom(roomId: String) {
        currentSession.value!!.roomService().leaveRoom(roomId)
    }

    suspend fun setRoomName(roomId: String, roomName: String) {
        getRoom(roomId)!!.stateService().updateName(roomName)
    }

    suspend fun setRoomAvatar(roomId: String, avatarUri: Uri, avatarFileName: String) {
        getRoom(roomId)!!.stateService().updateAvatar(
            avatarUri = avatarUri,
            fileName = avatarFileName,
        )
    }

    suspend fun removeRoomAvatar(roomId: String) {
        getRoom(roomId)!!.stateService().deleteAvatar()
    }

    suspend fun searchUsers(query: String): List<User> {
        var userFromId: User? = null
        if (query.startsWith('@')) {
            try {
                userFromId = resolveUser(query)
            } catch (_: Failure.ServerError) {

            }
        }
        return currentSession.value!!.userService().searchUsersDirectory(
            search = query,
            limit = 20,
            excludedUserIds = emptySet(),
        ) + listOfNotNull(userFromId)
    }

    fun getLiveRoomList(
        queryParams: RoomSummaryQueryParams = roomSummaryQueryParams(),
    ): LiveData<List<RoomSummary>> {
        return currentSession.value!!.roomService().getRoomSummariesLive(
            queryParams = queryParams
        )
    }

    fun getLiveRoomSummary(roomId: String): LiveData<Optional<RoomSummary>> {
        return currentSession.value!!.roomService().getRoomSummaryLive(roomId)
    }

    suspend fun createRoom(name: String?, invitedUserIds: List<String>): String {
        return currentSession.value!!.roomService().createRoom(
            CreateRoomParams().apply {
                this.name = name
                this.invitedUserIds = invitedUserIds.toMutableList()
                enableEncryptionIfInvitedUsersSupportIt = false
            }
        )
    }

    private suspend fun createDirectRoom(otherUserId: String): String {
        return currentSession.value!!.roomService().createRoom(
            CreateRoomParams().apply {
                invitedUserIds = mutableListOf(otherUserId)
                setDirectMessage()
                enableEncryptionIfInvitedUsersSupportIt = false
            }
        )
    }

    suspend fun getOrCreateDirectRoom(otherUserId: String): String {
        return currentSession.value!!.roomService().getExistingDirectRoomWithUser(otherUserId)
            ?: createDirectRoom(
                otherUserId = otherUserId
            )
    }

    fun resendAllFailedMessages(roomId: String) {
        getRoom(roomId)?.sendService()?.resendAllFailedMessages()
    }

    fun deleteFailedEcho(roomId: String, localEcho: TimelineEvent) {
        getRoom(roomId)!!.sendService().deleteFailedEcho(localEcho)
    }

    fun cancelSend(roomId: String, eventId: String) {
        getRoom(roomId)!!.sendService().cancelSend(eventId)
    }

    fun sendTextMessage(roomId: String, text: String) {
        getRoom(roomId)!!.sendService().sendTextMessage(
            text = text,
        )
        doSync()
    }

    fun sendFiles(roomId: String, attachments: List<ContentAttachmentData>) {
        getRoom(roomId)!!.sendService().sendMedias(
            attachments = attachments,
            compressBeforeSending = false,
            roomIds = setOf(roomId),
        )
    }

    fun sendEvent(roomId: String, eventType: String, content: Content?) {
        getRoom(roomId)!!.sendService().sendEvent(
            eventType = eventType,
            content = content,
        )
    }

    suspend fun sendStateEvent(
        roomId: String,
        eventType: String,
        stateKey: String,
        body: JsonDict,
    ) {
        getRoom(roomId)!!.stateService().sendStateEvent(
            eventType = eventType,
            stateKey = stateKey,
            body = body,
        )
    }

    suspend fun promoteUserAsModerator(roomId: String, userId: String) {
        getRoomPowerLevels(roomId)?.setUserPowerLevel(userId, Role.Moderator.value)?.let {
            sendStateEvent(
                roomId = roomId,
                eventType = EventType.STATE_ROOM_POWER_LEVELS,
                stateKey = "",
                it.toContent(),
            )
        }
    }

    suspend fun demoteUser(roomId: String, userId: String) {
        getRoomPowerLevels(roomId)?.setUserPowerLevel(userId, Role.Default.value)?.let {
            sendStateEvent(
                roomId = roomId,
                eventType = EventType.STATE_ROOM_POWER_LEVELS,
                stateKey = "",
                it.toContent(),
            )
        }
    }


    fun getTemporarySharableURI(messageContent: MessageWithAttachmentContent): Uri? {
        return currentSession.value!!.fileService().getTemporarySharableURI(messageContent)
    }

    suspend fun downloadFile(messageContent: MessageWithAttachmentContent): File {
        return currentSession.value!!.fileService().downloadFile(messageContent)
    }

    fun isFileInCache(messageContent: MessageWithAttachmentContent): Boolean {
        return currentSession.value!!.fileService().isFileInCache(messageContent)
    }

    fun resolveThumbnail(
        contentUrl: String?, width: Int, height: Int
    ): String? {
        return currentSession.value!!.contentUrlResolver().resolveThumbnail(
            contentUrl = contentUrl,
            width = width,
            height = height,
            method = ContentUrlResolver.ThumbnailMethod.SCALE,
        )
    }

    fun resolveAvatarThumbnail(contentUrl: String?, size: AvatarSize = AvatarSize.SMALL): String? {
        return resolveThumbnail(
            contentUrl = contentUrl,
            width = size.size,
            height = size.size,
        )
    }

    fun resolveMediaThumbnail(contentUrl: String?): String? {
        return resolveThumbnail(
            contentUrl = contentUrl,
            width = 800,
            height = 600,
        )
    }


    suspend fun inviteUser(roomId: String, userId: String) {
        getRoom(roomId)!!.membershipService().invite(userId)
    }

    suspend fun kickMember(roomId: String, userId: String) {
        getRoom(roomId)!!.membershipService().remove(userId)
    }

    suspend fun banMember(roomId: String, userId: String) {
        getRoom(roomId)!!.membershipService().ban(userId)
    }

    suspend fun unbanMember(roomId: String, userId: String) {
        getRoom(roomId)!!.membershipService().unban(userId)
    }

    fun sendTypingIndicator(roomId: String) {
        getRoom(roomId)!!.typingService().userIsTyping()
    }

    fun sendNotTypingIndicator(roomId: String) {
        getRoom(roomId)!!.typingService().userStopsTyping()
    }

    suspend fun markAsRead(roomId: String) {
        getRoom(roomId)!!.readService().markAsRead()
    }
}

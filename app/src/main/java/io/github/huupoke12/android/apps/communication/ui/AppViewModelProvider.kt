package io.github.huupoke12.android.apps.communication.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.huupoke12.android.apps.communication.CommunicationApplication
import io.github.huupoke12.android.apps.communication.ui.screens.BlockListViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.CreateRoomViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.FilePreviewViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.ResetPasswordViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.RoomDetailViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.RoomViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.SignInViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.SignOutViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.SignUpViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.UserViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.home.AccountViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.home.ContactListingViewModel
import io.github.huupoke12.android.apps.communication.ui.screens.home.RoomListingViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            FilePreviewViewModel(
                savedStateHandle = createSavedStateHandle(),
            )
        }
        initializer {
            SignInViewModel(
            )
        }
        initializer {
            SignUpViewModel(
            )
        }
        initializer {
            SignOutViewModel(
            )
        }
        initializer {
            ResetPasswordViewModel(
            )
        }
        initializer {
            RoomListingViewModel(
            )
        }
        initializer {
            ContactListingViewModel(
            )
        }
        initializer {
            AccountViewModel(
            )
        }
        initializer {
            UserViewModel(
                savedStateHandle = createSavedStateHandle(),
            )
        }
        initializer {
            RoomViewModel(
                savedStateHandle = createSavedStateHandle(),
            )
        }
        initializer {
            RoomDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
            )
        }
        initializer {
            BlockListViewModel(
            )
        }
        initializer {
            CreateRoomViewModel(
            )
        }
    }
}

fun CreationExtras.CommunicationApplication(): CommunicationApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CommunicationApplication)
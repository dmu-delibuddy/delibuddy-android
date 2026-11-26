package com.example.delibuddy.ui.room

import androidx.annotation.StringRes
import com.example.delibuddy.R
import com.example.delibuddy.data.room.RoomNotLoggedInException
import com.example.delibuddy.data.room.RoomNotFoundException
import com.example.delibuddy.data.room.RoomClosedException
import com.example.delibuddy.data.room.RoomFullException
import com.example.delibuddy.data.room.RoomAlreadyJoinedException
import com.example.delibuddy.data.room.RoomNotHostException
import com.example.delibuddy.data.room.RoomCannotLeaveInProgressException
import com.example.delibuddy.data.room.RoomInvalidStateException
import com.example.delibuddy.data.room.RoomAccountRequiredException

@StringRes
internal fun Throwable.toRoomError(@StringRes fallback: Int): Int = when (this) {
    is RoomNotLoggedInException -> R.string.common_error_not_logged_in
    is RoomNotFoundException -> R.string.room_error_not_found
    is RoomClosedException -> R.string.room_error_closed
    is RoomFullException -> R.string.room_error_full
    is RoomAlreadyJoinedException -> R.string.room_error_already_joined
    is RoomNotHostException -> R.string.room_error_not_host
    is RoomCannotLeaveInProgressException -> R.string.room_error_cannot_leave_in_progress
    is RoomInvalidStateException -> R.string.room_error_invalid_state
    is RoomAccountRequiredException -> R.string.room_error_account_required
    else -> fallback
}

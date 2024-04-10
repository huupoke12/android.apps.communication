package io.github.huupoke12.android.apps.communication.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toSystemLocalDateTime() = toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalTime.formattedHour() = hour.toString().padStart(2, '0')
fun LocalTime.formattedMinute() = minute.toString().padStart(2, '0')
fun LocalTime.formattedSecond() = second.toString().padStart(2, '0')
fun LocalTime.formattedHourMinute() = formattedHour() + ":" + formattedMinute()
fun LocalTime.formattedHourMinuteSecond() = formattedMinute() + ":" + formattedSecond()

package com.jeison.agrocontrol.ui.common

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object Formatters {
    private val localeCo = Locale("es", "CO")
    private val dateDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeCo)
    private val dateLong = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", localeCo)

    fun formatDate(isoOrYmd: String): String = runCatching {
        LocalDate.parse(isoOrYmd.take(10)).format(dateDisplay)
    }.getOrDefault(isoOrYmd)

    fun formatDateLong(date: LocalDate = LocalDate.now()): String =
        date.format(dateLong).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeCo) else it.toString() }

    fun formatNumber(value: Double): String =
        NumberFormat.getNumberInstance(localeCo).format(value)

    fun formatInteger(value: Int): String =
        NumberFormat.getIntegerInstance(localeCo).format(value)

    fun formatRelative(isoInstant: String?): String {
        if (isoInstant.isNullOrBlank()) return "Nunca sincronizado"
        val then = runCatching { Instant.parse(isoInstant) }.getOrNull() ?: return "Nunca sincronizado"
        val minutes = ChronoUnit.MINUTES.between(then, Instant.now())
        return when {
            minutes < 1 -> "Hace un momento"
            minutes < 60 -> "Hace $minutes min"
            minutes < 1440 -> "Hace ${minutes / 60} h"
            else -> "Hace ${minutes / 1440} d"
        }
    }

    fun greetingByHour(hour: Int = java.time.LocalTime.now().hour): String = when (hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

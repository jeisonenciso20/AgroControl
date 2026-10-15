package com.jeison.agrocontrol.ui.common

import com.jeison.agrocontrol.data.PendingTaskLocal
import com.jeison.agrocontrol.ui.components.TaskItemUi
import java.time.LocalDate

object PendingTaskUiMapper {

    fun potreroDueToday(tasks: List<PendingTaskLocal>, today: LocalDate = LocalDate.now()): List<PendingTaskLocal> =
        tasks
            .filter { it.tipo.equals("POTRERO", ignoreCase = true) }
            .filter { task ->
                val date = runCatching { LocalDate.parse(task.vencimiento.take(10)) }.getOrNull()
                date != null && !date.isAfter(today)
            }
            .sortedBy { it.vencimiento }

    fun toTaskItemUi(task: PendingTaskLocal, today: LocalDate = LocalDate.now()): TaskItemUi {
        val date = runCatching { LocalDate.parse(task.vencimiento.take(10)) }.getOrNull()
        val overdue = date?.isBefore(today) == true
        val statusLabel = when {
            task.status.equals("COMPLETED", ignoreCase = true) -> "Terminado"
            overdue -> "Vencido"
            date == today -> "Vence hoy"
            else -> "Pendiente"
        }
        return TaskItemUi(
            id = task.serverId,
            titulo = task.titulo,
            vencimiento = Formatters.formatDate(task.vencimiento),
            statusLabel = statusLabel,
            isOverdue = overdue,
            referencia = task.referencia,
            observacion = task.observacion,
        )
    }
}

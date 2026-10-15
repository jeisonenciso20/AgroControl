package com.jeison.agrocontrol.ui.navigation

import android.net.Uri

object Graph {
    const val AUTH = "auth"
    const val MAIN = "main"
}

sealed class Dest(val route: String) {
    data object Bienvenida : Dest("bienvenida")
    data object Login : Dest("login")
    data object Registro : Dest("registro")
    data object Inicio : Dest("inicio")
    data object Animales : Dest("animales")
    data object AnimalNuevo : Dest("animales/nuevo")
    data object AnimalDetalle : Dest("animales/detalle/{codigo}") {
        fun create(codigo: String) = "animales/detalle/${Uri.encode(codigo)}"
    }
    data object AnimalEditar : Dest("animales/{codigo}/editar") {
        fun create(codigo: String) = "animales/${Uri.encode(codigo)}/editar"
    }
    data object Produccion : Dest("produccion")
    data object ProduccionRegistrar : Dest("produccion/registrar?codigo={codigo}") {
        fun create(codigo: String = "") =
            if (codigo.isBlank()) "produccion/registrar?codigo="
            else "produccion/registrar?codigo=${Uri.encode(codigo)}"
    }
    data object Potreros : Dest("potreros")
    data object PotreroDetalle : Dest("potreros/{serverId}") {
        fun create(id: String) = "potreros/${Uri.encode(id)}"
    }
    data object PotreroNuevo : Dest("potreros/nuevo")
    data object PotreroEditar : Dest("potreros/{serverId}/editar") {
        fun create(id: String) = "potreros/${Uri.encode(id)}/editar"
    }
    data object PotreroTrabajo : Dest("potreros/trabajo?potrero={potrero}") {
        fun create(nombre: String) = "potreros/trabajo?potrero=${Uri.encode(nombre)}"
    }
    data object Reproduccion : Dest("reproduccion")
    data object ReproduccionDetalle : Dest("reproduccion/detalle/{codigo}") {
        fun create(codigo: String) = "reproduccion/detalle/${Uri.encode(codigo)}"
    }
    data object ReproduccionRegistrar : Dest("reproduccion/registrar?codigo={codigo}") {
        fun create(codigo: String = "") =
            if (codigo.isBlank()) "reproduccion/registrar?codigo="
            else "reproduccion/registrar?codigo=${Uri.encode(codigo)}"
    }
    data object Perfil : Dest("perfil")
    data object Pendientes : Dest("pendientes")
    data object Finanzas : Dest("finanzas")
    data object Reportes : Dest("reportes")
    data object Mas : Dest("mas")
}

enum class MainTab(val route: String, val label: String) {
    Inicio(Dest.Inicio.route, "Inicio"),
    Animales(Dest.Animales.route, "Animales"),
    Produccion(Dest.Produccion.route, "Producción"),
    Potreros(Dest.Potreros.route, "Potreros"),
    Mas(Dest.Mas.route, "Más"),
}

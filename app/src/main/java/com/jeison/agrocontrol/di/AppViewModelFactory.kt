package com.jeison.agrocontrol.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jeison.agrocontrol.AgroControlApp
import com.jeison.agrocontrol.ui.feature.animales.AnimalDetalleViewModel
import com.jeison.agrocontrol.ui.feature.animales.AnimalFormViewModel
import com.jeison.agrocontrol.ui.feature.animales.AnimalesListaViewModel
import com.jeison.agrocontrol.ui.feature.auth.AuthViewModel
import com.jeison.agrocontrol.ui.feature.finanzas.FinanzasViewModel
import com.jeison.agrocontrol.ui.feature.inicio.InicioViewModel
import com.jeison.agrocontrol.ui.feature.mas.MasViewModel
import com.jeison.agrocontrol.ui.feature.perfil.PerfilViewModel
import com.jeison.agrocontrol.ui.feature.pendientes.PendientesViewModel
import com.jeison.agrocontrol.ui.feature.potreros.PotreroDetalleViewModel
import com.jeison.agrocontrol.ui.feature.potreros.PotreroFormViewModel
import com.jeison.agrocontrol.ui.feature.potreros.PotreroTrabajoViewModel
import com.jeison.agrocontrol.ui.feature.potreros.PotrerosViewModel
import com.jeison.agrocontrol.ui.feature.produccion.ProduccionRegistrarViewModel
import com.jeison.agrocontrol.ui.feature.produccion.ProduccionViewModel
import com.jeison.agrocontrol.ui.feature.reportes.ReportesViewModel
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionDetalleViewModel
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionFormViewModel
import com.jeison.agrocontrol.ui.feature.reproduccion.ReproduccionViewModel

fun agroApp(extras: CreationExtras): AgroControlApp =
    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AgroControlApp

val AppViewModelFactory = viewModelFactory {
    initializer {
        AuthViewModel(agroApp(this).container)
    }
    initializer {
        InicioViewModel(agroApp(this).container)
    }
    initializer {
        AnimalesListaViewModel(agroApp(this).container)
    }
    initializer {
        AnimalDetalleViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        AnimalFormViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        PendientesViewModel(agroApp(this).container)
    }
    initializer {
        ProduccionViewModel(agroApp(this).container)
    }
    initializer {
        PotrerosViewModel(agroApp(this).container)
    }
    initializer {
        PotreroDetalleViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        PotreroFormViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        PotreroTrabajoViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        ProduccionRegistrarViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        ReproduccionFormViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        ReproduccionDetalleViewModel(createSavedStateHandle(), agroApp(this).container)
    }
    initializer {
        FinanzasViewModel(agroApp(this).container)
    }
    initializer {
        ReportesViewModel(agroApp(this).container)
    }
    initializer {
        ReproduccionViewModel(agroApp(this).container)
    }
    initializer {
        MasViewModel(agroApp(this).container)
    }
    initializer {
        PerfilViewModel(agroApp(this).container)
    }
}

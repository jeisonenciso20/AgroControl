package com.jeison.agrocontrol.ui.common

fun productionUnitFor(productType: String): String = when {
    productType.equals("LECHE", ignoreCase = true) -> "L"
    else -> "kg"
}

package com.jeison.agrocontrol.ui.common

fun productionUnitFor(productType: String): String = when {
    productType.equals("LECHE", ignoreCase = true) -> "L"
    productType.equals("HUEVO", ignoreCase = true) -> "und"
    else -> "kg"
}

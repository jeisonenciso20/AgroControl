package com.jeison.agrocontrol.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF2E6B30)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFB5F1AB)
val OnPrimaryContainerLight = Color(0xFF002106)
val SecondaryLight = Color(0xFF77592A)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFDDB2)
val OnSecondaryContainerLight = Color(0xFF291800)
val TertiaryLight = Color(0xFF1E6587)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFC5E7FF)
val OnTertiaryContainerLight = Color(0xFF001E2C)
val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFF9DEDC)
val OnErrorContainerLight = Color(0xFF410E0B)
val BackgroundLight = Color(0xFFF8F9F2)
val OnBackgroundLight = Color(0xFF1A1C18)
val SurfaceLight = Color(0xFFF8F9F2)
val OnSurfaceLight = Color(0xFF1A1C18)
val SurfaceVariantLight = Color(0xFFDEE5D8)
val OnSurfaceVariantLight = Color(0xFF3F4A3C)
val OutlineLight = Color(0xFF6F7A6B)
val OutlineVariantLight = Color(0xFFBFC9BA)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF2F4EC)
val SurfaceContainerLight = Color(0xFFECEFE6)
val SurfaceContainerHighLight = Color(0xFFE6E9E0)
val SurfaceContainerHighestLight = Color(0xFFE1E4DA)
val InverseSurfaceLight = Color(0xFF2F312C)
val InverseOnSurfaceLight = Color(0xFFF0F1E9)
val InversePrimaryLight = Color(0xFF9AD591)
val ScrimLight = Color(0xFF000000)

val PrimaryDark = Color(0xFF9AD591)
val OnPrimaryDark = Color(0xFF003910)
val PrimaryContainerDark = Color(0xFF14521A)
val OnPrimaryContainerDark = Color(0xFFB5F1AB)
val SecondaryDark = Color(0xFFE8C08A)
val OnSecondaryDark = Color(0xFF432C00)
val SecondaryContainerDark = Color(0xFF5D4214)
val OnSecondaryContainerDark = Color(0xFFFFDDB2)
val TertiaryDark = Color(0xFF8ECFF5)
val OnTertiaryDark = Color(0xFF003548)
val TertiaryContainerDark = Color(0xFF004C67)
val OnTertiaryContainerDark = Color(0xFFC5E7FF)
val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)
val BackgroundDark = Color(0xFF11140F)
val OnBackgroundDark = Color(0xFFE1E4DA)
val SurfaceDark = Color(0xFF11140F)
val OnSurfaceDark = Color(0xFFE1E4DA)
val SurfaceVariantDark = Color(0xFF3F4A3C)
val OnSurfaceVariantDark = Color(0xFFC2CBBD)
val OutlineDark = Color(0xFF8A9485)
val OutlineVariantDark = Color(0xFF3F4A3C)
val SurfaceContainerLowestDark = Color(0xFF0C0F0A)
val SurfaceContainerLowDark = Color(0xFF1A1C18)
val SurfaceContainerDark = Color(0xFF1E211C)
val SurfaceContainerHighDark = Color(0xFF282B26)
val SurfaceContainerHighestDark = Color(0xFF333631)
val InverseSurfaceDark = Color(0xFFE1E4DA)
val InverseOnSurfaceDark = Color(0xFF2F312C)
val InversePrimaryDark = Color(0xFF2E6B30)

data class AgroExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val offline: Color,
    val offlineContainer: Color,
    val milk: Color,
    val meat: Color,
    val female: Color,
    val male: Color,
)

val LightExtended = AgroExtendedColors(
    success = Color(0xFF1F6E2B),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFC8EFC4),
    onSuccessContainer = Color(0xFF002106),
    warning = Color(0xFF7A4F00),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDDB8),
    onWarningContainer = Color(0xFF291800),
    offline = Color(0xFF4A4F47),
    offlineContainer = Color(0xFFE1E4DA),
    milk = Color(0xFF1E6587),
    meat = Color(0xFF8C3B2E),
    female = Color(0xFF8B3F6B),
    male = Color(0xFF2B5C8A),
)

val DarkExtended = AgroExtendedColors(
    success = Color(0xFF8FD88F),
    onSuccess = Color(0xFF00390F),
    successContainer = Color(0xFF145220),
    onSuccessContainer = Color(0xFFC8EFC4),
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF422C00),
    warningContainer = Color(0xFF5E3F00),
    onWarningContainer = Color(0xFFFFDDB8),
    offline = Color(0xFFC2C8BE),
    offlineContainer = Color(0xFF333631),
    milk = Color(0xFF8ECFF5),
    meat = Color(0xFFFFB4A5),
    female = Color(0xFFFFAFD8),
    male = Color(0xFFA3C9FF),
)

val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    scrim = ScrimLight,
)

val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    scrim = ScrimLight,
)

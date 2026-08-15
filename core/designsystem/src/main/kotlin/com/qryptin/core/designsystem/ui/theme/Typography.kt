package com.qryptin.core.designsystem.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object QryptTypography {
    val TypoDisplayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    )

    val TypoDisplaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )

    val TypoTitleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )

    val TypoTitleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    )

    val TypoBodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val TypoBodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val TypoBodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    val TypoLabelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    val TypoLabelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}

val TypoDisplayLarge = QryptTypography.TypoDisplayLarge
val TypoDisplaySmall = QryptTypography.TypoDisplaySmall
val TypoTitleLarge = QryptTypography.TypoTitleLarge
val TypoTitleMedium = QryptTypography.TypoTitleMedium
val TypoBodyLarge = QryptTypography.TypoBodyLarge
val TypoBodyMedium = QryptTypography.TypoBodyMedium
val TypoBodySmall = QryptTypography.TypoBodySmall
val TypoLabelMedium = QryptTypography.TypoLabelMedium
val TypoLabelSmall = QryptTypography.TypoLabelSmall

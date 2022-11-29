package com.hoc.flowmvi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AbrilFatface = FontFamily(
  Font(resId = R.font.abril_fatface_regular)
)

val NunitoSans = FontFamily(
  Font(resId = R.font.nunitosans_regular),
  Font(resId = R.font.nunitosans_bold, weight = FontWeight.Bold)
)

val Typography = Typography(
  headlineLarge = TextStyle(
    fontFamily = AbrilFatface,
    fontWeight = FontWeight.Normal,
    fontSize = 30.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = NunitoSans,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp
  ),
  headlineSmall = TextStyle(
    fontFamily = NunitoSans,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = NunitoSans,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
  )
)

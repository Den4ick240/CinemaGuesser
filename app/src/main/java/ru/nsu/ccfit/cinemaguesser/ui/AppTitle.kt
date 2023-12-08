package ru.nsu.ccfit.cinemaguesser.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import ru.nsu.ccfit.cinemaguesser.R

@Composable
fun AppTitle() {
  Icon(painterResource(id = R.drawable.logo_dark), contentDescription = null)
}

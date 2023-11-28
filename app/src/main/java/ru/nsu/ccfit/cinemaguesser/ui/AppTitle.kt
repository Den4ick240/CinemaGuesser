package ru.nsu.ccfit.cinemaguesser.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.nsu.ccfit.cinemaguesser.R

@Composable
fun AppTitle() {
    Icon(painterResource(id = R.drawable.logo_dark), contentDescription = null)
}

package ru.nsu.ccfit.cinemaguesser.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.nsu.ccfit.cinemaguesser.R

@Composable
fun AppTitle() {
    Text(text = stringResource(R.string.app_title), style = MaterialTheme.typography.titleLarge)
}

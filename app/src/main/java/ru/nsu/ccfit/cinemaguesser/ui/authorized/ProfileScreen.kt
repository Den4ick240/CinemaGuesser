package ru.nsu.ccfit.cinemaguesser.ui.authorized

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.AccountManager
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.ValueFlow
import ru.nsu.ccfit.cinemaguesser.asValueFlow
import ru.nsu.ccfit.cinemaguesser.collectValueAsState

class ProfileViewModel(private val accountManager: AccountManager) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: ValueFlow<Boolean> = _loading.asValueFlow()

    fun logout() {
        viewModelScope.launch {
            _loading.value = true
            accountManager.logOut()
            _loading.value = false
        }
    }
}

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        val loading by viewModel.loading.collectValueAsState()
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.logout() },
            enabled = !loading
        ) {
            Text(text = stringResource(R.string.log_out_btn))
        }
    }
}
package ru.nsu.ccfit.cinemaguesser

import android.app.Application
import android.content.SharedPreferences
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.LoginViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.NewPasswordViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.PasswordRecoveryViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.RegisterViewModel

val appModule = module {
    single {
        getSharedPrefs(androidApplication())
    }

    single<SharedPreferences.Editor> {
        getSharedPrefs(androidApplication()).edit()
    }
    factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    singleOf(::AccountManager)
    viewModelOf(::NavigationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::PasswordRecoveryViewModel)
    viewModelOf(::NewPasswordViewModel)
}

fun getSharedPrefs(androidApplication: Application): SharedPreferences {
    return androidApplication.getSharedPreferences("default", android.content.Context.MODE_PRIVATE)
}
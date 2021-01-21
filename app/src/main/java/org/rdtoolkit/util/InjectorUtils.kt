package org.rdtoolkit.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.rdtoolkit.component.ComponentRepository
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.StaticPamphlets
import org.rdtoolkit.model.session.AppRepository
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.SessionRepositoryImpl
import org.rdtoolkit.model.session.getDatabase
import org.rdtoolkit.processing.WorkCoordinator
import org.rdtoolkit.ui.capture.CaptureViewModel
import org.rdtoolkit.ui.instruct.DisclaimerPage
import org.rdtoolkit.ui.instruct.PamphletViewModel
import org.rdtoolkit.ui.provision.ProvisionViewModel
import org.rdtoolkit.ui.sessions.SessionsViewModel

class InjectorUtils() {
    companion object {
        fun provideProvisionViewModelFactory(context: Context):
                ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return ProvisionViewModel(
                            provideSessionRepository(context),
                            provideDiagnosticsRepository(context)) as T
                }
            }
        }

        fun provideCaptureViewModelFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return CaptureViewModel(provideSessionRepository(context),
                            provideDiagnosticsRepository(context),
                            AppRepository(context)) as T
                }
            }
        }

        fun provideSessionsViewModelFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return SessionsViewModel(provideSessionRepository(context),
                            provideDiagnosticsRepository(context)) as T
                }
            }
        }

        fun providePamphletViewModelFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return PamphletViewModel(DisclaimerPage(context), AppRepository(context)) as T
                }
            }
        }

        fun provideSessionRepository(context: Context) : SessionRepository {
            return SessionRepositoryImpl(getDatabase(context).testSessionDao(), WorkCoordinator(context))
        }


        fun provideDiagnosticsRepository(context: Context) : DiagnosticsRepository {
            val repo = DiagnosticsRepository()
            repo.folioSource = StaticPamphlets(context)
            return repo
        }

        fun provideComponentRepository(context: Context) : ComponentRepository {
            return ComponentRepository(context)
        }

    }
}
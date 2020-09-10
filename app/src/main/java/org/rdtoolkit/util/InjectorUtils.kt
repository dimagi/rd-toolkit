package org.rdtoolkit.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.rdtoolkit.component.ComponentRepository
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.model.session.getDatabase
import org.rdtoolkit.ui.capture.CaptureViewModel
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
                            provideDiagnosticsRepository(context)) as T
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

        fun provideSessionRepository(context: Context) : SessionRepository {
            return SessionRepository(getDatabase(context).testSessionDao())
        }


        fun provideDiagnosticsRepository(context: Context) : DiagnosticsRepository {
            return DiagnosticsRepository()
        }

        fun provideComponentRepository(context: Context) : ComponentRepository {
            return ComponentRepository()
        }

    }
}
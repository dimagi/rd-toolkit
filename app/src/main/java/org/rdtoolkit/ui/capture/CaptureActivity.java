package org.rdtoolkit.ui.capture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.rdtoolkit.ui.provision.ProvisionViewModel;
import org.rdtoolkit.util.InjectorUtils;

import org.rdtoolkit.R;
import org.rdtoolkit.service.TestTimerService;

import static org.rdtoolkit.service.TestTimerServiceKt.NOTIFICATION_TAG_TEST_ID;

public class CaptureActivity extends AppCompatActivity {

    public static String EXTRA_SESSION_ID = "rdt_capture_session_id";

    CaptureViewModel captureViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        @NonNull
        String sessionId = this.getIntent().getStringExtra(EXTRA_SESSION_ID);

        captureViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.provideCaptureViewModelFactory(this))
                        .get(CaptureViewModel.class);

        captureViewModel.loadSession(sessionId);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.capture_timer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
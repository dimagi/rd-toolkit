package org.rdtoolkit.ui.provision;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.rdtoolkit.ui.capture.CaptureActivity;
import org.rdtoolkit.util.InjectorUtils;

import org.rdtoolkit.R;
import org.rdtoolkit.service.TestTimerService;

import static org.rdtoolkit.service.TestTimerServiceKt.NOTIFICATION_TAG_TEST_ID;

public class ProvisionActivity extends AppCompatActivity {

    ProvisionViewModel provisionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdt_provision);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        provisionViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.provideProvisionViewModelFactory(this))
                        .get(ProvisionViewModel.class);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.provision_define, R.id.provision_instructions, R.id.provision_start)
                .build();

        provisionViewModel.getInstructionsAvailable().observe(this, value -> {
            ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                    findItem(R.id.provision_instructions).setEnabled(value);
        });

        provisionViewModel.getStartAvailable().observe(this, value -> {
            ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                    findItem(R.id.provision_start).setEnabled(value);
        });


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }



    public void provisionNext(View view) {
        if (provisionViewModel.getViewInstructions().getValue()) {
            Navigation.findNavController(view).navigate(R.id.action_sessionProvision_to_sessionInstruct);
        } else {
            Navigation.findNavController(view).navigate(R.id.action_sessionProvision_to_captureFragment);
        }
    }


    public void confirmSession(View view) {
        String sessionID = provisionViewModel.commitSession();

        @NonNull
        Intent testTimerIntent = new Intent(this, TestTimerService.class);
        testTimerIntent.putExtra(NOTIFICATION_TAG_TEST_ID, sessionID);
        this.startService(testTimerIntent);

        Intent captureActivity = new Intent(this, CaptureActivity.class);
        captureActivity.putExtra(CaptureActivity.EXTRA_SESSION_ID, sessionID);
        this.startActivity(captureActivity);
        this.finish();
    }
}
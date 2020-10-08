package org.rdtoolkit.ui.provision;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.rdtoolkit.R;
import org.rdtoolkit.interop.BundleToConfiguration;
import org.rdtoolkit.interop.InterfacesKt;
import org.rdtoolkit.model.session.SessionMode;
import org.rdtoolkit.model.session.TestSession;
import org.rdtoolkit.service.TestTimerService;
import org.rdtoolkit.ui.capture.CaptureActivity;
import org.rdtoolkit.ui.instruct.PamphletViewModel;
import org.rdtoolkit.util.InjectorUtils;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static org.rdtoolkit.interop.InterfacesKt.INTENT_EXTRA_RESPONSE_TRANSLATOR;
import static org.rdtoolkit.interop.InterfacesKt.provisionReturnIntent;
import static org.rdtoolkit.service.TestTimerServiceKt.NOTIFICATION_TAG_TEST_ID;

public class ProvisionActivity extends AppCompatActivity {

    ProvisionViewModel provisionViewModel;
    PamphletViewModel pamphletViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdt_provision);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        provisionViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.provideProvisionViewModelFactory(this))
                        .get(ProvisionViewModel.class);

        pamphletViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.providePamphletViewModelFactory(this))
                        .get(PamphletViewModel.class);

        provisionViewModel.setConfig(
                getIntent().getStringExtra(InterfacesKt.INTENT_EXTRA_RDT_SESSION_ID),
                new BundleToConfiguration().map(
                        getIntent().getBundleExtra(InterfacesKt.INTENT_EXTRA_RDT_CONFIG_BUNDLE)));

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.provision_define, R.id.provision_instructions, R.id.provision_start)
                .build();

        MenuItem instructionItem = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.provision_instructions);

        provisionViewModel.getAreInstructionsAvailable().observe(this, value -> {
            instructionItem.setVisible(value);
        });

        provisionViewModel.getInstructionSets().observe(this, value -> {
            if (value.size() > 0) {
                pamphletViewModel.setSourcePamphlet(value.get(0));
            }
        });

        provisionViewModel.getStartAvailable().observe(this, value -> {
            ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                    findItem(R.id.provision_start).setEnabled(value);
        });

        provisionViewModel.getSessionConfig().observe(this, value -> {
            ((TextView)this.findViewById(R.id.tile_flavor_line)).setText(
                    String.format(getString(R.string.tile_txt_flavor_one),
                            value.getFlavorText(),
                            value.getFlavorTextTwo())
            );
        });

        provisionViewModel.getSelectedTestProfile().observe(this, value -> {
            if (value != null) {
                ((TextView)this.findViewById(R.id.tile_test_line)).setText(
                        String.format(getString(R.string.tile_txt_test_line),
                                value.readableName()));

            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }



    public void provisionNext(View view) {
        if (provisionViewModel.getAreInstructionsAvailable().getValue() &&
                provisionViewModel.getViewInstructions().getValue()) {
            Navigation.findNavController(view).navigate(R.id.action_sessionProvision_to_sessionInstruct);
        } else {
            Navigation.findNavController(view).navigate(R.id.action_sessionProvision_to_captureFragment);
        }
    }


    public void confirmSession(View view) {
        TestSession session = provisionViewModel.commitSession();

        @NonNull
        Intent testTimerIntent = new Intent(this, TestTimerService.class);
        testTimerIntent.putExtra(NOTIFICATION_TAG_TEST_ID, session.getSessionId());
        this.startService(testTimerIntent);

        TestSession.Configuration config = provisionViewModel.getSessionConfig().getValue();

        if (config.getSessionType() == SessionMode.ONE_PHASE) {
            Intent captureActivity = new Intent(this, CaptureActivity.class);
            captureActivity.setFlags(FLAG_ACTIVITY_FORWARD_RESULT);
            captureActivity.putExtra(InterfacesKt.INTENT_EXTRA_RDT_SESSION_ID, session.getSessionId());
            this.startActivity(captureActivity);
            this.finish();
        } else {
            Intent returnIntent = provisionReturnIntent(session);
            if (config.getOutputSessionTranslatorId() != null) {
                returnIntent.putExtra(INTENT_EXTRA_RESPONSE_TRANSLATOR, config.getOutputSessionTranslatorId());
            }
            this.setResult(RESULT_OK, returnIntent);
            this.finish();
        }
    }

    public void infoBackPressed(View view) {
        if (pamphletViewModel.hasBack()) {
            pamphletViewModel.pageBack();
            return;
        } else {
            Navigation.findNavController(view).navigate(R.id.action_sessionInstruct_to_sessionProvision);
        }
    }

    public void infoNextPressed(View view) {
        if (pamphletViewModel.hasNext()) {
            pamphletViewModel.pageNext();
            return;
        } else {
            Navigation.findNavController(view).navigate(R.id.action_sessionInstruct_to_captureFragment);
        }
    }
}
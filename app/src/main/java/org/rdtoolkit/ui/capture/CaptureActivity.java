package org.rdtoolkit.ui.capture;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rdtoolkit.R;
import org.rdtoolkit.component.CaptureConstraints;
import org.rdtoolkit.component.ComponentEventListener;
import org.rdtoolkit.component.ComponentManager;
import org.rdtoolkit.component.ComponentRepository;
import org.rdtoolkit.component.ImageCaptureResult;
import org.rdtoolkit.component.ImageClassifierComponent;
import org.rdtoolkit.component.ProcessingListener;
import org.rdtoolkit.component.Sandbox;
import org.rdtoolkit.component.TestImageCaptureComponent;
import org.rdtoolkit.model.diagnostics.Pamphlet;
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile;
import org.rdtoolkit.support.model.session.STATUS;
import org.rdtoolkit.support.model.session.TestReadableState;
import org.rdtoolkit.support.model.session.TestSession;
import org.rdtoolkit.ui.instruct.PamphletViewModel;
import org.rdtoolkit.util.InjectorUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import kotlin.NotImplementedError;

import static org.rdtoolkit.interop.InterfacesKt.captureReturnIntent;
import static org.rdtoolkit.support.interop.RdtIntentBuilder.INTENT_EXTRA_RDT_SESSION_ID;
import static org.rdtoolkit.support.interop.RdtIntentBuilder.INTENT_EXTRA_RESPONSE_TRANSLATOR;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.getSecondaryCaptureParams;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.wasSecondaryCaptureRequested;

public class CaptureActivity extends LocaleAwareCompatActivity {

    CaptureViewModel captureViewModel;
    PamphletViewModel pamphletViewModel;

    ComponentManager componentManager;

    ComponentManager secondaryComponentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        @NonNull
        String sessionId = this.getIntent().getStringExtra(INTENT_EXTRA_RDT_SESSION_ID);

        captureViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.provideCaptureViewModelFactory(this))
                        .get(CaptureViewModel.class);

        pamphletViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.providePamphletViewModelFactory(this))
                        .get(PamphletViewModel.class);

        componentManager  = new ComponentManager(this, new ComponentEventListener() {
            @Override
            public void testImageCaptured(@NotNull ImageCaptureResult imageResult) {
                CaptureActivity.this.testImageCaptured(imageResult);
            }
        }, captureViewModel.getReporter(), 100);

        secondaryComponentManager = new ComponentManager(this, new ComponentEventListener() {
            @Override
            public void testImageCaptured(@NotNull ImageCaptureResult imageResult) {
                CaptureActivity.this.secondaryImageCaptured(imageResult);
            }
        }, captureViewModel.getReporter(), 200);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.capture_timer, R.id.capture_results, R.id.capture_record)
                .build();

        MenuItem resultsTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_results);

        MenuItem captureTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_timer);

        MenuItem secondaryPhotoTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_secondary);

        MenuItem recordTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_record);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            invalidateOptionsMenu();
        }
        );

        if(captureViewModel.getTestSession().getValue() == null) {
            captureViewModel.loadSession(sessionId);
        }

        captureViewModel.getSessionIsInvalid().observe(this, value -> {
            if (value) {
                Toast.makeText(this, getString(R.string.capture_session_missing_msg), Toast.LENGTH_LONG).show();
                this.setResult(RESULT_CANCELED);
                this.finish();
            }
        });

        captureViewModel.getJobAidAvailable().observe(this, value -> {
            invalidateOptionsMenu();
        });

        captureViewModel.getJobAidPamphlets().observe(this, value -> {
            if (value.size() > 0) {
                pamphletViewModel.setSourcePamphlet(value.get(0));
            }
        });

        pamphletViewModel.onLastPage.observe(this, value -> {
            if (value) {
                captureViewModel.recordJobAidViewed();
            }
        });

        //Needed to ensure this declarative check processes.
        captureViewModel.getRequireWorkCheck().observe(this, result ->{

        });

        captureViewModel.getSecondaryCaptureRelevant().observe(this, result -> {
            secondaryPhotoTab.setVisible(result);
        });

        captureViewModel.getSessionStateInputs().observe(this, result -> {
            if (result.getFirst() == TestReadableState.EXPIRED && result.getSecond()) {
                captureTab.setVisible(false);
                resultsTab.setVisible(false);

                navController.navigate(R.id.capture_expire_to_record);
            } else {
                captureTab.setVisible(true);
                resultsTab.setVisible(true);
            }
        });

        captureViewModel.getSecondaryCaptureAvailableOrIrrelevant().observe(this, result -> {
            secondaryPhotoTab.setEnabled(result);
        });

        captureViewModel.getRecordAvailable().observe(this, result -> {
            recordTab.setEnabled(result);
        });

        captureViewModel.getProcessingState().observe(this, value -> {
            switch (value) {
                case PRE_CAPTURE:
                case ERROR:
                case PROCESSING:
                    resultsTab.setEnabled(false);
                    if (navController.getCurrentDestination().getId() != R.id.capture_timer) {
                        navController.navigate(R.id.capture_start_fresh);
                    }
                    break;
                case COMPLETE:
                case SKIPPED:
                    resultsTab.setEnabled(true);
                    if(navController.getCurrentDestination().getId() != R.id.capture_results) {
                        if (navController.getCurrentDestination().getId() == R.id.capture_timer) {
                            navController.navigate(
                                    R.id.action_capture_timer_to_capture_resultsFragment);
                        } else {
                            navController.navigate(R.id.capture_results);
                        }
                    }
                    break;
                default:
                    throw new NotImplementedError("Unimplemented Capture processing state");
            }
        });


        captureViewModel.getTestSession().observe(this, value -> {
            ((TextView)this.findViewById(R.id.tile_flavor_line)).setText(
                    String.format(getString(R.string.tile_txt_flavor_one),
                            value.getConfiguration().getFlavorText(),
                            value.getConfiguration().getFlavorTextTwo())
            );
        });

        captureViewModel.getTestProfile().observe(this, value -> {
            if (value != null) {
                ((TextView)this.findViewById(R.id.tile_test_line)).setText(
                        String.format(getString(R.string.tile_txt_test_line),
                                value.readableName()));

            }
        });

        captureViewModel.getTestProfile().observe(this, result -> {

            //This value sets the value of the profile, so this should be safe
            TestSession.Configuration config = captureViewModel.getTestSession().getValue().getConfiguration();

            CaptureConstraints captureConstraints = new CaptureConstraints(result.id(),
                    config.getFlags());
            ComponentRepository repository = InjectorUtils.Companion.provideComponentRepository(this);

            Sandbox sandbox = new Sandbox(this, sessionId);

            //TODO: Unify into a single plan method that can intersect these more carefully. Right now
            //it's possible for a classifier to lack a compatible capture
            ImageClassifierComponent classifierComponent = repository.getClassifierComponentForTest(captureConstraints, sandbox);
            List<String> captureModes = classifierComponent == null ? null :  classifierComponent.compatibleCaptureModes();
            TestImageCaptureComponent captureComponent = repository.
                    getCaptureComponentForTest(result.id(), captureConstraints, captureModes, sandbox);

            if (wasSecondaryCaptureRequested(config)) {
                CaptureConstraints secondaryCaptureConstraints =
                        new CaptureConstraints(result.id(), getSecondaryCaptureParams(config));

                TestImageCaptureComponent secondaryCaptureComponent = repository.
                        getCaptureComponentForTest(result.id(), secondaryCaptureConstraints, null, sandbox);

                if (secondaryCaptureComponent.toString().equals(captureComponent.toString())) {
                    //secondary and primary capture are the same, skip secondary capture
                    captureViewModel.getSecondaryCaptureCompatible().setValue(false);
                } else {
                    secondaryComponentManager.registerComponents(secondaryCaptureComponent);
                }
            }

            if (classifierComponent != null) {
                componentManager.registerComponents(captureComponent, classifierComponent);
            } else {
                captureViewModel.disableProcessing();
                componentManager.registerComponents(captureComponent);
            }
        });

        captureViewModel.getSessionCommit().observe(this, result -> {
            if (result.getFirst() == false &&
                    (result.getSecond().getState() == STATUS.COMPLETE || result.getSecond().getState() == STATUS.QUEUED)) {
                finishSession(result.getSecond());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.capture_actions, menu);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        MenuItem jobAidMenuItem = menu.findItem(R.id.action_view_job_aid);
        jobAidMenuItem.setVisible(navController.getCurrentDestination().getId() == R.id.capture_results && Boolean.TRUE.equals(captureViewModel.getJobAidAvailable().getValue()));

        MenuItem backFromJobAid = menu.findItem(R.id.action_back_from_job_aid);
        backFromJobAid.setVisible(navController.getCurrentDestination().getId() == R.id.capture_job_aid);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_view_job_aid:
                pamphletViewModel.goToPageOne();
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_results_to_captureJobAid);
                return true;
            case R.id.action_back_from_job_aid:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_job_aid_to_results);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        componentManager.notifyIntentCallback(requestCode, resultCode, data);
        secondaryComponentManager.notifyIntentCallback(requestCode, resultCode, data);
    }

    public void acknowledgeSecondaryRequest(View button) {
        captureViewModel.getSecondaryRequestAcknowledged().setValue(true);
    }

    public void captureSecondaryImage(View button) {
        secondaryComponentManager.getCaptureComponent().captureImage();
    }

    public void captureTestResult(View button) {
        componentManager.getCaptureComponent().captureImage();
    }

    public void proceedFromResultsCapture(View v) {
        if(captureViewModel.getSecondaryCaptureRelevant().getValue()) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_results_to_secondary);
        } else {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_results_to_captureRecordFragment);
        }
    }

    public void recordResults(View v) {
        if (!captureViewModel.getRequireWorkCheck().getValue()) {
            captureViewModel.commitResult();
        } else {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_record_to_work_check);
        }
    }

    public void reviewChoices(View v) {
        captureViewModel.setWorkCheckTriggered();
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.capture_trigger_work_check);
    }

    public void skipTimer(View v) {
        captureViewModel.forceTestReadable();
    }

    public void confirmRecordResults(View v) {
        captureViewModel.commitResult();
    }

    public void overrideCaptureError(View v) {
        captureViewModel.setProcessingSkipped();
    }

    public void overrideExpiration(View v) {
        captureViewModel.forceTestReadable();
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.capture_start_fresh);
    }

    private void finishSession(TestSession session) {
        Intent returnIntent = captureReturnIntent(session);
        if (session.getConfiguration().getOutputResultTranslatorId() != null) {
            returnIntent.putExtra(INTENT_EXTRA_RESPONSE_TRANSLATOR,
                    session.getConfiguration().getOutputResultTranslatorId());
        }
        this.setResult(RESULT_OK, returnIntent);
        this.finish();
    }

    private void processImage(ImageCaptureResult imageResult) {
        ImageClassifierComponent classifier = componentManager.getImageClassifierComponent();

        classifier.doImageProcessing(imageResult, new ProcessingListener() {
            @Override
            public void onClassifierError(@NotNull String error, @Nullable Pamphlet details) {
                captureViewModel.setProcessingError(error, details);

            }

            @Override
            public void onClassifierComplete(@NotNull Map<String, String> results) {
                captureViewModel.setClassifierResults(results);
            }
        });
    }

    private void testImageCaptured(@NotNull ImageCaptureResult imageResult) {
        captureViewModel.setCapturedImage(imageResult.getImages());
        ImageClassifierComponent classifier = componentManager.getImageClassifierComponent();
        if(classifier != null) {
            processImage(imageResult);
        }
    }

    private void secondaryImageCaptured(@NotNull ImageCaptureResult imageResult) {
        captureViewModel.setSecondaryImageCaptured(imageResult.getImages());
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_capture_secondary_to_record);
    }

    public void infoBackPressed(View view) {
        if (pamphletViewModel.hasBack()) {
            pamphletViewModel.pageBack();
            return;
        } else {
            Navigation.findNavController(view).navigate(R.id.action_capture_job_aid_to_results);
        }
    }

    public void infoNextPressed(View view) {
        if (pamphletViewModel.hasNext()) {
            pamphletViewModel.pageNext();
            return;
        } else {
            Navigation.findNavController(view).navigate(R.id.action_capture_job_aid_to_results);
        }
    }
}
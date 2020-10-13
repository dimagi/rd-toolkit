package org.rdtoolkit.ui.capture;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rdtoolkit.R;
import org.rdtoolkit.component.ComponentEventListener;
import org.rdtoolkit.component.ComponentManager;
import org.rdtoolkit.component.ComponentRepository;
import org.rdtoolkit.component.ImageCaptureResult;
import org.rdtoolkit.component.ImageClassifierComponent;
import org.rdtoolkit.component.TestImageCaptureComponent;
import org.rdtoolkit.interop.InterfacesKt;
import org.rdtoolkit.model.diagnostics.Pamphlet;
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile;
import org.rdtoolkit.model.session.STATUS;
import org.rdtoolkit.model.session.TestReadableState;
import org.rdtoolkit.model.session.TestSession;
import org.rdtoolkit.util.InjectorUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.rdtoolkit.interop.InterfacesKt.INTENT_EXTRA_RDT_SESSION_ID;
import static org.rdtoolkit.interop.InterfacesKt.captureReturnIntent;

public class CaptureActivity extends AppCompatActivity implements ComponentEventListener {

    CaptureViewModel captureViewModel;

    ComponentManager componentManager = new ComponentManager(this, this);

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

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.capture_timer, R.id.capture_results, R.id.capture_record)
                .build();

        MenuItem resultsTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_results);

        MenuItem captureTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_timer);


        MenuItem recordTab = ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_record);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        captureViewModel.loadSession(sessionId);

        captureViewModel.getSessionStateInputs().observe(this, result -> {
            if (result.getFirst() == TestReadableState.EXPIRED && result.getSecond()) {
                captureTab.setVisible(false);
                resultsTab.setVisible(false);
                recordTab.setEnabled(true);

                navController.navigate(R.id.capture_expire_to_record);
            } else {
                captureTab.setVisible(true);
                resultsTab.setVisible(true);

                setConfirmAvailable(captureViewModel.getTestSessionResult().getValue());
            }
        });


        captureViewModel.getProcessingState().observe(this, value -> {
            switch (value) {
                case PRE_CAPTURE:
                case ERROR:
                case PROCESSING:
                    resultsTab.setEnabled(false);
                    break;
                case COMPLETE:
                    resultsTab.setEnabled(true);
                    break;
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

        captureViewModel.getTestSessionResult().observe(this, value -> {
            setConfirmAvailable(value);
        });

        captureViewModel.getTestProfile().observe(this, result -> {
            HashSet<String> defaultTags = new HashSet<>();
            defaultTags.add("production");
            ComponentRepository repository = InjectorUtils.Companion.provideComponentRepository(this);

            //TODO: Unify into a single plan method that can intersect these more carefully. Right now
            //it's possible for a classifier to lack a compatible capture
            ImageClassifierComponent classifierComponent = repository.getClassifierComponentForTest(result.id(), defaultTags);

            List<String> captureModes = classifierComponent == null ? null :  classifierComponent.compatibleCaptureModes();

            TestImageCaptureComponent captureComponent = repository.
                    getCaptureComponentForTest(result.id(), defaultTags, captureModes);

            if (classifierComponent != null) {
                componentManager.registerComponents(captureComponent, classifierComponent);
            } else {
                captureViewModel.disableProcessing();
                componentManager.registerComponents(captureComponent);

            }
        });

        captureViewModel.getSessionCommit().observe(this, result -> {
            if (result.getFirst() == false &&
                    result.getSecond().getState() == STATUS.COMPLETE) {
                finishSession(result.getSecond());
            }
        });
    }

    private void setConfirmAvailable(TestSession.TestResult result) {
        boolean recordEnabled = false;
        RdtDiagnosticProfile profile = captureViewModel.getTestProfile().getValue();
        if(profile != null && result != null) {
            recordEnabled = profile.isResultSetComplete(result);
        }

        ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                findItem(R.id.capture_record).setEnabled(recordEnabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        componentManager.notifyIntentCallback(requestCode, resultCode, data);
    }

    public void captureTestResult(View button) {
        componentManager.getCaptureComponent().captureImage();
    }

    public void recordResults(View v) {
        captureViewModel.commitResult();
    }

    public void overrideExpiration(View v) {
        captureViewModel.setExpirationOverriden();
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.capture_override_expire);
    }

    private void finishSession(TestSession session) {
        Intent returnIntent = captureReturnIntent(session);
        if (session.getConfiguration().getOutputResultTranslatorId() != null) {
            returnIntent.putExtra(InterfacesKt.INTENT_EXTRA_RESPONSE_TRANSLATOR,
                    session.getConfiguration().getOutputResultTranslatorId());
        }
        this.setResult(RESULT_OK, returnIntent);
        this.finish();
    }

    @Override
    public void testImageCaptured(@NotNull ImageCaptureResult imageResult) {
        captureViewModel.setRawImageCapturePath(imageResult.getImagePath());
        ImageClassifierComponent classifier = componentManager.getImageClassifierComponent();
        if(classifier != null) {
            processImage(imageResult);
        } else {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                    R.id.action_capture_timer_to_capture_resultsFragment);
        }
    }

    private void processImage(ImageCaptureResult imageResult) {
        ImageClassifierComponent classifier = componentManager.getImageClassifierComponent();

        classifier.doImageProcessing(imageResult);
    }

    @Override
    public void onClassifierError(@NotNull String error, @Nullable Pamphlet details) {
        captureViewModel.setProcessingError(error, details);
    }

    @Override
    public void onClassifierComplete(@NotNull Map<String, String> results) {
        captureViewModel.setClassifierResults(results);
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                R.id.action_capture_timer_to_capture_resultsFragment);
    }
}
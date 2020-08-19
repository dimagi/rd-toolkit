package org.rdtoolkit.ui.capture;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.rdtoolkit.interop.InterfacesKt;
import org.rdtoolkit.model.diagnostics.DiagnosticOutcome;
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile;
import org.rdtoolkit.model.diagnostics.ResultProfile;
import org.rdtoolkit.model.session.STATUS;
import org.rdtoolkit.model.session.TestSession;
import org.rdtoolkit.ui.provision.ProvisionViewModel;
import org.rdtoolkit.util.InjectorUtils;

import org.rdtoolkit.R;
import org.rdtoolkit.service.TestTimerService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.rdtoolkit.interop.InterfacesKt.INTENT_EXTRA_RDT_SESSION_ID;
import static org.rdtoolkit.interop.InterfacesKt.captureReturnIntent;
import static org.rdtoolkit.service.TestTimerServiceKt.NOTIFICATION_TAG_TEST_ID;

public class CaptureActivity extends AppCompatActivity {

    public static int REQUEST_PHOTO_CAPTURE = 1;

    CaptureViewModel captureViewModel;

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

        captureViewModel.loadSession(sessionId);

        captureViewModel.getTestSessionResult().observe(this, value -> {
            RdtDiagnosticProfile profile = captureViewModel.getTestProfile().getValue();
            boolean recordEnabled = profile.isResultSetComplete(value);

            ((BottomNavigationView)this.findViewById(R.id.nav_view)).getMenu().
                    findItem(R.id.capture_record).setEnabled(recordEnabled);
        });

        captureViewModel.getSessionCommit().observe(this, result -> {
            if (result.getFirst() == false &&
                    result.getSecond().getState() == STATUS.COMPLETE) {
                finishSession(result.getSecond());
            }
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.capture_timer, R.id.capture_results, R.id.capture_record)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO_CAPTURE && resultCode == RESULT_OK) {
            File returnPhoto = new File(returnPhotoPath);
            if (returnPhoto.exists()) {
                captureViewModel.setRawImageCapturePath(returnPhotoPath);
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                        R.id.action_capture_timer_to_capture_resultsFragment);
            }
        }
    }

    String returnPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        returnPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void captureTestResult(View button) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.rdtoolkit.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_PHOTO_CAPTURE);
            }
        }
    }

    public void recordResults(View v) {
        captureViewModel.commitResult();
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
}
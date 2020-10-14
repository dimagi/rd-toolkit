package org.rdtoolkit.interop;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.rdtoolkit.interop.translator.InteropRepository;
import org.rdtoolkit.ui.capture.CaptureActivity;

import static org.rdtoolkit.interop.InterfacesKt.bootstrap;
import static org.rdtoolkit.interop.InterfacesKt.provisionIntent;
import static org.rdtoolkit.support.interop.RdtIntentBuilder.ACTION_TEST_PROVISION;
import static org.rdtoolkit.support.interop.RdtIntentBuilder.ACTION_TEST_PROVISION_AND_CAPTURE;

/**
 * Primary entry point for external requests for the toolkit. Determines how to process requests
 * and makes sure any needed configuration is available.
 *
 */
public class DispatcherActivity extends AppCompatActivity {

    private static int ACTIVITY_RESULT_PASSTHROUGH = RESULT_FIRST_USER;

    private InteropRepository interopRepo = new InteropRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent inbound = getIntent();
        String action = inbound.getAction();
        if (action.equals(ACTION_TEST_PROVISION) ||
                action.equals(ACTION_TEST_PROVISION_AND_CAPTURE)) {

            Intent passthrough = provisionIntent(this, inbound);
            this.startActivityForResult(passthrough, ACTIVITY_RESULT_PASSTHROUGH);

        } else {
            Intent passthrough = new Intent(this, CaptureActivity.class);
            bootstrap(passthrough, inbound);
            this.startActivityForResult(passthrough, ACTIVITY_RESULT_PASSTHROUGH);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED) {
            this.setResult(RESULT_CANCELED);
            this.finish();
        }
        if(requestCode == ACTIVITY_RESULT_PASSTHROUGH && resultCode == RESULT_OK) {
            this.setResult(resultCode, interopRepo.translateResponse(data));
            this.finish();
        }
    }
}

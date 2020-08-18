package org.rdtoolkit.interop;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.rdtoolkit.ui.capture.CaptureActivity;
import org.rdtoolkit.ui.provision.ProvisionActivity;

import static org.rdtoolkit.interop.InterfacesKt.bootstrap;

/**
 * Primary entry point for external requests for the toolkit. Determines how to process requests
 * and makes sure any needed configuration is available.
 *
 */
public class DispatcherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent inbound = getIntent();
        String action = inbound.getAction();
        if (action.equals(InterfacesKt.ACTION_TEST_PROVISION) ||
                action.equals(InterfacesKt.ACTION_TEST_PROVISION_AND_CAPTURE)) {

            Intent passthrough = new Intent(this, ProvisionActivity.class);
            bootstrap(passthrough, inbound);
            this.startActivity(passthrough);

        } else {
            Intent passthrough = new Intent(this, CaptureActivity.class);
            bootstrap(passthrough, inbound);
            this.startActivity(passthrough);

        }
    }
}

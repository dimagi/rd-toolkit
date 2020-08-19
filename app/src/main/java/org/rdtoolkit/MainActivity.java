package org.rdtoolkit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.rdtoolkit.interop.BundleToSession;
import org.rdtoolkit.interop.DispatcherActivity;
import org.rdtoolkit.interop.InterfacesKt;
import org.rdtoolkit.interop.TestIntentBuilder;
import org.rdtoolkit.model.session.TestSession;
import org.rdtoolkit.ui.capture.CaptureActivity;

import java.util.UUID;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static org.rdtoolkit.interop.InterfacesKt.INTENT_EXTRA_RDT_SESSION_BUNDLE;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_PROVISION = 1;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void simulateTestRequest(View view) {
        Intent i = new TestIntentBuilder()
                .forProvisioning().setSessionId(UUID.randomUUID().toString())
                //.requestTestProfile("debug_mal_pf_pv")
                .requestTestProfile("debug_sf_mal_pf_pv")
                .setFlavorOne("Clayton Sims")
                .setFlavorTwo("#4SFS")
                .setResultResponseTranslator("xform_response")
                .build();

        this.startActivityForResult(i, ACTIVITY_PROVISION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ACTIVITY_PROVISION && resultCode == RESULT_OK) {

        }
    }

    public void goToCapture(View view) {
        String sessionId = (String)view.getTag();
        Intent captureActivity = new Intent();
        captureActivity.setAction(InterfacesKt.ACTION_TEST_CAPTURE);
        captureActivity.putExtra(InterfacesKt.INTENT_EXTRA_RDT_SESSION_ID, sessionId);
        this.startActivityForResult(captureActivity, ACTIVITY_PROVISION);
    }
}
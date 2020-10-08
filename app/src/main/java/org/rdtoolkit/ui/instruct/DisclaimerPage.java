package org.rdtoolkit.ui.instruct;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.Page;

import java.io.InputStream;


public class DisclaimerPage implements Page {

    private Context context;

    public DisclaimerPage(Context context ){
        this.context = context;
    }

    @NotNull
    @Override
    public String getText() {
        return context.getResources().getString(R.string.reference_disclaimer_msg);
    }

    @Nullable
    @Override
    public InputStream getImageStream() {
        return null;
    }

    @Nullable
    @Override
    public String getConfirmationText() {
        return context.getResources().getString(R.string.reference_disclaimer_cbx_msg);
    }
}

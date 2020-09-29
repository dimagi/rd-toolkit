package org.rdtoolkit.ui.instruct;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.Page;
import org.rdtoolkit.util.InjectorUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.rdtoolkit.util.MediaUtilKt.configureImageView;

public class SessionInstruct extends Fragment {
    public static SessionInstruct newInstance() {
        return new SessionInstruct();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provision_instruct, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PamphletViewModel pamphletViewModel =
                new ViewModelProvider(requireActivity())
                        .get(PamphletViewModel.class);


        pamphletViewModel.getCurrentPage().observe(this.getViewLifecycleOwner(), value -> {
            ImageView iv = view.findViewById(R.id.provision_info_page_image);
            TextView tv = view.findViewById(R.id.provision_info_page_text);
            CheckBox disclaimer = view.findViewById(R.id.provision_checkbox_disclaimer);

            configureImageView(iv, value.getImageStream());

            tv.setText(value.getText());
            disclaimer.setText(value.getConfirmationText());
            disclaimer.setVisibility(value.getConfirmationText() == null ? View.GONE : View.VISIBLE);
        });
    }
}
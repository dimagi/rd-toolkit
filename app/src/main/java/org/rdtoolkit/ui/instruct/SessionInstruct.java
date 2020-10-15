package org.rdtoolkit.ui.instruct;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.rdtoolkit.R;

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

            Button back = view.findViewById(R.id.provision_info_btn_back);
            if (pamphletViewModel.hasBack()) {
                back.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            } else {
                back.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_arrow_back_white_24,0,0,0);
            }


            Button next = view.findViewById(R.id.provision_info_btn_next);
            if (pamphletViewModel.hasNext()) {
                next.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            } else {
                next.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_arrow_forward_white_24,0);
            }
        });
    }
}
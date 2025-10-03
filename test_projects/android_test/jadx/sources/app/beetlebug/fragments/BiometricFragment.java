package app.beetlebug.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import app.beetlebug.C0572R;
import app.beetlebug.FlagsOverview;
import app.beetlebug.ctf.BiometricActivityDeeplink;

/* loaded from: classes8.dex */
public class BiometricFragment extends Fragment {
    Button btn;
    ImageView m_btn;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_biometric, container, false);
        this.m_btn = (ImageView) view.findViewById(C0572R.id.back);
        this.btn = (Button) view.findViewById(C0572R.id.button);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.BiometricFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(BiometricFragment.this.getActivity(), (Class<?>) BiometricActivityDeeplink.class);
                BiometricFragment.this.startActivity(ctf_intent);
            }
        });
        float auth_score = this.sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        String auth_string = Float.toString(auth_score);
        if (auth_string.equals("6.25")) {
            this.btn.setEnabled(false);
            this.btn.setText("Done");
        }
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.BiometricFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(BiometricFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                BiometricFragment.this.startActivity(i);
            }
        });
        return view;
    }
}

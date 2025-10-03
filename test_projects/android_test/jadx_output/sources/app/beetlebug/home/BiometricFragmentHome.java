package app.beetlebug.home;

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
import app.beetlebug.MainActivity;
import app.beetlebug.ctf.BiometricActivityDeeplink;

/* loaded from: classes9.dex */
public class BiometricFragmentHome extends Fragment {
    Button m_btn;
    ImageView m_btn_back;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_biometric_home, container, false);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.m_btn_back = (ImageView) view.findViewById(C0572R.id.back);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn_back.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.BiometricFragmentHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(BiometricFragmentHome.this.getActivity(), (Class<?>) MainActivity.class);
                BiometricFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.BiometricFragmentHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(BiometricFragmentHome.this.getActivity(), (Class<?>) BiometricActivityDeeplink.class);
                BiometricFragmentHome.this.startActivity(ctf_intent);
            }
        });
        float auth_score = this.sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        String auth_string = Float.toString(auth_score);
        if (auth_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        return view;
    }
}

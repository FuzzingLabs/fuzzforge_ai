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
import app.beetlebug.ctf.EmbeddedSecretSourceCode;
import app.beetlebug.ctf.EmbeddedSecretStrings;

/* loaded from: classes8.dex */
public class SecretsFragment extends Fragment {
    ImageView m_back_btn;
    ImageView m_back_btn2;
    Button m_btn;
    Button m_btn2;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_secrets, container, false);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button2);
        this.m_back_btn = (ImageView) view.findViewById(C0572R.id.back);
        this.m_back_btn2 = (ImageView) view.findViewById(C0572R.id.back2);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.sharedPreferences = sharedPreferences;
        float secret_source_score = sharedPreferences.getFloat("ctf_score_secret_source", 0.0f);
        float secret_string_score = this.sharedPreferences.getFloat("ctf_score_secret_string", 0.0f);
        String score_string = Float.toString(secret_string_score);
        String score_source = Float.toString(secret_source_score);
        if (score_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        if (score_source.equals("6.25")) {
            this.m_btn2.setEnabled(false);
            this.m_btn2.setText("Done");
        }
        this.m_back_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SecretsFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(SecretsFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                SecretsFragment.this.startActivity(ctf_intent);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SecretsFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent secret_intent = new Intent(SecretsFragment.this.getActivity(), (Class<?>) EmbeddedSecretStrings.class);
                SecretsFragment.this.startActivity(secret_intent);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SecretsFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent secret_intent = new Intent(SecretsFragment.this.getActivity(), (Class<?>) EmbeddedSecretSourceCode.class);
                SecretsFragment.this.startActivity(secret_intent);
            }
        });
        return view;
    }
}

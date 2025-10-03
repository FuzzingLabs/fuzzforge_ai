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
import app.beetlebug.ctf.EmbeddedSecretSourceCode;
import app.beetlebug.ctf.EmbeddedSecretStrings;

/* loaded from: classes9.dex */
public class SecretsFragmentHome extends Fragment {
    Button m_btn;
    Button m_btn2;
    ImageView m_btn_back;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_secrets_fragments_home, container, false);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button2);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.m_btn_back = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SecretsFragmentHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(SecretsFragmentHome.this.getActivity(), (Class<?>) MainActivity.class);
                SecretsFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SecretsFragmentHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(SecretsFragmentHome.this.getActivity(), (Class<?>) EmbeddedSecretStrings.class);
                SecretsFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SecretsFragmentHome.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(SecretsFragmentHome.this.getActivity(), (Class<?>) EmbeddedSecretSourceCode.class);
                SecretsFragmentHome.this.startActivity(i);
            }
        });
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
        return view;
    }
}

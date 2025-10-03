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
import app.beetlebug.ctf.InsecureLoggingActivity;
import app.beetlebug.ctf.VulnerableClipboardActivity;

/* loaded from: classes9.dex */
public class SensitiveDataFragmentHome extends Fragment {
    ImageView m_back_btn;
    Button m_btn;
    Button m_btn2;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_sensitive_data, container, false);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button2);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.m_back_btn = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SensitiveDataFragmentHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(SensitiveDataFragmentHome.this.getActivity(), (Class<?>) MainActivity.class);
                SensitiveDataFragmentHome.this.startActivity(ctf_intent);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SensitiveDataFragmentHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(SensitiveDataFragmentHome.this.getActivity(), (Class<?>) InsecureLoggingActivity.class);
                SensitiveDataFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.SensitiveDataFragmentHome.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(SensitiveDataFragmentHome.this.getActivity(), (Class<?>) VulnerableClipboardActivity.class);
                SensitiveDataFragmentHome.this.startActivity(i);
            }
        });
        float clip_score = this.sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float log_score = this.sharedPreferences.getFloat("ctf_score_log", 0.0f);
        String log_string = Float.toString(log_score);
        if (log_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        String clip_string = Float.toString(clip_score);
        if (clip_string.equals("6.25")) {
            this.m_btn2.setEnabled(false);
            this.m_btn2.setText("Done");
        }
        return view;
    }
}

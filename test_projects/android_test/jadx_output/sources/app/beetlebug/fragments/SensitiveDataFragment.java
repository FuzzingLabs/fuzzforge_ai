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
import app.beetlebug.ctf.InsecureLoggingActivity;
import app.beetlebug.ctf.VulnerableClipboardActivity;

/* loaded from: classes8.dex */
public class SensitiveDataFragment extends Fragment {
    ImageView mBackButton;
    Button mBtn;
    Button mBtn2;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_sensitive_data, container, false);
        this.mBackButton = (ImageView) view.findViewById(C0572R.id.back);
        this.mBtn = (Button) view.findViewById(C0572R.id.button);
        this.mBtn2 = (Button) view.findViewById(C0572R.id.button2);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.mBtn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SensitiveDataFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(SensitiveDataFragment.this.getActivity(), (Class<?>) InsecureLoggingActivity.class);
                SensitiveDataFragment.this.startActivity(ctf_intent);
            }
        });
        this.mBtn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SensitiveDataFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent2 = new Intent(SensitiveDataFragment.this.getActivity(), (Class<?>) VulnerableClipboardActivity.class);
                SensitiveDataFragment.this.startActivity(ctf_intent2);
            }
        });
        this.mBackButton.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.SensitiveDataFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(SensitiveDataFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                SensitiveDataFragment.this.startActivity(ctf_intent);
            }
        });
        float clip_score = this.sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float log_score = this.sharedPreferences.getFloat("ctf_score_log", 0.0f);
        String log_string = Float.toString(log_score);
        if (log_string.equals("6.25")) {
            this.mBtn.setEnabled(false);
            this.mBtn.setText("Done");
        }
        String clip_string = Float.toString(clip_score);
        if (clip_string.equals("6.25")) {
            this.mBtn2.setEnabled(false);
            this.mBtn2.setText("Done");
        }
        return view;
    }
}

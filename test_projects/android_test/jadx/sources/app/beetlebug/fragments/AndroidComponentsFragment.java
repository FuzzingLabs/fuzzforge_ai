package app.beetlebug.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import app.beetlebug.C0572R;
import app.beetlebug.FlagsOverview;
import app.beetlebug.ctf.InsecureContentProvider;
import app.beetlebug.ctf.VulnerableActivityIntent;
import app.beetlebug.ctf.VulnerableServiceActivity;

/* loaded from: classes8.dex */
public class AndroidComponentsFragment extends Fragment {
    ImageView mBackButton;
    Button mButton;
    Button mButton3;
    Button mButton4;
    TextView mCtfTitle;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_android_components, container, false);
        this.mButton = (Button) view.findViewById(C0572R.id.button);
        this.mButton3 = (Button) view.findViewById(C0572R.id.button3);
        this.mButton4 = (Button) view.findViewById(C0572R.id.button4);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.sharedPreferences = sharedPreferences;
        float intent_redirect_score = sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        String pref_string = Float.toString(intent_redirect_score);
        if (pref_string.equals("6.25")) {
            this.mButton.setEnabled(false);
            this.mButton.setText("Done");
        }
        float service_score = this.sharedPreferences.getFloat("ctf_score_service", 0.0f);
        String service_string = Float.toString(service_score);
        if (service_string.equals("6.25")) {
            this.mButton3.setEnabled(false);
            this.mButton3.setText("Done");
        }
        float content_score = this.sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        String content_string = Float.toString(content_score);
        if (content_string.equals("6.25")) {
            this.mButton4.setEnabled(false);
            this.mButton4.setText("Done");
        }
        this.mCtfTitle = (TextView) view.findViewById(C0572R.id.textViewComponentsTitle);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.mBackButton = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AndroidComponentsFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(AndroidComponentsFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                AndroidComponentsFragment.this.startActivity(ctf_intent);
            }
        });
        this.mButton.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AndroidComponentsFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(AndroidComponentsFragment.this.getActivity(), (Class<?>) VulnerableActivityIntent.class);
                AndroidComponentsFragment.this.startActivity(ctf_intent);
            }
        });
        this.mButton3.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AndroidComponentsFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent3 = new Intent(AndroidComponentsFragment.this.getActivity(), (Class<?>) VulnerableServiceActivity.class);
                AndroidComponentsFragment.this.startActivity(ctf_intent3);
            }
        });
        this.mButton4.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AndroidComponentsFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent4 = new Intent(AndroidComponentsFragment.this.getActivity(), (Class<?>) InsecureContentProvider.class);
                AndroidComponentsFragment.this.startActivity(ctf_intent4);
            }
        });
        return view;
    }
}

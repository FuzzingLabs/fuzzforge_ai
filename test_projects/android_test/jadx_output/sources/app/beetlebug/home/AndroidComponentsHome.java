package app.beetlebug.home;

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
import app.beetlebug.MainActivity;
import app.beetlebug.ctf.InsecureContentProvider;
import app.beetlebug.ctf.VulnerableActivityIntent;
import app.beetlebug.ctf.VulnerableServiceActivity;

/* loaded from: classes9.dex */
public class AndroidComponentsHome extends Fragment {
    ImageView mBackButton;
    TextView mCtfTitle;
    Button m_btn;
    Button m_btn3;
    Button m_btn4;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_android_components, container, false);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn3 = (Button) view.findViewById(C0572R.id.button3);
        this.m_btn4 = (Button) view.findViewById(C0572R.id.button4);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.sharedPreferences = sharedPreferences;
        float intent_redirect_score = sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        String pref_string = Float.toString(intent_redirect_score);
        if (pref_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        float service_score = this.sharedPreferences.getFloat("ctf_score_service", 0.0f);
        String service_string = Float.toString(service_score);
        if (service_string.equals("6.25")) {
            this.m_btn3.setEnabled(false);
            this.m_btn3.setText("Done");
        }
        float content_score = this.sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        String content_string = Float.toString(content_score);
        if (content_string.equals("6.25")) {
            this.m_btn4.setEnabled(false);
            this.m_btn4.setText("Done");
        }
        this.mCtfTitle = (TextView) view.findViewById(C0572R.id.textViewComponentsTitle);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.mBackButton = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.AndroidComponentsHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(AndroidComponentsHome.this.getActivity(), (Class<?>) MainActivity.class);
                AndroidComponentsHome.this.startActivity(ctf_intent);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.AndroidComponentsHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(AndroidComponentsHome.this.getActivity(), (Class<?>) VulnerableActivityIntent.class);
                AndroidComponentsHome.this.startActivity(ctf_intent);
            }
        });
        this.m_btn3.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.AndroidComponentsHome.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent3 = new Intent(AndroidComponentsHome.this.getActivity(), (Class<?>) VulnerableServiceActivity.class);
                AndroidComponentsHome.this.startActivity(ctf_intent3);
            }
        });
        this.m_btn4.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.AndroidComponentsHome.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent4 = new Intent(AndroidComponentsHome.this.getActivity(), (Class<?>) InsecureContentProvider.class);
                AndroidComponentsHome.this.startActivity(ctf_intent4);
            }
        });
        return view;
    }
}

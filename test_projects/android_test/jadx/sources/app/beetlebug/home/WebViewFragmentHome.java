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
import app.beetlebug.ctf.WebViewURLActivity;
import app.beetlebug.ctf.WebViewXSSActivity;

/* loaded from: classes9.dex */
public class WebViewFragmentHome extends Fragment {
    Button m_btn;
    Button m_btn2;
    ImageView m_btn_back;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_web_view_home, container, false);
        this.m_btn_back = (ImageView) view.findViewById(C0572R.id.back);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button3);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.m_btn_back.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.WebViewFragmentHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(WebViewFragmentHome.this.getActivity(), (Class<?>) MainActivity.class);
                WebViewFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.WebViewFragmentHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(WebViewFragmentHome.this.getActivity(), (Class<?>) WebViewURLActivity.class);
                WebViewFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.WebViewFragmentHome.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(WebViewFragmentHome.this.getActivity(), (Class<?>) WebViewXSSActivity.class);
                WebViewFragmentHome.this.startActivity(i);
            }
        });
        float xss_score = this.sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float webview_score = this.sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        String web_string = Float.toString(webview_score);
        if (web_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        String xss_string = Float.toString(xss_score);
        if (xss_string.equals("6.25")) {
            this.m_btn2.setEnabled(false);
            this.m_btn2.setText("Done");
        }
        return view;
    }
}

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
import app.beetlebug.ctf.WebViewURLActivity;
import app.beetlebug.ctf.WebViewXSSActivity;

/* loaded from: classes8.dex */
public class WebViewFragment extends Fragment {
    ImageView btn;
    Button m_btn1;
    Button m_btn3;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_web_view, container, false);
        this.m_btn1 = (Button) view.findViewById(C0572R.id.button);
        this.m_btn3 = (Button) view.findViewById(C0572R.id.button3);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.btn = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.WebViewFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(WebViewFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                WebViewFragment.this.startActivity(ctf_intent);
            }
        });
        this.m_btn1.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.WebViewFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(WebViewFragment.this.getActivity(), (Class<?>) WebViewURLActivity.class);
                WebViewFragment.this.startActivity(ctf_intent);
            }
        });
        this.m_btn3.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.WebViewFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent2 = new Intent(WebViewFragment.this.getActivity(), (Class<?>) WebViewXSSActivity.class);
                WebViewFragment.this.startActivity(ctf_intent2);
            }
        });
        float xss_score = this.sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float webview_score = this.sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        String xss_string = Float.toString(xss_score);
        if (xss_string.equals("6.25")) {
            this.m_btn3.setEnabled(false);
            this.m_btn3.setText("Done");
        }
        String web_string = Float.toString(webview_score);
        if (web_string.equals("6.25")) {
            this.m_btn1.setEnabled(false);
            this.m_btn1.setText("Done");
        }
        return view;
    }
}

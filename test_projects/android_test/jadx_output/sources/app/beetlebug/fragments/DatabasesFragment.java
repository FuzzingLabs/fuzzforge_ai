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
import app.beetlebug.ctf.FirebaseDatabaseActivity;
import app.beetlebug.ctf.SQLInjectionActivity;

/* loaded from: classes8.dex */
public class DatabasesFragment extends Fragment {
    ImageView m_back;
    Button m_btn;
    Button m_btn2;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_databases, container, false);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button2);
        ImageView imageView = (ImageView) view.findViewById(C0572R.id.back);
        this.m_back = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.DatabasesFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(DatabasesFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                DatabasesFragment.this.startActivity(ctf_intent);
            }
        });
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.DatabasesFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(DatabasesFragment.this.getActivity(), (Class<?>) SQLInjectionActivity.class);
                DatabasesFragment.this.startActivity(ctf_intent);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.DatabasesFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(DatabasesFragment.this.getActivity(), (Class<?>) FirebaseDatabaseActivity.class);
                DatabasesFragment.this.startActivity(ctf_intent);
            }
        });
        float firebase_score = this.sharedPreferences.getFloat("ctf_score_firebase", 0.0f);
        float sqli_score = this.sharedPreferences.getFloat("ctf_score_sqli", 0.0f);
        String sqli_string = Float.toString(sqli_score);
        if (sqli_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        String web_string = Float.toString(firebase_score);
        if (web_string.equals("6.25")) {
            this.m_btn2.setEnabled(false);
            this.m_btn2.setText("Done");
        }
        return view;
    }
}

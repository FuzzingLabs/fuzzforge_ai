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
import app.beetlebug.ctf.InsecureStorageExternal;
import app.beetlebug.ctf.InsecureStorageSQLite;
import app.beetlebug.ctf.InsecureStorageSharedPref;

/* loaded from: classes9.dex */
public class InsecureStorageFragmentHome extends Fragment {
    Button m_btn;
    Button m_btn2;
    Button m_btn3;
    ImageView m_btn_back;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_insecure_storage_home, container, false);
        this.sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.m_btn_back = (ImageView) view.findViewById(C0572R.id.back);
        this.m_btn = (Button) view.findViewById(C0572R.id.button);
        this.m_btn2 = (Button) view.findViewById(C0572R.id.button2);
        this.m_btn3 = (Button) view.findViewById(C0572R.id.button3);
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.InsecureStorageFragmentHome.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(InsecureStorageFragmentHome.this.getActivity(), (Class<?>) InsecureStorageSharedPref.class);
                InsecureStorageFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn2.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.InsecureStorageFragmentHome.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(InsecureStorageFragmentHome.this.getActivity(), (Class<?>) InsecureStorageExternal.class);
                InsecureStorageFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn3.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.InsecureStorageFragmentHome.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(InsecureStorageFragmentHome.this.getActivity(), (Class<?>) InsecureStorageSQLite.class);
                InsecureStorageFragmentHome.this.startActivity(i);
            }
        });
        this.m_btn_back.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.home.InsecureStorageFragmentHome.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(InsecureStorageFragmentHome.this.getActivity(), (Class<?>) MainActivity.class);
                InsecureStorageFragmentHome.this.startActivity(i);
            }
        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.sharedPreferences = sharedPreferences;
        float shared_pref_score = sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        String pref_string = Float.toString(shared_pref_score);
        if (pref_string.equals("6.25")) {
            this.m_btn.setEnabled(false);
            this.m_btn.setText("Done");
        }
        float external_str_score = this.sharedPreferences.getFloat("ctf_score_external", 0.0f);
        String exter_string = Float.toString(external_str_score);
        if (exter_string.equals("6.25")) {
            this.m_btn2.setEnabled(false);
            this.m_btn2.setText("Done");
        }
        float sqlite_str_score = this.sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        String sqlite_string = Float.toString(sqlite_str_score);
        if (sqlite_string.equals("6.25")) {
            this.m_btn3.setEnabled(false);
            this.m_btn3.setText("Done");
        }
        return view;
    }
}

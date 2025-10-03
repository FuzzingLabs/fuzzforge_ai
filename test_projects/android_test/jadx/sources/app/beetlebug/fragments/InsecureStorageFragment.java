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
import app.beetlebug.ctf.InsecureStorageExternal;
import app.beetlebug.ctf.InsecureStorageSQLite;
import app.beetlebug.ctf.InsecureStorageSharedPref;

/* loaded from: classes8.dex */
public class InsecureStorageFragment extends Fragment {
    ImageView mBackButton;
    Button mButton;
    Button mButton3;
    Button mButton4;
    TextView mCtfTitle;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(C0572R.layout.fragment_insecure_storage, container, false);
        this.mCtfTitle = (TextView) view.findViewById(C0572R.id.ctfTitle);
        this.mButton = (Button) view.findViewById(C0572R.id.button);
        this.mButton3 = (Button) view.findViewById(C0572R.id.button3);
        this.mButton4 = (Button) view.findViewById(C0572R.id.button4);
        this.mBackButton = (ImageView) view.findViewById(C0572R.id.back);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("flag_scores", 0);
        this.sharedPreferences = sharedPreferences;
        float shared_pref_score = sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        String pref_string = Float.toString(shared_pref_score);
        if (pref_string.equals("6.25")) {
            this.mButton.setEnabled(false);
            this.mButton.setText("Done");
        }
        float external_str_score = this.sharedPreferences.getFloat("ctf_score_external", 0.0f);
        String exter_string = Float.toString(external_str_score);
        if (exter_string.equals("6.25")) {
            this.mButton4.setEnabled(false);
            this.mButton4.setText("Done");
        }
        float sqlite_str_score = this.sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        String sqlite_string = Float.toString(sqlite_str_score);
        if (sqlite_string.equals("6.25")) {
            this.mButton3.setEnabled(false);
            this.mButton3.setText("Done");
        }
        this.mBackButton.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.InsecureStorageFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(InsecureStorageFragment.this.getActivity(), (Class<?>) FlagsOverview.class);
                InsecureStorageFragment.this.startActivity(ctf_intent);
            }
        });
        this.mButton.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.InsecureStorageFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(InsecureStorageFragment.this.getActivity(), (Class<?>) InsecureStorageSharedPref.class);
                InsecureStorageFragment.this.startActivity(ctf_intent);
            }
        });
        this.mButton3.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.InsecureStorageFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent2 = new Intent(InsecureStorageFragment.this.getActivity(), (Class<?>) InsecureStorageSQLite.class);
                InsecureStorageFragment.this.startActivity(ctf_intent2);
            }
        });
        this.mButton4.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.InsecureStorageFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent3 = new Intent(InsecureStorageFragment.this.getActivity(), (Class<?>) InsecureStorageExternal.class);
                InsecureStorageFragment.this.startActivity(ctf_intent3);
            }
        });
        return view;
    }
}

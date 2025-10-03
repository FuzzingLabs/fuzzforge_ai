package app.beetlebug.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import app.beetlebug.C0572R;
import app.beetlebug.ctf.b33tleAdministrator;
import app.beetlebug.p001db.DatabaseHelper;

/* loaded from: classes8.dex */
public class AddUserFragment extends Fragment {
    Button add_user;
    ImageView back_btn;

    /* renamed from: db */
    private DatabaseHelper f113db;
    EditText pass;
    EditText username;

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(C0572R.layout.fragment_add_user, container, false);
        this.add_user = (Button) view.findViewById(C0572R.id.addUser);
        this.back_btn = (ImageView) view.findViewById(C0572R.id.back);
        this.username = (EditText) view.findViewById(C0572R.id.editTextUsername);
        this.pass = (EditText) view.findViewById(C0572R.id.editTextPassword);
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        this.f113db = databaseHelper;
        databaseHelper.open();
        this.add_user.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AddUserFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                EditText edt = (EditText) view.findViewById(C0572R.id.editTextUsername);
                EditText pass = (EditText) view.findViewById(C0572R.id.editTextPassword);
                String edt_username = edt.getText().toString();
                String edt_pass = pass.getText().toString();
                if (edt_username.isEmpty()) {
                    edt.setError("Username is empty");
                } else if (!edt_pass.isEmpty()) {
                    AddUserFragment.this.f113db.add(edt_username, edt_pass);
                    Toast.makeText(AddUserFragment.this.getActivity(), "User Added Successfully", 1).show();
                } else {
                    pass.setError("Password is required");
                }
            }
        });
        this.back_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.fragments.AddUserFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent ctf_intent = new Intent(AddUserFragment.this.getActivity(), (Class<?>) b33tleAdministrator.class);
                AddUserFragment.this.startActivity(ctf_intent);
            }
        });
        return view;
    }
}

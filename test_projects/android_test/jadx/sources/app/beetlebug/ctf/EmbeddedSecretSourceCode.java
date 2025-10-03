package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;

/* loaded from: classes6.dex */
public class EmbeddedSecretSourceCode extends AppCompatActivity {
    public String beetle_bug_shop_promo_code = "beetle1759";
    Button m_button;
    TextView m_price;
    EditText m_promo;
    Button m_purchase;
    SharedPreferences sharedPreferences;
    public static String flag_scores = "flag_scores";
    public static String ctf_score_secret_source = "ctf_score_secret_source";

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_embedded_secret_source_code);
        this.m_promo = (EditText) findViewById(C0572R.id.editTextPromoCode);
        this.m_button = (Button) findViewById(C0572R.id.button);
        this.m_purchase = (Button) findViewById(C0572R.id.buttonPurchase);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        this.m_purchase.setVisibility(8);
        this.m_button.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.EmbeddedSecretSourceCode.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                EmbeddedSecretSourceCode embeddedSecretSourceCode = EmbeddedSecretSourceCode.this;
                embeddedSecretSourceCode.m_price = (TextView) embeddedSecretSourceCode.findViewById(C0572R.id.textViewPrice);
                if (EmbeddedSecretSourceCode.this.m_promo.getText().toString().equals(EmbeddedSecretSourceCode.this.beetle_bug_shop_promo_code)) {
                    String price = EmbeddedSecretSourceCode.this.m_price.getText().toString();
                    int price_int = Integer.parseInt(price);
                    int new_price = price_int / 2;
                    String s = Integer.toString(new_price);
                    EmbeddedSecretSourceCode.this.m_price.setText(s);
                    Toast.makeText(EmbeddedSecretSourceCode.this, "Your new price is: " + new_price, 1).show();
                    final float user_score_secret_source = 6.25f;
                    SharedPreferences.Editor editor = EmbeddedSecretSourceCode.this.sharedPreferences.edit();
                    editor.putFloat(EmbeddedSecretSourceCode.ctf_score_secret_source, 6.25f);
                    editor.apply();
                    EmbeddedSecretSourceCode.this.m_purchase.setVisibility(0);
                    EmbeddedSecretSourceCode.this.m_purchase.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.EmbeddedSecretSourceCode.1.1
                        @Override // android.view.View.OnClickListener
                        public void onClick(View v2) {
                            Intent ctf_captured = new Intent(EmbeddedSecretSourceCode.this, (Class<?>) FlagCaptured.class);
                            String intent_secret_str = Float.toString(user_score_secret_source);
                            ctf_captured.putExtra("intent_str", intent_secret_str);
                            EmbeddedSecretSourceCode.this.startActivity(ctf_captured);
                        }
                    });
                    return;
                }
                Toast.makeText(EmbeddedSecretSourceCode.this, "Wrong discount code", 1).show();
            }
        });
    }

    public void purchaseItem(View view) {
        Intent i = new Intent(this, (Class<?>) FlagCaptured.class);
        startActivity(i);
    }
}

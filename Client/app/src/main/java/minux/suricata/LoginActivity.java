package minux.suricata;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends BaseActivity {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        /* 부제목 */
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("with Raspberry PI");
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#C7053D")), 5, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) findViewById(R.id.tv_login_intro)).setText(spannableStringBuilder);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        etEmail = (EditText) findViewById(R.id.et_login_email);
        etPassword = (EditText) findViewById(R.id.et_login_password);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.b_login_exit:
                        finish();
                        break;
                    case R.id.b_login_signin:
                        logIn();
                }
            }
        };
        findViewById(R.id.b_login_exit).setOnClickListener(onClickListener);
        findViewById(R.id.b_login_signin).setOnClickListener(onClickListener);
    }

    private void logIn() {
        if (!isValidateForm()) {
            return;
        }

        showProgressDialog();
        final String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
                etEmail.setText("");
                etPassword.setText("");
                if (task.isSuccessful()) {
                    databaseReference.child("user").child(getUid()).child("logon").setValue("true");
                    databaseReference.child("user").child(getUid()).child("preference").child("email").setValue(email);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError("Required");
            result = false;
        } else {
            etEmail.setError(null);
        }
        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError("Required");
            result = false;
        } else {
            etPassword.setError(null);
        }

        return result;
    }
}
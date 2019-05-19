package com.example.avatar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avatar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {
    @BindView(R.id.edt_email)
    EditText mEmail;
    @BindView(R.id.edt_pass)
    EditText mPass;
    @BindView(R.id.btn_signup)
    Button mSignup;
    @BindView(R.id.btn_signin)
    Button mSignin;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            updateUIFireBase();
        }
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dangKy();
            }
        });
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this,SigninActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void dangKy(){
        String email = mEmail.getText().toString();
        String pass = mPass.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(SignupActivity.this,"Đăng ký thành công",Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignupActivity.this,"Đăng ký thất bại",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){

    }

    private void updateUIFireBase(){
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
    }
}

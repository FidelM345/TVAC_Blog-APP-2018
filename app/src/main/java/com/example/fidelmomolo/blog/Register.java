package com.example.fidelmomolo.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity implements View.OnClickListener {


    EditText reg_email,reg_password,reg_confirm;
    Button reg_register,reg_account;
    ProgressBar progressBar;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

          mAuth=FirebaseAuth.getInstance();

          reg_email=findViewById(R.id.reg_email);
          reg_password=findViewById(R.id.reg_password);
          reg_confirm=findViewById(R.id.reg_confirm);
          reg_register=findViewById(R.id.reg_button);
          reg_account =findViewById(R.id.reg_Account);
          progressBar =findViewById(R.id.progressBar);

          reg_register.setOnClickListener(this);
          reg_account.setOnClickListener(this);

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if (currentUser !=null){

            goToMainActivity();
        }

    }


    private void goToMainActivity() {

        Intent intent=new Intent(Register.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {



        switch (view.getId()){

            case R.id.reg_button:


                String email=reg_email.getText().toString().trim();
                String pass=reg_password.getText().toString().trim();
                String pass1=reg_confirm.getText().toString().trim();

                if (!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(pass) &&!TextUtils.isEmpty(pass1)){

                    if (pass.equals(pass1)){
                        //checks whether the two entered password are the same


                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){


                                    gotoSetupActivity();


                                   // progressBar.setVisibility(View.INVISIBLE);
                                }
                                else {

                                    String error=task.getException().getMessage();
                                    Toast.makeText(Register.this, "Error: "+error, Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    }else {
                        Toast.makeText(this, "Please enter a Valid Password", Toast.LENGTH_LONG).show();
                    }
                }


                default:

                   //gotoLoginActivity();

                   finish();// takes you back to the Login Activity


        }





    }

    private void gotoLoginActivity() {
        Intent intent=new Intent(Register.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoSetupActivity() {
        Intent intent=new Intent(Register.this,SetupActivity.class);
        startActivity(intent);
       finish();
    }
}

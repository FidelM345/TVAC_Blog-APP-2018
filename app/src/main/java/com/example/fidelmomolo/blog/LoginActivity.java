package com.example.fidelmomolo.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

  private EditText login_email,login_password;
  private Button login_reg,login_button;
  FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();//creating an instance of Firebase authenticator class

        login_email=findViewById(R.id.login_email);
        login_password=findViewById(R.id.login_password);
        login_reg=findViewById(R.id.login_reg);
        login_button=findViewById(R.id.login_button);

         login_button.setOnClickListener(this);
         login_reg.setOnClickListener(this);




    }


    @Override
    protected void onStart() {
        super.onStart();

         FirebaseUser user = mAuth.getCurrentUser();// getting details of the current user from the authenticator class

        //if the user is logged in it takes him to the main activity
        if (user!=null){

            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

    }



    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.login_button){



          String email=login_email.getText().toString().trim();
          String pass=login_password.getText().toString().trim();

          if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(pass)){
              //checks whether the password and email field are empty

              mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                  signs in with email and password then the Complete listener methods checks whether the login is successful
//                  or not


                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {

                      if(task.isSuccessful()){

                      gotoMain();

                      }
                      else {

                          //gets the error message and stores it in variable error
                          String error=task.getException().getMessage();
                          Toast.makeText(LoginActivity.this, "Error: "+error, Toast.LENGTH_LONG).show();

                      }

                  }
              });


          }


        }else {
            Intent intent=new Intent(LoginActivity.this,Register.class);
            startActivity(intent);

        }

    }


    private void gotoMain() {

        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }




}

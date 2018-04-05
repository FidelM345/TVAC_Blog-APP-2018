package com.example.fidelmomolo.blog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

     Toolbar toolbar1;
     FirebaseAuth mAuth;
     FloatingActionButton floatingActionButton;
     FirebaseFirestore firestore;
     String current_user_id;
     BottomNavigationView bottomNavigationView;
     HomeFragment homeFragment;
     AccountFragment accountFragment;
     NotificationFragment notificationFragment;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         mAuth=FirebaseAuth.getInstance();
         firestore=FirebaseFirestore.getInstance();

        toolbar1=findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar1);
        getSupportActionBar().setTitle("The Beast");

        bottomNavigationView =findViewById(R.id.bottom_navigation);

        //FRAGMENTS intialization
         homeFragment=new HomeFragment();
         accountFragment=new AccountFragment();
         notificationFragment=new NotificationFragment();



             replaceFragment(homeFragment);//loading the home fragment in main activity

             bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                 //the listener handles the bottom navigation button clicks
                 @Override
                 public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                     switch (item.getItemId()){
                         case R.id.bottom_home:
                             replaceFragment(homeFragment);
                             return true;
                         case R.id.bottom_notification:
                             replaceFragment(notificationFragment);
                             return true;
                         case R.id.bottom_account:
                             replaceFragment(accountFragment);
                             return true;

                         default:
                             return false;
                     }


                 }
             });






         floatingActionButton=findViewById(R.id.add_post);
         floatingActionButton.setOnClickListener(this);



    }

     @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



        if (user == null) {

            sendToLogin();//sends user to the Login page if they are not signed in

            //Toast.makeText(this, "The Beast", Toast.LENGTH_LONG).show();
      }

        else {

             current_user_id=user.getUid();

             firestore.collection("User_Details").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.isSuccessful()){

                         if(!task.getResult().exists()){
                             //if the the profile details does not exist take user to the setup activity
                             Intent intent=new Intent(MainActivity.this,SetupActivity.class);
                             startActivity(intent);
                             finish();

                         }


                     }else{

                         String exception=task.getException().getMessage();
                         Toast.makeText(MainActivity.this, "MainActivity Error: "+exception, Toast.LENGTH_LONG).show();

                     }

                 }
             });

         }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    //the method inflates the menu items from the menu file
         getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

  /*  @Override
    protected void onDestroy() {
        super.onDestroy();


        firestore.getFirestoreSettings()
    }
*/
    @Override
     public boolean onOptionsItemSelected(MenuItem item) {

         if(item.getItemId()==R.id.action_logout){
             finish();
            // mAuth.signOut();
             sendToLogin();

         }

        if(item.getItemId()==R.id.action_settings){

            sendToSetUpActivity();
        }


         return true;
    }


     private void sendToSetUpActivity() {
          Intent intent=new Intent(MainActivity.this,SetupActivity.class);
          startActivity(intent);


      }

     private void sendToLogin() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();//ensures user does not go back by pressing back button

    }

     @Override
     public void onClick(View v) {

         switch (v.getId()){

             case R.id.add_post:
                 Intent intent=new Intent(MainActivity.this,NewPostActivity.class);
                 startActivity(intent);

         }
    }


    private  void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();

    }

}



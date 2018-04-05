package com.example.fidelmomolo.blog;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

     Toolbar toolbar;
     CircleImageView circleImageView;
     Uri mainImageUri;
     EditText editText;
     Button button;
     FirebaseUser currentUser;
     StorageReference mStorageRef;
     FirebaseAuth mAuth;
     ProgressBar progressBar;
     FirebaseFirestore firestore;
     String user_id;
     boolean isChaanged=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        //initializing the components
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mainImageUri=null;
        firestore=FirebaseFirestore.getInstance();

        toolbar=findViewById(R.id.setup_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Info");

        circleImageView=findViewById(R.id.profile_image);
        editText =findViewById(R.id.setup_name);
        button =findViewById(R.id.setup_buttton);
        progressBar=findViewById(R.id.setup_brogressbar);



        circleImageView.setOnClickListener(this);
        button.setOnClickListener(this);



        currentUser = mAuth.getCurrentUser();
        user_id=currentUser.getUid();//get the currently logged in user ID

        progressBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);//disables the setup button

        firestore.collection("User_Details").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()){
                        //checks whether data exists in given path or database

                        //retrieving results fetched from the database
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");

                        mainImageUri=Uri.parse(image); //converts string to Uri and stores it in the variable
                        editText.setText(name);
                        RequestOptions placeHolder=new RequestOptions();
                        placeHolder.placeholder(R.drawable.profile_image);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolder).load(image).into(circleImageView);
                    }


                }else{
                    String exception=task.getException().getMessage();

                    Toast.makeText(SetupActivity.this, "Data Retreival Error is: "+exception, Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                button.setEnabled(true);//re enables the setup button
            }
        });


    }




    @Override
    public void onClick(View view) {
        switch (view.getId()){
             // when the profile image is clicked do the following
            case R.id.profile_image:

                  // the first if statement checks whether the user is running android Mash mellow and above
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){

                      if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        // if the permission has been denied allow user to request for the permission

                          ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                      }else{

                          Toast.makeText(this, "The Beast", Toast.LENGTH_LONG).show();

                          CropImage.activity()
                                  .setGuidelines(CropImageView.Guidelines.ON)
                                  .setAspectRatio(1,1)
                                  .start(this);

                      }
                }else{

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(this);
                }

                break;

                //when the button is clicked do the following
              case  R.id.setup_buttton:
                  final String user_name=editText.getText().toString().trim();


                  if(isChaanged){ //becomes true when new image is selected

                      progressBar.setVisibility(View.VISIBLE);

                      if (!TextUtils.isEmpty(user_name) && mainImageUri !=null){


                    StorageReference image_path=mStorageRef.child("Profile_image").child(user_id+".jpg");

                    image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()){


                                storeFirestore(task,user_name);//used for storing image and the user name in firebase
                                Toast.makeText(SetupActivity.this, "Your Accounts settings have been updated", Toast.LENGTH_LONG).show();
                                Intent intent=new Intent(SetupActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }else {

                                String exception=task.getException().getMessage();

                                Toast.makeText(SetupActivity.this, "Image Error is: "+exception, Toast.LENGTH_LONG).show();
                            }

                        }
                    });



                }

                  }else {
                      storeFirestore(null,user_name);
                  }

        }

    }
    //used for storing image and the user name in firebase
    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task,String user_name) {
         Uri download_uri;

        if (task!=null){
            download_uri=task.getResult().getDownloadUrl();

        }else {
            download_uri=mainImageUri;

        }

        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(SetupActivity.this, "The image has been uploaded", Toast.LENGTH_LONG).show();

        Map<String,String>user_details=new HashMap<>();
        user_details.put("name",user_name);
        user_details.put("image",download_uri.toString());

         firestore.collection("User_Details").document(user_id).set(user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){


                    gotMainActivity();

                }else{

                    String exception=task.getException().getMessage();

                    Toast.makeText(SetupActivity.this, "Text Error is: "+exception, Toast.LENGTH_LONG).show();
                }

            }
        });


    }


    private void gotMainActivity() {

        Intent intent=new Intent(SetupActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    //handles the results of the athur image picker and sets the image on the circular image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(requestCode, resultCode, data); optional not madatory code

             if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage. getActivityResult(data);
             if (resultCode == RESULT_OK) {

                mainImageUri= result.getUri();
                circleImageView.setImageURI(mainImageUri);
                isChaanged=true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }


    }
}

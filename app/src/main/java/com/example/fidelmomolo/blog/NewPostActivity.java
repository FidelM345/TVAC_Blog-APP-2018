package com.example.fidelmomolo.blog;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MAX_LENGTH =100 ;
    Toolbar toolbar;
    Button post_button;
    EditText post_edit;
    ImageView post_image;
    Uri mainImageUri=null;
    ProgressBar progressBar;
    FirebaseFirestore firestore;
    StorageReference firebaseStorage;
    FirebaseAuth mAuth;
    Bitmap compressedImageBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        firebaseStorage=FirebaseStorage.getInstance().getReference();

        firestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();

        toolbar=findViewById(R.id.post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setTitle("The Beast");

         post_button=findViewById(R.id.post_publish);
         post_edit  =findViewById(R.id.post_description);
         post_image=findViewById(R.id.post_image);
         progressBar=findViewById(R.id.progressBar_Post);

         post_image.setOnClickListener(this);
         post_button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.post_image:
                //do the following when image is clicked
                aurtherImagePicker(); //method used for picking images easily
             break;

            case R.id.post_publish:
                //do the following when publish button is clicked

                final String description=post_edit.getText().toString().trim();
                      final String current_user_id=mAuth.getCurrentUser().getUid();

                if(!TextUtils.isEmpty(description)&& mainImageUri !=null){

                    progressBar.setVisibility(View.VISIBLE);

                    final String randomName=UUID.randomUUID().toString();//generates random strings


                    final StorageReference storageReference=firebaseStorage.child("Blog_images").child(randomName+".jpg");
                    storageReference.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {


                            if(task.isSuccessful()){

                                final String download_uri=task.getResult().getDownloadUrl().toString();

                                File actualImageFile=new File(mainImageUri.getPath());


                                try {
                                    compressedImageBitmap = new Compressor(NewPostActivity.this)
                                            //compressing image of high quality to a thumbnail bitmap for faster loading
                                            .setMaxWidth(60)
                                            .setMaxHeight(60)
                                            .setQuality(5)
                                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                            .compressToBitmap(actualImageFile);


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //compressing image of high quality to a thumbnail bitmap for faster loading
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();




                                UploadTask thumbfilepath=firebaseStorage.child("Blog_images/thumbs").child(randomName+".jpg").putBytes(data);

                                thumbfilepath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String thumbnail_uri=taskSnapshot.getDownloadUrl().toString();
                                        //if thumbnail has been uploaded successfully do the following
                                        Map<String,Object>contents=new HashMap<>();
                                        contents.put("description",description);
                                        contents.put("imageUri",download_uri);
                                        contents.put("thumbUri",thumbnail_uri);
                                        contents.put("user_id",current_user_id);
                                        contents.put("timestamp", FieldValue.serverTimestamp());

                                        firestore.collection("Posts").add(contents).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(NewPostActivity.this, "Your blog has been posted successfully ", Toast.LENGTH_LONG).show();
                                                    Intent intent=new Intent(NewPostActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();



                                                }else{
                                                    String exception=task.getException().getMessage();

                                                    Toast.makeText(NewPostActivity.this, "DESCRIPTION Error is: "+exception, Toast.LENGTH_LONG).show();
                                                }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        String exception=task.getException().getMessage();

                                        Toast.makeText(NewPostActivity.this, "Thumbnail Error is: "+exception, Toast.LENGTH_LONG).show();

                                            }
                                        });


                                    }
                                });

                            }else{

                                 progressBar.setVisibility(View.INVISIBLE);

                                 String exception=task.getException().getMessage();

                                 Toast.makeText(NewPostActivity.this, "IMAGE Error is: "+exception, Toast.LENGTH_LONG).show();

                            }

                        }
                    });




                }else{}

                break;

            default:

        }
    }

    //method used for picking images easily
    private void aurtherImagePicker() {

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(NewPostActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                // if the permission has been denied allow user to request for the permission

                ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},1);

            }else{



                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(4,3)
                        .setMinCropResultSize(512,512)
                        .start(this);

            }
        }else{
             //do the following if the android OS is less than mashmellow or android 6.0
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
    }


    //handles the results of the athur image picker and sets the image on the circular image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage. getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri= result.getUri();
                post_image.setImageURI(mainImageUri);


            }  else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }



}

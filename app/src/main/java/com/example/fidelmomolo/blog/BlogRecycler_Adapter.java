package com.example.fidelmomolo.blog;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Fidel M Omolo on 3/31/2018.
 */

public class BlogRecycler_Adapter extends RecyclerView.Adapter<BlogRecycler_Adapter.ViewHolder> {
     List<BlogPost_model_class>bloglist;
     Context context;
     FirebaseFirestore firestore;
     FirebaseAuth mAuth;

    public BlogRecycler_Adapter(List<BlogPost_model_class>bloglist) {
        //the constructor is receiving data from the list data structure in HomeFragment java class
        this.bloglist=bloglist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         //it inflates the custom made Layout file for list items
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=parent.getContext();
        firestore=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);



    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

         holder.setIsRecyclable(false);//makes the recycler views not to be recycled
        //gets data stored in the bloglist List data structure, the getDescription model class is found in the model class
         String description_data=bloglist.get(position).getDescription();
         String image_url=bloglist.get(position).getImageUri();
         String thumb_uri=bloglist.get(position).getThumbUri();
         /*long millisends=bloglist.get(position).getTimestamp().getTime();
         String dateString= DateFormat.format("MM/dd/yyy",new Date(millisends)).toString();*/

         String dateString="14/015/12";
         //likes features
         final String blogpostid=bloglist.get(position).BlogPostIdString;//gets the blog post id
         final String currentUserId=mAuth.getCurrentUser().getUid();


        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);


         //changes the color of the like button
        firestore.collection("Posts").document(blogpostid).collection("Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                //he has liked the post
                if(documentSnapshot.exists()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.blog_image_like_btn.setImageDrawable(context.getDrawable(R.mipmap.icon_dislike));
                    }
                }else{

                    //he has disliked the post

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        holder.blog_image_like_btn.setImageDrawable(context.getDrawable(R.mipmap.icon_like));
                    }

                }
            }
        });



        //updates the number of likes
        firestore.collection("Posts").document(blogpostid).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()){

                    //if the Likes collection is not empty do the following

                    int number_of_likes=queryDocumentSnapshots.size();

                    holder.updateCount(number_of_likes);

                }
                else {

                    //if the Likes collection if empty do the following
                    holder.updateCount(0);

                }

            }
        });








         holder.blog_image_like_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {






                 firestore.collection("Posts").document(blogpostid).collection("Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                         if (task.isSuccessful()){

                         if(!task.getResult().exists()){
                             //if the like does not exist do the following
                             Map<String,Object>likeMap=new HashMap<>();
                             likeMap.put("TimeStamp", FieldValue.serverTimestamp());

                             firestore.collection("Posts").document(blogpostid).collection("Likes").document(currentUserId).set(likeMap);

                         }

                         else {



                             firestore.collection("Posts").document(blogpostid).collection("Likes").document(currentUserId).delete();

                         }




                     }else {

                             String exception=task.getException().getMessage();

                             Toast.makeText(context, "Text Error is: "+exception, Toast.LENGTH_LONG).show();


                         }

                     }


                 });






             }
         });


         String user_id=bloglist.get(position).getUser_id();
         firestore.collection("User_Details").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                 if (task.isSuccessful()){



                     String userName=task.getResult().getString("name");
                     String image1=task.getResult().getString("image");

                     holder.setUserData(image1,userName);


                 }


                 else {

                     String exception=task.getException().getMessage();

                     Toast.makeText(context, "Text Error is: "+exception, Toast.LENGTH_LONG).show();

                 }


             }
         });


         //it accesses the setDescription(description_data) method from view holder class
         holder.setDescription(description_data);
         holder.setBlogImage(image_url,thumb_uri);
         holder.setTime(dateString);

    }

    @Override
    public int getItemCount() {

        return bloglist.size();//number of items to be populated in the recycler view
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView description,blogdate;
        ImageView imageView;
        TextView profile_name;
        CircleImageView profile_pic;
        ImageView blog_image_like_btn;
        TextView blog_image_like_count;




        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;

            blog_image_like_btn=mView.findViewById(R.id.blog_like_btn);

        }

        public  void setDescription(String descriptionText){

            description=mView.findViewById(R.id.blog_description);
            description.setText(descriptionText);
        }


        public void setBlogImage(String downloadUri,String Thumburi){

            imageView=mView.findViewById(R.id.blog_post_image);

            RequestOptions placeHolder=new RequestOptions();
            placeHolder.placeholder(R.drawable.add_image);

            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(downloadUri).thumbnail(
                    //loads the thumbnail if the image has not loaded first
                    Glide.with(context).load(Thumburi)
            ).into(imageView);


        }



        public void setTime(String date1){
            blogdate=mView.findViewById(R.id.blog_date);
            blogdate.setText(date1);

        }


        public void setUserData(String image,String name){
            profile_name=mView.findViewById(R.id.blog_user_name);
            profile_pic=mView.findViewById(R.id.blog_user_image);

            profile_name.setText(name);

            RequestOptions placeHolder=new RequestOptions();
            placeHolder.placeholder(R.drawable.profile_image);
            Glide.with(context).setDefaultRequestOptions(placeHolder).load(image).into(profile_pic);



        }


        public void updateCount(int count){

         blog_image_like_count=mView.findViewById(R.id.blog_like_count);
         blog_image_like_count.setText(count+" Likes");

        }




    }
}

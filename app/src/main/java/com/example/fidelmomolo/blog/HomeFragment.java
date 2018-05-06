package com.example.fidelmomolo.blog;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    RecyclerView blog_list_view;
    List<BlogPost_model_class>bloglist; //list of type blog post model class.
    FirebaseFirestore firestore;
    BlogRecycler_Adapter blogRecycler_adapter;
    FirebaseAuth mAuth;
    DocumentSnapshot lastVisible;
    Boolean isFirstPageLoad=true;//true when data is loaded for the first time

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home, container, false);

         mAuth=FirebaseAuth.getInstance();

         bloglist=new ArrayList<>();

         blogRecycler_adapter=new BlogRecycler_Adapter(bloglist);//initializing adapter

         blog_list_view=view.findViewById(R.id.blog_recycler_view);
         blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
         blog_list_view.setAdapter(blogRecycler_adapter);






        return view;//returns the inflated view
    }

    public void loadmorePost(){


        Query firstQuery=firestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        //addsnapshot handles real time data
        firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            //the addSnapshot Listener handles data in real time
            //getActivity() helps prevent crashes stops
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {


                if (!queryDocumentSnapshots.isEmpty()){

                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            String PostId=documentChange.getDocument().getId();

                            //converts data feteched from firebase into a form similar to the model class created
                            BlogPost_model_class blogPost_model_class = documentChange.getDocument().toObject(BlogPost_model_class.class).withId(PostId);
                            bloglist.add(blogPost_model_class); //data added to the List data structure the process is repeated
                            //Every time new data is received

                            blogRecycler_adapter.notifyDataSetChanged();//notify adapter when data set is changed

                        }

                    }

                }


            }
        });


    }


    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null) {
            firestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                //listener has been used for pagination
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom=!recyclerView.canScrollVertically(1);//positive one means top to bottom direction

                    if (reachedBottom){

                        String description=lastVisible.getString("description");
                        Toast.makeText(getActivity(), "Reached"+description, Toast.LENGTH_LONG).show();
                        loadmorePost();
                    }
                }
            });

            Query firstQuery=firestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);//used for pagination purposes

            //addsnapshot handles real time data
            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                //the addSnapshot Listener handles data in real time
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                    if (isFirstPageLoad) {
                        //ensures the last data to be added is always at the
                        //top of the recycler view

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);//pagination
                    }

                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            String PostId=documentChange.getDocument().getId();

                            //converts data feteched from firebase into a form similar to the model class created
                            BlogPost_model_class blogPost_model_class = documentChange.getDocument().toObject(BlogPost_model_class.class).withId(PostId);


                            if (isFirstPageLoad) {
                                bloglist.add(blogPost_model_class); //data added to the List data structure the process is repeated
                                //Every time new data is received
                            }

                            else{

                                bloglist.add(0,blogPost_model_class); //ensures the last data to be added is always at the
                                //top of the recycler view
                            }


                            blogRecycler_adapter.notifyDataSetChanged();//notify adapter when data set is changed

                        }

                    }

                    isFirstPageLoad=false; //just after the first page is loaded set it to false


                }
            });

        }




    }
}

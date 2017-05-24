package edu.uncc.chatapplication;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatroomActivity extends AppCompatActivity {

    ImageView sendMessageButton, addPhotosbutton;
    private DatabaseReference mDatabase;
    String currentID, imageURL, tripID;
    private FirebaseAuth mAuth;
    EditText messageText;
    public static int REQUEST_IMAGE = 11;
//    public static int RESULT_OK = 1;
    ScrollView scrollViewMessages;
    LinearLayout scrollLinearLayout;
    List<User> userListList;
    List<Trip> tripList;
    Trip currentTrip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        this.setTitle("ChatRoom");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        currentID = mAuth.getCurrentUser().getUid();

        messageText = (EditText) findViewById(R.id.editTextMessageInput);
        scrollViewMessages = (ScrollView) findViewById(R.id.scrollViewMessages);
        scrollLinearLayout = (LinearLayout) findViewById(R.id.scrollViewLinearLayout);

        if (getIntent().getExtras() != null){
            tripID = getIntent().getExtras().getString(CreateTripActivity.TRIP_ID);
        }

        sendMessageButton = (ImageView) findViewById(R.id.imageViewSendButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
//                String format = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//                String uuid = UUID.randomUUID().toString();
//                String key = mDatabase.child("Messages").child(tripID).push().getKey();
                ChatMessage newMessage = new ChatMessage(messageText.getText().toString(), imageURL, currentID, null,null);

                ArrayList<String> tripMembersList = currentTrip.getMemberList();
                for (int i = 0; i < tripMembersList.size(); i++){
                    String userID = tripMembersList.get(i);
                    String key = mDatabase.child("Messages").child(tripID).child(userID).push().getKey();
                    ChatMessage message = new ChatMessage(messageText.getText().toString(), imageURL, currentID, null,null);
                    message.setMessageID(key);
                    mDatabase.child("Messages").child(tripID).child(userID).child(key).setValue(message);
                }




                messageText.setText("");
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = (View) inflater.inflate(R.layout.sent_message, null);
                TextView userName = (TextView) view.findViewById(R.id.sentUser);
                userName.setText(mAuth.getCurrentUser().getDisplayName());
                TextView textMessage = (TextView) view.findViewById(R.id.textViewSent);
                textMessage.setText(newMessage.getMessageText() + "\n" + newMessage.getMessageTime());
                scrollLinearLayout.addView(view);
            }
        });

        addPhotosbutton = (ImageView) findViewById(R.id.imageViewAddPhotos);
        addPhotosbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Choose Picture"), REQUEST_IMAGE);

            }
        });

        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userListList = new ArrayList<User>();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    userListList.add(child.getValue(User.class));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("Trip").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tripList = new ArrayList<Trip>();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    tripList.add(child.getValue(Trip.class));
                }

                for (int i = 0; i < tripList.size(); i++){
                    Trip trip = tripList.get(i);
                    if (trip.getTripID().equals(tripID)){
                        currentTrip = trip;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        mDatabase.child("Messages").child(tripID).child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<ChatMessage> messageList = new ArrayList<ChatMessage>();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    messageList.add(child.getValue(ChatMessage.class));
                }



                for (int i = 0; i < messageList.size(); i++) {
                    final ChatMessage message = messageList.get(i);
                    String userNameDetails  = "";
                    if (message != null) {
                        for (int m = 0; m < userListList.size(); m++){
                            User user = userListList.get(m);
                            if (user.getUserId().equals(currentID)){
                                StringBuilder builder = new StringBuilder();
                                builder.append(user.getFirstName());
                                builder.append(" ");
                                builder.append(user.getLastName());
                                userNameDetails = builder.toString();
                            }
                        }
                        if (message.getMessageUser().equals(currentID)) {
///////////////////////////sent messages////////////////////////
                            if (message.getImageURL() == null) {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = (View) inflater.inflate(R.layout.sent_message, null);
                                TextView userName = (TextView) view.findViewById(R.id.sentUser);
                                userName.setText(userNameDetails);
                                TextView textMessage = (TextView) view.findViewById(R.id.textViewSent);
                                textMessage.setText(message.getMessageText() + "\n" );
                                view.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        deleteMessage(message);
                                        return true;
                                    }
                                });
                                scrollLinearLayout.addView(view);
                            } else {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = (View) inflater.inflate(R.layout.sent_image, null);
                                TextView userName = (TextView) view.findViewById(R.id.sentUser);
                                userName.setText(userNameDetails);
                                ImageView img = (ImageView) view.findViewById(R.id.sentImage);

                                Picasso.with(getApplicationContext())
                                        .load(message.getImageURL())
                                        .resize(120, 120)
                                        .centerCrop()
                                        .into(img);
//                                Uri uri = Uri.parse(message.getImageURL());
//                                img.setImageURI(uri);

                                view.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        deleteMessage(message);
                                        return true;
                                    }
                                });
                                scrollLinearLayout.addView(view);
                            }
                        } else {

                            if (message.getImageURL() == null) {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = (View) inflater.inflate(R.layout.received_message, null);
                                TextView userName = (TextView) view.findViewById(R.id.receivedUser);
                                userName.setText(userNameDetails);
                                TextView textMessage = (TextView) view.findViewById(R.id.textViewReceived);
                                textMessage.setText(message.getMessageText() + "\n" );

                                view.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        deleteMessage(message);
                                        return true;
                                    }
                                });
                                scrollLinearLayout.addView(view);
                            } else {
                                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View view = (View) inflater.inflate(R.layout.received_image, null);
                                TextView userName = (TextView) view.findViewById(R.id.receivedUser);
                                userName.setText(userNameDetails);
                                ImageView img = (ImageView) view.findViewById(R.id.receivedImage);

                                Picasso.with(getApplicationContext())
                                        .load(message.getImageURL())
                                        .resize(120, 120)
                                        .centerCrop()
                                        .into(img);
//                                Uri uri = Uri.parse(message.getImageURL());
//                                img.setImageURI(uri);

                                view.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        deleteMessage(message);
                                        return true;
                                    }
                                });
                                scrollLinearLayout.addView(view);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Chatroom", "On activity result");
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                if (data != null){
                    Log.d("Chatroom", "Not null");
                    final Uri selectedImage = data.getData();
                    imageURL = selectedImage.toString();

                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(getResources().getString(R.string.storage_ref_url));
                    StorageReference imagesRef = storageRef.child("MessageImages/" + selectedImage.getLastPathSegment());

                    UploadTask uploadTask = imagesRef.putFile(selectedImage);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getApplicationContext(), "Failure on image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.d("image", downloadUrl.toString());
                            imageURL = downloadUrl.toString();

                            ArrayList<String> tripMembersList = currentTrip.getMemberList();
                            for (int i = 0; i < tripMembersList.size(); i++){
                                String userID = tripMembersList.get(i);
                                String key = mDatabase.child("Messages").child(tripID).child(userID).push().getKey();
                                ChatMessage message = new ChatMessage(null, imageURL,  currentID, null, key);
                                mDatabase.child("Messages").child(tripID).child(userID).child(key).setValue(message);
                            }
                        }
                    });
                }
            }
        }
    }

   public void deleteMessage(final ChatMessage message){
       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
       alertDialogBuilder.setMessage("Do you really want to delete this message?");
       alertDialogBuilder.setNegativeButton("Yes",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       mDatabase.child("Messages").child(tripID).child(currentID).child(message.getMessageID()).removeValue();
                   }
               });
       alertDialogBuilder.setPositiveButton("No",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               });
       AlertDialog alertDialog = alertDialogBuilder.create();
       alertDialog.show();

   }


}

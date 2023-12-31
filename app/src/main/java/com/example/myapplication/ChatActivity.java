package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private Button SendButton;
    private EditText MessageInput;
    private RecyclerView MessageListView;
    private List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private TextView TheirNameDisplay;
    private String theirID, theirName, myID;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        myID = auth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();



        theirID = getIntent().getExtras().get("their_user_id").toString();
        theirName = getIntent().getExtras().get("their_user_name").toString();
        initializeComponents();
        //initMessageList();
        fetchMessages();
//        Message testM1 = new Message(myID, "test", "myid 1", "test");
//        Message testM2 = new Message("test", "test", "their msg1", "test");
//        Message testM3 = new Message(myID, "test", "myid 2", "test");
//        Message testM4 = new Message("test", "test", "their msg2", "test");
//        messageList.add(testM1);
//        messageList.add(testM2);
//        messageList.add(testM3);
//        messageList.add(testM4);
//            Message testM5 = new Message("test", "test", "their msg5", "test");
//            messageList.add(testM5);
    }

    private void initializeComponents(){
        SendButton = (Button) findViewById(R.id.btnSend);
        MessageInput = (EditText) findViewById(R.id.etMessageInput);
        MessageListView = (RecyclerView) findViewById(R.id.lvMessageList);
        TheirNameDisplay = (TextView) findViewById(R.id.tvTheirTitle);
        TheirNameDisplay.setText(theirName);

        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        findViewById(R.id.tvTheirTitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void sendMessage() {
        String messageText = MessageInput.getText().toString();

        if(!messageText.isEmpty()){
            Map<String, Object> message = new HashMap<>();
            message.put("sender", myID);
            message.put("receiver",theirID);
            message.put("message_txt", messageText);
            message.put("timestamp", FieldValue.serverTimestamp());
            db.collection("messages").add(message);
            MessageInput.setText("");
        }
        fetchMessages();
    }

    private void initMessageList() {
        MessageListView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        MessageListView.setAdapter(messageAdapter);
    }
    //problem: need to fetch messages from server, convert them into Message objects, and then put them in messageList in the order of timestamp
    private void fetchMessages() {
        messageList = new ArrayList<Message>();
//        Message testM5 = new Message("test", "test", "their msg front", "fewfawe");
//        messageList.add(testM5);
        db.collection("messages")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if((document.getString("sender").equals(myID)) || document.getString("receiver").equals(myID)) {
                                String senderID = document.getString("sender");
                                String receiverID = document.getString("receiver");
                                String messageTxt = document.getString("message_txt");
                                String timestamp = document.getString("timestamp");
                                Message testM6 = new Message(senderID, receiverID, messageTxt, timestamp);
                                messageList.add(testM6);
                                Log.d(TAG, document.getId() + " bark! => " + document.getData());
                                initMessageList();
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });

        Log.d(TAG, ""+messageList.size());

    }
}
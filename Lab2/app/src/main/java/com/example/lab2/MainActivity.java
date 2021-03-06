package com.example.lab2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.azure.android.communication.chat.ChatAsyncClient;
import com.azure.android.communication.chat.ChatThreadAsyncClient;
import com.azure.android.communication.chat.models.ChatMessageType;
import com.azure.android.communication.chat.models.ChatParticipant;
import com.azure.android.communication.chat.models.ChatThread;
import com.azure.android.communication.chat.models.CreateChatThreadRequest;
import com.azure.android.communication.chat.models.CreateChatThreadResult;
import com.azure.android.communication.chat.models.SendChatMessageRequest;
import com.azure.android.communication.common.CommunicationUserIdentifier;
import com.azure.android.core.http.Callback;
import com.azure.android.core.http.HttpHeader;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    final String endpoint = "<ACS Resource>";
    final String userAccessToken = "<User Token>";

    String chatThreadId;
    ChatAsyncClient client;

    TextView output;

    @Override
    protected void onStart() {
        super.onStart();

        createChatClient();


        Button startChat = findViewById(R.id.startChat);
        startChat.setOnClickListener(l -> call_createChatThread());

        Button sendMsg = findViewById(R.id.sendMsg);
        sendMsg.setOnClickListener(l -> sendChatMessage());

        output = findViewById(R.id.output);
        //output.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);

    }

    private void sendChatMessage() {
        // <CREATE A CHAT THREAD CLIENT>
        ChatThreadAsyncClient threadClient =
                new ChatThreadAsyncClient.Builder()
                        .endpoint(endpoint)
                        .credentialInterceptor(chain -> chain.proceed(chain.request()
                                .newBuilder()
                                .header(HttpHeader.AUTHORIZATION,"Bearer " + userAccessToken)
                                .build()))
                        .build();

        // <SEND A MESSAGE>
        // The chat message content, required.
        final String content = "Test message 1";
        // The display name of the sender, if null (i.e. not specified), an empty name will be set.
        final String senderDisplayName = "An important person";
        SendChatMessageRequest message = new SendChatMessageRequest()
                .setType(ChatMessageType.TEXT)
                .setContent(content)
                .setSenderDisplayName(senderDisplayName);

        // The unique ID of the thread.

        threadClient.sendChatMessage(chatThreadId, message, new Callback<String>() {
            @Override
            public void onSuccess(String messageId, okhttp3.Response response) {
                // A string is the response returned from sending a message, it is an id,
                // which is the unique ID of the message.
                final String chatMessageId = messageId;
                output.setTextColor(Color.GREEN);
                output.setText("The Chat mesage sent successful. Message ID is: " + chatMessageId);
                Log.i("MyApp", "The Chat message sent successful. Message ID is: " + chatMessageId);
                // Take further action.
            }

            @Override
            public void onFailure(Throwable throwable, okhttp3.Response response) {
                output.setTextColor(Color.RED);
                output.setText("Failed in sending chat: " + response.message());
                Log.i("MyApp", "Failed in sending chat");
                Log.i("MyApp", response.message());
                // Handle error.
            }
        });

    }

    private void call_createChatThread() {
        // <CREATE A CHAT THREAD>
        //  The list of ChatParticipant to be added to the thread.
        List<ChatParticipant> participants = new ArrayList<>();
        // The communication user ID you created before, required.
        String id = "<user_id>";
        // The display name for the thread participant.
        String displayName = "initial participant";
        participants.add(new ChatParticipant()
                .setId(new CommunicationUserIdentifier(id))
                .setDisplayName(displayName));

        // The topic for the thread.
        final String topic = "General";
        // The model to pass to the create method.
        CreateChatThreadRequest thread = new CreateChatThreadRequest()
                .setTopic(topic)
                .setParticipants(participants);

        // optional, set a repeat request ID
        final String repeatabilityRequestID = "123";

        client.createChatThread(thread, repeatabilityRequestID, new Callback<CreateChatThreadResult>() {
            public void onSuccess(CreateChatThreadResult result, okhttp3.Response response) {
                ChatThread chatThread = result.getChatThread();
                chatThreadId = chatThread.getId();
                output.setTextColor(Color.GREEN);
                output.setText("The Thread ID is: " + chatThreadId);
                Log.i("MyApp", "The Thread ID is: " + chatThreadId);
                // take further action
            }

            public void onFailure(Throwable throwable, okhttp3.Response response) {
                output.setTextColor(Color.RED);
                output.setText("Failed to create the chat thread " + response.message());
                Log.i("MyApp", "Failed to create the chat thread ");
                Log.i("MyApp", response.message());
            }
        });
    }

    private void createChatClient() {
        // <<Create a chat client>>

        client = new ChatAsyncClient.Builder()
                .endpoint(endpoint)
                .credentialInterceptor(chain -> chain.proceed(chain.request()
                        .newBuilder()
                        .header(HttpHeader.AUTHORIZATION, "Bearer " + userAccessToken)
                        .build()))
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

package br.gov.dpf.intelitrack.messaging;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.gov.dpf.intelitrack.components.NotificationController;

public class MessagingService extends FirebaseMessagingService {

    //Notification manager class
    private NotificationController notificationController;

    @Override
    public void onCreate()
    {
        //Call parent method
        super.onCreate();

        //Instantiate singleton notification builder
        notificationController = NotificationController.getInstance(this);
    }

    @Override
    public void onNewToken(String s)
    {
        //Update display name: currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName("DISPLAY NAME").build())
        super.onNewToken(s);

        //Get firebase instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //Try to get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //If user signed in
        if(currentUser != null)
        {
            // Update one field, creating the document if it does not already exist.
            Map<String, Object> data = new HashMap<>();

            // Update user field on DB
            data.put("token", s);
            data.put("lastActivity", new Date());

            //Get firestore DB instance
            FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();

            //Add registration token to users collection on Firestore DB
            firestoreDB.collection("Users").document(currentUser.getUid()).set(data);
        }
        Log.e("NEW_TOKEN",s);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
        {
            //Show notification to user
            notificationController.showNotification(remoteMessage.getData(), remoteMessage.getFrom());
        }
    }


    @Override
    public void onDeletedMessages() {
        Log.d("FCM", "onDeletedMessages: ");
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String msgID)
    {
        Log.e("FCM", "onMessageSent: " + msgID );
        super.onMessageSent(msgID);
    }

    @Override
    public void onSendError(String msgID, Exception exception)
    {
        Log.e("FCM", "onSendError ", exception );
        super.onSendError(msgID, exception);
    }
}

package br.gov.dpf.intelitrack.messaging;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

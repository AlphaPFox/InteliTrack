package br.gov.dpf.intelitrack.messaging;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseInstanceID extends FirebaseInstanceIdService
{
    // [START refresh_token]
    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token)
    {
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
            data.put("token", token);
            data.put("lastActivity", new Date());

            //Get firestore DB instance
            FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();

            //Add registration token to users collection on Firestore DB
            firestoreDB.collection("Users").document(currentUser.getUid()).set(data);
        }
    }
}

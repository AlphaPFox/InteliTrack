package br.gov.dpf.intelitrack.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import br.gov.dpf.intelitrack.DetailActivity;
import br.gov.dpf.intelitrack.entities.Tracker;

public class TrackerBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(final Context context, final Intent intent) {

        //If intent is a dismiss notification event
        if(intent.getAction() != null)
        {
            //Instantiate singleton notification builder
            NotificationController notificationController = NotificationController.getInstance(context);

             if(intent.hasExtra("DismissGroup"))
            {
                //Dismiss notification group (summary)
                notificationController.dismissNotificationGroup(intent.getAction());
            }
            else if (intent.hasExtra("DismissSingle"))
            {
                //Dismiss single notification
                notificationController.dismissNotification(intent.getStringExtra("GroupKey"), intent.getIntExtra("DismissSingle", 0));
            }
            else if (intent.hasExtra("CancelConfig"))
            {
                //Dismiss single notification
                notificationController.cancelConfiguration(intent.getStringExtra("GroupKey"), intent.getIntExtra("CancelConfig", 0));
            }

            if(intent.hasExtra("Tracker"))
            {
                //Change intent from broadcast receiver, to activity
                intent.setClass(context, DetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                //Start detail activity
                context.startActivity(intent);
            }
        }
    }
}

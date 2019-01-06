package br.gov.dpf.intelitrack.firestore;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.Query;

import java.util.Date;

import br.gov.dpf.intelitrack.MainActivity;
import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.entities.Tracker;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListAdapter extends BaseAdapter<ListAdapter.ViewHolder>  {

    //Linked activity
    private MainActivity mActivity;

    //Constructor
    protected ListAdapter(MainActivity activity, Query query) {
        super(activity, query);

        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        return new ViewHolder(inflater.inflate(R.layout.main_recycler_item_small, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    @SuppressWarnings("ConstantConditions")
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //Get tracker using index position
        final Tracker tracker = getSnapshot(position).toObject(Tracker.class);

        //Save tracker ID
        tracker.setID(getSnapshot(position).getId());

        // - replace the contents of the view with that element
        holder.lblTrackerName.setText(tracker.getName());

        // Check if tracker have any coordinates available
        if(tracker.getLastCoordinate() != null)
        {
            //Get last coordinate datetime
            holder.lblLastDatetime.setText(formatDateTime((Date) tracker.getLastCoordinate().get("datetime"), false, false));

            //Get textual address
            holder.lblLastPosition.setText(tracker.getLastCoordinate().get("address").toString());

            //Check if last coordinate is less than 24h ago
            if(new Date().getTime() - ((Date) tracker.getLastCoordinate().get("datetime")).getTime() < (1000*60*60*24))
            {
                //Recent coordinate, set text green
                holder.lblLastDatetime.setTextColor(Color.parseColor("#3f9d2c"));
            }
            else
            {
                //Old coordinate, set text red
                holder.lblLastDatetime.setTextColor(Color.RED);
            }
        }

        //Set user defined color
        holder.imageView.setCircleBackgroundColor(Color.parseColor(tracker.getBackgroundColor()));

        //Set model item image
        holder.imageView.setImageDrawable(mActivity.getResources().getDrawable(mActivity.getResources().getIdentifier("model_" + tracker.getModel().toLowerCase(), "drawable", mActivity.getPackageName())));

        //Set item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivity != null)
                {
                    //Disable click to avoid double clicking
                    holder.itemView.setClickable(false);

                    //Cal interface method on main activity
                    mActivity.OnTrackerSelected(tracker, holder);
                }
            }
        });

        //Set edit click listener
        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivity != null)
                {
                    //Cal interface method on main activity
                    mActivity.OnTrackerEdit(tracker, holder);
                }
            }
        });
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder
            extends RecyclerView.ViewHolder {


        //Public layout components (used on detail activity transition)
        TextView lblTrackerName, lblLastPosition, lblLastDatetime;
        CircleImageView imageView;
        ImageView imgEdit;

        ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            //Text fields
            lblTrackerName = itemView.findViewById(R.id.lblTrackerName);
            lblLastPosition = itemView.findViewById(R.id.lblLastPosition);
            lblLastDatetime = itemView.findViewById(R.id.lblTrackerModel);

            //Image views
            imageView = itemView.findViewById(R.id.imgTracker);
            imgEdit = itemView.findViewById(R.id.imgEdit);

        }
    }
}

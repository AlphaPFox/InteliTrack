package br.gov.dpf.intelitrack.firestore;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import br.gov.dpf.intelitrack.components.CircleProgressBar;
import br.gov.dpf.intelitrack.entities.Tracker;
import br.gov.dpf.intelitrack.MainActivity;
import br.gov.dpf.intelitrack.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class TrackerAdapter extends BaseAdapter<TrackerAdapter.ViewHolder>  {

    //Linked activity
    private MainActivity mActivity;
    private ArrayList<Integer> indexes = new ArrayList<>();

    //Constructor
    protected TrackerAdapter(MainActivity activity, Query query) {
        super(activity, query);

        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public TrackerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        return new ViewHolder(inflater.inflate(R.layout.main_recycler_item, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    @SuppressWarnings("ConstantConditions")
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //Get tracker using index position
        final Tracker tracker = getSnapshot(position).toObject(Tracker.class);

        //Save tracker ID
        tracker.setID(getSnapshot(position).getId());

        //Get last configuration and last coordinates
        final Map<String, Object> coordinates = tracker.getLastCoordinate();

        // - replace the contents of the view with that element
        holder.lblTrackerName.setText(tracker.getName());
        holder.lblTrackerModel.setText(String.format("Modelo: %s", tracker.formatTrackerModel()));

        // Check if tracker have any coordinates available
        if(tracker.getLastCoordinate() != null)
        {
            //Get last coordinate datetime
            holder.lblLastDatetime.setText(formatDateTime((Date) tracker.getLastCoordinate().get("datetime"), false, false));

            //Get textual address
            holder.lblLastPosition.setText(tracker.getLastCoordinate().get("address").toString());
        }
        else
        {
            //Get last coordinate datetime
            holder.lblLastDatetime.setText(R.string.txtWaitingTitle);

            //Get textual address
            holder.lblLastPosition.setText(R.string.lblNoLocation);
        }

        //Set user defined color
        holder.imageView.setCircleBackgroundColor(Color.parseColor("#" + tracker.getBackgroundColor().substring(3)));

        //Set button color
        holder.btnConfigure.setTextColor(holder.imageView.getCircleBackgroundColor());
        holder.btnExpand.setTextColor(holder.imageView.getCircleBackgroundColor());

        //Set model item image
        holder.imageView.setImageDrawable(mActivity.getResources().getDrawable(mActivity.getResources().getIdentifier("model_" + tracker.getModel().toLowerCase(), "drawable", mActivity.getPackageName())));

        //Change color to default loading animation
        holder.indeterminateProgress.getIndeterminateDrawable().setColorFilter(holder.imageView.getCircleBackgroundColor(), android.graphics.PorterDuff.Mode.SRC_IN);

        //Disable click on map area (opens google map app)
        holder.mapView.setClickable(false);

        //If tracker has coordinates available
        if (coordinates != null)
        {
            //Load map
            holder.mapView.onCreate(null);
            holder.mapView.onResume();
            holder.mapView.getMapAsync(new OnMapReadyCallback()
            {
                @Override
                public void onMapReady(final GoogleMap googleMap) {

                    //Get coordinates
                    GeoPoint dbCoordinates = (GeoPoint) coordinates.get("position");

                    //Initialize map
                    MapsInitializer.initialize(mActivity);
                    holder.googleMap = googleMap;
                    holder.googleMap.setMapType(mActivity.sharedPreferences.getInt("UserMapType", GoogleMap.MAP_TYPE_NORMAL));
                    holder.googleMap.getUiSettings().setMapToolbarEnabled(false);

                    //Define icon settings
                    IconGenerator iconFactory = new IconGenerator(mActivity);
                    iconFactory.setColor(Color.parseColor(tracker.getBackgroundColor()));
                    iconFactory.setTextAppearance(R.style.Marker);

                    //Get central coordinates
                    LatLng location = new LatLng(dbCoordinates.getLatitude(), dbCoordinates.getLongitude());

                    //If coordinates come from a GSM tower cell
                    if(coordinates.get("type").equals("GSM") && mActivity.sharedPreferences.getInt("Map_Radius", 4) > 0)
                    {
                        //Set a lower zoom to GSM coordinates
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));

                        //Create radius to represent gsm tower range
                        Circle gsmTower = googleMap.addCircle(new CircleOptions()
                                .center(location)
                                .strokeWidth(mActivity.getResources().getDimensionPixelSize(R.dimen.map_circle_width))
                                .strokeColor(Color.parseColor("#88" + tracker.getBackgroundColor().substring(3)))
                                .fillColor(Color.parseColor("#55" + tracker.getBackgroundColor().substring(3))));

                        //Get user option
                        switch (mActivity.sharedPreferences.getInt("Map_Radius", 4))
                        {
                            case 1:
                                //Preference: 200m
                                gsmTower.setRadius(200);
                                break;
                            case 2:
                                //Preference: 500m
                                gsmTower.setRadius(500);
                                break;
                            case 3:
                                //Preference: 1km
                                gsmTower.setRadius(1000);
                                break;
                            case 4:
                                //Preference: 2km
                                gsmTower.setRadius(2000);
                                break;
                            case 5:
                                //Preference: 5km
                                gsmTower.setRadius(5000);
                                break;
                        }

                        //Change marker color to be not transparent
                        iconFactory.setColor(Color.parseColor("#" + tracker.getBackgroundColor().substring(3)));
                    }
                    else
                    {
                        //Set a higher zoom to GSM coordinates
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
                    }

                    //Define map marker settings
                    MarkerOptions markerOptions = new MarkerOptions().
                            icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(formatDateTime((Date) coordinates.get("datetime"), true, true)))).
                            position(location).
                            anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

                    //Add marker on map
                    googleMap.addMarker(markerOptions);

                    //Define method to call after map is loaded
                    holder.googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {

                            //If coordinates is available
                            if (googleMap.getMapType() != GoogleMap.MAP_TYPE_NONE)
                            {
                                //Hide loading animation
                                ((View) holder.indeterminateProgress.getParent()).animate().setDuration(500).alpha(0f);
                            }
                        }
                    });
                }
            });
        }


        //Check if user wants to display this tracker at top
        if(mActivity.sharedPreferences.getBoolean("Favorite_" + tracker.getID(), false))
        {
            //Change image resource and tag
            holder.imgFavorite.setImageResource(R.drawable.ic_star_grey_24dp);
        }
        else
        {
            //Change image resource and tag
            holder.imgFavorite.setImageResource(R.drawable.ic_star_border_grey_24dp);
        }

        //Set item click listener
        holder.imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivity != null)
                {
                    //Cal interface method on main activity
                    mActivity.OnTrackerFavorite(tracker);
                }
            }
        });

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

        //Set edit click listener
        holder.imgExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If this is the clicked panel and its currently hidden
                if(holder.vwMoreInfo.getVisibility() == View.GONE)
                {
                    //Show linear layout
                    holder.vwMoreInfo.setVisibility(View.VISIBLE);

                    //Change icon from image view
                    holder.imgExpand.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_expand_less_grey_24dp));
                }
                else
                {
                    //Show linear layout
                    holder.vwMoreInfo.setVisibility(View.GONE);

                    //Change icon from image view
                    holder.imgExpand.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_expand_more_grey_24dp));
                }
            }
        });
    }

    @Override
    public void startListening() {
        super.startListening();
        indexes.clear();
    }

    @Override
    protected void onDocumentAdded(DocumentChange change) {
        super.onDocumentAdded(change);

        if(mActivity.sharedPreferences.getBoolean("Favorite_" + change.getDocument().getId(), false))
        {
            indexes.add(0, change.getNewIndex());
        }
        else
        {
            indexes.add(change.getNewIndex());
        }
    }

    @Override
    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(indexes.indexOf(change.getOldIndex()));
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            onDocumentAdded(change);
            notifyDataSetChanged();
        }
    }

    @Override
    protected void onDocumentRemoved(DocumentChange change) {
        int index = indexes.indexOf(change.getOldIndex());

        mSnapshots.remove(change.getOldIndex());
        indexes.remove(index);
        notifyItemRemoved(index);

        for(int i = 0; i < indexes.size(); i++)
            if(indexes.get(i) >= index)
                indexes.set(i, indexes.get(i) - 1);
    }

    @Override
    DocumentSnapshot getSnapshot(int index) {
        return super.getSnapshot(indexes.get(index));
    }

    //Recycling GoogleMap for list item
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder)
    {
        // Cleanup MapView here
        if (holder.googleMap != null)
        {
            holder.googleMap.clear();
            holder.googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }

        //Show loading animation again
        ((View) holder.indeterminateProgress.getParent()).animate().setDuration(500).alpha(1f);

        // Hide more info panel and change expand icon
        holder.vwMoreInfo.setVisibility(View.GONE);
        holder.imgExpand.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_expand_more_grey_24dp));

        super.onViewRecycled(holder);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        View vwMoreInfo;
        CircleProgressBar circleProgressBar;
        Button btnExpand, btnConfigure;
        ProgressBar indeterminateProgress;
        MapView mapView;

        //Public layout components (used on detail activity transition)
        public TextView lblTrackerName, lblTrackerModel, lblLastPosition, lblLastDatetime, lblBatteryValue, lblSignalValue;
        public CircleImageView imageView;
        public ImageView imgEdit, imgFavorite, imgExpand;

        //Google maps object
        GoogleMap googleMap;

        ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);


            //Text fields
            lblTrackerName = itemView.findViewById(R.id.lblTrackerName);
            lblTrackerModel = itemView.findViewById(R.id.lblTrackerModel);
            lblLastPosition = itemView.findViewById(R.id.lblLastPosition);
            lblLastDatetime = itemView.findViewById(R.id.lblLastDatetime);
            lblBatteryValue = itemView.findViewById(R.id.lblBatteryValue);
            lblSignalValue = itemView.findViewById(R.id.lblSignalValue);

            //Image views
            imageView = itemView.findViewById(R.id.imgTracker);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgExpand = itemView.findViewById(R.id.imgExpand);

            //Buttons
            btnExpand = itemView.findViewById(R.id.btnExpand);
            btnConfigure = itemView.findViewById(R.id.btnConfigure);

            //Views
            vwMoreInfo = itemView.findViewById(R.id.vwMoreInfo);

            //Progress bars
            indeterminateProgress = itemView.findViewById(R.id.indeterminateProgress);
            circleProgressBar = itemView.findViewById(R.id.circleProgressBar);

            //Google maps view
            mapView = itemView.findViewById(R.id.googleMap);
        }
    }
}

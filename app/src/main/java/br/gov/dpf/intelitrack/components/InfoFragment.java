package br.gov.dpf.intelitrack.components;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import br.gov.dpf.intelitrack.DetailActivity;
import br.gov.dpf.intelitrack.R;

public class InfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        //Define fragment layout file
        return inflater.inflate(R.layout.marker_infowindow, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get info window fragments
        final Bundle arguments = getArguments();
        final DetailActivity detailActivity = (DetailActivity) getActivity();

        //Get layout elements
        RelativeLayout vwTitleBar =  view.findViewById(R.id.vwTitlebar);
        TextView txtMarkerID = view.findViewById(R.id.txtMarkerID);
        TextView txtTrackerName = view.findViewById(R.id.txtTrackerName);
        TextView txtAddress = view.findViewById(R.id.txtAddress);
        TextView txtDatetime = view.findViewById(R.id.txtDateTime);
        TextView txtBatteryLevel = view.findViewById(R.id.txtBatteryLevel);
        TextView txtSignalLevel = view.findViewById(R.id.txtSignalLevel);
        ImageView imgAddress = view.findViewById(R.id.imgAddress);
        ImageView imgDatetime = view.findViewById(R.id.imgDatetime);
        ImageView imgSignal = view.findViewById(R.id.imgSignal);
        ImageView imgBattery = view.findViewById(R.id.imgBattery);
        Button btnNext = view.findViewById(R.id.btnNext);
        Button btnPrevious = view.findViewById(R.id.btnPrevious);

        //Load text fields
        txtMarkerID.setText(String.valueOf(arguments.getInt("ID")));
        txtTrackerName.setText(arguments.getString("TrackerName"));
        txtAddress.setText(arguments.getString("Address"));
        txtDatetime.setText(arguments.getString("Datetime"));
        txtBatteryLevel.setText(arguments.getString("BatteryLevel"));
        txtSignalLevel.setText(arguments.getString("SignalLevel"));

        //Get tracker color
        int tracker_color = Color.parseColor(arguments.getString("TrackerColor"));

        //Set title bar background color
        vwTitleBar.setBackgroundColor(tracker_color);
        txtMarkerID.setTextColor(tracker_color);

        //Set color on image icons
        imgAddress.getDrawable().setColorFilter(tracker_color, android.graphics.PorterDuff.Mode.SRC_IN);
        imgDatetime.getDrawable().setColorFilter(tracker_color, android.graphics.PorterDuff.Mode.SRC_IN);
        imgSignal.getDrawable().setColorFilter(tracker_color, android.graphics.PorterDuff.Mode.SRC_IN);
        imgBattery.getDrawable().setColorFilter(tracker_color, android.graphics.PorterDuff.Mode.SRC_IN);

        //Check if it is the first item
        if(arguments.getInt("ID") == 1)
        {
            //Disable previous button
            btnPrevious.setEnabled(false);
        }
        else
        {
            //Otherwise, set button click listener
            btnPrevious.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //Call method on DetailActivity to show previous marker info window
                    detailActivity.OnInfoWindowClick(arguments.getInt("ID"), -1);
                }
            });

        }

        //Check if it is the last item
        if(arguments.getInt("ID") == arguments.getInt("ItemCount"))
        {
            //Disable next button
            btnNext.setEnabled(false);
        }
        else
        {
            //Otherwise, set button click listener
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //Call method on DetailActivity to show next marker info window
                    detailActivity.OnInfoWindowClick(arguments.getInt("ID"), +1);
                }
            });
        }
    }
}

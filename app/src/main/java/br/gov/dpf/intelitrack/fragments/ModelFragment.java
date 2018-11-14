package br.gov.dpf.intelitrack.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.util.HashMap;

import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.components.TrackerPager;

public class ModelFragment extends SlideFragment
{
    public ModelFragment() {
        // Required empty public constructor
    }

    //Custom pager component
    private TrackerPager pager;

    //Hash table containing tracker settings
    private HashMap<String, String> settings;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     */
    // TODO: Rename and change types and number of parameters
    public static ModelFragment newInstance() {
        return new ModelFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_model, container, false);

        // Find pager in root view
        pager = root.findViewById(R.id.trackerLayout);

        // Initialize settings
        settings = new HashMap<>();

        // Return root element
        return root;
    }

    public HashMap<String, String> getSettings()
    {
        //If component loaded
        if(pager != null)
        {
            //Save settings
            settings.put("TrackerModel", pager.getSelectedModel());
        }

        //Get selected model from view pager
        return settings;
    }

}

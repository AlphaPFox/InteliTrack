package br.gov.dpf.intelitrack.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.util.HashMap;

import br.gov.dpf.intelitrack.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static br.gov.dpf.intelitrack.trackers.SettingsActivity.REQUEST_CONTACTS;
import static br.gov.dpf.intelitrack.trackers.SettingsActivity.REQUEST_PERMISSION;

public class SettingsFragment extends SlideFragment
{
    //Hash table containing tracker settings
    private HashMap<String, String> settings;

    //Indicates if user is allowed to go forward
    private boolean canProceed = false;

    @BindView(R.id.imgSelectedModel) ImageView imgSelectedModel;
    @BindView(R.id.lblSelectedModel) TextView lblSelectedModel;
    @BindView(R.id.lblIdentification) TextView lblIdentification;
    @BindView(R.id.txtIdentification) EditText txtIdentification;
    @BindView(R.id.txtTrackerName) EditText txtTrackerName;
    @BindView(R.id.ddwMobileNetworks) Spinner ddwMobileNetworks;
    @BindView(R.id.vwMobileNetworks) View vwMobileNetworks;
    @BindView(R.id.lblInfo) TextView lblInfo;

    @OnClick(R.id.btnImport)
    public void importData()
    {
        //Check if permission is granted to this app already
        if (ActivityCompat.checkSelfPermission(getIntroActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            //Build contacts intent
            Intent contacts = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

            //Start android activity
            startActivityForResult(contacts, REQUEST_CONTACTS);
        }
        else
        {
            //If no permission yet, request from user
            ActivityCompat.requestPermissions(getIntroActivity(), new String[]{ Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION);
        }
    }

    public SettingsFragment()
    {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        // Load mobile network operator data
        Spinner ddwMobileNetworks = root.findViewById(R.id.ddwMobileNetworks);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(container.getContext(), R.layout.dropdown_item, getResources().getStringArray(R.array.mobile_networks));
        ddwMobileNetworks.setAdapter(adapter);

        // Bind views
        ButterKnife.bind(this, root);

        return root;
    }

    @OnTextChanged({R.id.txtIdentification, R.id.txtTrackerName})
    protected void handleTextChange(Editable editable)
    {
        //Check name field
        if(txtTrackerName.getText().length() >= 5)
        {
            txtTrackerName.setError(null);
        }

        //Check ID field
        if(txtIdentification.getText().length() == 11)
        {
            txtIdentification.setError(null);
        }

        //If both fields are filled, allow to proceed
        canProceed = (txtTrackerName.getText().length() >= 5 && txtIdentification.getText().length() == 11);
    }

    public void loadSettings(HashMap<String, String> previousSettings)
    {
        //Get resource manager
        Resources res = getResources();

        //Save settings
        settings = previousSettings;

        //Set image and title from each corresponding model
        switch (settings.get("TrackerModel"))
        {
            case "tk102":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "TK102"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_tk102));
                break;
            case "tk103":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "TK102"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_tk103));
                break;
            case "tk306":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "TK306"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_tk306));
                break;
            case "spot":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "SPOT"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_spot));
                break;
            case "st940":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "ST-940"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_st940));
                break;
            case "pt39":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "PT-39"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_pt39));
                break;
            case "pt50x":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "PT-50X"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_pt50x));
                break;
        }

        //Set identification fields
        switch (settings.get("TrackerModel"))
        {
            case "spot":
                lblIdentification.setText(res.getString(R.string.lblSpotID));
                txtIdentification.setHint(res.getString(R.string.txtSpotIDHint));
                lblInfo.setText(res.getString(R.string.lblSpotInfo));
                vwMobileNetworks.setVisibility(View.GONE);
                break;

            case "st940":
                lblIdentification.setText(res.getString(R.string.lblIMEI));
                txtIdentification.setHint(res.getString(R.string.txtIMEIHint));
                lblInfo.setText(res.getString(R.string.lblIMEIInfo));
                vwMobileNetworks.setVisibility(View.GONE);
                break;

            default:
                lblIdentification.setText(res.getString(R.string.lblTrackerPhone));
                txtIdentification.setHint(res.getString(R.string.txtTrackerPhoneHint));
                lblInfo.setText(res.getString(R.string.mobile_networks));
                vwMobileNetworks.setVisibility(View.VISIBLE);
                break;
        }

        //Clear identification field
        txtIdentification.setText("");
        txtIdentification.setError(null);
    }

    public void loadContact(String contactName, String contactPhone)
    {
        //Get resource manager
        txtTrackerName.setText(contactName);
        txtIdentification.setText(contactPhone);
    }

    public HashMap<String, String> getSettings()
    {
        //If identification value present
        if(txtIdentification != null)
        {
            //Get identification value
            settings.put("TrackerID", txtIdentification.getText().toString());
            settings.put("TrackerName", txtTrackerName.getText().toString());
            settings.put("TrackerNetwork", ddwMobileNetworks.getSelectedItem().toString());

            //Hide keyboard if visible
            InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(txtIdentification.getWindowToken(), 0);
        }

        //Return tracker settings
        return settings;
    }

    public void showRequiredFields()
    {

        //Mark error on identification field
        if(txtTrackerName != null && txtTrackerName.getText().length() < 5)
        {
            txtTrackerName.requestFocus();
            txtTrackerName.setError("Campo obrigatório (mínimo 5 caracteres)");
        }
        else if(txtIdentification != null && txtIdentification.getText().length() != 11)
        {
            txtIdentification.requestFocus();
            txtIdentification.setError("Preencha no formato: 6799998888");
        }
    }

    @Override
    public boolean canGoForward()
    {
        return canProceed;
    }
}

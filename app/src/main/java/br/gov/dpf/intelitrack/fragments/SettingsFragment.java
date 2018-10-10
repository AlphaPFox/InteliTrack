package br.gov.dpf.intelitrack.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import br.gov.dpf.intelitrack.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class SettingsFragment extends SlideFragment
{
    private boolean canProceed = false;

    @BindView(R.id.imgSelectedModel) ImageView imgSelectedModel;
    @BindView(R.id.lblSelectedModel) TextView lblSelectedModel;
    @BindView(R.id.lblIdentification) TextView lblIdentification;
    @BindView(R.id.txtIdentification) EditText txtIdentification;
    @BindView(R.id.ddwMobileNetworks) Spinner ddwMobileNetworks;
    @BindView(R.id.vwMobileNetworks) View vwMobileNetworks;
    @BindView(R.id.lblInfo) TextView lblInfo;

    public SettingsFragment() {
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

    @OnTextChanged(R.id.txtIdentification)
    protected void handleTextChange(Editable editable) {
        if(editable.toString().length() > 10)
        {
            canProceed = true;
            txtIdentification.setError(null);
        }
        else
        {
            canProceed = false;
        }
    }

    public void setModel(String model)
    {
        //Get resource manager
        Resources res = getResources();

        //Set image and title from each corresponding model
        switch (model)
        {
            case "tk102b":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "TK102B"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_tk102b));
                break;
            case "tk306":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "TK306"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_tk306));
                break;
            case "gt02":
                lblSelectedModel.setText(res.getString(R.string.selected_model, "GT-02"));
                imgSelectedModel.setImageDrawable(res.getDrawable(R.drawable.model_gt02));
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
        }

        //Set identification fields
        switch (model)
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

    public String getTrackerID()
    {
        //Remove focus from edit text
        txtIdentification.clearFocus();

        //Get identification value
        return txtIdentification.getText().toString();
    }

    public String getMobileOperator()
    {
        return ddwMobileNetworks.getSelectedItem().toString();
    }

    public void showRequiredFields()
    {
        //Mark error on identification field
        if(txtIdentification != null)
        {
            txtIdentification.requestFocus();
            txtIdentification.setError("Campo obrigatório");
        }
    }

    @Override
    public boolean canGoForward()
    {
        return canProceed;
    }
}

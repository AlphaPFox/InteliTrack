package br.gov.dpf.intelitrack.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.util.HashMap;

import br.gov.dpf.intelitrack.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class OptionsFragment extends SlideFragment
{
    //Hash table containing tracker settings
    private HashMap<String, String> settings;

    @BindView(R.id.vwBatteryTime) View vwBatteryTime;
    @BindView(R.id.vwBatteryMove) View vwBatteryMove;
    @BindView(R.id.vwDefault) View vwDefault;
    @BindView(R.id.vwRealTime) View vwRealTime;
    @BindView(R.id.rdbBatteryTime) AppCompatRadioButton rdbBatteryTime;
    @BindView(R.id.rdbBatteryMove) AppCompatRadioButton rdbBatteryMove;
    @BindView(R.id.rdbDefault) AppCompatRadioButton rdbDefault;
    @BindView(R.id.rdbRealTime) AppCompatRadioButton rdbRealTime;
    @BindView(R.id.txtBatteryTimeLabel) TextView txtBatteryTimeLabel;
    @BindView(R.id.txtDefaultLabel) TextView txtDefaultLabel;

    //Indicates if user is allowed to go forward
    private boolean canProceed = false;

    @OnClick({R.id.rdbBatteryTime, R.id.rdbBatteryMove, R.id.rdbDefault, R.id.rdbRealTime})
    public void onConfigurationChecked(AppCompatRadioButton checkedRadio)
    {
        //Save settings
        settings.put("TrackerConfig", checkedRadio.getTag().toString());

        //Hide all descriptions
        changeVisibility(vwBatteryMove, false);
        changeVisibility(vwDefault, false);
        changeVisibility(vwRealTime, false);

        //Show item description
        changeVisibility((View) checkedRadio.getParent(), true);

        //Un-check every radio button
        rdbBatteryTime.setChecked(false);
        rdbBatteryMove.setChecked(false);
        rdbDefault.setChecked(false);
        rdbRealTime.setChecked(false);

        //Check selected radio
        checkedRadio.setChecked(true);

        //If selected battery time mode
        if(checkedRadio.getId() == R.id.rdbBatteryTime)
        {
            //Sleep modes
            final String[] options = {"A cada 1 hora", "A cada 6 horas", "A cada 12 horas", "Uma vez por dia"};
            final String[] values = {"1h", "6h", "12h", "1d"};

            //Show options to user
            showOptions(checkedRadio.getContext(), "Enviar posição do rastreador: ", options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    //Get user option
                    int option = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();

                    //Set text option
                    txtBatteryTimeLabel.setText("Envia posição: " + options[option]);

                    //Save settings
                    settings.put("TrackerConfigValue", values[option]);
                }
            });
        }
        else if(checkedRadio.getId() == R.id.rdbBatteryMove)
        {
            //Save settings
            settings.put("TrackerConfigValue", "deepshock");
        }
        else if(checkedRadio.getId() == R.id.rdbDefault)
        {
            //Sleep modes
            final String[] options = {"A cada 10 minutos", "A cada 15 minutos", "A cada 30 minutos", "Uma vez por hora"};
            final String[] values = {"010m", "015m", "030m", "001h"};

            //Show options to user
            showOptions(checkedRadio.getContext(), "Enviar posição do rastreador: ", options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    //Get user option
                    int option = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();

                    //Set text option
                    txtDefaultLabel.setText("Envia posição: " + options[option]);

                    //Save settings
                    settings.put("TrackerConfigValue", values[option]);
                }
            });
        }
        else if(checkedRadio.getId() == R.id.rdbRealTime)
        {
            //Save settings
            settings.put("TrackerConfigValue", "030s");
        }

        //User selected configuration, can now proceed
        canProceed = true;
    }

    @OnClick({R.id.vwBatteryTime, R.id.vwBatteryMove, R.id.vwDefault, R.id.vwRealTime})
    public void onConfigurationClicked(View clickedView)
    {
        //Un-check every radio button
        changeVisibility(vwBatteryMove, false);
        changeVisibility(vwBatteryTime, false);
        changeVisibility(vwDefault, false);
        changeVisibility(vwRealTime, false);

        //Show item description
        changeVisibility(clickedView, true);
    }

    @OnCheckedChanged(R.id.swNotification)
    public void onNotificationChanged(CompoundButton switchCompat, boolean checked)
    {
        //Save option
        settings.put("TrackerNotification", checked ? "enabled" : "disabled");
    }

    public OptionsFragment()
    {
        // Required empty public constructor
    }

    public static OptionsFragment newInstance() {
        return new OptionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_options, container, false);

        // Bind views
        ButterKnife.bind(this, root);

        // Return root element
        return root;
    }

    public void changeVisibility(View option, boolean checked)
    {
        //Find title and description text views
        TextView txtTitle = option.findViewWithTag("title");
        TextView txtDescription = option.findViewWithTag("description");

        //If radio button checked, show description
        if(checked)
        {
            //Show description
            txtDescription.setVisibility(View.VISIBLE);

            //Change icon to expand less
            txtTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less_white_24dp, 0);
        }
        else
        {
            //Hide description
            txtDescription.setVisibility(View.GONE);

            //Change icon to expand more
            txtTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more_white_24dp, 0);
        }
    }

    public void showAlert(Context context)
    {
        //Create alert with options
        new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Configuração não selecionada")
                .setMessage("Selecione uma opção de configuração para o rastreador antes de prosseguir.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        //Close dialog
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void showOptions(Context context, String alertTitle, String[] options, DialogInterface.OnClickListener positiveResult)
    {
        //Create single choice selection dialog
        new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(alertTitle)
                .setSingleChoiceItems(options, 0, null)
                .setPositiveButton("Confirmar", positiveResult)
                .setCancelable(false)
                .show();
    }

    public void loadSettings(HashMap<String, String> previousSettings)
    {
        //Save settings
        settings = previousSettings;

        //Set default Notification option
        settings.put("TrackerNotification", "enabled");
    }

    public HashMap<String, String> getSettings()
    {
        //Return current fragment settings
        return settings;
    }

    @Override
    public boolean canGoForward()
    {
        return canProceed;
    }

}

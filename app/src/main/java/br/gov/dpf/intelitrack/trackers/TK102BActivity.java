package br.gov.dpf.intelitrack.trackers;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.xw.repo.BubbleSeekBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import br.gov.dpf.intelitrack.DetailActivity;
import br.gov.dpf.intelitrack.MainActivity;
import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.TrackerSettingsActivity;
import br.gov.dpf.intelitrack.components.ProgressNotification;
import br.gov.dpf.intelitrack.entities.Configuration;
import br.gov.dpf.intelitrack.entities.Tracker;
import br.gov.dpf.intelitrack.fragments.ConfigFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class TK102BActivity extends AppCompatActivity implements EventListener<QuerySnapshot>
{
    //Object representing the tracker to be inserted/updated
    private Tracker tracker;

    //Activity flags
    private boolean editMode;

    //Database instance
    private FirebaseFirestore firestoreDB;

    //Menu item used to confirm and refresh settings
    private MenuItem confirmMenu, refreshMenu;

    //Flag indicating if a DB operation is running
    private boolean loading;

    //Bind views
    @BindView(R.id.vwMain) View vwMain;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.txtAPN) EditText txtAPN;
    @BindView(R.id.txtAPNUser) EditText txtAPNUser;
    @BindView(R.id.txtAPNPassword) EditText txtAPNPassword;
    @BindView(R.id.txtAdmin) EditText txtAdmin;
    @BindView(R.id.txtAdminIP) EditText txtAdminIP;
    @BindView(R.id.txtAdminIPPort) EditText txtAdminIPPort;
    @BindView(R.id.communicationMode) RadioGroup rdgCommunicationMode;
    @BindView(R.id.lblStatusCheck) TextView lblStatusCheck;
    @BindView(R.id.lblIMEI) TextView lblIMEI;
    @BindView(R.id.rdgOperationMode) RadioGroup rdgOperationMode;
    @BindView(R.id.rdgFix) RadioGroup rdgFix;
    @BindView(R.id.swAdmin) SwitchCompat swAdmin;
    @BindView(R.id.swLessGprs) SwitchCompat swLessGprs;
    @BindView(R.id.swMove) SwitchCompat swMove;
    @BindView(R.id.swSpeed) SwitchCompat swSpeed;
    @BindView(R.id.swShock) SwitchCompat swShock;
    @BindView(R.id.sbMove) BubbleSeekBar sbMove;
    @BindView(R.id.sbSpeed) BubbleSeekBar sbSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Load layout
        setContentView(R.layout.activity_tk102_b);

        //Bind views
        ButterKnife.bind(this);

        //Set toolbar
        setSupportActionBar(toolbar);

        //Load tracker from intent
        tracker = getIntent().getParcelableExtra("Tracker");

        //Check action bar
        if(getSupportActionBar() != null)
        {
            //Set back button on toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tracker.getName());
        }

        // Get DB instance
        firestoreDB = FirebaseFirestore.getInstance();

        //Check if editing existing tracker (Request_Update), resetting previous configuration (ResetConfig), or inserting new tracker (Request_Insert)
        if(getIntent().getIntExtra("Request", MainActivity.REQUEST_INSERT) == MainActivity.REQUEST_UPDATE && !getIntent().getBooleanExtra("ResetConfig", false))
        {
            //Set activity in edit mode
            editMode = true;

            //Set loading indicator
            loading = true;

            //Monitor tracker configurations
            firestoreDB.collection("Tracker/" + tracker.getID() + "/Configurations").addSnapshotListener(this);

            //Show general configuration settings
            findViewById(R.id.vwGeneralConfigurations).setVisibility(View.VISIBLE);

            //Hide fab
            findViewById(R.id.fab).setVisibility(View.GONE);
        }
        else
        {
            //Insert mode, load default APN
            txtAPN.setText(tracker.loadAPN());
            txtAPNUser.setText(tracker.loadAPNUserPass());
            txtAPNPassword.setText(tracker.loadAPNUserPass());
        }
    }

    @OnCheckedChanged({R.id.operationActive, R.id.operationStandBy, R.id.operationHibernate})
    public void onOperationModeChanged(CompoundButton button, boolean checked)
    {
        //Index to operation mode options
        int index_first = 0, index_last = 0;

        //If checked
        if(checked)
        {
            switch (button.getId())
            {
                case R.id.operationActive:
                    //Allow more precise tracking
                    index_first = 0;
                    index_last = 5;

                    //Check LESS GPRS mode option
                    swLessGprs.setChecked(false);

                    //Check default option
                    rdgFix.check(R.id.fix030s);
                    break;
                case R.id.operationStandBy:
                    //Stand by mode
                    index_first = 5;
                    index_last = 6;

                    //Check LESS GPRS mode option
                    swLessGprs.setChecked(true);

                    //Check default option
                    rdgFix.check(R.id.rdRequest);
                    break;
                case R.id.operationHibernate:
                    //Allow to save battery
                    index_first = 6;
                    index_last = 11;

                    //Check LESS GPRS mode option
                    swLessGprs.setChecked(true);

                    //Check default option
                    rdgFix.check(R.id.rdShock);
                    break;
            }

            //Disable option to change less GPRS config
            swLessGprs.setEnabled(false);

            //For every operation mode options
            for(int i = 0; i < rdgFix.getChildCount(); i++)
            {
                //Change visibility
                rdgFix.getChildAt(i).setVisibility(i >= index_first && i <= index_last ? View.VISIBLE : View.GONE);
            }
        }
    }

    @OnClick(R.id.btnEditLessGPRS)
    public void editLessGPRS(View button)
    {
        //Check operation mode
        if(rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeSMS)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de comunicação SMS.", Snackbar.LENGTH_LONG).show();
        }
        else if(rdgOperationMode.getCheckedRadioButtonId() == R.id.operationStandBy)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de operação Stand-by.", Snackbar.LENGTH_LONG).show();
        }
        else if(rdgOperationMode.getCheckedRadioButtonId() == R.id.operationHibernate)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de hibernação.", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            //Config available for edit
            onEditClick(button);
        }
    }

    @OnClick(R.id.btnEditShock)
    public void editShock(View button)
    {
        //Check operation mode
        if(rdgFix.getCheckedRadioButtonId() == R.id.rdShock)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de operação atual (FIX).", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            //Config available for edit
            onEditClick(button);
        }
    }

    @OnClick({R.id.btnEditAPN, R.id.btnEditUP, R.id.btnEditAdminIP})
    public void editGPRSSettings(View button)
    {
        //Check operation mode
        if(rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeSMS)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de comunicação SMS.", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            //Config available for edit
            onEditClick(button);
        }
    }

    @OnClick(R.id.btnEditAdmin)
    public void editAdminSettings(View button)
    {
        //Check operation mode
        if(rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeGPRS)
        {
            //Warn user
            Snackbar.make(button, "Não editável no modo de comunicação GPRS.", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            //Config available for edit
            onEditClick(button);
        }
    }

    @OnCheckedChanged(R.id.swAdmin)
    public void showAdminSettings(CompoundButton swAdmin, boolean checked)
    {
        //Change admin settings visibility
        findViewById(R.id.vwAdminSettings).setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.rdShock)
    public void onShockSelected(CompoundButton button, boolean checked)
    {
        //Change shock alert
        if(checked && rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeSMS && !swShock.isEnabled())
        {
            //Create alert with options
            new AlertDialog.Builder(TK102BActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Alerta de vibração")
                    .setIcon(R.drawable.ic_settings_grey_40dp)
                    .setMessage("Ative o alerta de vibração (SHOCK) para utilizar este modo de operação.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Close dialog
                            dialog.dismiss();
                        }
                    }).create()
                    .show();
        }
    }

    @OnCheckedChanged({R.id.swMove, R.id.swSpeed, R.id.swShock})
    public void onAlertChange(CompoundButton button, boolean checked)
    {
        //Change shock alert
        if(checked && rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeSMS && !swAdmin.isEnabled())
        {
            //Create alert with options
            new AlertDialog.Builder(TK102BActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Modo de comunicação SMS")
                    .setIcon(R.drawable.ic_settings_grey_40dp)
                    .setMessage("Ative o administrador de SMS (ADMIN) para receber alertas no modo SMS.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Close dialog
                            dialog.dismiss();
                        }
                    }).create()
                    .show();
        }
    }

    public void enableControls(View button, boolean blnEnable)
    {
        //Get button parent view group
        ViewGroup parent = (ViewGroup) button.getParent();

        //Find control with corresponding tag
        View control = ((ViewGroup) parent.getParent()).findViewWithTag("configControl");

        //If control is also a view group
        if(control instanceof ViewGroup)
        {
            //Cast control
            ViewGroup group = (ViewGroup) control;

            //For each children
            for(int i = 0; i < group.getChildCount(); i++)
            {
                //Enable and set visible
                group.getChildAt(i).setEnabled(blnEnable);
            }
        }
        else if(control != null)
        {
            //Enable control
            control.setEnabled(blnEnable);
        }

        //In edit mode
        if(editMode)
        {
            //Show btnSendCommand
            parent.getChildAt(blnEnable ? 1 : 0).setVisibility(View.VISIBLE);

            //Hide edit button
            button.setVisibility(View.GONE);
        }
    }

    public void onEditClick(View editButton)
    {
        //Enable controls for edit
        enableControls(editButton, true);
    }

    public void onExpandClick(View relativeLayout)
    {
        //Get parent
        ViewGroup parent = (ViewGroup) relativeLayout.getParent();

        //Collapse all panels on this configuration category
        for(int i = 0; i < parent.getChildCount(); i+=2)
        {
            //If this is the clicked panel and its currently hidden
            if(i == parent.indexOfChild(relativeLayout) && parent.getChildAt(i + 1).getVisibility() == View.GONE)
            {
                //Show linear layout
                parent.getChildAt(i + 1).setVisibility(View.VISIBLE);

                //Change icon from image view
                ((ImageView) parent.getChildAt(i).findViewWithTag("expandImage")).setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_grey_24dp));
            }
            else
            {
                //Hide configuration panel
                parent.getChildAt(i + 1).setVisibility(View.GONE);

                //Change icon from image view
                ((ImageView) parent.getChildAt(i).findViewWithTag("expandImage")).setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_grey_24dp));
            }
        }
    }

    public void onActionClick(View editButton)
    {
        //Get config name
        final String configName = editButton.getTag().toString().substring(4);

        //Create a new popup menu
        PopupMenu popup = new PopupMenu(this, editButton);

        //Get layout inflater
        popup.getMenuInflater().inflate(R.menu.config, popup.getMenu());

        //Define click actions
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.menu_confirm:

                        //Show dialog before confirm configuration
                        showAlert("Confirmar manualmente?", "Deseja confirmar que o rastreador executou esta configuração?", R.drawable.ic_settings_grey_40dp, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Update configuration status to confirmed
                                firestoreDB
                                        .document("Tracker/" + tracker.getID() + "/Configurations/" + configName)
                                        .update("status.finished", true,"status.step", "SUCCESS",
                                                "status.description", "Confirmado manualmente às " + new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(new Date()));

                                //Update tracker
                                firestoreDB
                                        .document("Tracker/" + tracker.getID())
                                        .update("lastConfiguration", null);

                                //Set loading indicator
                                confirmMenu.setActionView(new ProgressBar(TK102BActivity.this));
                            }
                        });

                        //End method
                        return true;

                    case R.id.menu_cancel:

                        //Show dialog before cancel configuration
                        showAlert("Cancelar configuração?", "Deseja cancelar a configuração?", R.drawable.status_warning, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Update configuration status to canceled
                                firestoreDB
                                        .document("Tracker/" + tracker.getID() + "/Configurations/" + configName)
                                        .update("status.finished", true,"status.step", "CANCELED",
                                                "status.description", "Configuração cancelada às " + new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(new Date()));

                                //Update tracker
                                firestoreDB
                                        .document("Tracker/" + tracker.getID())
                                        .update("lastConfiguration", null);

                                //Set loading indicator
                                confirmMenu.setActionView(new ProgressBar(TK102BActivity.this));
                            }
                        });

                        //End method
                        return true;

                    case R.id.menu_resend:

                        //Show dialog before resend configuration
                        showAlert("Reenviar configuração?", "Deseja reenviar configuração para o rastreador?", R.drawable.ic_refresh_grey_24dp, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Update configuration status to canceled
                                firestoreDB
                                        .document("Tracker/" + tracker.getID() + "/Configurations/" + configName)
                                        .update("status.finished", false,"status.step", "REQUESTED",
                                                "timestamp", FieldValue.serverTimestamp(),
                                                "status.description", "Aguardando envio para o rastreador.");

                                //Update tracker
                                firestoreDB
                                        .document("Tracker/" + tracker.getID())
                                        .update("lastConfiguration", FieldValue.delete());

                                //Set loading indicator
                                confirmMenu.setActionView(new ProgressBar(TK102BActivity.this));
                            }
                        });

                        //End method
                        return true;

                }
                return false;
            }
        });

        //Show popup menu
        popup.show();
    }

    public void onSendClick(View sendButton)
    {
        //Get config name
        final String configName = sendButton.getTag().toString().substring(4);

        //For each config
        switch (configName)
        {
            case "Admin":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Admin", "Configurando: Administrador SMS", txtAdmin.getText().toString(), swAdmin.isEnabled()));
                break;
            case "AccessPoint":
                //Update config value
                updateConfigValue(sendButton, new Configuration("AccessPoint", "Configurando: APN", txtAPN.getText().toString(), true));
                break;
            case "APNUserPass":
                //Update config value
                updateConfigValue(sendButton, new Configuration("APNUserPass", "Configurando: Usuário da APN", txtAPNUser.getText().toString() + " " + txtAPNPassword.getText().toString(), true));
                break;
            case "AdminIP":
                //Update config value
                updateConfigValue(sendButton, new Configuration("AdminIP", "Configurando: IP do servidor", txtAdminIP.getText().toString().isEmpty() ? null : txtAdminIP.getText().toString() + " " + txtAdminIPPort.getText().toString(), true));
                break;
            case "LessGPRS":
                //Update config value
                updateConfigValue(sendButton, new Configuration("LessGPRS", "Configurando: Uso reduzido de dados", null, swLessGprs.isChecked()));
                break;
            case "Move":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Move", swMove.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de movimentação", String.valueOf(sbMove.getProgress()), swMove.isChecked()));
                break;
            case "Speed":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Speed", swSpeed.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de velocidade", String.valueOf(sbSpeed.getProgress()), swSpeed.isChecked()));
                break;
            case "Shock":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Shock", swShock.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de vibração", null, swShock.isChecked()));
                break;
            case "StatusCheck":
                //Update config value
                updateConfigValue(sendButton, new Configuration("StatusCheck", "Solicitando informações", null, true));
                break;
            case "Begin":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Begin", "Inicializando dispositivo", null, true));
                break;
            case "IMEI":
                //Update config value
                updateConfigValue(sendButton, new Configuration("IMEI", "Configurando: IMEI do dispositivo", tracker.getIMEI(), true, "Confirmado durante durante a inclusão."));
                break;
            case "TimeZone":
                //Update config value
                updateConfigValue(sendButton, new Configuration("TimeZone", "Configurando: Fuso horário", null, true));
                break;
            case "Reset":
                //Update config value
                updateConfigValue(sendButton, new Configuration("Reset", "Solicitando reinicialização", null, true));
                break;
        }
    }

    @OnClick(R.id.btnSendCommunication)
    public void sendCommunicationConfig(final View button)
    {
        //Create alert with options
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Enviar configuração?")
                .setIcon(R.drawable.ic_settings_grey_40dp)
                .setMessage("Esta altera o modo de comunicação do rastreador, deseja continuar?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Initialize a new DB transaction
                        WriteBatch transaction = firestoreDB.batch();

                        // Get tracker reference
                        final DocumentReference trackerReference = firestoreDB.document("Tracker/" + tracker.getID());

                        // Create configuration collection
                        final CollectionReference configCollection = trackerReference.collection("Configurations");

                        //Load related configurations
                        loadCommunicationMode(configCollection, transaction);

                        //Request a new configuration from server
                        transaction.update(trackerReference, "lastConfiguration", FieldValue.delete());

                        //Execute DB operation
                        transaction
                                .commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //Show update to user
                                        Snackbar.make(vwMain, "Configuração alterada com sucesso.", Snackbar.LENGTH_LONG).show();

                                        // Disable controls
                                        enableControls(button, false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        //If user not allowed to edit
                                        if(e.getMessage().contains("PERMISSION_DENIED"))
                                        {
                                            //Show error to user
                                            Snackbar.make(vwMain, "Permissão de alteração negada.", Snackbar.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            //Show error to user
                                            Snackbar.make(vwMain, "Erro ao alterar dados do rastreador.", Snackbar.LENGTH_LONG).show();
                                        }

                                        // Hide loading indicator on menu
                                        confirmMenu.setActionView(null);
                                    }
                                });

                        //Close dialog
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Close dialog
                        dialog.dismiss();
                    }
                }).create()
                .show();
    }

    @OnClick(R.id.btnSendOperationMode)
    public void sendOperationConfig(final View button)
    {
        //Create alert with options
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Enviar configuração?")
                .setIcon(R.drawable.ic_settings_grey_40dp)
                .setMessage("Esta alteração modifica as configurações SLEEP, SCHEDULE e FIX deseja continuar?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Initialize a new DB transaction
                        WriteBatch transaction = firestoreDB.batch();

                        // Get tracker reference
                        final DocumentReference trackerReference = firestoreDB.document("Tracker/" + tracker.getID());

                        // Create configuration collection
                        final CollectionReference configCollection = trackerReference.collection("Configurations");

                        // Load operation mode configuration
                        loadOperationMode(configCollection, transaction);

                        //Request a new configuration from server
                        transaction.update(trackerReference, "lastConfiguration", FieldValue.delete());

                        //Execute DB operation
                        transaction
                                .commit()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //Show update to user
                                        Snackbar.make(vwMain, "Configuração alterada com sucesso.", Snackbar.LENGTH_LONG).show();

                                        // Disable controls
                                        enableControls(button, false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //If user not allowed to edit
                                        if(e.getMessage().contains("PERMISSION_DENIED"))
                                        {
                                            //Show error to user
                                            Snackbar.make(vwMain, "Permissão de alteração negada.", Snackbar.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            //Show error to user
                                            Snackbar.make(vwMain, "Erro ao alterar dados do rastreador.", Snackbar.LENGTH_LONG).show();
                                        }

                                        // Hide loading indicator on menu
                                        confirmMenu.setActionView(null);
                                    }
                                });

                        //Close dialog
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Close dialog
                        dialog.dismiss();
                    }
                }).create()
                .show();
    }

    @OnClick(R.id.btnSendPeriodicUpdate)
    public void sendPeriodicUpdateConfig(final View button)
    {
        switch (rdgOperationMode.getCheckedRadioButtonId())
        {
            case R.id.operationActive:
                //Set FIX 'request' for active mode
                updateConfigValue(button, new Configuration("PeriodicUpdate", "Configurando: Localização periódica", findViewById(rdgFix.getCheckedRadioButtonId()).getTag().toString(), true));
                break;

            case R.id.operationStandBy:
                //Set FIX 'request' for standby mode
                updateConfigValue(button, new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s001n", true));
                break;

            case R.id.operationHibernate:
                //Set default FIX option to hibernate configuration
                updateConfigValue(button, new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s***n", true));
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tk102b, menu);

        //Save action menu item
        confirmMenu = menu.findItem(R.id.action_add);

        //Show loading indicator
        refreshMenu = menu.findItem(R.id.action_refresh);

        //If is loading data
        if(loading)
        {
            //Show loading indicator
            confirmMenu.setActionView(new ProgressBar(this));
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                //End activity (returns to parent activity)
                onBackPressed();

                //End method
                return true;

            case R.id.action_add:

                //Method called to save data
                onSettingsConfirmed();

                //End method
                return true;

            case R.id.action_refresh:

                //Show loading indicator
                confirmMenu.setActionView(new ProgressBar(this));

                //Hide loading icon
                item.setVisible(false);

                //End method
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Show confirmation dialog
    public void showAlert(String title, String description, int icon, DialogInterface.OnClickListener positiveButton)
    {
        //Create alert with options
        new AlertDialog.Builder(TK102BActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(title)
                .setIcon(icon)
                .setMessage(description)
                .setPositiveButton("Sim", positiveButton)
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Close dialog
                        dialog.dismiss();
                    }
                }).create()
                .show();
    }

    //Update individual configuration on DB
    private void updateConfigValue(final View button, final Configuration config)
    {
        //Ask for user confirmation before send config
        showAlert("Enviar configuração?", "Deseja enviar esta configuração ao rastreador?", R.drawable.ic_settings_grey_40dp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // Set loading indicator
                confirmMenu.setActionView(new ProgressBar(TK102BActivity.this));

                //Update configuration status to confirmed
                firestoreDB
                        .collection("Tracker/" + tracker.getID() + "/Configurations")
                        .document(config.getName())
                        .set(config)
                        .addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //Show update to user
                                Snackbar.make(vwMain, "Configuração (" + config.getName() + ") alterada com sucesso.", Snackbar.LENGTH_LONG).show();

                                // Disable controls
                                enableControls(button, false);

                                //Update tracker
                                firestoreDB.document("Tracker/" + tracker.getID()).update("lastConfiguration", FieldValue.delete());
                            }
                        });


                //Set loading indicator
                confirmMenu.setActionView(new ProgressBar(TK102BActivity.this));
            }
        });
    }

    //Save all configurations after INSERT tracker
    private void onSettingsConfirmed()
    {
        //If editing existing tracker
        if(editMode)
        {
            //Close activity
            finish();
        }
        else
        {
            // Set loading indicator on menu
            confirmMenu.setActionView(new ProgressBar(this));

            // Initialize a new DB transaction
            WriteBatch transaction = firestoreDB.batch();

            // Get tracker reference
            final DocumentReference trackerReference;

            //Check if inserting new tracker or resetting configurations
            if(getIntent().getBooleanExtra("ResetConfig", false))
            {
                //Load existing tracker reference
                trackerReference = firestoreDB.document("Tracker/" + tracker.getID());

                //Request configuration update from server
                transaction.update(trackerReference, "lastConfiguration", null);
            }
            else
            {
                //Create a new tracker reference;
                trackerReference = firestoreDB.collection("Tracker").document();

                //Get firebase user ID
                String userID = FirebaseAuth.getInstance().getUid();

                //Set tracker permission settings
                tracker.addUser(userID);
                tracker.addAdmin(userID);

                // Insert tracker on DB
                transaction.set(trackerReference, tracker);
            }

            // Create configuration collection
            final CollectionReference configCollection = trackerReference.collection("Configurations");

            //Inserting new tracker, load general configs
            transaction.set(configCollection.document("Begin"), new Configuration("Begin", "Inicializando dispositivo", null, true));
            transaction.set(configCollection.document("TimeZone"), new Configuration("TimeZone", "Configurando: Fuso horário", null, true));
            transaction.set(configCollection.document("StatusCheck"), new Configuration("StatusCheck", "Solicitando informações", null, true));
            transaction.set(configCollection.document("IMEI"), new Configuration("IMEI", "Configurando: IMEI do dispositivo", tracker.getIMEI(), true, "Confirmado durante durante a inclusão."));
            transaction.set(configCollection.document("Reset"), new Configuration("Reset", "Reiniciando dispositivo", null, true, "Configuração não solicitada até o momento."));

            //Load communication configs
            transaction.set(configCollection.document("AccessPoint"), new Configuration("AccessPoint", "Configurando: APN", txtAPN.getText().toString(), true));
            transaction.set(configCollection.document("APNUserPass"), new Configuration("APNUserPass", "Configurando: Usuário / Senha", txtAPNUser.getText().toString() + " " + txtAPNPassword.getText().toString(), true));
            transaction.set(configCollection.document("AdminIP"), new Configuration("AdminIP", "Configurando: Acesso ao Intelitrack", txtAdminIP.getText().toString().isEmpty() ? null : txtAdminIP.getText().toString() + " " + txtAdminIPPort.getText().toString(), true));
            transaction.set(configCollection.document("Admin"), new Configuration("Admin", swAdmin.isChecked() ? "Ativando: " : "Desativando: " + "Administrador SMS", txtAdmin.getText().toString().isEmpty() ? null : txtAdmin.getText().toString(), swAdmin.isEnabled()));
            transaction.set(configCollection.document("LessGPRS"), new Configuration("LessGPRS", swAdmin.isChecked() ? "Ativando: " : "Desativando: " + "Uso reduzido GPRS", null, swLessGprs.isChecked()));

            //Load communication mode configurations
            loadCommunicationMode(configCollection, transaction);

            //Load SLEEP/SCHEDULE/FIX configuration
            loadOperationMode(configCollection, transaction);

            //Load alert configs
            transaction.set(configCollection.document("Shock"), new Configuration("Shock", swShock.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de vibração", null, swShock.isChecked()));
            transaction.set(configCollection.document("Move"), new Configuration("Move", swMove.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de movimentação", String.valueOf(sbMove.getProgress()), swMove.isChecked()));
            transaction.set(configCollection.document("Speed"), new Configuration("Speed", swSpeed.isChecked() ? "Ativando: " : "Desativando: " + "Alerta de velocidade", String.valueOf(sbSpeed.getProgress()), swSpeed.isChecked()));

            //Execute DB operation
            transaction
                    .commit()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //Retrieve tracker ID
                            tracker.setID(trackerReference.getId());

                            // Go to the details page for the selected restaurant
                            Intent intent = new Intent(TK102BActivity.this, DetailActivity.class);

                            //Initialize progressNotification
                            ProgressNotification updateIndicator = new ProgressNotification(TK102BActivity.this, tracker);

                            //Request configuration update
                            updateIndicator.initialize();

                            //Create a configuration array
                            Map<String, Object> configuration = new HashMap<>();

                            //Set pending configuration status
                            configuration.put("step", "PENDING");
                            configuration.put("status", "Aguardando resposta do servidor");
                            configuration.put("description", "Iniciando configuração do dispositivo");
                            configuration.put("pending", 1);
                            configuration.put("progress", 0);
                            configuration.put("datetime", new Date());

                            //Update tracker
                            tracker.setLastConfiguration(configuration);

                            // Put tracker data on intent
                            intent.putExtra("Tracker", tracker);

                            //Start Detail activity
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //If user not allowed to edit
                            if(e.getMessage().contains("PERMISSION_DENIED"))
                            {
                                //Show error to user
                                Snackbar.make(findViewById(android.R.id.content), "Permissão de alteração negada.", Snackbar.LENGTH_LONG).show();
                            }
                            else
                            {
                                //Show error to user
                                Snackbar.make(findViewById(android.R.id.content), "Erro ao alterar dados do rastreador.", Snackbar.LENGTH_LONG).show();
                            }

                            // Hide loading indicator on menu
                            confirmMenu.setActionView(null);
                        }
                    });
        }
    }

    //Save SCHEDULE/SLEEP/FIX/SHOCK configurations
    private void loadOperationMode(CollectionReference configCollection, WriteBatch transaction)
    {
        //Load SLEEP/SCHEDULE/FIX configuration
        switch (rdgOperationMode.getCheckedRadioButtonId())
        {
            case R.id.operationActive:
                //Disable SLEEP/SCHEDULE
                transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Desativando: Modo de stand-by", null, false));
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Desativando: Modo de hibernação", null, false));

                //Set FIX according to user option
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", findViewById(rdgFix.getCheckedRadioButtonId()).getTag().toString(), true));
                break;

            case R.id.operationStandBy:

                //Disable SCHEDULE
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Desativando: Modo de hibernação", null, false));

                //Check FIX option
                if(rdgFix.getCheckedRadioButtonId() == R.id.rdShock)
                {
                    //Set SLEEP shock option
                    transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Ativando: Modo de stand-by por movimento", "shock", true));
                }
                else
                {
                    //Set SLEEP time option
                    transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Ativando: Modo de stand-by por requisição", "time", true));
                }

                //Set FIX 'request' option
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s001n", true));
                break;

            case R.id.operationHibernate:

                //Check FIX option
                if(rdgFix.getCheckedRadioButtonId() == R.id.rdShock)
                {
                    //Config 'deep shock'
                    transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Ativando: Modo de hibernação", "deepshock", true));
                    transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Desativando: Agendamento", null, false));
                }
                else
                {
                    //Config 'schedule'
                    transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Desativando: Modo standby", null, false));
                    transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Ativando: Modo de hibernação", findViewById(rdgFix.getCheckedRadioButtonId()).getTag().toString(), true));
                }

                //Default FIX option to hibernate configuration
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s***n", true));
                break;
        }
    }

    //Save GPRS/SMS/LessGPRS/Admin configurations
    private void loadCommunicationMode(CollectionReference configCollection, WriteBatch transaction)
    {
        //Check communication option
        if(rdgCommunicationMode.getCheckedRadioButtonId() == R.id.modeGPRS)
        {
            //Set GPRS mode
            transaction.set(configCollection.document("SMS"), new Configuration("SMS", "Desativando: Modo SMS", null, false, "Configuração desativada na configuração inicial"));
            transaction.set(configCollection.document("GPRS"), new Configuration("GPRS", "Ativando: Modo GPRS", null, true));
        }
        else
        {
            //Set SMS mode
            transaction.set(configCollection.document("SMS"), new Configuration("SMS", "Ativando: Modo SMS", null, true));
            transaction.set(configCollection.document("GPRS"), new Configuration("GPRS", "Desativando: Modo GPRS", null, false, "Configuração desativada na configuração inicial"));
        }
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

        //Check for errors
        if (e != null)
        {
            //Log error
            Log.w("ConfigurationListener", "onEvent:error", e);
            return;
        }

        for (DocumentChange change : documentSnapshots.getDocumentChanges())
        {
            //Transform document snapshot to configuration object
            Configuration config = change.getDocument().toObject(Configuration.class);

            //Load configuration layout
            loadConfiguration(config);
        }

        // if loading indicator is available
        if(confirmMenu != null)
        {
            //Cancel loading indicator
            confirmMenu.setActionView(null);

            //Enable refresh menu
            refreshMenu.setVisible(true);
        }

        // Set loading flag to false
        loading = false;
    }

    //Load current configuration on layout
    public void loadConfiguration(Configuration config)
    {
        // Find corresponding image view
        ImageView imgStatus = vwMain.findViewWithTag("img" + config.getName());

        //If image available
        if(imgStatus != null)
        {
            //Show current configuration status
            if (config.getStatus().get("step").equals("SUCCESS"))
            {
                //Load success icon
                imgStatus.setImageResource(R.drawable.status_ok);
            }
            else if (config.getStatus().get("step").equals("ERROR"))
            {
                //Load error icon
                imgStatus.setImageResource(R.drawable.status_error);
            }
            else if (config.getStatus().get("step").equals("CANCELED"))
            {
                //Load error icon
                imgStatus.setImageResource(R.drawable.status_warning);
            }
            else
            {
                //Load info icon
                imgStatus.setImageResource(R.drawable.ic_settings_grey_40dp);
            }
        }

        // Find edit indicator
        View editConfig = vwMain.findViewWithTag("edit" + config.getName());

        // If available
        if(editConfig != null)
        {
            //Hide edit indicator if config finished
            editConfig.setVisibility((boolean) config.getStatus().get("finished") ? View.GONE : View.VISIBLE);
        }

        // Find confirm button
        View confirm = vwMain.findViewWithTag("confirm" + config.getName());

        // If available
        if(confirm != null)
        {
            //Show button to confirm manually if SMS sent
            confirm.setVisibility(config.getStatus().get("step").toString().equals("SMS_SENT") ? View.VISIBLE : View.GONE);
        }

        // Find text view
        TextView txtStatus = vwMain.findViewWithTag("txt" + config.getName());

        // If available
        if(txtStatus != null)
        {
            //Update status
            txtStatus.setText("- " + config.getStatus().get("description").toString());
            txtStatus.setTextColor((boolean) config.getStatus().get("finished") ? Color.parseColor("#3f9d2c") : Color.RED);
        }

        //Update configuration values
        switch (config.getName())
        {
            case "SMS":
                //Set communication mode
                rdgCommunicationMode.check(config.isEnabled() ? R.id.modeSMS : R.id.modeGPRS);
                break;
            case "GPRS":
                //Set communication mode
                rdgCommunicationMode.check(config.isEnabled() ? R.id.modeGPRS : R.id.modeSMS);
                break;
            case "Admin":
                //Set sms admin config
                swAdmin.setChecked(config.isEnabled());
                txtAdmin.setText(config.getValue() != null ? config.getValue() : "");
                break;
            case "AdminIP":
                //Set ip admin config
                txtAdminIP.setText(config.getValue() != null ? config.getValue().split(" ")[0] : "");
                txtAdminIPPort.setText(config.getValue() != null ? config.getValue().split(" ")[1] : "");
                break;
            case "AccessPoint":
                //Set APN config
                txtAPN.setText(config.getValue());
                break;
            case "APNUserPass":
                //Set APN user and password config
                txtAPNUser.setText(config.getValue() != null ? config.getValue().split(" ")[0] : "");
                txtAPNPassword.setText(config.getValue() != null ? config.getValue().split(" ")[1] : "");
                break;
            case "Schedule":
                //If hibernation mode
                if(config.isEnabled())
                {
                    //Set both config options
                    rdgOperationMode.check(R.id.operationHibernate);
                    rdgFix.check(vwMain.findViewWithTag(config.getValue()).getId());
                }
            case "Sleep":
                //Set stand-by mode
                if(config.isEnabled()) rdgOperationMode.check(R.id.operationStandBy);
                break;
            case "PeriodicUpdate":
                //Set periodic update option
                View fixOption = vwMain.findViewWithTag(config.getValue());
                if(fixOption != null) rdgFix.check(fixOption.getId());
                break;
            case "LessGPRS":
                //Set less gprs config
                swLessGprs.setChecked(config.isEnabled());
                break;
            case "Move":
                //Set alert configuration
                swMove.setChecked(config.isEnabled());
                break;
            case "Speed":
                //Set alert configuration
                swSpeed.setChecked(config.isEnabled());
                break;
            case "Shock":
                //Set alert configuration
                swShock.setChecked(config.isEnabled());
                break;
            case "StatusCheck":
                //Set alert configuration
                lblStatusCheck.setText(config.getValue() != null ? config.getValue() : "Não informado pelo rastreador");
                break;
            case "IMEI":
                //Set alert configuration
                lblIMEI.setText(config.getValue() != null ? config.getValue() : "Não informado pelo rastreador");
                break;
        }
    }
}

package br.gov.dpf.intelitrack.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.heinrichreimersoftware.materialintro.app.OnNavigationBlockedListener;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.components.ProgressNotification;
import br.gov.dpf.intelitrack.entities.Configuration;
import br.gov.dpf.intelitrack.entities.Tracker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends SlideFragment implements EventListener<DocumentSnapshot>{

    //Hash table containing tracker settings
    private HashMap<String, String> settings;

    //Store tracker settings
    private Tracker tracker;

    //Database instance
    private FirebaseFirestore firestoreDB;

    //Listener to configuration changes
    private ListenerRegistration listener;

    //Indicates if user is allowed to go forward
    private boolean canProceed = false;
    private boolean canGoBack = true;

    //Indicates error on config process
    private boolean configError = false;

    @BindView(R.id.btnConfig) Button btnConfig;
    @BindView(R.id.txtTitle) TextView txtTitle;
    @BindView(R.id.txtDescription) TextView txtDescription;
    @BindView(R.id.txtProgress) TextView txtProgress;
    @BindView(R.id.imgTitle) ImageView imgTitle;

    public ConfigFragment()
    {
        // Get DB instance
        firestoreDB = FirebaseFirestore.getInstance();
    }

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_config, container, false);

        // Bind views
        ButterKnife.bind(this, root);

        return root;
    }

    public void loadSettings(HashMap<String, String> previousSettings)
    {
        //Save settings
        settings = previousSettings;
    }

    @OnClick(R.id.btnConfig)
    public void configTracker()
    {
        //Check if user clicked to proceed
        if(canProceed && !configError)
        {
            //Close current fragment (end wizard)
            nextSlide();
        }
        else
        {
            //Disable button
            btnConfig.setText("Aguarde...");
            btnConfig.setEnabled(false);

            //Configuration in progress, create loading animation
            RotateAnimation rotate = new RotateAnimation(
                    0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );

            //Define animation settings
            rotate.setDuration(3000);
            rotate.setRepeatCount(Animation.INFINITE);

            //Apply animation to image
            imgTitle.startAnimation(rotate);

            //User can't go back now
            canGoBack = false;

            //If tracker not inserted yet
            if(tracker == null)
            {
                //Call method to insert tracker on DB
                insertTracker();
            }
            else
            {
                //Call method to configure tracker
                insertConfigurations();
            }
        }
    }

    public void insertTracker()
    {
        //Get firebase user ID
        String userID = FirebaseAuth.getInstance().getUid();

        //Initialize tracker
        tracker = new Tracker();

        //Save tracker data
        tracker.setName(settings.get("TrackerName"));
        tracker.setDescription("");
        tracker.setIdentification(settings.get("TrackerID"));
        tracker.setIMEI(settings.get("TrackerIMEI"));
        tracker.setNetwork(settings.get("TrackerNetwork"));
        tracker.setPassword("123456");

        //Set tracker permission settings
        tracker.addUser(userID);
        tracker.addAdmin(userID);

        //Save tracker model
        tracker.setModel(settings.get("TrackerModel"));

        //Save tracker color
        tracker.setBackgroundColor("#99049f1e");

        //Set step description
        txtDescription.setText("Registrando rastreador na plataforma Intelitrack...");

        //Display progress indicator
        txtProgress.setVisibility(View.VISIBLE);

        //Perform async insert
        firestoreDB.collection("Tracker")
                .add(tracker)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference trackerReference) {
                        //Set step description
                        txtDescription.setText("Iniciando configuração do dispositivo...");

                        //Retrieve tracker ID
                        tracker.setID(trackerReference.getId());

                        //Tracker saved on DB, user can't go back now
                        canProceed = true;
                        canGoBack = false;

                        //Re-enable button
                        btnConfig.setText("Prosseguir");
                        btnConfig.setEnabled(true);

                        //Call method to insert tracker configurations on DB
                        insertConfigurations();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //If user not allowed to edit
                        if (e.getMessage().contains("PERMISSION_DENIED"))
                        {
                            //Show error to user
                            txtDescription.setError("Erro: Permissão de inclusão negada");
                        } else {
                            //Show error to user
                            txtDescription.setError("Erro: Falha ao registrar rastreador na plataforma InteliTrack");
                        }

                        //Re-enable button
                        btnConfig.setText("Tentar novamente");
                        btnConfig.setEnabled(true);

                        //Clear animation from image
                        imgTitle.clearAnimation();

                        //Clear tracker values
                        tracker = null;

                        //User can go back again
                        canGoBack = true;
                        canProceed = false;
                    }
                });
    }

    public void insertConfigurations()
    {
        // Initialize a new DB transaction
        WriteBatch transaction = firestoreDB.batch();

        // Get tracker reference
        DocumentReference trackerReference = firestoreDB.document("Tracker/" + tracker.getID());

        // Create configuration collection
        final CollectionReference configCollection = trackerReference.collection("Configurations");

        //Clear any previous configuration
        transaction.update(trackerReference, "lastConfiguration", FieldValue.delete());

        //Insert general configuration
        transaction.set(configCollection.document("Begin"), new Configuration("Begin", "Inicializando dispositivo", null, true));
        transaction.set(configCollection.document("TimeZone"), new Configuration("TimeZone", "Configurando: Fuso horário", null, true));
        transaction.set(configCollection.document("StatusCheck"), new Configuration("StatusCheck", "Solicitando informações", null, true));
        transaction.set(configCollection.document("Reset"), new Configuration("Reset", "Reiniciando dispositivo", null, true, "Não solicitado até o momento."));

        //Check if IMEI available (obtained during test step)
        if(tracker.getIMEI() != null)
        {
            //Set confirmation finished status
            transaction.set(configCollection.document("IMEI"), new Configuration("IMEI", "Configurando: IMEI do dispositivo", tracker.getIMEI(), true, "Confirmado durante os testes de inclusão."));
        }
        else
        {
            //Request confirmation
            transaction.set(configCollection.document("IMEI"), new Configuration("IMEI", "Configurando: IMEI do dispositivo", tracker.getIMEI(), true));
        }

        //Load communication settings;
        transaction.set(configCollection.document("AccessPoint"), new Configuration("AccessPoint", "Configurando: APN", tracker.loadAPN(), true));
        transaction.set(configCollection.document("APNUserPass"), new Configuration("APNUserPass", "Configurando: Usuário / Senha", tracker.loadAPNUserPass() + " " + tracker.loadAPNUserPass(), true));
        transaction.set(configCollection.document("AdminIP"), new Configuration("AdminIP", "Configurando: Acesso ao Intelitrack", null, true));
        transaction.set(configCollection.document("GPRS"), new Configuration("GPRS", "Configurando: Modo GPRS", null, true));
        transaction.set(configCollection.document("LessGPRS"), new Configuration("LessGPRS", "Configurando: Uso reduzido de dados", null, true));
        transaction.set(configCollection.document("SMS"), new Configuration("SMS", "Desativando: Modo SMS", null, false, "Modo GPRS selecionado"));
        transaction.set(configCollection.document("Admin"), new Configuration("Admin", "Desativando: Administrador SMS", null, false));

        //Check configuration selected by user
        switch (settings.get("TrackerConfig"))
        {
            case "configBatteryTime":
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s***n", true));
                transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Configurando: Modo de hibernação", null, false));
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Configurando: Modo de hibernação", settings.get("TrackerConfigValue"), true));
                break;
            case "configBatteryMove":
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", "fix060s***n", true));
                transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Configurando: Modo de hibernação", settings.get("TrackerConfigValue"), true));
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Configurando: Modo de hibernação", null, false));
                break;
            case "configDefault":
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização por requisição", "fix060s001n", true));
                transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Configurando: Modo de hibernação", "time", true));
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Configurando: Modo de hibernação", null, false));
                break;
            case "configRealTime":
                transaction.set(configCollection.document("PeriodicUpdate"), new Configuration("PeriodicUpdate", "Configurando: Localização periódica", settings.get("TrackerConfigValue"), true));
                transaction.set(configCollection.document("Sleep"), new Configuration("Sleep", "Desativando: Modo de hibernação", null, false));
                transaction.set(configCollection.document("Schedule"), new Configuration("Schedule", "Desativando: Agendamento", null, false));
                break;
        }

        //Insert alert configs
        transaction.set(configCollection.document("Move"), new Configuration("Move", "Desativando: Alerta de movimentação", null, false, "Modo SMS selecionado"));
        transaction.set(configCollection.document("Speed"), new Configuration("Speed", "Desativando: Alerta de velocidade", null, false, "Modo SMS selecionado"));
        transaction.set(configCollection.document("Shock"), new Configuration("Shock", "Desativando: Alerta de vibração", null, false));

        // Get shared preferences editor
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getIntroActivity()).edit();

        // Check if user wants to see notifications
        boolean blnShowNotification = settings.get("TrackerNotification").equals("enabled");

        //Save notification options
        editor.putBoolean(tracker.getID() + "_Notifications", blnShowNotification);

        //Save option on shared preferences
        editor.putBoolean(tracker.getID() + "_Notify_StatusCheck", blnShowNotification);
        editor.putBoolean(tracker.getID() + "_Notify_LowBattery", blnShowNotification);
        editor.putBoolean(tracker.getID() + "_Notify_Movement", blnShowNotification);
        editor.putBoolean(tracker.getID() + "_Notify_Shock", blnShowNotification);

        //Execute DB operation
        transaction
                .commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        //Commit changes on shared preferences
                        editor.apply();

                        //Initialize progressNotification
                        ProgressNotification updateIndicator = new ProgressNotification(getContext(), tracker);

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

                        //Monitor for changes on configuration
                        listener = firestoreDB.document("Tracker/" + tracker.getID()).addSnapshotListener(ConfigFragment.this);

                        //No errors on config so far
                        configError = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //If user not allowed to edit
                        if (e.getMessage().contains("PERMISSION_DENIED"))
                        {
                            //Show error to user
                            txtDescription.setText("Erro: Permissão de inclusão negada");
                        } else {
                            //Show error to user
                            txtDescription.setText("Erro: Falha ao registrar configurações");
                        }

                        //Error on configuration
                        configError = true;
                        canProceed = false;

                        //Stop rotation
                        imgTitle.clearAnimation();

                        //Disable button
                        btnConfig.setText("Tentar novamente");
                        btnConfig.setEnabled(true);
                    }
                });
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

        //Check if document was not deleted
        if(documentSnapshot != null && documentSnapshot.exists())
        {
            //Try to get configuration status
            Map<String, Object> configuration = (Map<String, Object>) documentSnapshot.get("lastConfiguration");

            //Check if configuration has not started yet
            if (configuration != null)
            {
                //Set configuration progress
                txtProgress.setText(configuration.get("progress").toString() + "%");

                //Set notification title
                txtTitle.setText(configuration.get("description").toString());
                txtDescription.setText(configuration.get("status").toString());

                //If configuration finished (no longer pending)
                if (configuration.get("step").equals("ERROR"))
                {
                    //Flag config error
                    configError = true;

                    //Stop rotation
                    imgTitle.clearAnimation();

                    //Disable button
                    btnConfig.setText("Tentar novamente");
                    btnConfig.setEnabled(true);

                    //Remove listener
                    listener.remove();
                }
                else if(configuration.get("step").equals("SUCCESS"))
                {
                    //Stop rotation
                    imgTitle.clearAnimation();

                    //Disable button
                    btnConfig.setText("Finalizar");
                    btnConfig.setEnabled(true);

                    //Remove listener
                    listener.remove();
                }
            }
        }
    }

    public void showAlert(Context context, int direction)
    {
        if(direction == OnNavigationBlockedListener.DIRECTION_FORWARD)
        {
            //Create alert with options
            new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Realizar configuração")
                    .setMessage("A configuração do dispositivo é necessária para prosseguir.")
                    .setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //If configuration not started yet
                            if (btnConfig.isEnabled()) {
                                //Initialize configuration
                                configTracker();
                            }
                            //Close dialog
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Agora não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Close dialog
                            dialog.dismiss();
                        }
                    }).create()
                    .show();
        }
        else
        {
            //Create alert with options
            new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle("Configuração iniciada")
                    .setMessage("Não é possível retornar após o início da configuração.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Close dialog
                            dialogInterface.dismiss();
                        }
                    }).create()
                    .show();
        }
    }


    @Override
    public boolean canGoBackward()
    {
        return canGoBack;
    }

    @Override
    public boolean canGoForward()
    {
        return canProceed;
    }
}

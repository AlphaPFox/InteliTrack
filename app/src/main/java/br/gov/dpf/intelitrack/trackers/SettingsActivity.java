package br.gov.dpf.intelitrack.trackers;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import br.gov.dpf.intelitrack.DetailActivity;
import br.gov.dpf.intelitrack.MainActivity;
import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.TrackerSettingsActivity;
import br.gov.dpf.intelitrack.components.AnimatingProgressBar;
import br.gov.dpf.intelitrack.components.TcpTask;
import br.gov.dpf.intelitrack.entities.Tracker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    //Default device model
    private String mModel = "tk102b";

    //Default color option
    private String mColor = "#99ff0000";

    //Menu item used to confirm settings
    private MenuItem confirmMenu;

    //Async TCP connection task
    private TcpTask mConnection;

    //Object representing the tracker to be inserted/updated
    private Tracker tracker;

    //Activity flags
    private boolean editMode;

    //Static fields
    public static final int REQUEST_PERMISSION = 1;
    public static final int REQUEST_CONTACTS = 1;

    //Bind views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.vwLoading) View vwLoading;
    @BindView(R.id.txtTrackerIdentification) EditText txtTrackerID;
    @BindView(R.id.txtTrackerIMEI) EditText txtTrackerIMEI;
    @BindView(R.id.txtTrackerPassword) EditText txtTrackerPassword;
    @BindView(R.id.txtTrackerName) EditText txtTrackerName;
    @BindView(R.id.txtMobileNetwork) EditText txtMobileNetwork;
    @BindView(R.id.txtNoTrackerDescription) TextView txtTrackerDescription;
    @BindView(R.id.lblTrackerIdentification) TextView lblTrackerID;
    @BindView(R.id.lblTrackerIMEI) TextView lblTrackerIMEI;
    @BindView(R.id.lblTrackerPassword) TextView lblTrackerPassword;
    @BindView(R.id.lblTrackerIdentificationSubtitle) TextView lblTrackerSubtitle;
    @BindView(R.id.lblTest) TextView lblTest;
    @BindView(R.id.pgrTest) AnimatingProgressBar pgrTest;
    @BindView(R.id.btnTest) Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Set activity layout
        setContentView(R.layout.activity_settings);

        //Bind views
        ButterKnife.bind(this);

        //Load toolbar
        setSupportActionBar(toolbar);

        //Check action bar
        if(getSupportActionBar() != null)
        {
            //Set back button on toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Check if activity was called to edit existing tracker
        if(getIntent().getIntExtra("Request", MainActivity.REQUEST_INSERT) == MainActivity.REQUEST_UPDATE)
        {
            //Load tracker from intent
            tracker = getIntent().getParcelableExtra("Tracker");

            //Set activity in edit mode
            editMode = true;

            //Load tracker data
            loadData();

            //Hide add floating action button
            findViewById(R.id.fab).setVisibility(View.GONE);
        }
        else
        {
            //Activity called to insert a new tracker, initialize object
            tracker = new Tracker();
        }

        //Handle click events for color options
        loadColors((GridLayout) findViewById(R.id.vwColors));

        //Handle click events for model options
        loadModels((LinearLayout) findViewById(R.id.vwModels));
    }

    @OnClick({R.id.fab, R.id.btnTest})
    public void settingsConfirmed()
    {
        //Method called to submit form
        onSettingsConfirmed();
    }

    @OnClick(R.id.txtMobileNetwork)
    public void selectMobileNetwork()
    {
        //Get mobile network list
        final String[] mobileNetworks = getResources().getStringArray(R.array.mobile_networks);

        //Create single choice selection dialog
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Escolha a operadora")
                .setSingleChoiceItems(mobileNetworks, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Update value on form
                        txtMobileNetwork.setText(mobileNetworks[which]);

                        //Close dialog
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    //Called when editing an existing tracker
    public void loadData()
    {
        //Load tracker settings
        txtTrackerName.setText(tracker.getName());
        txtTrackerPassword.setText(tracker.getPassword());
        txtTrackerIMEI.setText(tracker.getIMEI());
        txtTrackerID.setText(tracker.getIdentification());
        txtTrackerDescription.setText(tracker.getDescription());
        txtMobileNetwork.setText(tracker.getNetwork());

        //Hide model selected
        findViewById(R.id.vwModelCardView).setVisibility(View.GONE);

        //Check support action bar
        if(getSupportActionBar() != null)
        {
            //Change activity title
            getSupportActionBar().setTitle(tracker.getName());
        }

        //Update layout according to tracker model
        changeLabels(tracker.getModel());
    }

    //Called when editing or inserting a new tracker
    public void onSettingsConfirmed()
    {
        //Get tracker name and identification
        String trackerName = txtTrackerName.getText().toString();
        String trackerIdentification = txtTrackerID.getText().toString().replaceAll("[^0-9]", "");
        String trackerIMEI = txtTrackerIMEI.getText().toString();
        String trackerPassword = txtTrackerPassword.getText().toString();
        String trackerDescription = txtTrackerDescription.getText().toString();
        String trackerNetwork = txtMobileNetwork.getText().toString();

        //Check user input
        if(mModel.equals("pt39") || mModel.equals("gt02"))
        {
            //Alert user, unsupported models
            Snackbar.make(txtTrackerName, "Modelo atualmente não suportado pela plataforma", Snackbar.LENGTH_LONG).show();
        }
        else if(trackerName.length() < 5)
        {
            //Show error
            txtTrackerName.setError("Campo obrigatório (mínimo 5 caracteres)");
            txtTrackerName.requestFocus();
        }
        else if(trackerIdentification.length() < 5)
        {
            //Show error
            txtTrackerID.setError("Campo obrigatório (mínimo 5 caracteres)");
            txtTrackerID.requestFocus();
        }
        else if(mModel.equals("tk102b") && trackerIdentification.length() != 11)
        {
            //Show error
            txtTrackerID.setError("Preencha no formato: 6799998888");
            txtTrackerID.requestFocus();
        }
        else
        {
            //Save tracker data
            tracker.setName(trackerName);
            tracker.setDescription(trackerDescription);
            tracker.setIdentification(trackerIdentification);
            tracker.setIMEI(trackerIMEI);
            tracker.setPassword(trackerPassword);
            tracker.setNetwork(trackerNetwork);

            //Check if user set password
            if(tracker.getPassword().isEmpty())
            {
                //Set default password
                tracker.setPassword("123456");
            }

            //Save tracker model
            tracker.setModel(mModel);

            //Save tracker color
            tracker.setBackgroundColor(mColor);

            // Input is validated, set loading indicator on menu
            confirmMenu.setActionView(new ProgressBar(this));

            //Check if activity is in edit mode
            if(editMode)
            {
                // Perform update on DB
                FirebaseFirestore.getInstance()
                        .collection("Tracker")
                        .document(tracker.getID())
                        .set(tracker)
                        .addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid) {

                                // Go to the details page for the selected restaurant
                                Intent intent = new Intent(SettingsActivity.this, DetailActivity.class);

                                // Put tracker data on intent
                                intent.putExtra("Tracker", tracker);

                                // Start detail activity
                                startActivity(intent);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
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
            else
            {
                //Check if already performed a test before
                if(mConnection != null)
                {
                    //Cancel previous test
                    mConnection.cancel(true);

                    //Reset progress
                    pgrTest.resetProgress();
                }

                //Perform test to contact tracker
                mConnection = new TcpTask("192.168.1.200", "5001");

                //Add expected server communication pattern
                mConnection.addResponse("AUTH: OK", "TEST_" + tracker.getModel() + "_" + tracker.getIdentification() + "_" + tracker.getPassword());

                //Initialize first step
                vwLoading.setVisibility(View.VISIBLE);

                //Scroll to top
                ((ScrollView) findViewById(R.id.vwMainScroll)).smoothScrollTo(0, 0);

                //Initialize connection
                mConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpTask.OnMessageReceived()
                {
                    @Override
                    public void messageReceived(String message)
                    {

                        //Check message status
                        if (message.equals("CONNECTED"))
                        {
                            //Connection OK -> Update status (step 1/5)
                            pgrTest.setProgress(10);
                            pgrTest.setSecondaryProgress(20);

                            //Update text
                            lblTest.setText("Autenticando com o servidor Intelitrack...");
                        }
                        else if (message.equals("AUTH: OK"))
                        {
                            //Authentication OK -> Update status (step 2/5)
                            pgrTest.setProgress(20);
                            pgrTest.setSecondaryProgress(45);

                            //Update text
                            lblTest.setText("Enviando mensagem ao rastreador...");
                        }
                        else if (message.equals("SMS SENT"))
                        {
                            //SMS sent to tracker -> Update status (step 3/5)
                            pgrTest.setProgress(45);
                            pgrTest.setSecondaryProgress(80);

                            //Update text
                            lblTest.setText("Aguardando confirmação de entrega...");
                        }
                        else if (message.equals("DELIVERY REPORT"))
                        {
                            //SMS delivered to tracker -> Update status (step 4/5)
                            pgrTest.setProgress(80);
                            pgrTest.setSecondaryProgress(90);

                            //Update text
                            lblTest.setText("Esperando resposta do rastreador...");
                        }
                        else if (message.startsWith("IMEI:"))
                        {
                            //Tracker IMEI received -> Update status (step 5/5)
                            pgrTest.setIndeterminate(true);

                            //Update text
                            lblTest.setText("Teste finalizado, carregando configurações...");

                            //Save IMEI from tracker
                            tracker.setIMEI(message.substring(5).trim());

                            //Test finished successfully, ending connection
                            mConnection.cancel(true);

                            //Start config activity after 1,5 secs
                            btnTest.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //Create intent to call next activity (Tracker Configurations)
                                    Intent intent = new Intent(SettingsActivity.this, TK102BActivity.class);

                                    //Save create date
                                    tracker.setLastUpdate(new Date());

                                    //Put tracker data on intent
                                    intent.putExtra("Tracker", tracker);

                                    //Inform activity intention: insert new tracker
                                    intent.putExtra("Request", MainActivity.REQUEST_INSERT);

                                    //Start next activity
                                    startActivityForResult(intent, MainActivity.REQUEST_INSERT);
                                }
                            }, 1500);
                        }
                        else
                        {
                            //Unexpected step, show message error message to user
                            pgrTest.setIndeterminate(true);

                            //Show error message
                            lblTest.setText(message);

                            // Hide loading indicator on menu
                            confirmMenu.setActionView(null);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        //Check if permission granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            //Build contacts intent
            Intent contacts = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

            //Start android activity
            startActivityForResult(contacts, REQUEST_CONTACTS);
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, final Intent intent)
    {
        //Check if this is a request contact activity result
        if (requestCode == REQUEST_CONTACTS && resultCode == RESULT_OK)
        {
            //Cursors and strings used to query data
            Cursor cursor1, cursor2;
            String contactName, contactNumber, contactID, query_result;

            //Get data from intent
            Uri data = intent.getData();

            //Check if data is valid
            if(data != null)
            {
                //Perform first query on result
                cursor1 = getContentResolver().query(data, null, null, null, null);

                //If valid result
                if (cursor1 != null && cursor1.moveToFirst()) {
                    //Get contact data
                    contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    contactID = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    query_result = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    //Open second query
                    cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID, null, null);

                    //Check if phone number is available
                    if (query_result.equals("1") && cursor2 != null) {

                        //For each available phone number
                        while (cursor2.moveToNext()) {
                            //Get phone number
                            contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            //Update text fields
                            txtTrackerName.setText(contactName);
                            txtTrackerID.setText(contactNumber.replaceAll("[^0-9]", ""));
                        }

                        //Close second cursor
                        cursor2.close();
                    }

                    //Close first cursor
                    cursor1.close();
                }
            }
        }
        else if(resultCode == MainActivity.RESULT_CANCELED)
        {
            // User canceled (back pressed), hide loading
            confirmMenu.setActionView(null);
        }
        else
        {
            // User completed operation, send result to MainActivity
            setResult(resultCode);

            // End activity (returns to parent activity -> OnActivityResult)
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);

        //Save action menu item
        confirmMenu = menu.findItem(R.id.action_add);

        //If in edit mode
        if(editMode)
        {
            menu.add(Menu.NONE, R.id.action_tracker_settings, Menu.NONE, "Configurações do dispositivo");
            menu.add(Menu.NONE, R.id.action_notification_settings, Menu.NONE, "Opções de notificação");
        }
        else
        {
            //Show contacts option menu if inserting new tracker
            menu.findItem(R.id.action_contacts).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                //End activity (returns to parent activity)
                finish();

                //End method
                return true;

            case R.id.action_add:

                //Method called to save data
                onSettingsConfirmed();

                //End method
                return true;

            case R.id.action_tracker_settings:
            case R.id.action_notification_settings:

                //Create intent to call next activity (Tracker Configurations)
                Intent intent = new Intent(SettingsActivity.this, TK102BActivity.class);

                //Put tracker data on intent
                intent.putExtra("Tracker", tracker);

                //Inform activity intention: change tracker model
                intent.putExtra("UpdateNotifications", (item.getItemId() == R.id.action_notification_settings));

                //Inform activity intention: insert new tracker
                intent.putExtra("Request", MainActivity.REQUEST_UPDATE);

                //Start next activity
                startActivity(intent);

                //End method
                return true;

            case R.id.action_contacts:

                //Check if permission is granted to this app already
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                {
                    //Build contacts intent
                    Intent contacts = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

                    //Start android activity
                    startActivityForResult(contacts, REQUEST_CONTACTS);
                }
                else
                {
                    //If no permission yet, request from user
                    ActivityCompat.requestPermissions(this,new String[]{ android.Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION);
                }

                //End method
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        //End activity (returns to parent activity)
        finish();
    }

    @Override
    protected void onDestroy()
    {
        if(mConnection != null)
        {
            mConnection.cancel(true);
            mConnection.disconnect();
        }
        super.onDestroy();
    }

    public void changeLabels(String model)
    {
        //Get resources manager
        Resources resources = getResources();

        //Check model value
        switch (model) {
            case "spot":
                //Set text values
                lblTrackerID.setText(resources.getString(R.string.lblFeedID));
                lblTrackerSubtitle.setText(resources.getString(R.string.lblFeedIDSubtitle));
                txtTrackerID.setHint(resources.getString(R.string.txtFieldIDHint));

                //Change input type to allow text
                txtTrackerID.setInputType(InputType.TYPE_CLASS_TEXT);

                //Set password visibility
                txtTrackerPassword.setVisibility(View.VISIBLE);
                lblTrackerPassword.setVisibility(View.VISIBLE);
                txtTrackerIMEI.setVisibility(View.GONE);
                lblTrackerIMEI.setVisibility(View.GONE);
                break;

            case "st940":
                //Set text values
                lblTrackerID.setText(resources.getString(R.string.lblSuntechID));
                lblTrackerSubtitle.setText(resources.getString(R.string.lblSuntechSubtitle));
                txtTrackerID.setHint(resources.getString(R.string.txtSuntechHint));

                //Change input type to allow text
                txtTrackerID.setInputType(InputType.TYPE_CLASS_NUMBER);

                //Set password visibility
                txtTrackerPassword.setVisibility(View.GONE);
                lblTrackerPassword.setVisibility(View.GONE);
                txtTrackerIMEI.setVisibility(View.GONE);
                lblTrackerIMEI.setVisibility(View.GONE);
                break;

            default:
                //Set text values
                lblTrackerID.setText(resources.getString(R.string.lblPhoneNumber));
                lblTrackerSubtitle.setText(resources.getString(R.string.lblPhoneNumberSubtitle));
                txtTrackerID.setHint(resources.getString(R.string.txtPhoneNumberHint));

                //Change input type to allow text
                txtTrackerID.setInputType(InputType.TYPE_CLASS_PHONE);

                //Set password visibility
                txtTrackerPassword.setVisibility(View.VISIBLE);
                lblTrackerPassword.setVisibility(View.VISIBLE);
                txtTrackerIMEI.setVisibility(View.VISIBLE);
                lblTrackerIMEI.setVisibility(View.VISIBLE);

                break;
        }

        //If activity is not in edit mode
        if(!editMode)
        {
            //Clear any previous text on identification
            txtTrackerID.setText("");
        }
    }

    public void loadModels(final LinearLayout vwModels)
    {
        //For each device model
        for (int i = 0; i < vwModels.getChildCount(); i++)
        {
            //Get checkbox representing a color
            LinearLayout vwModel = (LinearLayout) vwModels.getChildAt(i);

            vwModel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //When clicked, for each device model
                    for (int i = 0; i < vwModels.getChildCount(); i++)
                    {
                        //Set background transparent = not selected
                        vwModels.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.Transparent));
                    }

                    //Set background color only for the selected device model
                    view.setBackgroundColor(getResources().getColor(R.color.colorSelected));

                    //Get current selected model
                    mModel = view.getTag().toString();

                    //Change layout labels according to selected model
                    changeLabels(mModel);
                }
            });

            //If activity is in edit mode and this is the corresponding tracker color
            if(editMode && vwModel.getTag().equals(tracker.getModel()))
            {
                //Set checked status
                vwModel.setBackgroundColor(getResources().getColor(R.color.colorSelected));

                //Get current selected color
                mModel = tracker.getModel();
            }
        }

        //If activity is not in edit mode
        if(!editMode)
        {
            //Select first item as default
            vwModels.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.colorSelected));
        }
    }

    public void loadColors(final GridLayout vwColors){

        //For each device model
        for (int i = 0; i < vwColors.getChildCount(); i++)
        {
            //Get checkbox representing a color
            AppCompatCheckBox vwColor = (AppCompatCheckBox) vwColors.getChildAt(i);

            //Set on click event listener for this checkbox
            vwColor.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //When clicked, for each device model
                    for (int i = 0; i < vwColors.getChildCount(); i++)
                    {
                        //Set checkbox state to unchecked
                        ((AppCompatCheckBox) vwColors.getChildAt(i)).setChecked(false);
                    }

                    //Get selected checkbox
                    AppCompatCheckBox checkbox = ((AppCompatCheckBox) view);

                    //Set checked status
                    checkbox.setChecked(true);

                    //Get current selected color
                    mColor = checkbox.getTag().toString();
                }
            });

            //If activity is in edit mode and this is the corresponding tracker color
            if(editMode && vwColor.getTag().equals(tracker.getBackgroundColor()))
            {
                //Set checked status
                vwColor.setChecked(true);

                //Get current selected color
                mColor = tracker.getBackgroundColor();
            }
        }

        //If activity is not in edit mode
        if(!editMode)
        {
            //Select first item as default
            ((AppCompatCheckBox) vwColors.getChildAt(0)).setChecked(true);
        }
    }
}

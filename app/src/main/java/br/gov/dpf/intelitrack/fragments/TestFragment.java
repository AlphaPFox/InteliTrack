package br.gov.dpf.intelitrack.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.util.HashMap;

import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.components.CheckList;
import br.gov.dpf.intelitrack.components.TcpTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends SlideFragment
{
    //Hash table containing tracker settings
    private HashMap<String, String> settings;

    //Async TCP connection task
    private TcpTask mConnection;

    //Indicates if user is allowed to go forward
    private boolean canProceed = false;

    @BindView(R.id.txtStepDescription) TextView txtStepDescription;
    @BindView(R.id.btnTest) Button btnTest;
    @BindView(R.id.checkStep1) CheckList checkStep1;
    @BindView(R.id.checkStep2) CheckList checkStep2;
    @BindView(R.id.checkStep3) CheckList checkStep3;
    @BindView(R.id.checkStep4) CheckList checkStep4;
    @BindView(R.id.checkStep5) CheckList checkStep5;

    @OnClick(R.id.btnTest)
    public void performTest(View button)
    {
        //Get resources manager
        final Resources resources = getResources();

        //Reset any checklist previous status
        resetChecklist();

        //Disable button
        btnTest.setText("Aguarde...");
        btnTest.setEnabled(false);

        //Show text view
        txtStepDescription.setVisibility(View.VISIBLE);

        //Initialize new connection
        mConnection = new TcpTask("192.168.1.200", "5001");

        //Add expected server communication pattern
        mConnection.addResponse("AUTH: OK", "TEST_" + settings.get("TrackerModel") + "_" + settings.get("TrackerID") + "_123456");

        //Initialize first step
        checkStep1.startProgress();

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
                    checkStep1.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize second step
                    checkStep2.startProgress();
                }
                else if (message.equals("AUTH: OK"))
                {
                    //Authentication OK -> Update status (step 2/5)
                    checkStep2.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize third step
                    checkStep3.startProgress();
                }
                else if (message.equals("SMS SENT"))
                {
                    //SMS sent to tracker -> Update status (step 3/5)
                    checkStep3.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize fourth step
                    checkStep4.startProgress();
                }
                else if (message.equals("DELIVERY REPORT"))
                {
                    //SMS delivered to tracker -> Update status (step 4/5)
                    checkStep4.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize last step
                    checkStep5.startProgress();
                }
                else if (message.startsWith("IMEI:"))
                {
                    //Tracker IMEI received -> Update status (step 5/5)
                    checkStep5.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Test finished successfully, ending connection
                    mConnection.cancel(true);
                    mConnection.disconnect();

                    //Save IMEI from tracker
                    settings.put("TrackerIMEI", message.substring(5).trim());

                    //Test finished
                    canProceed = true;

                    //Enable button
                    btnTest.setText("Testar novamente");
                    btnTest.setEnabled(true);
                }
                else
                {
                    //Unexpected step, show message error message to user
                    Snackbar.make(btnTest, message, Snackbar.LENGTH_LONG).show();

                    //Enable button
                    btnTest.setText("Tentar novamente");
                    btnTest.setEnabled(true);

                    //Update checklist (unexpected step)
                    updateChecklist(resources.getDrawable(R.drawable.status_error));
                }
            }
        });

        //Allow to restart test after 30 seconds
        btnTest.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                btnTest.setText("Reinciar teste");
                btnTest.setEnabled(true);
            }
        }, 30000);
    }


    public TestFragment()
    {
        // Required empty public constructor
    }

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_test, container, false);

        // Bind views
        ButterKnife.bind(this, root);

        return root;
    }

    public void loadSettings(HashMap<String, String> previousSettings)
    {
        //Save settings
        settings = previousSettings;

        //Check if connection started previously
        if(mConnection != null)
        {
            //Cancel previous connection
            mConnection.cancel(true);
            mConnection.disconnect();
        }

        //Reset any checklist previous status
        resetChecklist();

        //Set default button text
        btnTest.setText("Iniciar teste");
        btnTest.setEnabled(true);

        //Reset flag
        canProceed = false;
    }


    public HashMap<String, String> getSettings()
    {
        //Check if connection started previously
        if(mConnection != null)
        {
            //Cancel previous connection
            mConnection.cancel(true);
            mConnection.disconnect();
        }

        //Return current fragment settings
        return settings;
    }

    private void resetChecklist()
    {
        //Reset checklist
        checkStep1.setDefault();
        checkStep2.setDefault();
        checkStep3.setDefault();
        checkStep4.setDefault();
        checkStep5.setDefault();
    }
    private void updateChecklist(Drawable drawable)
    {
        //Search running step and update status
        if(checkStep1.isRunning()) checkStep1.setResult(drawable);
        else if (checkStep2.isRunning()) checkStep2.setResult(drawable);
        else if (checkStep3.isRunning()) checkStep3.setResult(drawable);
        else if (checkStep4.isRunning()) checkStep4.setResult(drawable);
        else if (checkStep5.isRunning()) checkStep5.setResult(drawable);
    }

    public void showAlert(Context context)
    {
        //Create alert with options
        new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Teste não finalizado!")
                .setMessage("Finalize o teste de comunicação antes de prosseguir.")
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

    @Override
    public boolean canGoForward()
    {
        return canProceed;
    }

    @Override
    public void onDestroy()
    {
        if(mConnection != null)
        {
            mConnection.cancel(true);
            mConnection.disconnect();
        }
        super.onDestroy();
    }
}

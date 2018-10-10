package br.gov.dpf.intelitrack.fragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

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

    private TcpTask mConnection;
    private String mTrackerNetwork, mTrackerModel, mTrackerID;

    @BindView(R.id.txtStepDescription) TextView txtStepDescription;
    @BindView(R.id.btnTest) Button btnTest;
    @BindView(R.id.checkStep1) CheckList checkStep1;
    @BindView(R.id.checkStep2) CheckList checkStep2;
    @BindView(R.id.checkStep3) CheckList checkStep3;
    @BindView(R.id.checkStep4) CheckList checkStep4;

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
        mConnection.addResponse("AUTH: OK", "TEST_" + mTrackerModel + "_" + mTrackerID + "_123456");

        //Initialize first step
        checkStep1.startProgress();

        //Initialize connection
        mConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new TcpTask.OnMessageReceived()
        {
            @Override
            public void messageReceived(String message)
            {
                //Authentication accepted
                if (message.equals("AUTH: OK"))
                {
                    //Update status (step 1/4)
                    checkStep1.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize second step
                    checkStep2.startProgress();
                }
                else if (message.equals("SMS SENT"))
                {
                    //Update status (step 2/4)
                    checkStep2.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize third step
                    checkStep3.startProgress();
                }
                else if (message.equals("DELIVERY REPORT"))
                {
                    //Update status (step 3/4)
                    checkStep3.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Initialize last step
                    checkStep4.startProgress();
                }
                else if (message.startsWith("IMEI:"))
                {
                    //Update status (step 4/4)
                    checkStep4.setResult(resources.getDrawable(R.drawable.status_ok));

                    //Test finished successfully, ending connection
                    mConnection.cancel(true);

                    //Enable button
                    btnTest.setText("Testar novamente");
                    btnTest.setEnabled(true);
                }
                else
                {
                    //Unexpected step, show message error message to user
                    Snackbar.make(checkStep1, message, Snackbar.LENGTH_LONG).show();

                    //Enable button
                    btnTest.setText("Tentar novamente");
                    btnTest.setEnabled(true);

                    //Update checklist (unexpected step)
                    updateChecklist(resources.getDrawable(R.drawable.status_error));
                }
            }
        });
    }


    public TestFragment()
    {
        // Required empty public constructor
    }

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_test, container, false);

        // Bind views
        ButterKnife.bind(this, root);

        return root;
    }

    public void setTrackerID(final String trackerID, String trackerMobileNetwork, final String trackerModel)
    {
        //Save tracker settings
        mTrackerID = trackerID;
        mTrackerNetwork = trackerMobileNetwork;
        mTrackerModel = trackerModel;

        //Check if connection started previously
        if(mConnection != null)
        {
            //Cancel previous connection
            mConnection.cancel(true);
        }

        //Reset any checklist previous status
        resetChecklist();

        //Set default button text
        btnTest.setText("Iniciar teste");
        btnTest.setEnabled(true);
    }

    private void resetChecklist()
    {
        //Reset checklist
        checkStep1.setDefault();
        checkStep2.setDefault();
        checkStep3.setDefault();
        checkStep4.setDefault();
    }
    private void updateChecklist(Drawable drawable)
    {
        if(checkStep1.isRunning()) checkStep1.setResult(drawable);
        else if (checkStep2.isRunning()) checkStep2.setResult(drawable);
        else if (checkStep3.isRunning()) checkStep3.setResult(drawable);
        else if (checkStep4.isRunning()) checkStep4.setResult(drawable);
    }
}

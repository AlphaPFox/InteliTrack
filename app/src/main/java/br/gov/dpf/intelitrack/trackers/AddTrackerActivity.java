package br.gov.dpf.intelitrack.trackers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.OnNavigationBlockedListener;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import br.gov.dpf.intelitrack.R;
import br.gov.dpf.intelitrack.fragments.ConfigFragment;
import br.gov.dpf.intelitrack.fragments.OptionsFragment;
import br.gov.dpf.intelitrack.fragments.ModelFragment;
import br.gov.dpf.intelitrack.fragments.SettingsFragment;
import br.gov.dpf.intelitrack.fragments.TestFragment;

import static br.gov.dpf.intelitrack.trackers.SettingsActivity.REQUEST_CONTACTS;

public class AddTrackerActivity extends IntroActivity {

    //Initialize fragments used on this context
    final ModelFragment modelfragment = ModelFragment.newInstance();
    final SettingsFragment settingsFragment = SettingsFragment.newInstance();
    final TestFragment testFragment = TestFragment.newInstance();
    final OptionsFragment optionsFragment = OptionsFragment.newInstance();
    final ConfigFragment configFragment = ConfigFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Fist slide - Welcome
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Inclusão de novo rastreador")
                .description("Siga as instruções contidas nos próximos passos para cadastrar um novo rastreador na plataforma Intelitrack.")
                .image(R.drawable.tracker_add)
                .background(R.color.page_tracker_add)
                .backgroundDark(R.color.page_tracker_add_dark)
                .scrollable(true)
                .build());

        //Second slide - Select model
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_select)
                .backgroundDark(R.color.page_tracker_select_dark)
                .fragment(modelfragment)
                .build());


        //Third slide - Tracker identification
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_id)
                .backgroundDark(R.color.page_tracker_id_dark)
                .fragment(settingsFragment)
                .build());

        //Fourth slide - Perform communication test
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_test)
                .backgroundDark(R.color.page_tracker_test_dark)
                .fragment(testFragment)
                .build());

        //Fifth slide - Select options
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_config)
                .backgroundDark(R.color.page_tracker_config_dark)
                .fragment(optionsFragment)
                .build());

        //Last fragment - Tracker configuration
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_success)
                .backgroundDark(R.color.page_tracker_success_dark)
                .fragment(configFragment)
                .build());

        //Define back button function
        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);

        addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                switch (position)
                {
                    case 2:
                        //Get selected tracker model from previous step
                        settingsFragment.loadSettings(modelfragment.getSettings());
                        break;
                    case 3:
                        //Get tracker identification settings
                        testFragment.loadSettings(settingsFragment.getSettings());
                        break;
                    case 4:
                        //Get tracker test result
                        optionsFragment.loadSettings(testFragment.getSettings());
                        break;
                    case 5:
                        //Get tracker configuration options
                        configFragment.loadSettings(optionsFragment.getSettings());
                        break;
                }

            }
            @Override public void onPageScrollStateChanged(int state) { }
        });

        addOnNavigationBlockedListener(new OnNavigationBlockedListener() {
            @Override
            public void onNavigationBlocked(int position, int direction)
            {
                switch (position)
                {
                    case 2:
                        settingsFragment.showRequiredFields();
                        break;
                    case 3:
                        testFragment.showAlert(AddTrackerActivity.this);
                        break;
                    case 4:
                        optionsFragment.showAlert(AddTrackerActivity.this);
                        break;
                    case 5:
                        configFragment.showAlert(AddTrackerActivity.this, direction);
                        break;
                }
            }
        });
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
    protected void onActivityResult (int requestCode, int resultCode, final Intent intent) {

        //Cursors and strings used to query data
        Cursor cursor1, cursor2;
        String contactName, contactNumber, contactID, query_result;

        if (intent != null)
        {
            //Get data from intent
            Uri data = intent.getData();

            //Check if data is valid
            if (data != null) {
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
                            settingsFragment.loadContact(contactName, contactNumber.replaceAll("[^0-9]", ""));
                        }

                        //Close second cursor
                        cursor2.close();
                    } else {
                        //Update text fields
                        settingsFragment.loadContact(contactName, "");
                    }

                    //Close first cursor
                    cursor1.close();
                }
            }
        }
    }
}

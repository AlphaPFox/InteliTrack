package br.gov.dpf.intelitrack;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.OnNavigationBlockedListener;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;

import junit.framework.Test;

import br.gov.dpf.intelitrack.fragments.ModelFragment;
import br.gov.dpf.intelitrack.fragments.SettingsFragment;
import br.gov.dpf.intelitrack.fragments.TestFragment;

public class AddTrackerActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Initialize fragments used on this context
        final ModelFragment modelfragment = ModelFragment.newInstance();
        final SettingsFragment settingsFragment = SettingsFragment.newInstance();
        final TestFragment testFragment = TestFragment.newInstance();

        //Fist slide - Welcome
        addSlide(new SimpleSlide.Builder()
                .titleHtml("Inclusão de novo rastreador")
                .description("Siga as instruções contidas nos próximos passos para cadastrar um novo rastreador na plataforma Intelitrack.")
                .image(R.drawable.tracker_add)
                .background(R.color.page_tracker_add)
                .backgroundDark(R.color.page_tracker_add_dark)
                .scrollable(true)
                .build());

        //Second slide - Tracker model
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_select)
                .backgroundDark(R.color.page_tracker_select_dark)
                .fragment(modelfragment)
                .build());


        //Third slide - Welcome
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_id)
                .backgroundDark(R.color.page_tracker_id_dark)
                .fragment(settingsFragment)
                .build());


        //Fourth slide - Welcome
        addSlide(new FragmentSlide.Builder()
                .background(R.color.page_tracker_test)
                .backgroundDark(R.color.page_tracker_test_dark)
                .fragment(testFragment)
                .buttonCtaLabel("Pular etapa")
                .build());

        addSlide(new SimpleSlide.Builder()
                .titleHtml("Inclusão de novo rastreador")
                .description("Siga as instruções contidas nos próximos passos para cadastrar um novo rastreador na plataforma Intelitrack.")
                .image(R.drawable.tracker_add)
                .background(R.color.page_tracker_id)
                .backgroundDark(R.color.page_tracker_id_dark)
                .scrollable(true)
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
                        settingsFragment.setModel(modelfragment.getSelectedModel());
                        break;
                    case 3:
                        //Hide keyboard from previous form if visible
                        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

                        //Get tracker settings
                        testFragment.setTrackerID(settingsFragment.getTrackerID(), settingsFragment.getMobileOperator(), modelfragment.getSelectedModel());
                    case 4:



                        break;

                }

            }
            @Override public void onPageScrollStateChanged(int state) { }
        });

        addOnNavigationBlockedListener(new OnNavigationBlockedListener() {
            @Override
            public void onNavigationBlocked(int position, int direction)
            {
                if(position == 2)
                {
                    settingsFragment.showRequiredFields();
                }
            }
        });
    }


}

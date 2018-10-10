package br.gov.dpf.intelitrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
{
    //Static variables
    private static final String KEY_FIRST_START = "first_start";
    private static final int REQUEST_INTRO = 0;

    //Bind views
    @BindView(R.id.btnLogin) CircularProgressButton btnLogin;
    @BindView(R.id.txtEmail) EditText txtEmail;
    @BindView(R.id.txtPassword) EditText txtPassword;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Method called after intro
        super.onActivityResult(requestCode, resultCode, data);

        //Load shared preferences
        SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this).edit();

        //Save flag so user don't have to see the intro again
        sharedPreferences.putBoolean(KEY_FIRST_START, false);
        sharedPreferences.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get firebase instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //Try to get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Create intent used to navigate to next activity
        final Intent intent = new Intent();

        //Check if user is signed in (non-null)
        if(currentUser != null)
        {
            //Send to main activity screen
            intent.setClass(this, MainActivity.class);

            //Set corresponding flags (don't return to this activity)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            //Start next activity
            startActivity(intent);
        }
        else if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_FIRST_START, true))
        {
            //Send to introduction activity
            intent.setClass(this, FirstStartActivity.class);

            //Start next activity
            startActivityForResult(intent, REQUEST_INTRO);
        }

        //Load layout
        setContentView(R.layout.activity_login);

        //Load component
        ButterKnife.bind(this);

        //Set button corner radius
        btnLogin.setInitialCornerRadius(35);

        //Create snackbar
        final Snackbar snackbar = Snackbar.make(btnLogin, "", Snackbar.LENGTH_LONG);

        //Set dismiss action
        snackbar.setAction("Dispensar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        //Set dismiss button color
        snackbar.setActionTextColor(Color.parseColor("#DC493C"));

        //Add callback to snackbar
        snackbar.addCallback(new Snackbar.Callback()
        {
            @Override
            public void onShown(Snackbar snackbar)
            {
                //On shown, change button state to error (by default)
                btnLogin.doneLoadingAnimation(Color.parseColor("#DC493C"), BitmapFactory.decodeResource(getResources(), R.drawable.ic_warning_white));
            }

            @Override
            public void onDismissed(Snackbar snackbar, int event)
            {
                //On dismiss, return button to original state
                btnLogin.revertAnimation();
            }
        });

        //Define login button behavior
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Start button loading animation
                btnLogin.startAnimation();

                //Check if e-mail is valid
                if(txtEmail.getText().toString().length() < 8 || !txtEmail.getText().toString().contains("@"))
                {
                    //Invalid e-mail message
                    snackbar.setText("Insira um e-mail válido");

                    //Show snackbar
                    snackbar.show();
                }
                else if(txtPassword.getText().toString().length() < 4)
                {
                    //Invalid password message
                    snackbar.setText("Insira uma senha válida");

                    //Show snackbar
                    snackbar.show();
                }
                else
                {
                    //Get firebase instance
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    //Valid input, perform firebase login
                    mAuth.signInWithEmailAndPassword(txtEmail.getText().toString(), txtPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        //Sign in success, update UI with the signed-in user's information
                                        btnLogin.doneLoadingAnimation(Color.parseColor("#4DB6AC"), BitmapFactory.decodeResource(getResources(), R.drawable.ic_check_white));

                                        //500 ms delay
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run()
                                            {
                                                //Send to main activity screen
                                                intent.setClass(LoginActivity.this, MainActivity.class);

                                                //Set corresponding flags (don't return to this activity)
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                //Start next activity
                                                startActivity(intent);
                                            }
                                        }, 500);
                                    }
                                    else
                                    {
                                        //Invalid e-mail message
                                        snackbar.setText("Falha de autenticação");

                                        //Show snackbar
                                        snackbar.show();
                                    }
                                }
                            });
                }
            }
        });

        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //do what you want on the press of 'done'
                    btnLogin.performClick();
                }
                return false;
            }
        });
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
}


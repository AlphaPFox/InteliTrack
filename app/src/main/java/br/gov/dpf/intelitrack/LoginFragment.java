package br.gov.dpf.intelitrack;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginFragment extends SlideFragment
{

    @BindView(R.id.txtUserName) EditText txtUserName;
    @BindView(R.id.txtPassword) EditText txtPassword;

    private boolean loggedIn = false;
    private Handler loginHandler = new Handler();
    private Runnable loginRunnable = new Runnable()
    {
        @Override
        public void run() {

            Toast.makeText(getContext(), "Login realizado com sucesso", Toast.LENGTH_SHORT).show();

            loggedIn = true;
            updateNavigation();
        }
    };

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    //Define skip intro click behavior
    public View.OnClickListener performLogin = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            txtUserName.setEnabled(false);
            txtPassword.setEnabled(false);
            loginHandler.postDelayed(loginRunnable, 2000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        ButterKnife.bind(this, view);

        txtUserName.setEnabled(!loggedIn);
        txtPassword.setEnabled(!loggedIn);

        return view;
    }

    @Override
    public void onDestroy() {
        loginHandler.removeCallbacks(loginRunnable);
        super.onDestroy();
    }

    @Override
    public boolean canGoForward() {
        return loggedIn;
    }
}
package com.example.petshop;

import static com.example.petshop.pelengkap.DateValidator.String2Date;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petshop.ui.LoginSignup.LoginActivity;
import com.example.petshop.ui.LoginSignup.SignUpActivity;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.ui.MainActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Objects;

// https://codingwitht.com/material-design-login-screen-in-android-city-guide-part-8/
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        AndroidThreeTen.init(this);
        LocalStorage localStorage = new LocalStorage(this);

        if (!localStorage.getToken().isEmpty()) {
            if (Objects.requireNonNull(String2Date(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .before(String2Date(localStorage.getSesi())))
            {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                localStorage.setToken("");
                Toast.makeText(this, "Sessi anda sudah habis, silahkan login kembali", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void callLoginScreen(View view) {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.login_btn), "transition_login");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, pairs);
        startActivity(intent, options.toBundle());
    }

    public void callSignUpScreen(View view) {

        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(findViewById(R.id.signup_btn), "transition_signup");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, pairs);
        startActivity(intent, options.toBundle());
    }
}
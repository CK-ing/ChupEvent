package com.example.chupevent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginScreen extends AppCompatActivity {

    private ImageView backgroundImageView;
    private int[] images = {R.drawable.loginscreen1, R.drawable.loginscreen2, R.drawable.loginscreen3}; // Replace with your image resources
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Button signupbutton;
    private TextView signintextview;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Start fade-out animation
            fadeOutAndChangeImage();

            // Schedule next image change in 5 seconds
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Initialize background ImageView
        backgroundImageView = findViewById(R.id.backgroundImageView);

        // Start the background image cycling
        handler.post(runnable);

        signupbutton = findViewById(R.id.btn_signup_login_screen);
        signintextview = findViewById(R.id.loginLink);

        signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, SelectRole.class);
                startActivity(intent);
            }
        });
        signintextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, SigninActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fadeOutAndChangeImage() {
        // Create fade-out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500); // Duration of fade-out
        fadeOut.setFillAfter(true);

        // Set an animation listener to update the image when fade-out ends
        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {}

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                // Change the image after fade-out ends
                backgroundImageView.setImageResource(images[currentIndex]);
                currentIndex = (currentIndex + 1) % images.length;

                // Start fade-in animation
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(500); // Duration of fade-in
                fadeIn.setFillAfter(true);
                backgroundImageView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {}
        });

        // Start fade-out animation
        backgroundImageView.startAnimation(fadeOut);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Stop handler when activity is destroyed
    }
}
package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class SigninActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "SigninActivity";
    private Button btnsignin;
    private LinearLayout googlesignin;
    private TextView tvSignup;
    private ImageButton backbtn;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        backbtn = findViewById(R.id.backButton);
        btnsignin = findViewById(R.id.btn_sign_in);
        googlesignin = findViewById(R.id.googleSignInButton);
        tvSignup = findViewById(R.id.signUpText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("572927376351-cdtmfmcjik992v164cqrqnjfqcc32119.apps.googleusercontent.com") // Replace with your actual client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googlesignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnsignin.setOnClickListener(v -> signInWithEmail());

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, SelectRole.class);
                startActivity(intent);
            }
        });
    }
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void signInWithEmail() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SigninActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign-in successful
                            Toast.makeText(SigninActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SigninActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                Toast.makeText(SigninActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String email = acct.getEmail();

        // Check if a user with this email exists in the Firebase Realtime Database
        database.getReference("Users")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // User exists, proceed to authenticate with Firebase
                            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                            auth.signInWithCredential(credential)
                                    .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Toast.makeText(SigninActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Sign-in failed
                                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                                Toast.makeText(SigninActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // User does not exist, inform the user and sign out from Google
                            Toast.makeText(SigninActivity.this, "No account found. Please sign up first.", Toast.LENGTH_SHORT).show();
                            googleSignInClient.signOut();  // Sign out from GoogleSignInClient
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Database error occurred
                        Toast.makeText(SigninActivity.this, "Failed to check user existence. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
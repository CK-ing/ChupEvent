package com.example.chupevent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "SignupActivity";
    private ImageView backbtn;
    private TextView tvSignin;
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnSignUp;
    private LinearLayout googlesignup;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    GoogleSignInClient googleSignInClient;
    Intent intent = getIntent();
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Get the role from the previous activity
        role = getIntent().getStringExtra("role");
        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        // Initialize UI elements
        backbtn = findViewById(R.id.iv_back_btn_signup);
        tvSignin = findViewById(R.id.loginLink);
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnSignUp = findViewById(R.id.btn_sign_up);
        googlesignup = findViewById(R.id.googleSignUpButton);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("572927376351-cdtmfmcjik992v164cqrqnjfqcc32119.apps.googleusercontent.com") // Replace with your actual client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        googlesignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpWithGoogle();
            }
        });
        // Back button functionality
        backbtn.setOnClickListener(v -> finish());
        // Sign In link functionality
        tvSignin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
        });
        btnSignUp.setOnClickListener(v -> signUpUser());

        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle password visibility
                    isPasswordVisible = !isPasswordVisible;
                    etPassword.setInputType(isPasswordVisible ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            isPasswordVisible ? R.drawable.ic_baseline_open_eye_24 : R.drawable.ic_baseline_password_24, 0);
                    // Move the cursor to the end
                    etPassword.setSelection(etPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        etConfirmPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etConfirmPassword.getRight() - etConfirmPassword.getCompoundDrawables()[2].getBounds().width())) {
                    // Toggle confirm password visibility
                    isConfirmPasswordVisible = !isConfirmPasswordVisible;
                    etConfirmPassword.setInputType(isConfirmPasswordVisible ?
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            isConfirmPasswordVisible ? R.drawable.ic_baseline_open_eye_24 : R.drawable.ic_baseline_password_24, 0);
                    // Move the cursor to the end
                    etConfirmPassword.setSelection(etConfirmPassword.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }
        // Use Firebase Authentication to create the user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Successfully created the user
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                // Add user data to Realtime Database
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("role", role);
                                // Add role-specific fields
                                if ("student".equals(role)) {
                                    userData.put("registeredEvents", new HashMap<>());
                                    userData.put("pastEvents", new HashMap<>());// Placeholder for registered events
                                } else if ("organizer".equals(role)) {
                                    userData.put("organizedEvents", new HashMap<>()); // Placeholder for organized events
                                } else if ("administrator".equals(role)) {
                                    userData.put("approvedEvents", new HashMap<>()); // Placeholder for approved events
                                }
                                // Store user data in Firebase Realtime Database

                                databaseReference.child(userId).setValue(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                                    // Navigate to the main activity
                                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SignupActivity.this, "Failed to save user data. Please try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Sign-up failed
                            Toast.makeText(SignupActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUpWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                Toast.makeText(SignupActivity.this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // First, check if this Google account email already exists in the database
        String email = acct.getEmail();
        database.getReference("Users")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Email already exists in the database, don't create an account in Firebase Authentication
                            Toast.makeText(SignupActivity.this, "Account with this Google account already exists.", Toast.LENGTH_SHORT).show();
                            googleSignInClient.signOut();
                        } else {
                            // Email does not exist in the database, proceed to sign in and save user data
                            auth.signInWithCredential(credential)
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign-in success, proceed to save user data
                                                FirebaseUser user = auth.getCurrentUser();
                                                if (user != null) {
                                                    // Prepare user data
                                                    HashMap<String, Object> map = new HashMap<>();
                                                    map.put("name", user.getDisplayName());
                                                    map.put("email", user.getEmail());
                                                    map.put("role", role);

                                                    // Save data to Firebase Realtime Database
                                                    database.getReference().child("Users").child(user.getUid())
                                                            .setValue(map)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(SignupActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                                        finish();
                                                                    } else {
                                                                        // Database save failed, remove account from Firebase Authentication
                                                                        user.delete();
                                                                        Toast.makeText(SignupActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            } else {
                                                // Sign-in failed, show error message
                                                Log.w(TAG, "signUpWithCredential:failure", task.getException());
                                                Toast.makeText(SignupActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SignupActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
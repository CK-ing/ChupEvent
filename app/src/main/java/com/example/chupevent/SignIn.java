package com.example.chupevent;


import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;


public class SignIn extends AppCompatActivity {
    Button btn_signin_google,btn_login;
    EditText et_email,et_password;
    TextView tv_reg;
    private FirebaseAuth fbauth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9091;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        btn_signin_google=findViewById(R.id.btn_signin_g);
        btn_login=findViewById(R.id.btn_signin_login);
        et_email=findViewById(R.id.et_signin_email);
        et_password=findViewById(R.id.et_signin_password);
        tv_reg=findViewById(R.id.tv_signin_reg);
        //initialize fbauth
        fbauth=FirebaseAuth.getInstance();
        //initialize google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("148123029570-vjkiop5832u42u4icksboetikgoqda4q.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //sign in using uncle g

        btn_signin_google.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //end of sign in using uncle g

        tv_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignIn.this, Dummy_Register.class);
                startActivity(intent);
            }
        });

        //sign in using normal email n password credentials

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=et_email.getText().toString();
                String password=et_password.getText().toString();

                email.trim();
                password.trim();

                if(email.isEmpty()||password.isEmpty()){

                    AlertDialog.Builder builder=new AlertDialog.Builder(SignIn.this);
                    builder.setMessage("Please enter a valid email and password or if account not registered please click on Dummy_Register to create an account.").setTitle("Warning").setPositiveButton("OK",null);
                    AlertDialog dialog=builder.create();
                    dialog.show();

                }else{

                    fbauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                Toast.makeText(SignIn.this,"Successfull signup",Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(SignIn.this, Dummy_home.class);
                                //add flags
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{
                                Toast.makeText(SignIn.this,"Error",Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

            }
        });


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                firebaseAuthWithGoogle(account);
            } else {
                // Google sign-in failed, handle this case
                Log.w(TAG, "Google sign-in failed: Account is null");
                Toast.makeText(SignIn.this, "Google sign-in failed: Account is null", Toast.LENGTH_SHORT).show();
            }

        } catch (ApiException e) {
            // Sign-in failed, log the error code and message
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(SignIn.this, "Google sign-in failed. Error code: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct != null) {
            // Get credential
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

            // Firebase sign-in
            fbauth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fbauth.getCurrentUser();
                            Log.d(TAG, "firebaseAuthWithGoogle:success");
                            Toast.makeText(SignIn.this, "Successful Login", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignIn.this, Dummy_home.class);
                            startActivity(intent);
                            finish(); // Add finish() to clear back stack
                        } else {
                            // Log the exception if Firebase sign-in fails
                            Log.w(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.w(TAG, "Account is null in firebaseAuthWithGoogle");
            Toast.makeText(SignIn.this, "Failed to get account", Toast.LENGTH_SHORT).show();
        }
    }



}
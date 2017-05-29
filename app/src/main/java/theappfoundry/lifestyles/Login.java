package theappfoundry.lifestyles;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "Login"; // for debugging
    private EditText emailField;
    private EditText passField;
    private Button signIn;
    private Button signUp;
    private Button writeTo;
    private Button signOut;
    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        // UI components
        passField = (EditText)findViewById(R.id.passField);
        emailField = (EditText)findViewById(R.id.emailField);
        signIn = (Button)findViewById(R.id.signInButton);
        signUp = (Button)findViewById(R.id.signUpButton);
        writeTo = (Button)findViewById(R.id.writeButton);
        signOut = (Button)findViewById(R.id.signOutButton);

        //OnclickListeners for buttons
        signIn.setOnClickListener(this);
        signUp.setOnClickListener(this);
        writeTo.setOnClickListener(this);
        signOut.setOnClickListener(this);




        // [START declare_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END declare_auth]


        // Helps track when the user signs in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };




    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    //createAccount(string,string)
    //Creates a user account from email & pass. Inside, validates email and password and sends
    //user a email verification link. Account is still stored on Firebase but not verified.
    /////////////////////////////////////////////////////////////////////////////////////////
    public void createAccount(String email, String password) {
        Log.d(TAG, "createAccount: " + email);
        if (!validateForm()) {
            return;
        }


        //showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            sendVerificationEmail();
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            FirebaseException e = (FirebaseException )task.getException();
                            Toast.makeText(Login.this, "Authentication Failed" + e.getMessage() ,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
        // [END create_user_with_email]



    }


    /////////////////////////////////////////////////////////////////////////////////////////
    //createAccount(string,string)
    //Signs in a user if there is an account created with matching information. If email is
    //not yet verified, user cannot sign in.
    /////////////////////////////////////////////////////////////////////////////////////////
    private void signIn(String email, String password){
        Log.d(TAG, "signIn:" + email);
        if(!validateForm()){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (!firebaseUser.isEmailVerified()) { // If email is not verified, toast and return
                                Toast.makeText(Login.this, "Email has not been verified, check email", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                return;
                            }
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(Login.this, "Authentication Failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });



    }


    // Validates the password
    private boolean validateForm() {
        boolean valid = true;

        String email = emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Required.");
            valid = false;
        } else {
            emailField.setError(null);
        }

        String password = passField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passField.setError("Required.");
            valid = false;
        } else {
            passField.setError(null);
        }

        return valid;
    }


    private void getCurrentUser(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
        }



    }


    @Override
    public void onClick(View v) {
        int i = v.getId(); // Get id of view that was clicked
        if(i == R.id.signUpButton){
            createAccount(emailField.getText().toString(), passField.getText().toString());
        }else if(i == R.id.signInButton){
            signIn(emailField.getText().toString(), passField.getText().toString());
        }else if(i == R.id.writeButton){
            // Write a message to the database
            database = FirebaseDatabase.getInstance(); // Where I left off.. trying to write to the database
            myRef = database.getReference();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null){
                Profile myProfile = new Profile("Ben", "Hunt");
                myRef.child("users").child(user.getUid()).setValue(myProfile); // JSON tree hierarchy
                Log.d(TAG, "onClick: " + (user.getUid()));
            }
        }else if(i == R.id.signOutButton){
            FirebaseAuth.getInstance().signOut(); // signs user out
        }
    }

    public void sendVerificationEmail(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });

    }



    ///////////////////////////////
    //Email Verification Listener to see if firebase user has verified their email.
    //Used to not let people sign in if they have not verified.
    //////////////////////////////
    public void checkVerification(){

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null ) {
                    Log.e(TAG, firebaseUser.isEmailVerified() ? "User is signed in and email is verified" : "Email is not verified");
                } else {
                    Log.e(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }




}



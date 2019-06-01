/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.diraapps.timetrack;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Iterator;


public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private String TAGLOG = "TimeTrack";
    DatabaseReference dbTracking;
    ValueEventListener dbListener;
    private static final String TAG = "EmailPassword";
    private EditText mEmailField;
    private EditText mPasswordField;
    DatabaseReference ultimoTiempo;
    private GPSTracker gpsTracker;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        // Buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dbTracking != null && dbTracking != null) {

                    try {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                            ActivityCompat.requestPermissions(EmailPasswordActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    double latitude = 0;
                    double longitude = 0;

                    gpsTracker = new GPSTracker(EmailPasswordActivity.this);
                    if (gpsTracker.canGetLocation()) {
                        latitude = gpsTracker.getLatitude();
                        longitude = gpsTracker.getLongitude();
                    }

                    ultimoTiempo.child("enter").setValue(new Date());
                    ultimoTiempo.child("coordenadas_enter").child("latitud").setValue(latitude);
                    ultimoTiempo.child("coordenadas_enter").child("longitud").setValue(longitude);
                }
            }
        });
        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dbTracking != null && ultimoTiempo != null) {

                    double latitude_exit = 0;
                    double longitude_exit = 0;

                    gpsTracker = new GPSTracker(EmailPasswordActivity.this);
                    if (gpsTracker.canGetLocation()) {
                        latitude_exit = gpsTracker.getLatitude();
                        longitude_exit = gpsTracker.getLongitude();
                    }

                    ultimoTiempo.child("exit").setValue(new Date());
                    ultimoTiempo.child("coordenadas_exit").child("latitud").setValue(latitude_exit);
                    ultimoTiempo.child("coordenadas_exit").child("longitud").setValue(longitude_exit);
                    //Creamos un nuevo valor
                    dbTracking.push();
                }
            }
        });
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateDB(currentUser);
            updateUI(currentUser);
        }
    }
    // [END on_start_check_user]

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateDB(user);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Usuario o contraseña incorrectos.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {

                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        email = email + "@garlez.com";

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private DataSnapshot ultimoTiempo(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> tiempos = dataSnapshot.getChildren();
        Iterator<DataSnapshot> itr = tiempos.iterator();
        DataSnapshot tiempo = null;
        while (itr.hasNext()) {
            tiempo = itr.next();
        }

        //Guardamos una referencia al último tiempo
        if (tiempo != null && !tiempo.hasChild("exit")) {
            this.ultimoTiempo = tiempo.getRef();
        } else {
            this.ultimoTiempo = dbTracking.push();
        }
        return tiempo;
    }

    private void actualizarBotonesEntradaSalida(DataSnapshot dataSnapshot) {
        DataSnapshot ultimoTiempo = ultimoTiempo(dataSnapshot);
        if (ultimoTiempo != null) {
            if (ultimoTiempo.child("exit").exists()) {
                findViewById(R.id.enter).setEnabled(true);
                findViewById(R.id.exit).setEnabled(false);
            } else {
                findViewById(R.id.enter).setEnabled(false);
                findViewById(R.id.exit).setEnabled(true);
            }
        } else {
            findViewById(R.id.enter).setEnabled(true);
            findViewById(R.id.exit).setEnabled(false);
        }

    }

    private void updateDB(final FirebaseUser user) {
        if (dbTracking != null && dbListener != null) {
            dbTracking.removeEventListener(dbListener);
        }

        dbTracking = FirebaseDatabase.getInstance().getReference()
                .child("tracking")
                .child(user.getUid());

        dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                actualizarBotonesEntradaSalida(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAGLOG, "Error!", databaseError.toException());
            }
        };

        dbTracking.addValueEventListener(dbListener);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
            findViewById(R.id.emailFields).setVisibility(View.GONE);
            findViewById(R.id.passwordFields).setVisibility(View.GONE);
            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.enter).setVisibility(View.VISIBLE);
            findViewById(R.id.exit).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.emailFields).setVisibility(View.VISIBLE);
            findViewById(R.id.passwordFields).setVisibility(View.VISIBLE);
            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
            findViewById(R.id.enter).setVisibility(View.GONE);
            findViewById(R.id.exit).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailSignInButton) {
            signIn(mEmailField.getText().toString() + "@garlez.com", mPasswordField.getText().toString());
        } else if (i == R.id.signOutButton) {
            signOut();
        }
    }
}

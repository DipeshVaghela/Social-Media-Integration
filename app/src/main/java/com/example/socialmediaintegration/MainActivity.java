package com.example.socialmediaintegration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CallbackManager CbM;
    private FirebaseAuth FbA;
    private CardView btnFLogin;
    private static final String TAG = "FacebookAuthentication";
    private static final String TAGgoogle = "GoogleAuthentication";
    GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInButton signInButton;
    private static final int RC_SIGN_IN = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for facebook sign in
        FbA = FirebaseAuth.getInstance(); // used for both
        FacebookSdk.sdkInitialize(getApplicationContext());
        CbM = CallbackManager.Factory.create();

        btnFLogin = (CardView) findViewById(R.id.btnFlogin);

        btnFLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnFLogin.setEnabled(false);

                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(CbM, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG,"On Success " + loginResult);
                        forHandlingFbAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG,"On Cancel ");
//                        Toast.makeText(MainActivity.this, "Can not connect", Toast.LENGTH_SHORT).show();
                        btnFLogin.setEnabled(true);

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG,"On Error " + error);
                        btnFLogin.setEnabled(true);
                    }
                });
            }
        });

        // for google sign in
        // Configure Google Sign In

        signInButton = findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else
        {
            CbM.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void forHandlingFbAccessToken(AccessToken FbToken)
    {
        Log.d(TAG,"Handle Facebook Token " + FbToken);


        AuthCredential credential = FacebookAuthProvider.getCredential(FbToken.getToken());
        FbA.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "You are successfully logged in", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Sign in with Credential : Successful ");
                    FirebaseUser user = FbA.getCurrentUser();
                    sendData(user);
                }
                else {
                    Log.d(TAG,"Sign in with Credential : Failed " , task.getException());
                    Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    btnFLogin.setEnabled(true);
                }
            }
        });
    }

    private void sendData(FirebaseUser user)
    {
        Profile profile = Profile.getCurrentProfile();
        ;
        String displayName  = user.getDisplayName();
        String photoUrl = profile.getProfilePictureUri(300,300).toString();
        String uId = user.getUid();
        String email = user.getEmail();

        Intent i = new Intent(MainActivity.this,UserInformation.class);
        i.putExtra("which","facebook");
        i.putExtra("displayName",displayName);
        i.putExtra("photoUrl",photoUrl);
        i.putExtra("email",email);
        i.putExtra("uId",uId);

        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curUser = FbA.getCurrentUser();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null)
        {
            Intent i = new Intent(MainActivity.this,UserInformation.class);
            i.putExtra("which","google");
            startActivity(i);
        }

        if(curUser != null)
        {
            sendData(curUser);
        }

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully
            Intent i = new Intent(MainActivity.this,UserInformation.class);
            i.putExtra("which","google");
            startActivity(i);

        }
        catch (ApiException e)
        {
            Log.w(TAGgoogle, "signInResult:failed code=" + e.getStatusCode());
        }
    }

}
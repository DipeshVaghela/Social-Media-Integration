package com.example.socialmediaintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserInformation extends AppCompatActivity {

    private FirebaseAuth FbA;
    GoogleSignInClient mGoogleSignInClient;
    private CardView btnLogout;
    private CircleImageView ivPhoto;
    private TextView tvName,tvEMail,tvUID;
    String which;
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_information_activity);

        ivPhoto =  findViewById(R.id.ivPhoto);
        tvName = findViewById(R.id.tvName);
        tvEMail = findViewById(R.id.tvEmail);
        tvUID = findViewById(R.id.tvUID);
        btnLogout = findViewById(R.id.btnLogout);

        which = getIntent().getStringExtra("which");

        FbA = FirebaseAuth.getInstance();
//      accessToken = AccessToken.getCurrentAccessToken();

        if(which.equals("facebook"))
        {
            // For Facebook
            String displayName = getIntent().getStringExtra("displayName");
            String email = getIntent().getStringExtra("email");
            String uId = getIntent().getStringExtra("uId");
            String photoUrl = getIntent().getStringExtra("photoUrl");

            if(photoUrl != null)
            {
                Picasso.get().load(photoUrl).into(ivPhoto);
            }
            else
            {
                ivPhoto.setImageResource(R.drawable.person);
            }

            if(displayName == null)
            {
                tvName.setText("");
            }
            else
            {
                tvName.setText(displayName);
            }

            if(email == null)
            {
                tvEMail.setText("");
            }
            else
            {
                tvEMail.setText(email);
            }

            if(uId == null)
            {
                tvUID.setText("");
            }
            else
            {
                tvUID.setText(uId);
            }

            //for logout from facebook
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FbA.signOut();
                    LoginManager.getInstance().logOut();
                    goBackToParent();
                }
            });
        }

        if(which.equals("google"))
        {
            //for Google
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                tvName.setText(personName);
                tvEMail.setText(personEmail);
                tvUID.setText(personId);

                Glide.with(this).load(String.valueOf(personPhoto)).into(ivPhoto);
                //for log out from google
                btnLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.btnLogout:
                                signOut();
                                break;
                        }
                        goBackToParent();
                    }
                });

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curUser = FbA.getCurrentUser();
        if(which.equals("facebook"))
        {
            if(curUser == null)
            {
                goBackToParent();
            }
        }

    }

    public void goBackToParent() {
        Toast.makeText(UserInformation.this,"You are logged out !!",Toast.LENGTH_SHORT).show();
        Intent i = new Intent(UserInformation.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(which.equals("facebook"))
        {
            FbA.signOut();
            LoginManager.getInstance().logOut();
            goBackToParent();
        }
        else
        {
            signOut();
            goBackToParent();
        }

    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(UserInformation.this,"You are logged out !!",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

}
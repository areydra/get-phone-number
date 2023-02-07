package com.example.getphonenumber;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;

public class MainActivity extends AppCompatActivity {

    TextView textPhoneNumber;
    private static final int CREDENTIAL_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding views
        textPhoneNumber = findViewById(R.id.phone_number);
    }


    //=======================================================//
    // OLD API OF GETTING PHONE NUMBER HINT (GOOGLE ACCOUNT) //
    //=======================================================//

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK)
        {
            // Obtain the phone number from the result
            Credential credentials = data.getParcelableExtra(Credential.EXTRA_KEY);
            textPhoneNumber.setText(credentials.getId().substring(3));
        }
        else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE)
        {
            // *** No phone numbers available ***
            Toast.makeText(getApplicationContext(), "No phone numbers found", Toast.LENGTH_LONG).show();
        }


    }


    public void GetNumber(View v) {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();


        PendingIntent intent = Credentials.getClient(getApplicationContext()).getHintPickerIntent(hintRequest);
        try
        {
            startIntentSenderForResult(intent.getIntentSender(), CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0,new Bundle());
        }
        catch (IntentSender.SendIntentException e)
        {
            e.printStackTrace();
        }
    }

    //=====================================================//
    // NEWEST API OF GETTING PHONE NUMBER HINT (SIM-BASED) //
    //=====================================================//
    ActivityResultLauncher<IntentSenderRequest> phoneNumberHintIntentResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            try {
                                String phoneNumber = Identity.getSignInClient(getApplicationContext()).getPhoneNumberFromIntent(result.getData());
                                textPhoneNumber.setText(phoneNumber);
                            } catch (Exception e) {
//                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

    public void GetNumberNewWay(View v) {
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();

        Identity.getSignInClient(MainActivity.this)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener(result -> {
                    try {
                        IntentSender intentSender = result.getIntentSender();
                        phoneNumberHintIntentResultLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
                    } catch (Exception e) {
//                        Log.i("Error launching", "error occurred in launching Activity result");
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
//                    Log.i("Failure occurred", e.toString());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
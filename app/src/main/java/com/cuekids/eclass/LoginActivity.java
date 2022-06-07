package com.cuekids.eclass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.cuekids.eclass.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    Animation btgone, btgtwo;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;
    String nameFromDB, emailFromDB, ageFromDB, phoneFromDB, imageUrlFromDB, phone, newAccount = "";
    FirebaseAuth mAuth;
    String verificationId;
    String countryCode;
    private SMSReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        binding.progressBar.getProgress();
        binding.progressBar.setVisibility(View.GONE);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);
        binding.animationpurposebuttonslayout.startAnimation(btgone);
        binding.animationpurposeloginbuttonlayout.startAnimation(btgtwo);

        binding.btnlogin.setOnClickListener(v -> {
            if (!validatePhone())
                return;
            Toast.makeText(LoginActivity.this, "Please Wait!", Toast.LENGTH_SHORT).show();
            startSMSListener();
            isUser();
        });

        binding.finalloginbutton.setOnClickListener(v -> {
            if (!validateOTP())
                return;
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.startAnimation(btgtwo);
            verifyCode(Objects.requireNonNull(binding.otpinputlayout.getEditText()).getText().toString());
        });
    }

    private void startSMSListener() {
        try {
            smsReceiver = new SMSReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
            this.registerReceiver(smsReceiver, intentFilter);

            smsReceiver.setOTPListener(new SMSReceiver.OTPReceiveListener() {
                @Override
                public void onOTPReceived(String otp) {
                    Objects.requireNonNull(binding.otpinputlayout.getEditText()).setText(otp);
                    verifyCode(otp);
                    if (smsReceiver != null) {
                        unregisterReceiver(smsReceiver);
                        smsReceiver = null;
                    }
                }
                @Override
                public void onOTPTimeOut() {
                }
                @Override
                public void onOTPReceivedError(String error) {
                }
            });
            SmsRetrieverClient client = SmsRetriever.getClient(this);
            Task<Void> task = client.startSmsRetriever();
            task.addOnSuccessListener(aVoid -> {
                // API successfully started
            });
            task.addOnFailureListener(e -> {
                // Fail to start API
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    private boolean validatePhone() {
        String code = binding.countryCodeHolder.getSelectedCountryCodeWithPlus();
        if (code.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please select Country", Toast.LENGTH_SHORT).show();
            binding.phoneinputlayout.requestFocus();
            return false;
        }
        String val = Objects.requireNonNull(binding.phoneinputlayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Enter your Phone Number", Toast.LENGTH_SHORT).show();
            binding.phoneinputlayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateOTP() {
        String val = Objects.requireNonNull(binding.otpinputlayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(LoginActivity.this, "OTP field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.otpinputlayout.requestFocus();
            return false;
        } else if (val.length() != 6) {
            Toast.makeText(LoginActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
            binding.otpinputlayout.requestFocus();
            return false;
        } else
            return true;
    }

    private void isUser() {
        countryCode = binding.countryCodeHolder.getSelectedCountryCodeWithPlus();
        phone = countryCode + Objects.requireNonNull(binding.phoneinputlayout.getEditText()).getText().toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    newAccount = "no";
                    nameFromDB = dataSnapshot.child(phone).child("name").getValue(String.class);
                    emailFromDB = dataSnapshot.child(phone).child("email").getValue(String.class);
                    ageFromDB = dataSnapshot.child(phone).child("age").getValue(String.class);
                    phoneFromDB = dataSnapshot.child(phone).child("phone").getValue(String.class);
                    imageUrlFromDB = dataSnapshot.child(phone).child("imageUrl").getValue(String.class);
                } else {
                    newAccount = "yes";
                }

                binding.phoneLayout.setVisibility(View.GONE);
                binding.otplayout.setVisibility(View.VISIBLE);
                if(newAccount.equals("yes"))
                    binding.finalloginbutton.setText("Next");
                binding.otpinputlayout.startAnimation(btgone);
                binding.finalloginbutton.startAnimation(btgtwo);
                Toast.makeText(LoginActivity.this, "We are sending you an OTP", Toast.LENGTH_SHORT).show();
                sendVerificationCode(phone);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void redirecting()
    {
        if(newAccount.equals("yes")) {
            Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
            i.putExtra("phone", phone);
            startActivity(i);
            finish();
        }
        else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
            Query checkUser = reference.orderByChild("phone").equalTo(phoneFromDB);
            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userHelperClass helperClass = new userHelperClass(nameFromDB, emailFromDB, ageFromDB, phoneFromDB, imageUrlFromDB);
                        reference.child(phoneFromDB).setValue(helperClass);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("age", ageFromDB);
                        intent.putExtra("phone", phoneFromDB);
                        intent.putExtra("url", imageUrlFromDB);
                        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
                        shpEditor = shp.edit();
                        shpEditor.putString("name", nameFromDB);
                        shpEditor.putString("email", emailFromDB);
                        shpEditor.putString("age", ageFromDB);
                        shpEditor.putString("phone", phoneFromDB);
                        shpEditor.putString("url", imageUrlFromDB);
                        shpEditor.apply();
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Your phone number has been verified successfully !", Toast.LENGTH_SHORT).show();
                        redirecting();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)        // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)                // Timeout duration
                .setActivity(this)   // Activity (for callback binding)
                .setCallbacks(mCallBack)        // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                Objects.requireNonNull(binding.otpinputlayout.getEditText()).setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }
}
package com.cuekids.eclass;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cuekids.eclass.databinding.ActivityMainBinding;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;
    SharedPreferences shp;
    Animation atg, btgone, btgtwo;
    SharedPreferences.Editor shpEditor;
    Uri profileUri;
    String name, email, age, phone, tempPhone;
    String downloadImageUrl, imagePathName;
    FirebaseAuth mAuth;
    String verificationId;
    private SMSReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        binding.progressBar.getProgress();
        binding.progressBar.setVisibility(View.GONE);

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);
        binding.buttonAnimationLayout.startAnimation(atg);
        binding.buttonsLayout.startAnimation(btgone);

        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        if (shp == null)
            shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        name = shp.getString("name", "");
        email = shp.getString("email", "");
        age = shp.getString("age", "");
        phone = shp.getString("phone", "");
        downloadImageUrl = shp.getString("url", "empty");

        Objects.requireNonNull(binding.nameInputLayout.getEditText()).setText(name);
        Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).setText(email);
        Objects.requireNonNull(binding.phoneInputLayout.getEditText()).setText(phone);
        Objects.requireNonNull(binding.ageInputLayout.getEditText()).setText(age);

        if(downloadImageUrl.equals("empty"))
            binding.iupload.setBackgroundResource(R.drawable.ic_baseline_person_24);
        else
            Glide.with(binding.iupload.getContext()).load(downloadImageUrl).circleCrop().into(binding.iupload);

        final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null ){
                        Toast.makeText(MainActivity.this, "Uploading. Please Wait", Toast.LENGTH_SHORT).show();
                        binding.buttonsLayout.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.progressBar.startAnimation(btgtwo);
                        profileUri = uri;
                        imagePathName = UUID.randomUUID().toString()+".jpg";
                        uploadPhoto();
                    }
                });

        binding.nameEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                binding.nameUpdateButton.setVisibility(View.VISIBLE);
            else
                binding.nameUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.phoneEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                binding.phoneUpdateButton.setVisibility(View.VISIBLE);
            else
                binding.phoneUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.otpEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                binding.otpButton.setVisibility(View.VISIBLE);
            else
                binding.otpButton.setVisibility(View.INVISIBLE);
        });

        binding.emailIdEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                binding.emailUpdateButton.setVisibility(View.VISIBLE);
            else
                binding.emailUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.ageEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                binding.ageUpdateButton.setVisibility(View.VISIBLE);
            else
                binding.ageUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.iupload.setOnClickListener(v -> mGetContent.launch("image/*"));

        binding.nameUpdateButton.setOnClickListener(v -> {
            String tempName = binding.nameInputLayout.getEditText().getText().toString();
            if(!tempName.equals(name) && validateName())
                updateName(tempName);
        });

        binding.phoneUpdateButton.setOnClickListener(v -> {
            tempPhone = binding.phoneInputLayout.getEditText().getText().toString();
            if(!tempPhone.equals(phone) && validatePhone())
                alreadyExists();
        });

        binding.emailUpdateButton.setOnClickListener(v -> {
            String tempEmail = binding.emailIdInputLayout.getEditText().getText().toString();
            if(!tempEmail.equals(email) && validateEmail())
                updateEmail(tempEmail);
        });

        binding.ageUpdateButton.setOnClickListener(v -> {
            String tempAge = binding.ageInputLayout.getEditText().getText().toString();
            if(!tempAge.equals(age) && validateAge())
                updateAge(tempAge);

        });

        binding.otpButton.setOnClickListener(v -> {
            String otp = Objects.requireNonNull(binding.otpInputLayout.getEditText()).getText().toString();
            if (validateOTP()) {
                verifyCode(otp);
            }
        });

        binding.websiteButton.setOnClickListener(v-> {
            Intent i = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(i);
        });

        binding.logoutbutton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure");
            builder.setMessage("You want to logout ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                if (shp == null)
                    shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
                shpEditor = shp.edit();
                shpEditor.clear();
                shpEditor.apply();
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void uploadPhoto() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("UserProfileImages").child(imagePathName);
        UploadTask uploadTask = imageRef.putFile(profileUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if(!task.isSuccessful()){
                throw Objects.requireNonNull(task.getException());
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if(task.isComplete()){
                downloadImageUrl = task.getResult().toString();
                binding.progressBar.setVisibility(View.GONE);
                binding.buttonsLayout.setVisibility(View.VISIBLE);
                binding.buttonsLayout.startAnimation(btgtwo);
                Glide.with(binding.iupload.getContext()).load(profileUri).circleCrop().into(binding.iupload);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUser =  reference.orderByChild("phone").equalTo(phone);
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reference.child(phone).removeValue();
                        userHelperClass helperClass = new userHelperClass(name, email, age, phone, downloadImageUrl);
                        reference.child(phone).setValue(helperClass);
                        shpEditor = shp.edit();
                        shpEditor.putString("url", downloadImageUrl);
                        shpEditor.apply();
                        Toast.makeText(MainActivity.this, "Profile Picture Uploaded", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
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
                    Objects.requireNonNull(binding.otpInputLayout.getEditText()).setText(otp);
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

    private boolean validateName() {
        String val = Objects.requireNonNull(binding.nameInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(MainActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.nameInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateEmail() {
        String val = Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(MainActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.emailIdInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateAge() {
        String val = Objects.requireNonNull(binding.ageInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(MainActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.ageInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validatePhone() {
        String val = Objects.requireNonNull(binding.phoneInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter your Phone Number", Toast.LENGTH_SHORT).show();
            binding.phoneInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateOTP() {
        String val = Objects.requireNonNull(binding.otpInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(MainActivity.this, "OTP field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.otpInputLayout.requestFocus();
            return false;
        } else if (val.length() != 6) {
            Toast.makeText(MainActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
            binding.otpInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    public void alreadyExists() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser =  reference.orderByChild("phone").equalTo(tempPhone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    Toast.makeText(MainActivity.this, "This phone number is already registered with us", Toast.LENGTH_LONG).show();
                }
                else {
                    binding.phoneInputLayout.setVisibility(View.GONE);
                    binding.phoneUpdateButton.setVisibility(View.GONE);
                    binding.otpInputLayout.setVisibility(View.VISIBLE);
                    binding.otpButton.setVisibility(View.VISIBLE);
                    startSMSListener();
                    Toast.makeText(MainActivity.this, "Sending OTP ! Please Wait", Toast.LENGTH_SHORT).show();
                    sendVerificationCode(tempPhone);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateName(String tempName) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser =  reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child(phone).removeValue();
                userHelperClass helperClass = new userHelperClass(tempName, email, age, phone, downloadImageUrl);
                reference.child(phone).setValue(helperClass);
                shpEditor = shp.edit();
                shpEditor.putString("name", tempName);
                shpEditor.apply();
                name = tempName;
                Toast.makeText(MainActivity.this, "Name Updated Successfully", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updatePhone() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser =  reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child(phone).removeValue();
                userHelperClass helperClass = new userHelperClass(name, email, age, tempPhone, downloadImageUrl);
                reference.child(tempPhone).setValue(helperClass);
                shpEditor = shp.edit();
                shpEditor.putString("phone", tempPhone);
                shpEditor.apply();
                phone = tempPhone;
                Toast.makeText(MainActivity.this, "Phone Number updated successfully !", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateEmail(String tempEmail) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser =  reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child(phone).removeValue();
                userHelperClass helperClass = new userHelperClass(name, tempEmail, age, phone, downloadImageUrl);
                reference.child(phone).setValue(helperClass);
                shpEditor = shp.edit();
                shpEditor.putString("email", tempEmail);
                shpEditor.apply();
                email = tempEmail;
                Toast.makeText(MainActivity.this, "Email Updated Successfully", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateAge(String tempAge) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser =  reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reference.child(phone).removeValue();
                userHelperClass helperClass = new userHelperClass(name, email, tempAge, phone, downloadImageUrl);
                reference.child(phone).setValue(helperClass);
                shpEditor = shp.edit();
                shpEditor.putString("age", tempAge);
                shpEditor.apply();
                age = tempAge;
                Toast.makeText(MainActivity.this, "Age Updated Successfully", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Objects.requireNonNull(binding.otpInputLayout.getEditText()).setText("");
                        binding.otpInputLayout.setVisibility(View.GONE);
                        binding.otpButton.setVisibility(View.GONE);
                        binding.phoneInputLayout.setVisibility(View.VISIBLE);
                        updatePhone();
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
                Objects.requireNonNull(binding.otpInputLayout.getEditText()).setText(code);
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
package com.cuekids.eclass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cuekids.eclass.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;
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

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class profileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentProfileBinding binding;
    SharedPreferences shp;
    Animation atg, btgone, btgtwo;
    SharedPreferences.Editor shpEditor;
    Uri profileUri;
    String name, email, age, phone, tempPhone;
    String downloadImageUrl, imagePathName;
    FirebaseAuth mAuth;
    String verificationId;
    private SMSReceiver smsReceiver;
    Context context;

    public profileFragment() {
        // Required empty public constructor
    }

    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        context = getActivity();
        assert context != null;

        binding.progressBar.getProgress();
        binding.progressBar.setVisibility(View.GONE);

        atg = AnimationUtils.loadAnimation(getActivity(), R.anim.atg);
        btgone = AnimationUtils.loadAnimation(getActivity(), R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(getActivity(), R.anim.btgtwo);
        binding.buttonAnimationLayout.startAnimation(atg);
        binding.buttonsLayout.startAnimation(btgone);

        shp = getActivity().getSharedPreferences("myPreferences", MODE_PRIVATE);
        if (shp == null)
            shp = getActivity().getSharedPreferences("myPreferences", MODE_PRIVATE);
        name = shp.getString("name", "");
        email = shp.getString("email", "");
        age = shp.getString("age", "");
        phone = shp.getString("phone", "");
        downloadImageUrl = shp.getString("url", "empty");

        Objects.requireNonNull(binding.nameInputLayout.getEditText()).setText(name);
        Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).setText(email);
        Objects.requireNonNull(binding.phoneInputLayout.getEditText()).setText(phone);
        Objects.requireNonNull(binding.ageInputLayout.getEditText()).setText(age);

        if (downloadImageUrl.equals("empty"))
            binding.iupload.setBackgroundResource(R.drawable.ic_baseline_person_24);
        else
            Glide.with(binding.iupload.getContext()).load(downloadImageUrl).circleCrop().into(binding.iupload);

        final ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(getContext(), "Uploading. Please Wait", Toast.LENGTH_SHORT).show();
                        binding.buttonsLayout.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.progressBar.startAnimation(btgtwo);
                        profileUri = uri;
                        imagePathName = UUID.randomUUID().toString() + ".jpg";
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

        binding.iupload.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Change profile picture");
            builder.setPositiveButton("Upload", (dialog, which) -> mGetContent.launch("image/*"));
            builder.setNegativeButton("Remove", (dialog, which) -> {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUser = reference.orderByChild("phone").equalTo(phone);
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reference.child(phone).removeValue();
                        userHelperClass helperClass = new userHelperClass(name, email, age, phone, "empty");
                        reference.child(phone).setValue(helperClass);
                        shpEditor = shp.edit();
                        shpEditor.putString("url", "empty");
                        shpEditor.apply();
                        downloadImageUrl = "empty";
                        binding.iupload.setImageDrawable(null);
                        binding.iupload.setBackgroundResource(R.drawable.ic_baseline_person_24);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        binding.nameUpdateButton.setOnClickListener(v -> {
            String tempName = binding.nameInputLayout.getEditText().getText().toString();
            if (!tempName.equals(name) && validateName())
                updateName(tempName);
            binding.nameEditText.clearFocus();
            hideKeyboard();
            binding.nameUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.phoneUpdateButton.setOnClickListener(v -> {
            tempPhone = binding.phoneInputLayout.getEditText().getText().toString();
            if (!tempPhone.equals(phone) && validatePhone())
                alreadyExists();
            binding.phoneEditText.clearFocus();
            hideKeyboard();
            binding.phoneUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.emailUpdateButton.setOnClickListener(v -> {
            String tempEmail = binding.emailIdInputLayout.getEditText().getText().toString();
            if (!tempEmail.equals(email) && validateEmail())
                updateEmail(tempEmail);
            binding.emailIdEditText.clearFocus();
            hideKeyboard();
            binding.emailUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.ageUpdateButton.setOnClickListener(v -> {
            String tempAge = binding.ageInputLayout.getEditText().getText().toString();
            if (!tempAge.equals(age) && validateAge())
                updateAge(tempAge);
            binding.ageEditText.clearFocus();
            hideKeyboard();
            binding.ageUpdateButton.setVisibility(View.INVISIBLE);
        });

        binding.otpButton.setOnClickListener(v -> {
            String otp = Objects.requireNonNull(binding.otpInputLayout.getEditText()).getText().toString();
            if (validateOTP()) {
                verifyCode(otp);
            }
        });

        binding.logoutbutton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Are you sure");
            builder.setMessage("You want to logout ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                if (shp == null)
                    shp = getActivity().getSharedPreferences("myPreferences", MODE_PRIVATE);
                shpEditor = shp.edit();
                shpEditor.clear();
                shpEditor.apply();
                Intent intent = new Intent(getContext(), SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
        return binding.getRoot();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void uploadPhoto() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("UserProfileImages").child(imagePathName);
        UploadTask uploadTask = imageRef.putFile(profileUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isComplete()) {
                downloadImageUrl = task.getResult().toString();
                binding.progressBar.setVisibility(View.GONE);
                binding.buttonsLayout.setVisibility(View.VISIBLE);
                binding.buttonsLayout.startAnimation(btgtwo);
                Glide.with(binding.iupload.getContext()).load(profileUri).circleCrop().into(binding.iupload);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUser = reference.orderByChild("phone").equalTo(phone);
                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        reference.child(phone).removeValue();
                        userHelperClass helperClass = new userHelperClass(name, email, age, phone, downloadImageUrl);
                        reference.child(phone).setValue(helperClass);
                        shpEditor = shp.edit();
                        shpEditor.putString("url", downloadImageUrl);
                        shpEditor.apply();
                        Toast.makeText(getContext(), "Profile Picture Uploaded", Toast.LENGTH_LONG).show();
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
            context.registerReceiver(smsReceiver, intentFilter);

            smsReceiver.setOTPListener(new SMSReceiver.OTPReceiveListener() {
                @Override
                public void onOTPReceived(String otp) {
                    Objects.requireNonNull(binding.otpInputLayout.getEditText()).setText(otp);
                    verifyCode(otp);
                    if (smsReceiver != null) {
                        context.unregisterReceiver(smsReceiver);
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
            SmsRetrieverClient client = SmsRetriever.getClient(context);
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
    public void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            context.unregisterReceiver(smsReceiver);
        }
    }

    private boolean validateName() {
        String val = Objects.requireNonNull(binding.nameInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(getActivity(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.nameInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateEmail() {
        String val = Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(getActivity(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.emailIdInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateAge() {
        String val = Objects.requireNonNull(binding.ageInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(getActivity(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.ageInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validatePhone() {
        String val = Objects.requireNonNull(binding.phoneInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(getActivity(), "Enter your Phone Number", Toast.LENGTH_SHORT).show();
            binding.phoneInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateOTP() {
        String val = Objects.requireNonNull(binding.otpInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(getActivity(), "OTP field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.otpInputLayout.requestFocus();
            return false;
        } else if (val.length() != 6) {
            Toast.makeText(getActivity(), "Incorrect OTP", Toast.LENGTH_SHORT).show();
            binding.otpInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    public void alreadyExists() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(tempPhone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Toast.makeText(getActivity(), "This phone number is already registered with us", Toast.LENGTH_LONG).show();
                } else {
                    binding.phoneInputLayout.setVisibility(View.GONE);
                    binding.phoneUpdateButton.setVisibility(View.GONE);
                    binding.otpInputLayout.setVisibility(View.VISIBLE);
                    binding.otpButton.setVisibility(View.VISIBLE);
                    startSMSListener();
                    Toast.makeText(getActivity(), "Sending OTP ! Please Wait", Toast.LENGTH_SHORT).show();
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
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
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
                Toast.makeText(getActivity(), "Name Updated Successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updatePhone() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
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
                Toast.makeText(getActivity(), "Phone Number updated successfully !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateEmail(String tempEmail) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
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
                Toast.makeText(getActivity(), "Email Updated Successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateAge(String tempAge) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
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
                Toast.makeText(getActivity(), "Age Updated Successfully", Toast.LENGTH_LONG).show();
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
                        hideKeyboard();
                        updatePhone();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)        // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS)                // Timeout duration
                .setActivity(getActivity())   // Activity (for callback binding)
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
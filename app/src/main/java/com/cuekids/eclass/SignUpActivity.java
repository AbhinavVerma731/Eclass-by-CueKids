package com.cuekids.eclass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.cuekids.eclass.databinding.ActivitySignUpBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    Animation btgone, btgtwo;
    String name, email, age, phone;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        phone = i.getStringExtra("phone");

        binding.progressBar.getProgress();
        binding.progressBar.setVisibility(View.GONE);

        btgone = AnimationUtils.loadAnimation(this, R.anim.btgone);
        btgtwo = AnimationUtils.loadAnimation(this, R.anim.btgtwo);

        binding.buttonAnimationLayout.startAnimation(btgone);
        binding.btnSignup.startAnimation(btgtwo);

        binding.btnSignup.setOnClickListener(v -> {
            if(validateFirstName() && validateLastName() && validateEmail() && validateAge())
                signUpUser();
        });
    }

    private boolean validateFirstName() {
        String val = Objects.requireNonNull(binding.firstNameInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.firstNameInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateLastName() {
        String val = Objects.requireNonNull(binding.lastNameInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.lastNameInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateEmail() {
        String val = Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.emailIdInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    private boolean validateAge() {
        String val = Objects.requireNonNull(binding.ageInputLayout.getEditText()).getText().toString();
        if (val.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            binding.ageInputLayout.requestFocus();
            return false;
        } else
            return true;
    }

    public void signUpUser()
    {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressBar.startAnimation(btgtwo);
        String firstName = Objects.requireNonNull(binding.firstNameInputLayout.getEditText()).getText().toString();
        String lastName = Objects.requireNonNull(binding.lastNameInputLayout.getEditText()).getText().toString();
        name = firstName + " " + lastName;
        email = Objects.requireNonNull(binding.emailIdInputLayout.getEditText()).getText().toString();
        age = Objects.requireNonNull(binding.ageInputLayout.getEditText()).getText().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("phone").equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userHelperClass helperClass = new userHelperClass(name, email, age, phone, "empty");
                    reference.child(phone).setValue(helperClass);
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("age", age);
                    intent.putExtra("phone", phone);
                    shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
                    shpEditor = shp.edit();
                    shpEditor.putString("name", name);
                    shpEditor.putString("email", email);
                    shpEditor.putString("age", age);
                    shpEditor.putString("phone", phone);
                    shpEditor.apply();
                    Toast.makeText(SignUpActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
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
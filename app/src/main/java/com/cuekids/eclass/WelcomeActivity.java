package com.cuekids.eclass;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.cuekids.eclass.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;
    private int dotsCount;
    private ImageView[] dots;
    Animation atg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPagerWelcomeAdapter viewPagerWelcomeAdapter = new ViewPagerWelcomeAdapter(this);
        binding.viewPager.setAdapter(viewPagerWelcomeAdapter);

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        binding.viewPager.startAnimation(atg);
        binding.sliderDots.startAnimation(atg);
        binding.goToLogin.startAnimation(atg);

        dotsCount = viewPagerWelcomeAdapter.getCount();
        dots = new ImageView[dotsCount];

        for(int i=0; i<dotsCount; i++)
        {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 0, 16, 0);
            binding.sliderDots.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for(int i=0; i<dotsCount; i++)
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        binding.goToLogin.setOnClickListener(v -> {
           Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
           startActivity(i);
        });
    }
}
package com.example.cuekids;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cuekids.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (MainActivity.this.checkConnectivity()) {
                binding.webview.setVisibility(View.VISIBLE);
                binding.noInternetLayout.setVisibility(View.GONE);
                view.loadUrl(url);
                return true;
            } else {
                binding.webview.setVisibility(View.GONE);
                binding.noInternetLayout.setVisibility(View.VISIBLE);
                return false;
            }
        }
    }

    private ActivityMainBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.webview.setWebViewClient(new MyWebViewClient(){
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        WebSettings webSettings = binding.webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        webSettings.setLoadsImagesAutomatically(true);
        binding.webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        String url = "https://eclass.cuekids.in";
        setWebView(url);

        binding.swiperefresh.setOnRefreshListener(() -> {
            binding.swiperefresh.setRefreshing(true);
            new Handler().postDelayed(() -> {
                binding.swiperefresh.setRefreshing(false);
                setWebView(url);
            }, 4500);
        });

        binding.swiperefresh.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_dark),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_red_dark)
        );

        binding.logoutbutton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure");
            builder.setMessage("You want to logout ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(MainActivity.this, "Please wait! Clearing your data", Toast.LENGTH_SHORT).show();
                if (shp == null)
                    shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
                shpEditor = shp.edit();
                shpEditor.clear();
                shpEditor.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void setWebView(String url) {

        if (checkConnectivity()) {
            binding.webview.setVisibility(View.VISIBLE);
            binding.noInternetLayout.setVisibility(View.GONE);
            binding.webview.loadUrl(url);
        } else {
            binding.webview.setVisibility(View.GONE);
            binding.noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
    }

    boolean checkConnectivity() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = manager.getActiveNetworkInfo();
        return (i != null && i.isConnected() && i.isAvailable());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && binding.webview.canGoBack()) {
            binding.webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
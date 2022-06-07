package com.cuekids.eclass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cuekids.eclass.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (WebViewActivity.this.checkConnectivity()) {
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

    private ActivityWebViewBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.webview.setWebViewClient(new MyWebViewClient());
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
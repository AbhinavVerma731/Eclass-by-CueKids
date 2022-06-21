package com.cuekids.eclass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cuekids.eclass.databinding.FragmentHomeBinding;

public class homeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (checkConnectivity()) {
                binding.noInternetLayout.setVisibility(View.GONE);
                binding.backButtonLayout.setVisibility(View.VISIBLE);
                binding.webview.setVisibility(View.VISIBLE);
                view.loadUrl(url);
                return true;
            } else {
                binding.backButtonLayout.setVisibility(View.GONE);
                binding.webview.setVisibility(View.GONE);
                binding.noInternetLayout.setVisibility(View.VISIBLE);
                return false;
            }
        }
    }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentHomeBinding binding;

    public homeFragment() {
        // Required empty public constructor
    }

    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

        binding.webview.setWebViewClient(new homeFragment.MyWebViewClient());
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

        binding.backButton.setOnClickListener(v -> {
            if (binding.webview.canGoBack()) {
                binding.webview.goBack();
            }
        });

        return binding.getRoot();
    }

    private void setWebView(String url) {
        if (checkConnectivity()) {
            binding.noInternetLayout.setVisibility(View.GONE);
            binding.backButtonLayout.setVisibility(View.VISIBLE);
            binding.webview.setVisibility(View.VISIBLE);
            binding.webview.loadUrl(url);
        } else {
            binding.backButtonLayout.setVisibility(View.GONE);
            binding.webview.setVisibility(View.GONE);
            binding.noInternetLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
    }

    boolean checkConnectivity() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = manager.getActiveNetworkInfo();
        return (i != null && i.isConnected() && i.isAvailable());
    }
}
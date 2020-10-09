package com.photatos.dalin.mlkit.ghost.view.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.ghost.account.AccountManager;
import com.photatos.dalin.mlkit.ghost.model.entity.Post;
import com.photatos.dalin.mlkit.ghost.util.AppUtils;
import com.photatos.dalin.mlkit.ghost.util.NetworkUtils;
import com.photatos.dalin.mlkit.ghost.view.BundleKeys;

public class PostViewFragment extends BaseFragment
        implements WebViewFragment.OnWebViewCreatedListener {

    private Post mPost;
    private int mContentHashCode;
    private WebViewFragment mWebViewFragment;

    public static PostViewFragment newInstance(@NonNull Post post) {
        PostViewFragment fragment = new PostViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(BundleKeys.POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);
        bindView(view);

        mPost = getArguments().getParcelable(BundleKeys.POST);

        mWebViewFragment = WebViewFragment.newInstance("file:///android_asset/post-preview.html");
        mWebViewFragment.setOnWebViewCreatedListener(this);
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.web_view_container, mWebViewFragment)
                .commit();

        return view;
    }

    @Override
    public void onWebViewCreated() {
        final String blogUrl = AccountManager.getActiveBlogUrl();
        mWebViewFragment.setJSInterface(new Object() {
            @JavascriptInterface
            public String getTitle() {
                return mPost.getTitle();
            }

            @JavascriptInterface
            public String getMarkdown() {
                return mPost.getMarkdown();
            }

            @JavascriptInterface
            public String getBlogUrl() {
                return blogUrl;
            }
        }, "POST");
        mWebViewFragment.setWebViewClient(new WebViewFragment.DefaultWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                updatePreview();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // launch links in external browser
                AppUtils.openUri(PostViewFragment.this,
                        NetworkUtils.makeAbsoluteUrl(blogUrl, url));
                return true;
            }
        });
    }

    public void updatePreview() {
        mWebViewFragment.evaluateJavascript("updateTitle()");
        int contentHashCode = mPost.getMobiledoc().hashCode();
        if (contentHashCode != mContentHashCode) {
            mWebViewFragment.evaluateJavascript("preview()");
            mContentHashCode = contentHashCode;
        }
    }

    public void setPost(@NonNull Post post) {
        mPost = post;
        updatePreview();
    }

}

package com.photatos.dalin.mlkit.ghost.view.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import com.photatos.dalin.mlkit.R;

public class GhostV0ErrorFragment extends BaseFragment {

    @BindView(R.id.error_title)
    TextView mErrorTitle;
    @BindView(R.id.error_content)
    TextView mErrorContent;
    @BindView(R.id.error_tip)
    TextView mErrorTip;
    @BindView(R.id.error_legacy)
    TextView mErrorLegacy;

    public static GhostV0ErrorFragment newInstance() {
        GhostV0ErrorFragment fragment = new GhostV0ErrorFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public GhostV0ErrorFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ghost_v0_error, container, false);
        bindView(view);
        String appName = getString(R.string.app_name);
        mErrorTitle.setText(Html.fromHtml(mErrorTitle.getText().toString()));
        mErrorContent.setText(Html.fromHtml(mErrorContent.getText().toString()));
        mErrorTip.setText(Html.fromHtml(mErrorTip.getText().toString()));
        mErrorLegacy.setText(Html.fromHtml(String.format(mErrorLegacy.getText().toString(), appName)));
        mErrorTitle.setMovementMethod(LinkMovementMethod.getInstance());
        mErrorContent.setMovementMethod(LinkMovementMethod.getInstance());
        mErrorTip.setMovementMethod(LinkMovementMethod.getInstance());
        mErrorLegacy.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

}

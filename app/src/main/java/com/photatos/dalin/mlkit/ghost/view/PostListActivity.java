package com.photatos.dalin.mlkit.ghost.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
/*
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;
*/
import androidx.annotation.ColorInt;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.Toolbar;


import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.photatos.dalin.mlkit.ghost.model.entity.Tag;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;
import com.photatos.dalin.mlkit.BuildConfig;
import com.photatos.dalin.mlkit.R;
import com.photatos.dalin.mlkit.ghost.SpectreApplication;
import com.photatos.dalin.mlkit.ghost.account.AccountManager;
import com.photatos.dalin.mlkit.ghost.error.SyncException;
import com.photatos.dalin.mlkit.ghost.event.BlogSettingsLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.CreatePostEvent;
import com.photatos.dalin.mlkit.ghost.event.DataRefreshedEvent;
import com.photatos.dalin.mlkit.ghost.event.ForceCancelRefreshEvent;
import com.photatos.dalin.mlkit.ghost.event.LogoutEvent;
import com.photatos.dalin.mlkit.ghost.event.LogoutStatusEvent;
import com.photatos.dalin.mlkit.ghost.event.PostConflictFoundEvent;
import com.photatos.dalin.mlkit.ghost.event.PostCreatedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostsLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.RefreshDataEvent;
import com.photatos.dalin.mlkit.ghost.event.UserLoadedEvent;
import com.photatos.dalin.mlkit.ghost.model.entity.Post;
import com.photatos.dalin.mlkit.ghost.model.entity.Setting;
import com.photatos.dalin.mlkit.ghost.network.GhostApiUtils;
import com.photatos.dalin.mlkit.ghost.util.DeviceUtils;
import com.photatos.dalin.mlkit.ghost.util.log.Log;
import com.photatos.dalin.mlkit.ghost.view.image.BorderedCircleTransformation;
import com.photatos.dalin.mlkit.ghost.view.widget.SpaceItemDecoration;
import retrofit2.Response;

import static com.photatos.dalin.mlkit.ghost.util.NetworkUtils.isConnectionError;
import static com.photatos.dalin.mlkit.ghost.util.NetworkUtils.makePicassoUrl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import android.widget.SearchView;
import androidx.appcompat.widget.SearchView;


public class PostListActivity extends BaseActivity {

    private static final String TAG = "PostListActivity";
    private static final int REQUEST_CODE_VIEW_POST = 1;

    private final List<Post> mPosts = new ArrayList<>();

    private final List<Post> mOnlyPosts = new ArrayList<>();

    private final List<Post> mSearchPosts = new ArrayList<>();


    private PostAdapter mPostAdapter;

    private PostAdapter mPostSearchAdapter;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRefreshDataRunnable;
    private Runnable mRefreshTimeoutRunnable;

    private static final int REFRESH_FREQUENCY = 10 * 60 * 1000;    // in milliseconds

    // NOTE: very large timeout is needed for cases like initial sync on a blog with 100s of posts
    private static final int REFRESH_TIMEOUT = 5 * 60 * 1000;       // in milliseconds





    @BindView(R.id.toolbar)                     Toolbar mToolbar;
    @BindView(R.id.app_bar_bg)
    View mAppBarBg;
    @BindView(R.id.user_image)
    ImageView mUserImageView;
    @BindView(R.id.user_blog_title)
    TextView mBlogTitleView;
    @BindView(R.id.swipe_refresh_layout)        SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.post_list_container)
    FrameLayout mPostListContainer;
    @BindView(R.id.post_list)                   RecyclerView mPostList;

    @BindView(R.id.new_post_reveal)
    View mNewPostRevealView;
    @BindView(R.id.new_post_reveal_shrink)
    View mNewPostRevealShrinkView;
    @BindDimen(R.dimen.toolbar_height)      int mToolbarHeight;
    @BindDimen(R.dimen.tabbar_height)       int mTabbarHeight;
    @ColorInt private                       int mColorAccent;
    @ColorInt private                       int mColorPrimary;

    //point
    private Integer PointValue;
    private Integer PostValue;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("text");
    DatabaseReference conditionRefPost = mRootRef.child("post");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (! AccountManager.hasActiveBlog()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (! AccountManager.getActiveBlog().isLoggedIn()) {
            // is it safe to infer that an active blog which is not logged in must mean the
            // password has changed or Ghost Auth code is expired?
            credentialsExpired();
        }

        setLayout(R.layout.activity_post_list);

         //search
        SearchView simpleSearchView = (SearchView) findViewById(R.id.simpleSearchView);

        //simpleSearchView.setLayoutParams(new ActionBar.LayoutParams(Gravity.RIGHT));

        CharSequence query = simpleSearchView.getQuery();

        simpleSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetpost();
            }
        });
        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchpost(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            }
        });

        setSupportActionBar(mToolbar);
        if (BuildConfig.DEBUG) {
            SpectreApplication.getInstance().addDebugDrawer(this);
        }

        // get rid of the default action bar confetti
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(0);

        // constants for animation
        TypedValue typedColorValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedColorValue, true);
        mColorAccent = typedColorValue.data;
        getTheme().resolveAttribute(R.attr.colorPrimary, typedColorValue, true);
        mColorPrimary = typedColorValue.data;

        // initialize post list UI
        //final String activeBlogUrl = AccountManager.getActiveBlogUrl();
        //http://34.67.19.15/tag/my-pic/
        //
        final String activeBlogUrl = getString(R.string.mypicurl);

        mPostAdapter = new PostAdapter(this, mOnlyPosts, activeBlogUrl, getPicasso(), v -> {
        //mPostAdapter = new PostAdapter(this, mPosts, activeBlogUrl, getPicasso(), v -> {
            int pos = mPostList.getChildLayoutPosition(v);
            android.util.Log.d(TAG, " PostAdapter pos: " + pos);
            if (pos == RecyclerView.NO_POSITION) return;
            Post post = (Post) mPostAdapter.getItem(pos);
            android.util.Log.d(TAG, " post.getTags(): " + post.getTags());


            if (! GhostApiUtils.INSTANCE.hasOnlyMarkdownCard(post.getMobiledoc())) {
                //Snackbar.make(mPostList, R.string.koenig_post_error,
                Snackbar.make(mPostList, R.string.koenig_post_error,
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (post.isMarkedForDeletion()) {
                Snackbar.make(mPostList, R.string.status_marked_for_deletion_open_error,
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(PostListActivity.this, PostViewActivity.class);
            intent.putExtra(BundleKeys.POST, post);
            intent.putExtra(BundleKeys.START_EDITING, false);
            Bundle activityOptions = ActivityOptions.makeScaleUpAnimation(v, 0, 0,
                    v.getWidth(), v.getHeight()).toBundle();
            startActivityForResult(intent, REQUEST_CODE_VIEW_POST, activityOptions);
        });
        mPostList.setAdapter(mPostAdapter);
        mPostList.setLayoutManager(new StaggeredGridLayoutManager(
                getResources().getInteger(R.integer.post_grid_num_columns),
                StaggeredGridLayoutManager.VERTICAL));
        mPostList.setItemAnimator(new DefaultItemAnimator());
        int hSpace = getResources().getDimensionPixelOffset(R.dimen.card_grid_hspace);
        int vSpace = getResources().getDimensionPixelOffset(R.dimen.card_grid_vspace);
        mPostList.addItemDecoration(new SpaceItemDecoration(hSpace, vSpace));

        // use a fixed-width grid on large screens
        int screenWidth = DeviceUtils.getScreenWidth(this);
        int maxContainerWidth = getResources().getDimensionPixelSize(R.dimen.post_grid_max_width);
        if (screenWidth > maxContainerWidth) {
            int containerPadding = (screenWidth - maxContainerWidth) / 2;
            ViewCompat.setPaddingRelative(mToolbar,
                    ViewCompat.getPaddingStart(mToolbar) + containerPadding,
                    mToolbar.getPaddingTop(),
                    ViewCompat.getPaddingEnd(mToolbar) + containerPadding,
                    mToolbar.getPaddingBottom());
            ViewCompat.setPaddingRelative(mPostList,
                    ViewCompat.getPaddingStart(mPostList) + containerPadding,
                    mPostList.getPaddingTop(),
                    ViewCompat.getPaddingEnd(mPostList) + containerPadding,
                    mPostList.getPaddingBottom());
        }

        final Drawable appbarShadowDrawable;
        appbarShadowDrawable = ContextCompat.getDrawable(this, R.drawable.appbar_shadow);
        mPostListContainer.setForeground(null);     // hide the shadow initially
        mPostList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int scrollY = mPostList.computeVerticalScrollOffset();
                mAppBarBg.setTranslationY(-scrollY);
                mPostListContainer.setForeground(scrollY <= 0 ? null : appbarShadowDrawable);
            }
        });

        mRefreshDataRunnable = () -> refreshData(false);
        mRefreshTimeoutRunnable = this::refreshTimedOut;
        mSwipeRefreshLayout.setColorSchemeColors(mColorAccent, mColorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(() -> refreshData(false));

        //point
        addListenerOnButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // load cached data immediately
        refreshData(true);
        // reset views involved in new post animation
        mNewPostRevealView.setVisibility(View.INVISIBLE);
        mNewPostRevealShrinkView.setScaleY(1f);
        mNewPostRevealShrinkView.setBackgroundColor(mColorAccent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelDataRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPostList != null) {
            // cancel any ongoing image requests, courtesy http://stackoverflow.com/a/33961706/504611
            // not doing this in onPause or onStop because there we wouldn't want to clear the list itself
            mPostList.setAdapter(null);

            //

        }
        mRefreshDataRunnable = null;    // the runnable holds an implicit reference to the activity!
                                        // allow it to get GC'ed to avoid a memory leak
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.post_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_homepage:
                startBrowserActivity(AccountManager.getActiveBlogUrl());
                return true;
            case R.id.action_refresh:
                refreshData(false);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.action_logout:
                getBus().post(new LogoutEvent(AccountManager.getActiveBlogUrl(), false));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VIEW_POST && resultCode == PostViewActivity.RESULT_CODE_DELETED) {
            Snackbar.make(mPostList, R.string.post_deleted, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onDataRefreshedEvent(DataRefreshedEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);
        cancelRefreshTimeout();
        scheduleDataRefresh();

        if (event.apiFailure == null) {
            return;
        }

        Throwable error = event.apiFailure.error;
        Response response = event.apiFailure.response;
        if (error != null && isConnectionError(error)) {
            Toast.makeText(this, R.string.network_timeout, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Generic error message triggered during refresh");
            if (error != null) {
                Log.exception(new SyncException("sync failed", error));
            } else if (response != null) {
                try {
                    Log.exception(new SyncException("Response: " + response.errorBody().string()));
                } catch (Exception exception) {
                    Log.exception(new SyncException("Sync failed, but threw this when trying to " +
                            "log response", exception));
                }
            }
        }
        Toast.makeText(this, R.string.refresh_failed, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onUserLoadedEvent(UserLoadedEvent event) {
        if (event.user.getProfileImage() != null) {
            if (event.user.getProfileImage().isEmpty()) {
                return;
            }
            String blogUrl = AccountManager.getActiveBlogUrl();
            String imageUrl = makePicassoUrl(blogUrl, event.user.getProfileImage());
            getPicasso()
                    .load(imageUrl)
                    .transform(new BorderedCircleTransformation())
                    .fit()
                    .into(mUserImageView);
        } else {
            // As of Ghost v2.13.1 (possibly earlier), profile image is null if not set
            Log.w(TAG, "user image is null!");
        }
    }

    @Subscribe
    public void onBlogSettingsLoadedEvent(BlogSettingsLoadedEvent event) {
        String blogTitle = getString(R.string.app_name);
        for (Setting setting : event.settings) {
            if (setting.getKey().equals("title")) {
                blogTitle = setting.getValue();
            }
//            android.util.Log.d(TAG, " setting.getKey(): " + setting.getKey());
//            android.util.Log.d(TAG, " setting.getValue(): " + setting.getValue());


        }
        mBlogTitleView.setText(blogTitle);
    }


    public void resetpost() {

        if (mPosts.size() > 0) {
            mOnlyPosts.clear();
            android.util.Log.d(TAG, " mPosts.size(): " + mPosts.size());
            for (int i = 0; i < mPosts.size(); i++) {
                Post post=mPosts.get(i);
                android.util.Log.d(TAG, " post.getTitle(): " + post.getTitle());
                // only post
                if(checkpost(post)) {
                    mOnlyPosts.add(post);  //post.getTitle()
                    android.util.Log.d(TAG, " BOARD:======================= ");
                    android.util.Log.d(TAG, " post.getTitle(): " + post.getTitle());
                }
                //android.util.Log.d(TAG, "event.posts.get(i): " + event.posts.get(i));
            }

            mPostAdapter.notifyDataSetChanged();

        }

    }


    public void searchpost(String searchtxt) {

        if (mPosts.size() > 0) {

            android.util.Log.d(TAG, " mPosts.size(): " + mPosts.size());
            mOnlyPosts.clear();
            for (int i = 0; i < mPosts.size(); i++) {
                Post post=mPosts.get(i);
                android.util.Log.d(TAG, " post.getTitle(): " + post.getTitle());
                // only post
                if(checkpost(post)) {
                    android.util.Log.d(TAG, " BOARD:======================= ");
                    if (post.getTitle().contains(searchtxt)) {
                        mOnlyPosts.add(post);  //post.getTitle()
                        android.util.Log.d(TAG, " searchtxt:======================= ");
                        android.util.Log.d(TAG, " post.getTitle(): " + post.getTitle());
                    }
                }
                //android.util.Log.d(TAG, "event.posts.get(i): " + event.posts.get(i));
            }

            mPostAdapter.notifyDataSetChanged();

        }

    }


    public boolean checkpost(Post post) {

        List<Tag> tags = post.getTags();
        if (tags.size() > 0) {
            if (tags.get(0).getName().equals("BOARD")) {
                return true;
            }
        }
        return false;
    }
    @Subscribe
    public void onPostsLoadedEvent(PostsLoadedEvent event) {
        // this exists to let animation run to completion because posts are loaded
        // twice on launch: once cached data, and once from the network
        if (mPosts.equals(event.posts)) {
            return;
        }
        mPosts.clear();
        mPosts.addAll(event.posts);
        android.util.Log.d(TAG, " mPosts.size(): " + mPosts.size());
        for (int i=0;i <mPosts.size();i++){
            Post post=event.posts.get(i);
            // only post
            if(checkpost(post)){
                mOnlyPosts.add(post);  //post.getTitle()
                android.util.Log.d(TAG, " BOARD:======================= " );
                android.util.Log.d(TAG, " post.getTitle(): "+post.getTitle() );
            }
            //android.util.Log.d(TAG, "event.posts.get(i): " + event.posts.get(i));
        }

        if (mPosts.size() >= event.postsFetchLimit) {
            android.util.Log.d(TAG, " mPosts.size(): " + mPosts.size());
            android.util.Log.d(TAG, " mPosts.size(): " + mPosts.size());
            CharSequence message = Html.fromHtml(getString(R.string.post_limit_exceeded,
                    getString(R.string.app_name), event.postsFetchLimit,
                    "https://github.com/TryGhost/Ghost-Android/issues/15"));
            android.util.Log.d(TAG, " message: " + message);
            mPostAdapter.showFooter(message);
        } else {
            mPostAdapter.hideFooter();
        }
        mPostAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.new_post_btn)
    public void onNewPostBtnClicked(View btn) {
        Runnable createPost = () -> getBus().post(new CreatePostEvent());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // circular reveal animation
            int[] revealViewLocation = new int[2], btnLocation = new int[2];
            mNewPostRevealView.getLocationOnScreen(revealViewLocation);
            btn.getLocationOnScreen(btnLocation);
            int centerX = btnLocation[0] - revealViewLocation[0] + btn.getWidth()/2;
            int centerY = btnLocation[1] - revealViewLocation[1] + btn.getHeight()/2;
            float endRadius = (float) Math.hypot(centerX, centerY);
            Animator revealAnimator = ViewAnimationUtils.createCircularReveal(
                    mNewPostRevealView, centerX, centerY, 0, endRadius);
            revealAnimator.setDuration(500);
            revealAnimator.setInterpolator(new AccelerateInterpolator());
            mNewPostRevealView.setVisibility(View.VISIBLE);

            // background color animation
            ValueAnimator colorAnimator = ValueAnimator
                    .ofObject(new ArgbEvaluator(), mColorAccent, mColorPrimary);
            colorAnimator.addUpdateListener(animator ->
                    mNewPostRevealShrinkView.setBackgroundColor((int) animator.getAnimatedValue()));
            colorAnimator.setDuration(500);
            colorAnimator.setInterpolator(new AccelerateInterpolator());

            // shrink animation
            float startHeight = mNewPostRevealShrinkView.getHeight();
            float targetScaleY = (mToolbarHeight + mTabbarHeight) / startHeight;
            ObjectAnimator shrinkAnimator = ObjectAnimator.ofFloat(mNewPostRevealShrinkView,
                    "scaleY", targetScaleY);
            shrinkAnimator.setStartDelay(150);
            shrinkAnimator.setDuration(300);
            shrinkAnimator.setInterpolator(new DecelerateInterpolator());

            // play reveal + color change together, followed by shrink
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(revealAnimator).with(colorAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shrinkAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            createPost.run();
                        }
                    });
                    shrinkAnimator.start();
                }
            });
            animatorSet.start();
        } else {
            createPost.run();
        }
    }



    public void addListenerOnButton() {
        conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                if (text==null)
                {
                    PointValue =100;
                } else
                {
                    PointValue = Integer.parseInt(text);
                }
                // point.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        conditionRefPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String textPost = dataSnapshot.getValue(String.class);
                if (textPost==null)
                {
                    PostValue =1;
                } else
                {
                    PostValue = Integer.parseInt(textPost);
                }
                // point.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    @Subscribe
    public void onPostCreatedEvent(PostCreatedEvent event) {
        Intent intent = new Intent(PostListActivity.this, PostViewActivity.class);
        intent.putExtra(BundleKeys.POST, event.newPost);
        intent.putExtra(BundleKeys.START_EDITING, true);
        startActivityForResult(intent, REQUEST_CODE_VIEW_POST);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // setup the next Activity to fade-in, since we just finished the circular reveal
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        // increase point ---------------------------------
        PointValue=PointValue+1000;
        String text=Integer.toString(PointValue);
        conditionRef.setValue(text);
        //-------------------------------------------------
        // increase point count
        PostValue=PostValue+1;
        String textpost=Integer.toString(PostValue);
        conditionRefPost.setValue(textpost);
        //--------------------------------------------------
    }

    @Subscribe
    public void onLogoutStatusEvent(LogoutStatusEvent event) {
        if (!event.succeeded && event.hasPendingActions) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.unsynced_changes_msg))
                    .setPositiveButton(R.string.dont_logout, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.logout, (dialog, which) -> {
                        dialog.dismiss();
                        getBus().post(new LogoutEvent(AccountManager.getActiveBlogUrl(), true));
                    })
                    .create();
            alertDialog.show();
        } else {
            finish();
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            startActivity(logoutIntent);
        }
    }

    @Subscribe
    public void onPostConflictFoundEvent(PostConflictFoundEvent event) {
        Intent intent = new Intent(this, PostConflictResolutionActivity.class);
        intent.putExtra(BundleKeys.LOCAL_POST, event.localPost);
        intent.putExtra(BundleKeys.SERVER_POST, event.serverPost);
        startActivity(intent);
    }

    // private methods
    private void scheduleDataRefresh() {
        // cancel already-scheduled refresh event
        cancelDataRefresh();
        // NOTE do not pass this::refreshData directly, because that creates a new Runnable and
        // hence cannot be removed using Handler.removeCallbacks later, indirectly causing the
        // entire Activity to leak!
        mHandler.postDelayed(mRefreshDataRunnable, REFRESH_FREQUENCY);
    }

    private void cancelDataRefresh() {
        mHandler.removeCallbacks(mRefreshDataRunnable);
        cancelRefreshTimeout();
    }

    private void refreshData(boolean loadCachedData) {
        getBus().post(new RefreshDataEvent(loadCachedData));
        mHandler.postDelayed(mRefreshTimeoutRunnable, REFRESH_TIMEOUT);
    }

    private void cancelRefreshTimeout() {
        mHandler.removeCallbacks(mRefreshTimeoutRunnable);
    }

    private void refreshTimedOut() {
        getBus().post(new ForceCancelRefreshEvent());
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, R.string.refresh_failed, Toast.LENGTH_LONG).show();
        scheduleDataRefresh();
    }

}

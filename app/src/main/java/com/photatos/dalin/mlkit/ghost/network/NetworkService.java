package com.photatos.dalin.mlkit.ghost.network;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import com.photatos.dalin.mlkit.ghost.account.AccountManager;
import com.photatos.dalin.mlkit.ghost.analytics.AnalyticsService;
import com.photatos.dalin.mlkit.ghost.auth.AuthService;
import com.photatos.dalin.mlkit.ghost.auth.AuthStore;
import com.photatos.dalin.mlkit.ghost.auth.LoginOrchestrator;
import com.photatos.dalin.mlkit.ghost.error.PostConflictFoundException;
import com.photatos.dalin.mlkit.ghost.event.ApiCallEvent;
import com.photatos.dalin.mlkit.ghost.event.ApiErrorEvent;
import com.photatos.dalin.mlkit.ghost.event.BlogSettingsLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.BusProvider;
import com.photatos.dalin.mlkit.ghost.event.CreatePostEvent;
import com.photatos.dalin.mlkit.ghost.event.DataRefreshedEvent;
import com.photatos.dalin.mlkit.ghost.event.DeletePostEvent;
import com.photatos.dalin.mlkit.ghost.event.FileUploadErrorEvent;
import com.photatos.dalin.mlkit.ghost.event.FileUploadEvent;
import com.photatos.dalin.mlkit.ghost.event.FileUploadedEvent;
import com.photatos.dalin.mlkit.ghost.event.ForceCancelRefreshEvent;
import com.photatos.dalin.mlkit.ghost.event.GhostVersionLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.LoadBlogSettingsEvent;
import com.photatos.dalin.mlkit.ghost.event.LoadGhostVersionEvent;
import com.photatos.dalin.mlkit.ghost.event.LoadPostsEvent;
import com.photatos.dalin.mlkit.ghost.event.LoadTagsEvent;
import com.photatos.dalin.mlkit.ghost.event.LoadUserEvent;
import com.photatos.dalin.mlkit.ghost.event.LogoutEvent;
import com.photatos.dalin.mlkit.ghost.event.LogoutStatusEvent;
import com.photatos.dalin.mlkit.ghost.event.PostConflictFoundEvent;
import com.photatos.dalin.mlkit.ghost.event.PostCreatedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostDeletedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostReplacedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostSavedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostSyncedEvent;
import com.photatos.dalin.mlkit.ghost.event.PostsLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.RefreshDataEvent;
import com.photatos.dalin.mlkit.ghost.event.SavePostEvent;
import com.photatos.dalin.mlkit.ghost.event.SyncPostsEvent;
import com.photatos.dalin.mlkit.ghost.event.TagsLoadedEvent;
import com.photatos.dalin.mlkit.ghost.event.UserLoadedEvent;
import com.photatos.dalin.mlkit.ghost.model.RealmUtils;
import com.photatos.dalin.mlkit.ghost.model.entity.AuthToken;
import com.photatos.dalin.mlkit.ghost.model.entity.BlogMetadata;
import com.photatos.dalin.mlkit.ghost.model.entity.ConfigurationParam;
import com.photatos.dalin.mlkit.ghost.model.entity.ETag;
import com.photatos.dalin.mlkit.ghost.model.entity.PendingAction;
import com.photatos.dalin.mlkit.ghost.model.entity.Post;
import com.photatos.dalin.mlkit.ghost.model.entity.Setting;
import com.photatos.dalin.mlkit.ghost.model.entity.Tag;
import com.photatos.dalin.mlkit.ghost.model.entity.User;
import com.photatos.dalin.mlkit.ghost.network.entity.PostList;
import com.photatos.dalin.mlkit.ghost.network.entity.PostStubList;
import com.photatos.dalin.mlkit.ghost.network.entity.SettingsList;
import com.photatos.dalin.mlkit.ghost.network.entity.UserList;
import com.photatos.dalin.mlkit.ghost.util.DateTimeUtils;
import com.photatos.dalin.mlkit.ghost.util.NetworkUtils;
import com.photatos.dalin.mlkit.ghost.util.PostUtils;
import com.photatos.dalin.mlkit.ghost.util.functions.Action0;
import com.photatos.dalin.mlkit.ghost.util.functions.Action1;
import com.photatos.dalin.mlkit.ghost.util.functions.Action2;
import com.photatos.dalin.mlkit.ghost.util.log.Log;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkService implements
        LoginOrchestrator.HACKListener,
        AuthService.Listener
{

    private static final String TAG = "NetworkService";

    // number of posts to fetch
    private static final int POSTS_FETCH_LIMIT = 30;

    private Realm mRealm = null;
    private GhostApiService mApi = null;
    private AuthToken mAuthToken = null;
    private AuthService mAuthService = null;

    private boolean mbSyncOnGoing = false;
    private ApiFailure mRefreshError = null;
    private final ArrayDeque<ApiCallEvent> mApiEventQueue = new ArrayDeque<>();
    private final ArrayDeque<ApiCallEvent> mRefreshEventsQueue = new ArrayDeque<>();

    public void start(OkHttpClient httpClient) {
        Log.i(TAG, "Initializing NetworkService...");
        getBus().register(this);
        if (AccountManager.hasActiveBlog()) {
            BlogMetadata activeBlog = AccountManager.getActiveBlog();
            GhostApiService api = GhostApiUtils.INSTANCE.getRetrofit(activeBlog.getBlogUrl(), httpClient)
                    .create(GhostApiService.class);
            setApiService(activeBlog.getBlogUrl(), api);

            mRealm = Realm.getInstance(activeBlog.getDataRealmConfig());
            mAuthToken = new AuthToken(mRealm.where(AuthToken.class).findFirst());
        }
    }

    // I don't know how to call this from the Application class!
    @SuppressWarnings("unused")
    public void stop() {
        getBus().unregister(this);
        mRealm.close();
    }

    // TODO temporary crutch while I refactor this huge class
    @Override
    public void setApiService(String blogUrl, GhostApiService api) {
        mApi = api;
        if (mAuthService != null) {
            mAuthService.unlisten(this);
        }
        mAuthService = AuthService.createWithStoredCredentials(blogUrl, api);
        mAuthService.listen(this);
    }

    @Override
    public void onNewAuthToken(AuthToken authToken) {
        Log.d(TAG, "Got new access token = " + authToken.getAccessToken());
        authToken.setCreatedAt(DateTimeUtils.getEpochSeconds());
        mAuthToken = new AuthToken(createOrUpdateModel(authToken));
        flushApiEventQueue(false);
    }

    @Override
    public void onNewLogin(String blogUrl, AuthToken authToken) {
        AccountManager.setActiveBlog(blogUrl);
        BlogMetadata activeBlog = AccountManager.getActiveBlog();
        mRealm = Realm.getInstance(activeBlog.getDataRealmConfig());
        onNewAuthToken(authToken);
    }

    @Override
    public void onUnrecoverableFailure() {
        flushApiEventQueue(true);
    }

    @Subscribe
    public void onRefreshDataEvent(RefreshDataEvent event) {
        // do nothing if a refresh is already in progress
        // optimization disabled because sometimes (rarely) the queue doesn't get emptied correctly
        // e.g., logout followed by login
//        if (! mRefreshEventsQueue.isEmpty()) {
//            refreshSucceeded(null);
//            return;
//        }

        mRefreshEventsQueue.clear();
        mRefreshError = null;           // clear last error if any
        mRefreshEventsQueue.addAll(Arrays.asList(
                new LoadUserEvent(true),
                new LoadBlogSettingsEvent(true),
                new SyncPostsEvent(true)
        ));

        for (ApiCallEvent refreshEvent : mRefreshEventsQueue) {
            if (event.loadCachedData) {
                refreshEvent.loadCachedData();
            }
            getBus().post(refreshEvent);
        }
    }

    @Subscribe
    public void onForceCancelRefreshEvent(ForceCancelRefreshEvent event) {
        // sometimes (rarely) the DataRefreshedEvent is not sent because an ApiCallEvent
        // doesn't get cleared from the queue, this is to guard against that
        if (! mRefreshEventsQueue.isEmpty()) {
            mRefreshEventsQueue.clear();
            mRefreshError = null;           // clear last error if any
        }
    }

    @Subscribe
    public void onLoadGhostVersionEvent(LoadGhostVersionEvent event) {
        if (mAuthToken == null) {
            return; // can't do much, not logged in
        }

        final String UNKNOWN_VERSION = "Unknown";

        if (! event.forceNetworkCall) {
            RealmQuery<ConfigurationParam> query = mRealm.where(ConfigurationParam.class)
                    .equalTo("key", "version", Case.INSENSITIVE);
            if (query.count() > 0) {
                getBus().post(new GhostVersionLoadedEvent(query.findFirst().getValue()));
                return;
            }
        }

        mApi.getVersion(mAuthToken.getAuthHeader()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    try {
                        String ghostVersion = response.body()
                                .get("configuration").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("version").getAsString();
                        getBus().post(new GhostVersionLoadedEvent(ghostVersion));
                    } catch (Exception e) {
                        getBus().post(new GhostVersionLoadedEvent(UNKNOWN_VERSION));
                    }
                } else {
                    getBus().post(new GhostVersionLoadedEvent(UNKNOWN_VERSION));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable error) {
                // error in transport layer, or lower
                getBus().post(new GhostVersionLoadedEvent(UNKNOWN_VERSION));
            }
        });
    }

    @Subscribe
    public void onLoadUserEvent(final LoadUserEvent event) {
        if (event.loadCachedData || ! event.forceNetworkCall) {
            RealmResults<User> users = mRealm.where(User.class).findAll();
            if (users.size() > 0) {
                getBus().post(new UserLoadedEvent(users.first()));
                refreshSucceeded(event);
                return;
            }
            // else no users found in db, force a network call!
        }

        mApi.getCurrentUser(mAuthToken.getAuthHeader(), loadEtag(ETag.TYPE_CURRENT_USER)).enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                if (response.isSuccessful()) {
                    UserList userList = response.body();
                    storeEtag(response.headers(), ETag.TYPE_CURRENT_USER);
                    createOrUpdateModel(userList.users);
                    getBus().post(new UserLoadedEvent(userList.users.get(0)));

                    // download all posts again to enforce role-based permissions for this user
                    removeEtag(ETag.TYPE_ALL_POSTS);
                    getBus().post(new SyncPostsEvent(false));

                    refreshSucceeded(event);
                } else {
                    // fallback to cached data
                    RealmResults<User> users = mRealm.where(User.class).findAll();
                    if (users.size() > 0) {
                        getBus().post(new UserLoadedEvent(users.first()));
                    }

                    if (NetworkUtils.isNotModified(response)) {
                        refreshSucceeded(event);
                    } else if (NetworkUtils.isUnauthorized(response)) {
                        // defer the event and try to re-authorize
                        refreshAccessToken(event);
                    } else {
                        ApiFailure<UserList> apiFailure = new ApiFailure<>(response);
                        getBus().post(new ApiErrorEvent(apiFailure));
                        refreshFailed(event, apiFailure);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable error) {
                // error in transport layer, or lower
                ApiFailure apiFailure = new ApiFailure<>(error);
                getBus().post(new ApiErrorEvent(apiFailure));
                refreshFailed(event, apiFailure);
            }
        });
    }

    @Subscribe
    public void onLoadBlogSettingsEvent(final LoadBlogSettingsEvent event) {
        if (event.loadCachedData || ! event.forceNetworkCall) {
            RealmResults<Setting> settings = mRealm.where(Setting.class).findAll();
            if (settings.size() > 0) {
                getBus().post(new BlogSettingsLoadedEvent(settings));
                refreshSucceeded(event);
                return;
            }
            // no settings found in db, force a network call!
        }

        mApi.getSettings(mAuthToken.getAuthHeader(), loadEtag(ETag.TYPE_BLOG_SETTINGS)).enqueue(new Callback<SettingsList>() {
            @Override
            public void onResponse(@NonNull Call<SettingsList> call, @NonNull Response<SettingsList> response) {
                if (response.isSuccessful()) {
                    SettingsList settingsList = response.body();
                    storeEtag(response.headers(), ETag.TYPE_BLOG_SETTINGS);
                    createOrUpdateModel(settingsList.settings);
                    // TODO this is dead code; permalink setting was removed in Ghost 2.0
                    // see https://github.com/TryGhost/Ghost/pull/9768/files
                    savePermalinkFormat(settingsList.settings);
                    getBus().post(new BlogSettingsLoadedEvent(settingsList.settings));
                    refreshSucceeded(event);
                } else {
                    // fallback to cached data
                    RealmResults<Setting> settings = mRealm.where(Setting.class).findAll();
                    if (settings.size() > 0) {
                        getBus().post(new BlogSettingsLoadedEvent(settings));
                    }

                    if (NetworkUtils.isNotModified(response)) {
                        refreshSucceeded(event);
                    } else if (NetworkUtils.isUnauthorized(response)) {
                        // defer the event and try to re-authorize
                        refreshAccessToken(event);
                    } else {
                        ApiFailure<SettingsList> apiFailure = new ApiFailure<>(response);
                        getBus().post(new ApiErrorEvent(apiFailure));
                        refreshFailed(event, apiFailure);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SettingsList> call, @NonNull Throwable error) {
                // error in transport layer, or lower
                ApiFailure<SettingsList> apiFailure = new ApiFailure<>(error);
                getBus().post(new ApiErrorEvent(apiFailure));
                refreshFailed(event, apiFailure);
            }
        });
    }

    @Subscribe
    public void onLoadPostsEvent(final LoadPostsEvent event) {
        if (event.loadCachedData || ! event.forceNetworkCall) {
            List<Post> posts = getPostsSorted();
            // if there are no posts, there could be 2 cases:
            // 1. there are actually no posts
            // 2. we just haven't fetched any posts from the server yet (Realm returns an empty list in this case too)
            if (posts.size() > 0) {
                getBus().post(new PostsLoadedEvent(posts, POSTS_FETCH_LIMIT));
                refreshSucceeded(event);
                return;
            }
        }

        RealmResults<User> users = mRealm.where(User.class).findAll();
        if (users.size() == 0) {
            return;
        }

        User user = users.first();
        String authorFilter = null;     // Retrofit skips parameters with null values
        if (user.isOnlyAuthorOrContributor()) {
            authorFilter = "author:" + user.getSlug();
        }

        final Call<PostList> postListCall = mApi.getPosts(mAuthToken.getAuthHeader(),
                loadEtag(ETag.TYPE_ALL_POSTS), authorFilter, POSTS_FETCH_LIMIT);
        postListCall.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(@NonNull Call<PostList> call, @NonNull Response<PostList> response) {
                if (response.isSuccessful()) {
                    PostList postList = response.body();
                    storeEtag(response.headers(), ETag.TYPE_ALL_POSTS);

                    // delete local copies of posts that are no longer within the POSTS_FETCH_LIMIT
                    // FIXME if there are any locally-edited posts at this point that were pushed
                    // FIXME beyond POSTS_FETCH_LIMIT, their local copies will also be deleted!
                    Iterable<Post> deletedPosts = Observable.fromIterable(mRealm.where(Post.class).findAll())
                            .filter(cached -> ! postList.contains(cached.getId()))
                            .blockingIterable();
                    deleteModels(deletedPosts);

                    // skip edited posts because they've not yet been uploaded
                    RealmResults<Post> localOnlyEdits = mRealm.where(Post.class)
                            .in("pendingActions.type", new String[] {
                                    PendingAction.EDIT_LOCAL,
                                    PendingAction.EDIT
                            })
                            .findAll();
                    for (int i = postList.getPosts().size() - 1; i >= 0; --i) {
                        for (int j = 0; j < localOnlyEdits.size(); ++j) {
                            if (postList.getPosts().get(i).getId().equals(localOnlyEdits.get(j).getId())) {
                                postList.getPosts().remove(i);
                            }
                        }
                    }

                    // make sure drafts have a publishedAt of FAR_FUTURE so they're sorted to the top
                    Observable.fromIterable(postList.getPosts())
                            .filter(post -> post.getPublishedAt() == null)
                            .forEach(post -> post.setPublishedAt(DateTimeUtils.FAR_FUTURE));

                    // now create / update received posts
                    // TODO use Realm#insertOrUpdate() for faster insertion here: https://realm.io/news/realm-java-1.1.0/
                    createOrUpdateModel(postList.getPosts());
                    getBus().post(new PostsLoadedEvent(getPostsSorted(), POSTS_FETCH_LIMIT));

                    refreshSucceeded(event);
                } else {
                    // fallback to cached data
                    getBus().post(new PostsLoadedEvent(getPostsSorted(), POSTS_FETCH_LIMIT));
                    if (NetworkUtils.isNotModified(response)) {
                        refreshSucceeded(event);
                    } else if (NetworkUtils.isUnauthorized(response)) {
                        // defer the event and try to re-authorize
                        refreshAccessToken(event);
                    } else {
                        ApiFailure<PostList> apiFailure = new ApiFailure<>(response);
                        getBus().post(new ApiErrorEvent(apiFailure));
                        refreshFailed(event, apiFailure);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PostList> call, @NonNull Throwable error) {
                // error in transport layer, or lower
                ApiFailure<PostList> apiFailure = new ApiFailure<>(error);
                getBus().post(new ApiErrorEvent(apiFailure));
                refreshFailed(event, apiFailure);
            }
        });
    }

    @Subscribe
    public void onCreatePostEvent(final CreatePostEvent event) {
        Log.i(TAG, "[onCreatePostEvent] creating new post");
        Post newPost = new Post();
        newPost.setMobiledoc(GhostApiUtils.INSTANCE.initializeMobiledoc());
        newPost.addPendingAction(PendingAction.CREATE);
        newPost.setId(getTempUniqueId(Post.class));
        createOrUpdateModel(newPost);                    // save the local post to db
        getBus().post(new PostCreatedEvent(newPost));
        getBus().post(new SyncPostsEvent(false));
    }

    @Subscribe
    public void onSyncPostsEvent(final SyncPostsEvent event) {
        if (event.loadCachedData) {
            LoadPostsEvent loadPostsEvent = new LoadPostsEvent(false);
            mRefreshEventsQueue.add(loadPostsEvent);
            getBus().post(loadPostsEvent);
            refreshSucceeded(event);
            return;
        }
        // FIXME (1) this prevents e.g., double draft creation but it may prevent e.g. a post from
        // FIXME     being synced when it is triggered when a previous sync is in progress
        // FIXME (2) also ensure 2 sync requests with different values for forceNetworkCall do
        // FIXME     result in a network call instead of blindly skipping (see comment in
        // FIXME     onSavePostEvent marked "synchack")
        // don't trigger another sync if it is already in progress
        if (mbSyncOnGoing) {
            return;
        }

        // load cached data, to update the post list immediately when a conflict is resolved
        getBus().post(new PostsLoadedEvent(getPostsSorted(), POSTS_FETCH_LIMIT));

        final List<Post> localDeletedPosts = copyPosts(mRealm.where(Post.class)
                .equalTo("pendingActions.type", PendingAction.DELETE)
                .findAll());
        final List<Post> localNewPosts = copyPosts(mRealm.where(Post.class)
                .equalTo("pendingActions.type", PendingAction.CREATE)
                .findAll());
        final List<Post> localEditedPosts = copyPosts(mRealm.where(Post.class)
                .equalTo("pendingActions.type", PendingAction.EDIT)
                .findAll());

        Deque<Post> postUploadQueue = new ArrayDeque<>();
        postUploadQueue.addAll(localDeletedPosts);
        postUploadQueue.addAll(localNewPosts);
        postUploadQueue.addAll(localEditedPosts);

        // nothing to upload
        if (postUploadQueue.isEmpty()) {
            LoadPostsEvent loadPostsEvent = new LoadPostsEvent(event.forceNetworkCall);
            mRefreshEventsQueue.add(loadPostsEvent);
            getBus().post(loadPostsEvent);
            refreshSucceeded(event);
            return;
        }

        // keep track of new posts created successfully and posts deleted successfully, so the local copies can be deleted
        List<Post> postsToDelete = new ArrayList<>();

        // ugly hack (suggested by the IDE) because this must be declared "final"
        final ApiFailure[] uploadError = {null};

        final Action0 syncFinishedCB = () -> {
            // delete local copies
            if (!postsToDelete.isEmpty()) {
                RealmQuery<Post> deleteQuery = mRealm.where(Post.class);
                for (int i = 0; i < postsToDelete.size(); ++i) {
                    Post post = postsToDelete.get(i);
                    if (i > 0) deleteQuery.or();
                    deleteQuery.equalTo("id", post.getId());
                    Log.i(TAG, "[onSyncPostsEvent] deleted local copy of post %s",
                            post.getId());
                }
                deleteModels(deleteQuery.findAll());
            }

            ApiFailure apiFailure = uploadError[0];
            if (apiFailure != null && NetworkUtils.isUnauthorized(apiFailure.response)) {
                // defer the event and try to re-authorize
                refreshAccessToken(event);
            } else {
                if (apiFailure != null && NetworkUtils.isNotModified(apiFailure.response)) {
                    refreshSucceeded(event);
                } else {
                    //noinspection ConstantConditions
                    refreshFailed(event, apiFailure);
                }
                getBus().post(new PostsLoadedEvent(getPostsSorted(), POSTS_FETCH_LIMIT));
                // if forceNetworkCall is true, first load from the db, AND only then from the network,
                // to avoid a crash because local posts have been deleted above but are still being
                // displayed, so we need to refresh the UI first
                if (event.forceNetworkCall) {
                    LoadPostsEvent loadPostsEvent = new LoadPostsEvent(true);
                    mRefreshEventsQueue.add(loadPostsEvent);
                    getBus().post(loadPostsEvent);
                }
            }

            // SYNC COMPLETE
            mbSyncOnGoing = false;
        };

        final Action2<Post, ApiFailure> onFailure = (post, apiFailure) -> {
            uploadError[0] = apiFailure;
            postUploadQueue.removeFirstOccurrence(post);
            getBus().post(new ApiErrorEvent(apiFailure));
            if (postUploadQueue.isEmpty()) syncFinishedCB.call();
        };

        // MAKE SURE THIS IS NEVER true LONGER THAN IT NEEDS TO BE, CHECK ALL EXIT POINTS OF THIS FN
        mbSyncOnGoing = true;

        // 1. DELETED POSTS
        // the loop variable is *local* to the loop block, so it can be captured in a closure easily
        // this is unlike JavaScript, in which the same loop variable is mutated
        for (final Post localPost : localDeletedPosts) {
            Log.i(TAG, "[onSyncPostsEvent] deleting post %s", localPost.getId());
            mApi.deletePost(mAuthToken.getAuthHeader(), localPost.getId()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        AnalyticsService.logDraftDeleted();
                        Log.i(TAG, "[onSyncPostsEvent] deleted remote post %s",
                                localPost.getId());
                        postUploadQueue.removeFirstOccurrence(localPost);
                        postsToDelete.add(localPost);
                        if (postUploadQueue.isEmpty()) syncFinishedCB.call();
                    } else {
                        Log.e(TAG, "[onSyncPostsEvent] failed to delete remote post, " +
                                "id %s", localPost.getId());
                        onFailure.call(localPost, new ApiFailure<>(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable error) {
                    Log.e(TAG, "[onSyncPostsEvent] failed to delete remote post, " +
                            "id %s", localPost.getId());
                    onFailure.call(localPost, new ApiFailure<>(error));
                }
            });
        }

        // 2. NEW POSTS
        for (final Post localPost : localNewPosts) {
            Log.i(TAG, "[onSyncPostsEvent] creating post with local id %s", localPost.getId());
            mApi.createPost(mAuthToken.getAuthHeader(), PostStubList.from(localPost)).enqueue(new Callback<PostList>() {
                @Override
                public void onResponse(@NonNull Call<PostList> call, @NonNull Response<PostList> response) {
                    if (response.isSuccessful()) {
                        PostList postList = response.body();
                        AnalyticsService.logNewDraftUploaded();
                        Post updatedPost = copyPosts(createOrUpdateModel(postList.getPosts())).get(0);
                        Log.i(TAG, "[onSyncPostsEvent] created post %s", updatedPost.getId());
                        postUploadQueue.removeFirstOccurrence(localPost);
                        postsToDelete.add(localPost);
                        // new posts do not have the mobiledoc field set, so retain
                        // those values from the local copy
                        updatedPost.setMobiledoc(localPost.getMobiledoc());
                        // FIXME this is a new post! how do subscribers know which post changed?
                        getBus().post(new PostReplacedEvent(updatedPost));
                        if (postUploadQueue.isEmpty()) syncFinishedCB.call();
                    } else {
                        Log.e(TAG, "[onSyncPostsEvent] failed to sync new post, " +
                                "local id %s", localPost.getId());
                        onFailure.call(localPost, new ApiFailure<>(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PostList> call, @NonNull Throwable error) {
                    Log.e(TAG, "[onSyncPostsEvent] failed to sync new post, " +
                            "local id %s", localPost.getId());
                    onFailure.call(localPost, new ApiFailure<>(error));
                }
            });
        }

        // 3. EDITED POSTS
        Action1<Post> uploadEditedPost = (editedPost) -> {
            Log.i(TAG, "[onSyncPostsEvent] updating post %s", editedPost.getId());
            PostStubList postStubList = PostStubList.from(editedPost);
            mApi.updatePost(mAuthToken.getAuthHeader(), editedPost.getId(), postStubList).enqueue(new Callback<PostList>() {
                @Override
                public void onResponse(@NonNull Call<PostList> call, @NonNull Response<PostList> response) {
                    if (response.isSuccessful()) {
                        PostList postList = response.body();
                        Post syncedPost = postList.getPosts().get(0);
                        Post realmPost = mRealm.where(Post.class)
                                .equalTo("id", editedPost.getId())
                                .findFirst();
                        // saved posts do not have the mobiledoc field set, so retain
                        // those values from a pre-save copy
                        syncedPost.setMobiledoc(realmPost.getMobiledoc());
                        if (!realmPost.getId().equals(syncedPost.getId())) {
                            Log.wtf("Trying to update a post with a different id! " +
                                    "syncedPost.id = %s, realmPost.id = %s",
                                    syncedPost.getId(), realmPost.getId());
                        }
                        Log.i(TAG, "[onSyncPostsEvent] updated post %s",
                                syncedPost.getId());
                        createOrUpdateModel(postList.getPosts());
                        postUploadQueue.removeFirstOccurrence(editedPost);
                        getBus().post(new PostSyncedEvent(syncedPost));
                        if (postUploadQueue.isEmpty()) syncFinishedCB.call();
                    } else {
                        Log.e(TAG, "[onSyncPostsEvent] failed to update post %s",
                                editedPost.getId());
                        onFailure.call(editedPost, new ApiFailure<>(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PostList> call, @NonNull Throwable error) {
                    Log.e(TAG, "[onSyncPostsEvent] failed to update post %s",
                            editedPost.getId());
                    onFailure.call(editedPost, new ApiFailure<>(error));
                }
            });
        };
        for (final Post localPost : localEditedPosts) {
            Log.i(TAG, "[onSyncPostsEvent] downloading edited post %s for comparison",
                    localPost.getId());
            mApi.getPost(mAuthToken.getAuthHeader(), localPost.getId()).enqueue(new Callback<PostList>() {
                @Override
                public void onResponse(@NonNull Call<PostList> call, @NonNull Response<PostList> response) {
                    if (response.isSuccessful()) {
                        PostList postList = response.body();
                        Post serverPost = null;
                        boolean hasConflict = false;
                        if (!postList.getPosts().isEmpty()) {
                            serverPost = postList.getPosts().get(0);
                            hasConflict = (serverPost.getUpdatedAt() != null
                                    && !serverPost.getUpdatedAt().equals(localPost.getUpdatedAt()));
                        }
                        if (hasConflict && PostUtils.isDirty(serverPost, localPost)) {
                            Log.w(TAG, "[onSyncPostsEvent] conflict found for post %s", localPost.getId());
                            postUploadQueue.removeFirstOccurrence(localPost);
                            if (postUploadQueue.isEmpty()) syncFinishedCB.call();
                            localPost.setConflictState(Post.CONFLICT_UNRESOLVED);
                            createOrUpdateModel(localPost);
                            Log.i(TAG, "localPost updated at: %s", localPost.getUpdatedAt().toString());
                            Log.i(TAG, "serverPost updated at: %s", serverPost.getUpdatedAt().toString());
                            Log.i(TAG, "localPost contents: %s", localPost.getMarkdown());
                            Log.i(TAG, "serverPost contents: %s", serverPost.getMarkdown());
                            Log.exception(new PostConflictFoundException());
                            getBus().post(new PostConflictFoundEvent(localPost, serverPost));
                        } else {
                            uploadEditedPost.call(localPost);
                        }
                    } else {
                        Log.w(TAG, "[onSyncPostsEvent] couldn't get server post for " +
                                "conflict detection - uploading local copy optimistically");
                        uploadEditedPost.call(localPost);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PostList> call, @NonNull Throwable error) {
                    Log.w(TAG, "[onSyncPostsEvent] couldn't get server post for " +
                            "conflict detection - uploading local copy optimistically");
                    uploadEditedPost.call(localPost);
                }
            });
        }
    }

    @Subscribe
    public void onSavePostEvent(SavePostEvent event) {
        Post updatedPost = event.post;
        Post realmPost = mRealm.where(Post.class)
                .equalTo("id", event.post.getId())
                .findFirst();
        Log.i(TAG, "[onSavePostEvent] post id = %s", event.post.getId());

        if (realmPost.hasPendingAction(PendingAction.DELETE)) {
            RuntimeException e = new IllegalArgumentException("Trying to save deleted post with id = " + realmPost.getId());
            Log.exception(e);
        }

        // TODO bug in edge-case:
        // TODO 1. user publishes draft, offline => pending actions = { EDIT }
        // TODO 2. then makes some edits offline which are auto-saved => pending actions = { EDIT_LOCAL }
        // TODO the post will not actually be published now!
        // TODO to resolve this we would require some notion of pending actions associated with
        // TODO specific fields of a post rather than the entire post

        // save tags to Realm first
        for (Tag tag : updatedPost.getTags()) {
            if (tag.getId() == null) {
                tag.setId(getTempUniqueId(Tag.class));
                createOrUpdateModel(tag);
            }
        }

        // At first glance, this might seem redundant: shouldn't the post being saved have the same
        // timestamp as the one in the DB? Actually, no, not if a sync was in progress when the post
        // was opened. This prevents spurious conflict detections caused by the following flow:
        // post saved => sync started => post opened again => sync complete => post saved again
        // NOTE: This logic does not harm conflict detection AS LONG AS THE POST IS SAVED BEFORE
        // THE NEXT SYNC OCCURS.
        if (realmPost.getUpdatedAt() != null && !realmPost.getUpdatedAt().equals(updatedPost.getUpdatedAt())) {
            updatedPost.setUpdatedAt(realmPost.getUpdatedAt());
        }
        createOrUpdateModel(updatedPost);                  // save the local post to db

        // must set PendingActions after other stuff, else the updated post's pending actions will
        // override the one in Realm!
        //noinspection StatementWithEmptyBody
        if (realmPost.hasPendingAction(PendingAction.CREATE)) {
            // no-op; if the post is yet to be created, we DO NOT change the PendingAction on it
        } else if (realmPost.isDraft()) {
            clearAndSetPendingActionOnPost(realmPost, PendingAction.EDIT);
        } else if ((realmPost.isScheduled() || realmPost.isPublished()) && event.isAutoSave) {
            clearAndSetPendingActionOnPost(realmPost, PendingAction.EDIT_LOCAL);
        } else {
            // user hit "update" explicitly, on a scheduled or published post, so mark it for uploading
            clearAndSetPendingActionOnPost(realmPost, PendingAction.EDIT);
        }

        Post savedPost = new Post(realmPost);   // realmPost is guaranteed to be up-to-date
        getBus().post(new PostSavedEvent(savedPost));
        // FIXME #synchack: force a network call because this preempts sync requests from the data
        // FIXME refresh phase triggered when going back to the post list
        getBus().post(new SyncPostsEvent(true));
    }

    @Subscribe
    public void onDeletePostEvent(DeletePostEvent event) {
        String postId = event.post.getId();
        Log.i(TAG, "[onDeletePostEvent] post id = %s", postId);

        Post realmPost = mRealm.where(Post.class).equalTo("id", postId).findFirst();
        if (realmPost == null) {
            RuntimeException e = new IllegalArgumentException("Trying to delete post with non-existent id = " + postId);
            Log.exception(e);
        } else if (realmPost.hasPendingAction(PendingAction.CREATE)) {
            deleteModel(realmPost);
            getBus().post(new PostDeletedEvent(postId));
        } else {
            // don't delete locally until the remote copy is deleted
            clearAndSetPendingActionOnPost(realmPost, PendingAction.DELETE);
            getBus().post(new PostDeletedEvent(postId));

            // DON'T trigger a sync here, because it is automatically triggered by the post list anyway
            // triggering it twice causes crashes due to invalid Realm objects (deleted twice)
            //getBus().post(new SyncPostsEvent(false));
        }
    }

    @SuppressLint("DefaultLocale")
    @Subscribe
    public void onFileUploadEvent(FileUploadEvent event) {
        Log.i(TAG, "[onFileUploadEvent] uploading file");

        final InputStream inputStream = event.inputStream;
        final String filename = event.filename;
        final String mimeType = event.mimeType;

        byte[] fileBytes = null;
        try {
            final int CHUNK_SIZE = 4096;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(inputStream.available());
            byte[] buffer = new byte[CHUNK_SIZE];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            fileBytes = baos.toByteArray();
        } catch (IOException e) {
            getBus().post(new FileUploadErrorEvent(new ApiFailure(e)));
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                getBus().post(new FileUploadErrorEvent(new ApiFailure(e)));
            }
        }

        if (fileBytes == null) {
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse(mimeType), fileBytes);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("uploadimage", filename, body);

        mApi.uploadFile(mAuthToken.getAuthHeader(), filePart).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    String url = response.body().getAsString();
                    getBus().post(new FileUploadedEvent(url));
                } else {
                    if (NetworkUtils.isUnauthorized(response)) {
                        // defer the event and try to re-authorize
                        refreshAccessToken(event);
                    } else {
                        ApiFailure<JsonElement> apiFailure = new ApiFailure<>(response);
                        getBus().post(new FileUploadErrorEvent(apiFailure));
                        getBus().post(new ApiErrorEvent(apiFailure));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                // error in transport layer, or lower
                getBus().post(new ApiErrorEvent(new ApiFailure(error)));
            }
        });
    }

    @Subscribe
    public void onLoadTagsEvent(LoadTagsEvent event) {
        RealmResults<Tag> tags = mRealm.where(Tag.class).findAllSorted("name");
        List<Tag> tagsCopy = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            tagsCopy.add(new Tag(tag.getName()));
        }
        getBus().post(new TagsLoadedEvent(tagsCopy));
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {
        if (!event.forceLogout) {
            long numPostsWithPendingActions = mRealm
                    .where(Post.class)
                    .isNotEmpty("pendingActions")
                    .count();
            if (numPostsWithPendingActions > 0) {
                getBus().post(new LogoutStatusEvent(false, true));
                return;
            }
        }

        // revoke access and refresh tokens in the background
        mAuthService.revokeToken(mAuthToken);

        // clear all persisted blog data to avoid primary key conflicts
        mRealm.close();
        Realm.deleteRealm(mRealm.getConfiguration());
        String activeBlogUrl = AccountManager.getActiveBlogUrl();
        new AuthStore().deleteCredentials(activeBlogUrl);
        AccountManager.deleteBlog(activeBlogUrl);

        // switch the Realm to the now-active blog
        if (AccountManager.hasActiveBlog()) {
            mRealm = Realm.getInstance(AccountManager.getActiveBlog().getDataRealmConfig());
        }

        // reset state, to be sure
        mAuthToken = null;
        mAuthService = null;
        mApiEventQueue.clear();
        mRefreshEventsQueue.clear();
        mbSyncOnGoing = false;
        mRefreshError = null;
        getBus().post(new LogoutStatusEvent(true, false));
    }


    // private methods
    private void clearAndSetPendingActionOnPost(@NonNull Post post, @PendingAction.Type String newPendingAction) {
        List<PendingAction> pendingActions = post.getPendingActions();
        mRealm.executeTransaction(realm -> {
            // make a copy since the original is a live-updating RealmList
            List<PendingAction> pendingActionsCopy = new ArrayList<>(pendingActions);
            for (PendingAction pa : pendingActionsCopy) {
                RealmObject.deleteFromRealm(pa);
            }
            pendingActions.clear();
            post.addPendingAction(newPendingAction);
        });
    }

    private void refreshAccessToken(@Nullable final ApiCallEvent eventToDefer) {
        if (eventToDefer != null) {
            mApiEventQueue.addLast(eventToDefer);
        }
        mAuthService.refreshToken(mAuthToken);
    }

    private void flushApiEventQueue(boolean loadCachedData) {
        Bus bus = getBus();
        boolean isQueueEmpty;
        while (! mApiEventQueue.isEmpty()) {
            ApiCallEvent event = mApiEventQueue.remove();
            isQueueEmpty = mApiEventQueue.isEmpty();
            if (loadCachedData) event.loadCachedData();
            bus.post(event);
            if (isQueueEmpty) {     // don't retry, gets into infinite loop
                mApiEventQueue.clear();
            }
        }
    }

    private void refreshSucceeded(@Nullable ApiCallEvent sourceEvent) {
        refreshDone(sourceEvent, null);
    }

    private void refreshFailed(@Nullable ApiCallEvent sourceEvent, @NonNull ApiFailure apiFailure) {
        refreshDone(sourceEvent, apiFailure);
    }

    private void refreshDone(@Nullable ApiCallEvent sourceEvent, @Nullable ApiFailure apiFailure) {
        mRefreshEventsQueue.removeFirstOccurrence(sourceEvent);
        if (apiFailure != null) {
            mRefreshError = apiFailure;      // turn on error flag if *any* request fails
        }
        if (mRefreshEventsQueue.isEmpty()) {
            getBus().post(new DataRefreshedEvent(mRefreshError));
            mRefreshError = null;       // clear last error if any
        }
    }

    private void savePermalinkFormat(List<Setting> settings) {
        for (Setting setting : settings) {
            if ("permalinks".equals(setting.getKey()) && setting.getValue() != null) {
                BlogMetadata activeBlog = AccountManager.getActiveBlog();
                activeBlog.setPermalinkFormat(setting.getValue());
                AccountManager.addOrUpdateBlog(activeBlog);
            }
        }
    }

    private List<Post> getPostsSorted() {
        // FIXME time complexity O(n) for copying + O(n log n) for sorting!
        RealmResults<Post> realmPosts = mRealm.where(Post.class).findAll();
        List<Post> unmanagedPosts = copyPosts(realmPosts);
        Collections.sort(unmanagedPosts, PostUtils.COMPARATOR_MAIN_LIST);
        return unmanagedPosts;
    }

    private void storeEtag(Headers headers, @ETag.Type String etagType) {
        for (String name : headers.names()) {
            if ("ETag".equals(name) && !headers.values(name).isEmpty()) {
                ETag etag = new ETag(etagType, headers.values(name).get(0));
                createOrUpdateModel(etag);
            }
        }
    }

    private String loadEtag(@ETag.Type String etagType) {
        ETag etag = mRealm.where(ETag.class).equalTo("type", etagType).findFirst();
        return (etag != null) ? etag.getTag() : "";
    }

    private void removeEtag(@ETag.Type String etagType) {
        RealmResults<ETag> etags = mRealm.where(ETag.class).equalTo("type", etagType).findAll();
        if (etags.size() > 0) {
            deleteModel(etags.first());
        }
    }

    /**
     * Generates a temporary primary key until the actual id is generated by the server. <b>Be
     * careful when calling this in a loop, if you don't save the object before calling it again,
     * you'll get the same id twice!</b>
     */
    @NonNull
    private <T extends RealmModel> String getTempUniqueId(Class<T> clazz) {
        int tempId = Integer.MAX_VALUE;
        while (mRealm.where(clazz).equalTo("id", String.valueOf(tempId)).findAll().size() > 0) {
            --tempId;
        }
        return String.valueOf(tempId);
    }

    private List<Post> copyPosts(List<Post> posts) {
        List<Post> copied = new ArrayList<>(posts.size());
        for (Post model : posts) {
            copied.add(new Post(model));
        }
        return copied;
    }

    private <T extends RealmModel> T createOrUpdateModel(T object) {
        return createOrUpdateModel(object, null);
    }

    private <T extends RealmModel> T createOrUpdateModel(T object,
                                                         @Nullable Runnable afterTransaction) {
        return RealmUtils.executeTransaction(mRealm, realm -> {
            T realmObject = mRealm.copyToRealmOrUpdate(object);
            if (afterTransaction != null) {
                afterTransaction.run();
            }
            return realmObject;
        });
    }

    private <T extends RealmModel> List<T> createOrUpdateModel(Iterable<T> objects) {
        return createOrUpdateModel(objects, null);
    }

    private <T extends RealmModel> List<T> createOrUpdateModel(Iterable<T> objects,
                                                               @Nullable Runnable afterTransaction) {
        if (! objects.iterator().hasNext()) {
            return Collections.emptyList();
        }
        return RealmUtils.executeTransaction(mRealm, realm -> {
            List<T> realmObjects = mRealm.copyToRealmOrUpdate(objects);
            if (afterTransaction != null) {
                afterTransaction.run();
            }
            return realmObjects;
        });
    }

    private <T extends RealmModel> void deleteModel(T realmObject) {
        RealmUtils.executeTransaction(mRealm, realm -> {
            RealmObject.deleteFromRealm(realmObject);
        });
    }

    private <T extends RealmModel> void deleteModels(Iterable<T> realmObjects) {
        if (! realmObjects.iterator().hasNext()) {
            return;
        }
        RealmUtils.executeTransaction(mRealm, realm -> {
            for (T realmObject : realmObjects) {
                RealmObject.deleteFromRealm(realmObject);
            }
        });
    }

    private Bus getBus() {
        return BusProvider.getBus();
    }

}

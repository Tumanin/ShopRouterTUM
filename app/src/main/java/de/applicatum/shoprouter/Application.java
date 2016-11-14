package de.applicatum.shoprouter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import de.applicatum.shoprouter.model.Products.ProductsController;
import de.applicatum.shoprouter.model.Shops.ShopsController;
import de.applicatum.shoprouter.ui.LoginActivity;
import de.applicatum.shoprouter.ui.MainActivity;
import de.applicatum.shoprouter.utils.AppLog;
import de.applicatum.shoprouter.utils.StringUtil;
import de.applicatum.shoprouter.utils.UserProfile;


public class Application extends MultiDexApplication implements Replication.ChangeListener  {

    public static final String TAG = "Application";

    private static final String SYNC_URL_HTTP = "http://us-east.testfest.couchbasemobile.com:4984/todolite";

    // Storage Type: .SQLITE_STORAGE or .FORESTDB_STORAGE
    private static final String STORAGE_TYPE = Manager.SQLITE_STORAGE;

    // Encryption (Don't store encryption key in the source code. We are doing it here just as an example):
    private static final boolean ENCRYPTION_ENABLED = false;
    private static final String ENCRYPTION_KEY = "seekrit";

    // Logging:
    private static final boolean LOGGING_ENABLED = true;

    // Guest database:
    private static final String GUEST_DATABASE_NAME = "guest";
    private static final String GLOBAL_DATABASE_NAME = "global";

    private Manager mManager;
    private Database mGlobalDatabase;
    private Database mUserDatabase;
    private Replication mPull;
    private Replication mPush;
    private Throwable mReplError;
    private String mCurrentUserId;
    @Override
    public void onCreate() {
        super.onCreate();
        enableLogging();
        //loginAsGuest(null);
        ButterKnife.setDebug(true);
        ProductsController.getInstance().loadProductList(this);
        //loadData();
    }

    public void loadData(){
        ProductsController.getInstance().loadShoppingLists(this);
        ShopsController.getInstance().loadShopRoomObjects(this);
        ShopsController.getInstance().loadShops(this);
    }

    private void enableLogging() {
        if (LOGGING_ENABLED) {
            Manager.enableLogging(TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);
        }
    }

    private Manager getManager() {
        if (mManager == null) {
            try {
                AndroidContext context = new AndroidContext(getApplicationContext());
                mManager = new Manager(context, Manager.DEFAULT_OPTIONS);
            } catch (Exception e) {
                Log.e(TAG, "Cannot create Manager object", e);
            }
        }
        return mManager;
    }

    public Database getGlobalDatabase() {
        return mGlobalDatabase;
    }

    private void setGlobalDatabase(Database database) {
        this.mGlobalDatabase = database;
    }

    public Database getUserDatabase() {
        return mUserDatabase;
    }

    public void setUserDatabase(Database mUserDatabase) {
        this.mUserDatabase = mUserDatabase;
    }

    private Database getDatabase(String name) {
        try {
            String dbName = "db" + StringUtil.MD5(name);
            DatabaseOptions options = new DatabaseOptions();
            options.setCreate(true);
            options.setStorageType(STORAGE_TYPE);
            options.setEncryptionKey(ENCRYPTION_ENABLED ? ENCRYPTION_KEY : null);
            return getManager().openDatabase(dbName, options);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot create database for name: " + name, e);
        }
        return null;
    }

    public void loginAsFacebookUser(Activity activity, String token, String userId, String name) {
        setCurrentUserId(userId);
        setUserDatabase(getDatabase(userId));
        setGlobalDatabase(getDatabase(GLOBAL_DATABASE_NAME));
        String profileDocID = "p:" + userId;
        Document profile = mGlobalDatabase.getExistingDocument(profileDocID);
        if (profile == null) {
            try {
                Map<String, Object> properties = new HashMap<>();
                properties.put("type", "profile");
                properties.put("user_id", userId);
                properties.put("name", name);

                profile = mGlobalDatabase.getDocument(profileDocID);
                profile.putProperties(properties);

                // Migrate guest data to user:
                UserProfile.migrateGuestData(getDatabase(GUEST_DATABASE_NAME), profile);
            } catch (CouchbaseLiteException e) {
                Log.e(TAG, "Cannot create a new user profile", e);
            }
        }

        startReplication(AuthenticatorFactory.createFacebookAuthenticator(token));
        login(activity);
    }

    public void loginAsGuest(Activity activity) {
        setUserDatabase(getDatabase(GUEST_DATABASE_NAME));
        setGlobalDatabase(getDatabase(GLOBAL_DATABASE_NAME));
        setCurrentUserId(null);
        login(activity);
    }

    private void login(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public void logout() {
        setCurrentUserId(null);
        stopReplication();
        setGlobalDatabase(null);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setAction(LoginActivity.ACTION_LOGOUT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setCurrentUserId(String userId) {
        this.mCurrentUserId = userId;
    }

    public String getCurrentUserId() {
        return this.mCurrentUserId;
    }

    /** Replicator */

    private URL getSyncUrl() {
        URL url = null;
        try {
            url = new URL(SYNC_URL_HTTP);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid sync url", e);
        }
        return url;
    }

    private void startReplication(Authenticator auth) {
        if (mPull == null) {
            mPull = mGlobalDatabase.createPullReplication(getSyncUrl());
            mPull.setContinuous(true);
            mPull.setAuthenticator(auth);
            mPull.addChangeListener(this);
        }

        if (mPush == null) {
            mPush = mGlobalDatabase.createPushReplication(getSyncUrl());
            mPush.setContinuous(true);
            mPush.setAuthenticator(auth);
            mPush.addChangeListener(this);
        }

        mPull.stop();
        mPull.start();

        mPush.stop();
        mPush.start();
    }

    private void stopReplication() {
        if (mPull != null) {
            mPull.removeChangeListener(this);
            mPull.stop();
            mPull = null;
        }

        if (mPush != null) {
            mPush.removeChangeListener(this);
            mPush.stop();
            mPush = null;
        }
    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        Throwable error = null;
        if (mPull != null) {
            //noinspection ThrowableResultOfMethodCallIgnored
            error = mPull.getLastError();

        }

        if (error == null || error == mReplError)
            //noinspection ThrowableResultOfMethodCallIgnored
            error = mPush.getLastError();

        if (error != mReplError) {
            mReplError = error;
            if (mReplError != null)
                showErrorMessage(mReplError.getMessage(), null);
        }
    }

    /** Database View */

    public View getShopsView(){
        View view = mGlobalDatabase.getView("shops");
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get("type");
                    if ("shop".equals(type)){
                        List<Object> keys = new ArrayList<>();
                        keys.add(document.get("id"));
                        keys.add(document.get("name"));
                        keys.add(document.get("latitude"));
                        keys.add(document.get("longitude"));
                        keys.add(document.get("rating"));
                        keys.add(document.get("creator"));
                        keys.add(document.get("width"));
                        keys.add(document.get("height"));
                        keys.add(document.get("children"));

                        emitter.emit(keys, document);
                    }
                }
            };
            view.setMap(mapper, "1.0");
        }
        return view;
    }

    public View getShopRoomObjectsView(){
        View view = mGlobalDatabase.getView("shopRoomObjects");
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get("type");
                    AppLog.d(TAG, "getShopRoomObjectsView", "document.get(type): "+document.get("type"));
                    if ("enterexit".equals(type) || "cashdesk".equals(type)){
                        List<Object> keys = new ArrayList<>();
                        keys.add(document.get("id"));
                        keys.add(document.get("name"));
                        keys.add(document.get("x"));
                        keys.add(document.get("y"));

                        emitter.emit(keys, document);
                    }else if("shelf".equals(type)){
                        List<Object> keys = new ArrayList<>();
                        keys.add(document.get("id"));
                        keys.add(document.get("name"));
                        keys.add(document.get("x"));
                        keys.add(document.get("y"));
                        keys.add(document.get("productTopLeft"));
                        keys.add(document.get("productTopRight"));
                        keys.add(document.get("productDownLeft"));
                        keys.add(document.get("productDownRight"));

                        emitter.emit(keys, document);
                    }
                }
            };
            view.setMap(mapper, "1.0");
        }
        return view;
    }

    public View getShoppingListItemsView(){
        View view = mUserDatabase.getView("shoppingListItems");
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get("type");
                    if ("shoppingListItem".equals(type)){
                        List<Object> keys = new ArrayList<>();
                        keys.add(document.get("id"));
                        keys.add(document.get("name"));
                        keys.add(document.get("product"));
                        emitter.emit(keys, document);
                    }
                }
            };
            view.setMap(mapper, "1.0");
        }
        return view;
    }

    public View getShoppingListsView(){
        View view = mUserDatabase.getView("shoppingLists");
        if (view.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get("type");
                    if ("shoppingList".equals(type)){
                        List<Object> keys = new ArrayList<>();
                        keys.add(document.get("id"));
                        keys.add(document.get("name"));
                        keys.add(document.get("description"));
                        keys.add(document.get("children"));
                        keys.add(document.get("productGroup"));
                        emitter.emit(keys, document);
                    }
                }
            };
            view.setMap(mapper, "1.0");
        }
        return view;
    }

    public View getUserProfilesView() {
        View view = mGlobalDatabase.getView("profiles");
        if (view.getMap() == null) {
            Mapper map = new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if ("profile".equals(document.get("type")))
                        emitter.emit(document.get("name"), null);
                }
            };
            view.setMap(map, "1.0");
        }
        return view;
    }

    /** Display error message */

    public void showErrorMessage(final String errorMessage, final Throwable throwable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.util.Log.e(TAG, errorMessage, throwable);
                String msg = String.format("%s: %s",
                        errorMessage, throwable != null ? throwable : "");
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
        mainHandler.post(runnable);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

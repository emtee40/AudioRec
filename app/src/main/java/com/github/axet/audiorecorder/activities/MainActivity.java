package com.github.axet.audiorecorder.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.axet.androidlibrary.services.StorageProvider;
import com.github.axet.androidlibrary.widgets.AboutPreferenceCompat;
import com.github.axet.androidlibrary.widgets.SearchView;
import com.github.axet.audiolibrary.app.Recordings;
import com.github.axet.audiolibrary.app.Storage;
import com.github.axet.audiorecorder.R;
import com.github.axet.audiorecorder.app.MainApplication;
import com.github.axet.audiorecorder.services.RecordingService;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = MainActivity.class.getSimpleName();

    FloatingActionButton fab;
    Handler handler = new Handler();

    ListView list;
    Recordings recordings;
    Storage storage;
    View progressEmpty;
    View progressText;

    int themeId;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (a.equals(Intent.ACTION_SCREEN_OFF)) {
                moveTaskToBack(true);
            }
        }
    };

    public static void startActivity(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
    }

    public void setAppTheme(int id) {
        super.setTheme(id);
        themeId = id;
    }

    public static int getAppTheme(Context context) {
        return MainApplication.getTheme(context, R.style.AppThemeLight_NoActionBar, R.style.AppThemeDark_NoActionBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppTheme(getAppTheme(this));
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_main);

        progressEmpty = findViewById(R.id.progress_empty);
        progressText = findViewById(R.id.progress_text);

        storage = new Storage(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordings.select(-1);
                RecordingActivity.startActivity(MainActivity.this, false);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        list = (ListView) findViewById(R.id.list);
        recordings = new Recordings(this, list);
        list.setAdapter(recordings);
        list.setEmptyView(findViewById(R.id.empty_list));
        recordings.setToolbar((ViewGroup) findViewById(R.id.recording_toolbar));

        RecordingService.startIfPending(this);

        IntentFilter ff = new IntentFilter();
        ff.addAction(Intent.ACTION_SCREEN_OFF);
        ff.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, ff);
    }

    void checkPending() {
        if (storage.recordingPending()) {
            RecordingActivity.startActivity(MainActivity.this, true);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_show_folder);
        Intent intent = StorageProvider.openFolderIntent(this, storage.getStoragePath());
        item.setIntent(intent);
        if (!StorageProvider.isFolderCallable(this, intent, StorageProvider.getAuthority())) {
            item.setVisible(false);
        }

        MenuItem search = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                recordings.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recordings.searchClose();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar base clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_about) {
            AboutPreferenceCompat.showDialog(this, R.raw.about);
            return true;
        }

        if (id == R.id.action_show_folder) {
            Intent intent = item.getIntent();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (themeId != getAppTheme(this)) {
            finish();
            MainActivity.startActivity(this);
            return;
        }

        invalidateOptionsMenu(); // update storage folder intent

        try {
            storage.migrateLocalStorage();
        } catch (RuntimeException e) {
            Error(e);
        }

        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        final String last = shared.getString(MainApplication.PREFERENCE_LAST, "");
        Runnable done = new Runnable() {
            @Override
            public void run() {
                final int selected = getLastRecording(last);
                progressEmpty.setVisibility(View.GONE);
                progressText.setVisibility(View.VISIBLE);
                if (selected != -1) {
                    recordings.select(selected);
                    list.smoothScrollToPosition(selected);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.setSelection(selected);
                        }
                    });
                }
            }
        };
        progressEmpty.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.GONE);

        recordings.load(!last.isEmpty(), done);

        checkPending();

        updateHeader();
    }

    int getLastRecording(String last) {
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0; i < recordings.getCount(); i++) {
            Storage.RecordingUri f = recordings.getItem(i);
            if (f.name.equals(last)) {
                SharedPreferences.Editor edit = shared.edit();
                edit.putString(MainApplication.PREFERENCE_LAST, "");
                edit.commit();
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (Storage.permitted(MainActivity.this, permissions)) {
                    try {
                        storage.migrateLocalStorage();
                    } catch (RuntimeException e) {
                        Error(e);
                    }
                    recordings.load(false, null);
                    checkPending();
                } else {
                    Toast.makeText(this, R.string.not_permitted, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        handler.post(new Runnable() {
            @Override
            public void run() {
                list.smoothScrollToPosition(recordings.getSelected());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordings.close();
        unregisterReceiver(receiver);
    }

    void updateHeader() {
        Uri uri = storage.getStoragePath();
        long free = storage.getFree(uri);
        long sec = Storage.average(this, free);
        TextView text = (TextView) findViewById(R.id.space_left);
        text.setText(MainApplication.formatFree(this, free, sec));
    }

    public void Error(Throwable e) {
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            Throwable t = e;
            while (t.getCause() != null)
                t = t.getCause();
            msg = t.getClass().getSimpleName();
        }
        Error(msg);
    }

    public void Error(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(msg);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}

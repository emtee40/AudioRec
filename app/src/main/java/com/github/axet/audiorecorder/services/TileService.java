package com.github.axet.audiorecorder.services;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import com.github.axet.audiorecorder.R;
import com.github.axet.audiorecorder.activities.RecordingActivity;
import com.github.axet.audiorecorder.app.AudioApplication;

@TargetApi(24)
public class TileService extends android.service.quicksettings.TileService {
    IntentFilter filters = new IntentFilter();
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTile();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        filters.addAction(RecordingActivity.START_RECORDING);
        filters.addAction(RecordingActivity.STOP_RECORDING);
        registerReceiver(receiver, filters);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    void updateTile() {
        Tile tile = getQsTile();
        if (tile == null)
            return; // some broken devices has tile == null within onStartListening()
        if (AudioApplication.from(this).recording != null) {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_stop_black_24dp));
            tile.setLabel(getString(R.string.tile_stop_recording));
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_mic_24dp));
            tile.setLabel(getString(R.string.tile_start_recording));
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        if (getQsTile().getLabel().equals(getString(R.string.tile_start_recording)))
            RecordingActivity.startActivity(this, false);
        else
            RecordingActivity.stopRecording(this);
    }
}

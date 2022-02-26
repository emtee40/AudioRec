package com.github.axet.audiorecorder.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import com.github.axet.audiolibrary.app.Sound;
import com.github.axet.audiorecorder.R;
import com.github.axet.audiorecorder.app.AudioApplication;
import com.github.axet.audiorecorder.app.Storage;

import java.util.ArrayList;
import java.util.Date;

public class RecordingSourcePreferenceCompat extends ListPreference {
    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecordingSourcePreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingSourcePreferenceCompat(Context context) {
        super(context);
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        update(newValue);
        return super.callChangeListener(newValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Object def = super.onGetDefaultValue(a, index);
        update(def);
        return def;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
        CharSequence[] text = getEntries();
        CharSequence[] values = getEntryValues();
        ArrayList<CharSequence> tt = new ArrayList<>();
        ArrayList<CharSequence> vv = new ArrayList<>();
        String raw = getContext().getString(R.string.source_raw);
        String internal = getContext().getString(R.string.source_internal);
        for (int i = 0; i < values.length; i++) {
            String v = values[i].toString();
            String t = text[i].toString();
            if (v.equals(raw) && !Sound.isUnprocessedSupported(getContext()))
                continue;
            if (v.equals(internal) && Build.VERSION.SDK_INT < 29)
                continue;
            vv.add(v);
            tt.add(t);
        }
        setEntryValues(vv.toArray(new CharSequence[0]));
        setEntries(tt.toArray(new CharSequence[0]));
        update(getValue()); // defaultValue null after defaults set
    }

    public void update(Object value) {
        String v = (String) value;
        setSummary(v);
    }
}

package com.jmstudios.redmoon.activity;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.jmstudios.redmoon.R;
import com.jmstudios.redmoon.fragment.ShadesFragment;
import com.jmstudios.redmoon.helper.FilterCommandFactory;
import com.jmstudios.redmoon.helper.FilterCommandSender;
import com.jmstudios.redmoon.model.SettingsModel;
import com.jmstudios.redmoon.presenter.ShadesPresenter;
import com.jmstudios.redmoon.service.ScreenFilterService;

public class ShadesActivity extends AppCompatActivity {
    private static final String TAG = "ShadesActivity";
    private static final boolean DEBUG = true;
    private static final String FRAGMENT_TAG_SHADES = "jmstudios.fragment.tag.SHADES";

    private ShadesPresenter mPresenter;
    private SettingsModel mSettingsModel;
    private SwitchCompat mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shades);

        FragmentManager fragmentManager = getFragmentManager();

        ShadesFragment view;

        // Only create and attach a new fragment on the first Activity creation.
        // On Activity re-creation, retrieve the existing fragment stored in the FragmentManager.
        if (savedInstanceState == null) {
            if (DEBUG) Log.i(TAG, "onCreate - First creation");

            view = new ShadesFragment();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, view, FRAGMENT_TAG_SHADES)
                    .commit();
        } else {
            if (DEBUG) Log.i(TAG, "onCreate - Re-creation");

            view = (ShadesFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG_SHADES);
        }

        // Wire MVP classes
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSettingsModel = new SettingsModel(getResources(), sharedPreferences);
        FilterCommandFactory filterCommandFactory = new FilterCommandFactory(this);
        FilterCommandSender filterCommandSender = new FilterCommandSender(this);

        mPresenter = new ShadesPresenter(view, mSettingsModel, filterCommandFactory, filterCommandSender);
        view.registerPresenter(mPresenter);

        // Make Presenter listen to settings changes
        mSettingsModel.setOnSettingsChangedListener(mPresenter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        final MenuItem item = menu.findItem(R.id.screen_filter_switch);
        mSwitch = (SwitchCompat) item.getActionView();
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPresenter.sendCommand(isChecked ?
                                           ScreenFilterService.COMMAND_ON :
                                           ScreenFilterService.COMMAND_OFF);
                }
            });

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSettingsModel.openSettingsChangeListener();
        mPresenter.onStart();
    }

    @Override
    protected void onStop() {
        mSettingsModel.closeSettingsChangeListener();
        super.onStop();
    }

    public SwitchCompat getSwitch() {
        return mSwitch;
    }

    public int getColorTempProgress() {
        return mSettingsModel.getShadesColor();
    }

    public int getIntensityLevelProgress() {
        return mSettingsModel.getShadesIntensityLevel();
    }
}

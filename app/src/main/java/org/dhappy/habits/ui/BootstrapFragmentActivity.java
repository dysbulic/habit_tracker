package org.dhappy.habits.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.dhappy.habits.BootstrapApplication;
import org.dhappy.habits.Injector;

import butterknife.Views;

/**
 * Base class for all Bootstrap Activities that need fragments.
 */
public class BootstrapFragmentActivity extends SherlockFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.inject(this);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);

        Views.inject(this);
    }

}

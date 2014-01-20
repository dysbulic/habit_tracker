package org.dhappy.habits;

import android.accounts.AccountManager;
import android.content.Context;

import org.dhappy.habits.authenticator.BootstrapAuthenticatorActivity;
import org.dhappy.habits.authenticator.LogoutService;
import org.dhappy.habits.core.CheckIn;
import org.dhappy.habits.core.TimerService;
import org.dhappy.habits.ui.BootstrapTimerActivity;
import org.dhappy.habits.ui.CarouselActivity;
import org.dhappy.habits.ui.CheckInsListFragment;
import org.dhappy.habits.ui.ItemListFragment;
import org.dhappy.habits.ui.NewsActivity;
import org.dhappy.habits.ui.NewsListFragment;
import org.dhappy.habits.ui.UserActivity;
import org.dhappy.habits.ui.UserListFragment;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
(
        complete = false,

        injects = {
                BootstrapApplication.class,
                BootstrapAuthenticatorActivity.class,
                CarouselActivity.class,
                BootstrapTimerActivity.class,
                CheckInsListFragment.class,
                NewsActivity.class,
                NewsListFragment.class,
                UserActivity.class,
                UserListFragment.class,
                TimerService.class
        }

)
public class BootstrapModule  {

    @Singleton
    @Provides
    Bus provideOttoBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    LogoutService provideLogoutService(final Context context, final AccountManager accountManager) {
        return new LogoutService(context, accountManager);
    }

}

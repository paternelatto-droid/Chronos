package com.latto.chronos;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.User;
import com.latto.chronos.models.UserSession;
import com.latto.chronos.utils.SessionManager;
import com.latto.chronos.views.CalendarFragment;
import com.latto.chronos.views.MembreFragment;
import com.latto.chronos.views.PastorAvailabilityFragment;
import com.latto.chronos.views.UpcomingEventsFragment;
import com.latto.chronos.views.UserSettingsFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private final Fragment calendarFragment = new CalendarFragment();
    private final Fragment upcomingEventsFragment = new UpcomingEventsFragment();
    private final Fragment membreFragment = new MembreFragment();
    private final Fragment pastorAvailabilityFragment = new PastorAvailabilityFragment();
    private final Fragment userSettingsFragment = new UserSettingsFragment();
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private ApiService api;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }


        // 🔹 Initialisation API & Session
        api = ApiClient.getService(this);
        session = new SessionManager(this);

        // 🔹 Charger permissions
        UserSession.loadPermissions(getApplicationContext());

        // ✅ Vérification login et token
        if (!session.isLoggedIn() || !session.isTokenValid()) {
            //Toast.makeText(this, "Session expirée, reconnectez-vous", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // 🔹 BottomNavigation
        bottomNavigationView = findViewById(R.id.MainActivity_BottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // ✅ MASQUER LES MENUS NON AUTORISÉS
        applyPermissionVisibilityToMenu();

        // ✅ Charger uniquement les fragments autorisés
        loadAuthorizedFragments();

        // ✅ Ouvrir automatiquement le premier menu visible
        autoSelectFirstVisibleTab();

        // ✅ Validation finale du token côté serveur
        validateUserToken(session.getToken());
    }

    private void applyPermissionVisibilityToMenu() {
        if (!UserSession.hasPermission("event.view")) {
            bottomNavigationView.getMenu().findItem(R.id.BottomNavigation_Item_Calendar).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.BottomNavigation_Item_UpcomingEvents).setVisible(false);
        }
        if (!UserSession.hasPermission("member.view")) {
            bottomNavigationView.getMenu().findItem(R.id.BottomNavigation_Item_Membre).setVisible(false);
        }
        if (!UserSession.hasPermission("settings.view")) {
            bottomNavigationView.getMenu().findItem(R.id.BottomNavigation_Item_Settings).setVisible(false);
        }
    }

    private void loadAuthorizedFragments() {
        if (UserSession.hasPermission("event.view")) {
            fragmentManager.beginTransaction()
                    .add(R.id.MainActivity_FrameLayout_Container, calendarFragment)
                    .commit();

            fragmentManager.beginTransaction()
                    .add(R.id.MainActivity_FrameLayout_Container, upcomingEventsFragment)
                    .hide(upcomingEventsFragment)
                    .commit();
        }

        if (UserSession.hasPermission("member.view")) {
            fragmentManager.beginTransaction()
                    .add(R.id.MainActivity_FrameLayout_Container, membreFragment)
                    .hide(membreFragment)
                    .commit();
        }
        if (UserSession.hasPermission("settings.view")) {
            fragmentManager.beginTransaction()
                    .add(R.id.MainActivity_FrameLayout_Container, userSettingsFragment)
                    .hide(userSettingsFragment)
                    .commit();
        }
    }

    private void autoSelectFirstVisibleTab() {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            if (item.isVisible()) {
                bottomNavigationView.setSelectedItemId(item.getItemId());
                break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
    if (id==R.id.BottomNavigation_Item_Calendar){
        if (UserSession.hasPermission("event.view")) {
            showFragment(calendarFragment);
            return true;
        }
    }else if (id==R.id.BottomNavigation_Item_UpcomingEvents){
        if (UserSession.hasPermission("event.view")) {
            UpcomingEventsFragment frag = (UpcomingEventsFragment) upcomingEventsFragment;
            frag.loadDataIfNeeded();
            frag.setUpRecyclerView();
            showFragment(upcomingEventsFragment);
            return true;
        }
    } else if (id == R.id.BottomNavigation_Item_Membre) {
        if (UserSession.hasPermission("member.view")) {
            showFragment(membreFragment);
            return true;
        }
   }else if (id==R.id.BottomNavigation_Item_Settings){
        if (UserSession.hasPermission("settings.view")) {
            showFragment(userSettingsFragment);
            return true;
        }
    }
    return false;
    }

    private void showFragment(Fragment fragmentToShow) {
        fragmentManager.beginTransaction()
                .hide(calendarFragment)
                .hide(upcomingEventsFragment)
                .hide(membreFragment)
                .hide(pastorAvailabilityFragment)
                .hide(userSettingsFragment)
                .show(fragmentToShow)
                .commit();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void validateUserToken(String token) {
        api.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.code() == 401 || response.code() == 403) {
                    //Toast.makeText(MainActivity.this, "Session expirée, reconnectez-vous", Toast.LENGTH_SHORT).show();
                    session.clearSession();
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {}
        });
    }
}

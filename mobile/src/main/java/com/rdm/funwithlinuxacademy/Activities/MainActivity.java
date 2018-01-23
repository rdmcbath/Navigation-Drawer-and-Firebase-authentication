package com.rdm.funwithlinuxacademy.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.email.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rdm.funwithlinuxacademy.Fragments.FragmentDashboard;
import com.rdm.funwithlinuxacademy.Fragments.FragmentLibrary;
import com.rdm.funwithlinuxacademy.Fragments.FragmentNotes;
import com.rdm.funwithlinuxacademy.Fragments.FragmentNugget;
import com.rdm.funwithlinuxacademy.Fragments.FragmentQuiz;
import com.rdm.funwithlinuxacademy.Fragments.FragmentSaved;
import com.rdm.funwithlinuxacademy.R;
import com.rdm.funwithlinuxacademy.SearchActivity;

import java.util.Arrays;

/**
 * Created by Rebecca on 1/1/2018.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final int RC_SIGN_IN = 1;
    private static final String ANONYMOUS = "anonymous";
    private static final String SELECTED_ID = "selected";

    private int mNavItemSelected;
    private Toolbar toolbar;
    private NavigationView navigationView = null;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Username
    private String mUsername;
    private View mHeader;

    // Views for the navigation header
    private TextView mProfileNameText;
    private TextView mProfileEmailText;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        //Default UserName
        mUsername = ANONYMOUS;
        //Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the Navigation Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation Header that contains the name, email and profile image
        mHeader = navigationView.getHeaderView(0);

        mProfileImage = mHeader.findViewById(R.id.header_profile_image);
        mProfileNameText = mHeader.findViewById(R.id.header_profile_name);
        mProfileEmailText = mHeader.findViewById(R.id.header_profile_email);

        // Retaining the state
//        mNavItemSelected = savedInstanceState == null ? R.id.nav_Alcoholic : savedInstanceState.getInt(SELECTED_ID);
//        navigate(mNavItemSelected);

        // Checks if the user is signed in or not
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if (user.getPhotoUrl() != null) {  // If the user is signed in and there is a
                        // photo available
                        onSignedInInitialize(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
                    } else {
                        onSignedInInitialize(user.getEmail());

                    }
                } else {
                    // User is signed out
                    onSignedOutTeardown();

                    // If the version is higher than lollipop then we set the style in firebase
                    // login UI or we just leave it to be the same
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                                        .setProviders
                                                (Arrays.asList
                                                (new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                        .setLogo(R.drawable.front_page)
                                        .setTheme(R.style.AppThemeFirebaseAuth)
                                        .setIsSmartLockEnabled(false)
                                        .build(),
                                RC_SIGN_IN);
                    } else {
                        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                                        .setProviders(Arrays.asList
                                                (new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                        .setLogo(R.drawable.front_page)
                                        .setIsSmartLockEnabled(false)
                                        .build(),
                                RC_SIGN_IN);
                    }
                }
            }
        };

        // Restoring the title after rotation
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("TITLE");
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving the title
        outState.putInt(SELECTED_ID, mNavItemSelected);
        String title = getSupportActionBar().getTitle().toString();
        outState.putString("TITLE", title);
    }

    // When the user signs out the user name is set to anonymous.
    private void onSignedOutTeardown() {
        mUsername = ANONYMOUS;
    }

    private void onSignedInInitialize(String user, String email, Uri imageUrl) {
        if (mUsername != null && !user.isEmpty()) {
            mUsername = user;
            mProfileNameText.setText(mUsername);
        }
        if (email != null && !email.isEmpty()) {
            mProfileEmailText.setText(email);
        }
        if (imageUrl != null) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .crossFade()
                    .error(R.drawable.penguin_round_icon)
                    .into(mProfileImage);
        }
    }

    private void onSignedInInitialize(String email) {
        Glide.with(getApplicationContext())
                .load(R.drawable.penguin_round_icon)
                .crossFade()
                .into(mProfileImage);

        if (email != null && !email.isEmpty()) {
            mProfileEmailText.setText(email);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add the auth state listener when the activity is resumed.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auth state listener is removed when the activity is paused
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        // This is will close the drawer after something is selected.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();

        // The search query is sent to the search activity using the listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String queryAdjusted = query.replaceAll(" ", "%20");

                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra(getString(R.string.search_intent_query), queryAdjusted);
                startActivity(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            FirebaseAuth.getInstance().signOut();

            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        finish();
                    }
                }
            };
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onItemSelected(Cocktail cocktail) {
//        /*
//        * Check that the detail fragment is present in the main activity.
//        * */
//        FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment);
//        if (detailsFragment == null) {
//            Intent mCocktailDetailIntent = new Intent(this, ActivityDetails.class);
//            mCocktailDetailIntent.putExtra(getString(R.string.details_intent_cocktail), cocktail);
//            startActivity(mCocktailDetailIntent);
//        } else {
//            detailsFragment.updateContent(cocktail);
//        }
//
//    }

    // Navigate to the selected fragment when clicked in the navigation drawer.
    private void navigate(int id) {
        navigationView.setCheckedItem(id);
        if (id == R.id.nav_dashboard) {

            FragmentDashboard fragment = new FragmentDashboard();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.dashboard));

        } else if (id == R.id.nav_saved) {

            FragmentSaved fragment = new FragmentSaved();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.saved));

        } else if (id == R.id.nav_library) {

            FragmentLibrary fragment = new FragmentLibrary();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.library));


        } else if (id == R.id.nav_nuggets) {

            FragmentNugget fragment = new FragmentNugget();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.nuggets));

        } else if (id == R.id.nav_quizzes) {

            FragmentQuiz fragment = new FragmentQuiz();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.quiz));


        } else if (id == R.id.nav_notes) {

            FragmentNotes fragment = new FragmentNotes();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.notes));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        mNavItemSelected = item.getItemId();
        navigate(mNavItemSelected);
        return true;
    }
}


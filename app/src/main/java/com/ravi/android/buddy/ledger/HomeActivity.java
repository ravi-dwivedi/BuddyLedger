package com.ravi.android.buddy.ledger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ravi.android.buddy.ledger.adaptor.HomeActivityViewPagerAdaptor;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.fragment.FriendsListFragment;
import com.ravi.android.buddy.ledger.fragment.GroupInfoFragment;
import com.ravi.android.buddy.ledger.fragment.GroupsListFragment;
import com.ravi.android.buddy.ledger.fragment.MyLedgerFragment;
import com.ravi.android.buddy.ledger.fragment.SummaryFragment;
import com.ravi.android.buddy.ledger.fragment.UserProfileFragment;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.receiver.ServiceStartUpReceiver;
import com.ravi.android.buddy.ledger.utility.CircleTransformation;
import com.ravi.android.buddy.ledger.utility.PrefManager;

/**
 * Created by ravi on 7/1/17.
 */

public class HomeActivity extends AppCompatActivity implements FriendsListFragment.OnUserItemClick, GroupsListFragment.OnGroupItemClick, UserProfileFragment.OnUserTransactionDelete {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtEmail;
    private FloatingActionButton fab;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_MY_SUMMARY = "mySummary";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_RATE_US = "rateUs";
    private static final String TAG_HELPANDFEEDBACK = "helpAndFeedBack";
    private static final String TAG_HOW_TO_USE = "howToUse";
    private static final String TAG_LOG_OUT = "logOut";
    public static String CURRENT_TAG = TAG_HOME;

    private ViewPager mViewPager;
    private HomeActivityViewPagerAdaptor mHomeActivityViewPagerAdaptor;

    private PrefManager prefManager;

    private User superUser;

    private Boolean isTwoPane = false;

    private Fragment activeRightFrgment;

    private AdView mAdView;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        isTwoPane = findViewById(R.id.container_details_fragment_main) != null;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setEnabled(false);
                lauchActivityCorrespondingToFabClick();
            }
        });
        fab.show();

        setNavigationViews();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }

        setHomeActivityTabsUsingViewPager();
        prefManager = new PrefManager(this);
        setServiceStartUpReceiver();
        setUpAdMobAdds();
        setUpFireBaseAnalytics();
    }

    private void setUpAdMobAdds() {
        MobileAds.initialize(getApplicationContext(),
                getResources().getString(R.string.banner_ad_unit_id_home_activity));
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setUpFireBaseAnalytics() {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Sets whether analytics collection is enabled for this app on this device.
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

        //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 20 seconds just for the fun
        mFirebaseAnalytics.setMinimumSessionDuration(20000);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
        mFirebaseAnalytics.setSessionTimeoutDuration(500);

        Bundle bundle = new Bundle();
        bundle.putString("UserName", superUser.getName());
        bundle.putString("UserEmail", superUser.getEmail());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void lauchActivityCorrespondingToFabClick() {
        if (mViewPager != null) {
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    startActivityForResult(new Intent(this, UserOperationsActivity.class),
                            ApplicationGlobals.USER_OPERATION_ACTIVITY_REQUEST_CODE);
                    break;
                case 1:
                    startActivityForResult(new Intent(this, GroupOperationsActivity.class),
                            ApplicationGlobals.GROUP_OPERATION_ACTIVITY_REQUEST_CODE);
                    break;
                case 2:
                    startActivityForResult(new Intent(this, TransactionOperationsActivity.class),
                            ApplicationGlobals.GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE);
                    break;
            }
        }
        overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fab.setEnabled(true);
        if (resultCode == RESULT_OK) {
            if (requestCode == ApplicationGlobals.USER_OPERATION_ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("USER_OPEARATION", false)) {
                if (mHomeActivityViewPagerAdaptor.getItem(0).isAdded()) {
                    ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
                    if (activeRightFrgment != null && activeRightFrgment instanceof UserProfileFragment) {
                        ((UserProfileFragment) activeRightFrgment).reloadUserInfo();
                    }
                }
            } else if (requestCode == ApplicationGlobals.GROUP_OPERATION_ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("GROUP_OPEARATION", false)) {
                if (mHomeActivityViewPagerAdaptor.getItem(1).isAdded()) {
                    ((GroupsListFragment) mHomeActivityViewPagerAdaptor.getItem(1)).restartGroupListLoader();
                    if (activeRightFrgment != null && activeRightFrgment instanceof GroupInfoFragment) {
                        ((GroupInfoFragment) activeRightFrgment).reloadGroupInfo();
                    }
                }
            } else if (requestCode == ApplicationGlobals.GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("TRANSACTION_OPEARATION", false)) {
                if (mHomeActivityViewPagerAdaptor.getItem(2).isAdded() && mHomeActivityViewPagerAdaptor.getItem(0).isAdded()) {
                    ((SummaryFragment) mHomeActivityViewPagerAdaptor.getItem(2)).restartSummaryListLoader();
                    ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
                    if (activeRightFrgment != null && activeRightFrgment instanceof GroupInfoFragment) {
                        ((GroupInfoFragment) activeRightFrgment).restartGroupTransactionLoader();
                    }
                }
            } else if (requestCode == ApplicationGlobals.USER_OPERATION_ACTIVITY_FOR_SUPER_USER_REQUEST_CODE
                    && data.getBooleanExtra("USER_OPEARATION", false)) {
                loadNavHeaderInformation();
            } else if (requestCode == ApplicationGlobals.USER_PROFILE_ACTIVITY_REQUEST_CODE
                    && (data.getBooleanExtra("USER_OPEARATION", false) || data.getBooleanExtra("TRANSACTION_OPEARATION", false))) {
                if (mHomeActivityViewPagerAdaptor.getItem(2).isAdded() && mHomeActivityViewPagerAdaptor.getItem(0).isAdded()) {
                    ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
                    ((SummaryFragment) mHomeActivityViewPagerAdaptor.getItem(2)).restartSummaryListLoader();
                }
            } else if (requestCode == ApplicationGlobals.GROUP_INFO_ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("GROUP_OPEARATION", false)) {
                if (mHomeActivityViewPagerAdaptor.getItem(1).isAdded()) {
                    ((GroupsListFragment) mHomeActivityViewPagerAdaptor.getItem(1)).restartGroupListLoader();
                }
            } else if (requestCode == ApplicationGlobals.USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("TRANSACTION_OPEARATION", false)) {
                if (mHomeActivityViewPagerAdaptor.getItem(0).isAdded() && mHomeActivityViewPagerAdaptor.getItem(2).isAdded()) {
                    ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
                    ((SummaryFragment) mHomeActivityViewPagerAdaptor.getItem(2)).restartSummaryListLoader();
                    if (activeRightFrgment != null && activeRightFrgment instanceof UserProfileFragment) {
                        ((UserProfileFragment) activeRightFrgment).restartTransactionLoader();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrencyChooserIfRequired();
        /*
        if (mHomeActivityViewPagerAdaptor.getItem(0).isAdded()) {
            ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
        }
        if (mHomeActivityViewPagerAdaptor.getItem(1).isAdded()) {
            ((GroupsListFragment) mHomeActivityViewPagerAdaptor.getItem(1)).restartGroupListLoader();
        }
        if (mHomeActivityViewPagerAdaptor.getItem(2).isAdded()) {
            ((SummaryFragment) mHomeActivityViewPagerAdaptor.getItem(2)).restartSummaryListLoader();
        }
        loadNavHeaderInformation();
        */
    }

    private void setHomeActivityTabsUsingViewPager() {
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.nt

        if (mHomeActivityViewPagerAdaptor == null) {
            mHomeActivityViewPagerAdaptor = new HomeActivityViewPagerAdaptor(getSupportFragmentManager(), this.getApplicationContext());
        }

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.home_activity_tabs_pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mHomeActivityViewPagerAdaptor);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setNavigationViews() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtEmail = (TextView) navHeader.findViewById(R.id.email);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_home_activity_titles);

        // load nav menu header data
        loadNavHeaderInformation();

        // initializing navigation menu
        setUpNavigationView();
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeaderInformation() {
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UserOperationsActivity.class);
                intent.putExtra("USER_ID", superUser.getId());
                intent.putExtra("GENDER", superUser.getGender());
                intent.putExtra("IMG_LOC", superUser.getUserImageLocation());
                intent.putExtra("PHONE_NUM", superUser.getNumber());
                intent.putExtra("EMAIL", superUser.getEmail());
                intent.putExtra("NAME", superUser.getName());
                intent.putExtra("EDIT", true);
                startActivityForResult(intent,
                        ApplicationGlobals.USER_OPERATION_ACTIVITY_FOR_SUPER_USER_REQUEST_CODE);
                overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });

        final String[] USER_COLUMNS = {
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_CREATION_DATE,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_EMAIL,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NUMBER,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER
        };

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor userDataCursor = getContentResolver().query(UserEntry.buildUserUri(), USER_COLUMNS,
                        UserEntry.COLUMN_USER_ID + " = " + ApplicationGlobals.superUserId, null, null);
                if (userDataCursor != null && userDataCursor.moveToFirst()) {
                    superUser = new User();
                    superUser.setId(userDataCursor.getLong(0));
                    superUser.setCreationDate(userDataCursor.getString(1));
                    superUser.setEmail(userDataCursor.getString(2));
                    superUser.setName(userDataCursor.getString(3));
                    superUser.setNumber(userDataCursor.getString(4));
                    superUser.setUserImageLocation(userDataCursor.getString(5));
                    superUser.setGender(userDataCursor.getString(6));
                    (HomeActivity.this).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtName.setText(superUser.getName());
                            txtEmail.setText(superUser.getEmail());
                            if (superUser.getUserImageLocation() != null && !superUser.getUserImageLocation().equalsIgnoreCase("")) {
                                Glide.with(getApplicationContext())
                                        .load(Uri.parse(superUser.getUserImageLocation()))
                                        .crossFade()
                                        .thumbnail(0.5f)
                                        .bitmapTransform(new CircleTransformation(getApplicationContext()))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(imgProfile);
                            } else {
                                switch (superUser.getGender()) {
                                    case "MALE":
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.male_profile_big)
                                                .crossFade()
                                                .thumbnail(0.5f)
                                                .bitmapTransform(new CircleTransformation(getApplicationContext()))
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(imgProfile);
                                        break;
                                    case "FEMALE":
                                        Glide.with(getApplicationContext())
                                                .load(R.drawable.female_profile_big)
                                                .crossFade()
                                                .thumbnail(0.5f)
                                                .bitmapTransform(new CircleTransformation(getApplicationContext()))
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(imgProfile);
                                        break;
                                }
                            }
                        }
                    });
                }
                if (userDataCursor != null) {
                    userDataCursor.close();
                }
            }
        });
        thread1.start();
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_my_summary:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_MY_SUMMARY;
                        loadMySummaryActivity();
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_settings:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SETTINGS;
                        startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_rateUs:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_RATE_US;
                        openPlayStoreAppForRating();
                        break;
                    case R.id.nav_helpAndFeedback:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_HELPANDFEEDBACK;
                        Intent helpIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "ravi.dwivedi23@gmail.com", null));
                        //intent.setType("text/html");
                        helpIntent.putExtra(Intent.EXTRA_SUBJECT, "Buddy Ledger App on PlayStore");
                        startActivity(Intent.createChooser(helpIntent, "send Email"));
                        break;
                    case R.id.nav_how_to_use:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_HOW_TO_USE;
                        // We normally won't show the welcome slider again in real app
                        // but this is for testing
                        PrefManager prefManager = new PrefManager(getApplicationContext());

                        // make first time launch TRUE
                        prefManager.setFirstTimeLaunch(true);

                        startActivity(new Intent(HomeActivity.this, TutorailActivity.class));
                        drawer.closeDrawers();
                        return true;
                    /*case R.id.nav_log_out:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_LOG_OUT;
                        break;
                        */
                    case R.id.nav_about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(HomeActivity.this, PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        //drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                //Fragment fragment = getHomeFragment();
                //FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                //       android.R.anim.fade_out);
                //fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                //fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            //mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        //invalidateOptionsMenu();
    }


    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    // show or hide the fab
    private void toggleFab() {
        //if (navItemIndex == 0)
        fab.show();
        //else
        //  fab.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.menu_home_activity, menu);
        }

        // when fragment is notifications, load the menu created for notifications
        if (navItemIndex == 3) {
            getMenuInflater().inflate(R.menu.notifications, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_action_add_friend:
                startActivityForResult(new Intent(this, UserOperationsActivity.class),
                        ApplicationGlobals.USER_OPERATION_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.menu_action_add_group:
                startActivityForResult(new Intent(this, GroupOperationsActivity.class),
                        ApplicationGlobals.GROUP_OPERATION_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.menu_action_add_transaction:
                startActivityForResult(new Intent(this, TransactionOperationsActivity.class),
                        ApplicationGlobals.USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
        return true;
    }

    /**
     * Displays the currency chooser dialog if not previously shown
     */
    public void showCurrencyChooserIfRequired() {
        if (!prefManager.isCurrencyDialogShown()) {
            // Set the dialog shown preference to true
            prefManager.setCurrencyDialogShown(true);
            //Show the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCustomTitle(LayoutInflater.from(this).inflate(R.layout.dialog_currency_choser_header, null, false));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dialog_currency_choser_item);
            adapter.addAll(getResources().getStringArray(R.array.currency_locales));
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            prefManager.setUserCurrency("¢");
                            break;
                        case 1:
                            prefManager.setUserCurrency("$");
                            break;
                        case 2:
                            prefManager.setUserCurrency("€");
                            break;
                        case 3:
                            prefManager.setUserCurrency("£");
                            break;
                        case 4:
                            prefManager.setUserCurrency("₣");
                            break;
                        case 5:
                            prefManager.setUserCurrency("₱");
                            break;
                        case 6:
                            prefManager.setUserCurrency("₹");
                            break;
                        case 7:
                            prefManager.setUserCurrency("Rs");
                            break;
                        case 8:
                            prefManager.setUserCurrency("R");
                            break;
                        case 9:
                            prefManager.setUserCurrency("RM");
                            break;
                        case 10:
                            prefManager.setUserCurrency("¥");
                            break;
                        case 11:
                            prefManager.setUserCurrency("₩");
                            break;
                        default:
                            prefManager.setUserCurrency("");
                            break;
                    }
                    dialog.dismiss();
                    onResume();
                }
            })
                    .create()
                    .show();
        }
    }

    /**
     * Sets alarm to start the notification service to push notifications to user of pending balances
     */
    public void setServiceStartUpReceiver() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), ServiceStartUpReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, ServiceStartUpReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setup periodic alarm every 5 seconds. Not required now because now times are directly set in the method call using
        // AlarmManager.INTERVAL_HALF_HOUR
        //long firstMillis = System.currentTimeMillis(); // first run of alarm is immediate
        //int intervalMillis = 5000; // 5 seconds
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = preferences.getString("notificationInterval", "12");
        AlarmManager alarm = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (preferences.getBoolean("notificationSwitch", true)) {
            switch (interval) {
                case "30":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * 60 * 1000,
                            AlarmManager.INTERVAL_HALF_HOUR, pIntent);
                    break;
                case "60":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 60 * 1000,
                            AlarmManager.INTERVAL_HOUR, pIntent);
                    break;
                case "3":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2 * 60 * 60 * 1000,
                            3 * 60 * 60 * 1000, pIntent);
                    break;
                case "6":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3 * 60 * 60 * 1000,
                            6 * 60 * 60 * 1000, pIntent);
                    break;
                case "12":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6 * 60 * 60 * 1000,
                            AlarmManager.INTERVAL_HALF_DAY, pIntent);
                    break;
                case "24":
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 12 * 60 * 60 * 1000,
                            AlarmManager.INTERVAL_DAY, pIntent);
                    break;
            }
        }
    }

    public Boolean isTwoPane() {
        return this.isTwoPane;
    }

    @Override
    public void onUserItemClick(final long user_id) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle arguments = new Bundle();
        arguments.putLong("USER_ID", user_id);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_details_fragment_main, fragment, UserProfileFragment.DETAIL_UserProfileFragment)
                .commit();
        activeRightFrgment = fragment;
    }

    @Override
    public void onGroupItemClick(long group_id) {
        GroupInfoFragment fragment = new GroupInfoFragment();
        Bundle arguments = new Bundle();
        arguments.putLong("GROUP_ID", group_id);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_details_fragment_main, fragment, GroupInfoFragment.DETAIL_GroupInfoFragment)
                .commit();
        activeRightFrgment = fragment;
    }

    private void loadMySummaryActivity() {
        if (isTwoPane()) {
            MyLedgerFragment fragment = new MyLedgerFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_details_fragment_main, fragment, MyLedgerFragment.DETAIL_MyLedgerFragment)
                    .commit();
        } else {
            startActivity(new Intent(HomeActivity.this, MyLedgerActivity.class));
        }
    }

    @Override
    public void userTransactionDelete() {
        if (mHomeActivityViewPagerAdaptor.getItem(0).isAdded() && mHomeActivityViewPagerAdaptor.getItem(2).isAdded()) {
            ((FriendsListFragment) mHomeActivityViewPagerAdaptor.getItem(0)).restartFriendListLoader();
            ((SummaryFragment) mHomeActivityViewPagerAdaptor.getItem(2)).restartSummaryListLoader();
        }
    }


    private void openPlayStoreAppForRating() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }
}
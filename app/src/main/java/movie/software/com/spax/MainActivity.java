/*
 *  Copyright 2015 sourcestream GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package movie.software.com.spax;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;

import movie.software.com.spax.Global.GlobalClass;
import movie.software.com.spax.adapter.GoogleDirectionAPI;
import movie.software.com.spax.adapter.SearchDB;
import movie.software.com.spax.controller.ActivityStallDetails;
import movie.software.com.spax.controller.CastDetails;
import movie.software.com.spax.controller.GalleryList;
import movie.software.com.spax.controller.GenresList;
import movie.software.com.spax.controller.MapSearchActivity;
import movie.software.com.spax.controller.MovieDetails;
import movie.software.com.spax.controller.MovieSlideTab;
import movie.software.com.spax.controller.NotificationsFragment;
import movie.software.com.spax.controller.SearchList;
import movie.software.com.spax.controller.SettingsFragment;
import movie.software.com.spax.controller.TVDetails;
import movie.software.com.spax.controller.TVSlideTab;
import movie.software.com.spax.controller.TrailerList;
import movie.software.com.spax.controller.ViewCartList;
import movie.software.com.spax.custom.HttpPostClass;
import movie.software.com.spax.loginregistration.ProfileSettings;
import movie.software.com.spax.orders.MyHistoryFragment;
import movie.software.com.spax.orders.MyOrdersFragment;
import movie.software.com.spax.orders.OrderList;
import movie.software.com.spax.other.CircleTransform;
import movie.software.com.spax.services.SpaxService;

/**
 * Main class program starts from here.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //region variables
    private final int CacheSize = 52428800; // 50MB
    private final int MinFreeSpace = 2048; // 2MB
    private static final long maxMem = Runtime.getRuntime().maxMemory();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private NavigationView navigationView;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;

    //Mapstuff
    public GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    Fragment currentFragment = null;

    private MovieSlideTab movieSlideTab = new MovieSlideTab();
    private TVSlideTab tvSlideTab = new TVSlideTab();
    private About about = new About();
    private Privacy privacy = new Privacy();
    private GenresList genresList = new GenresList();
    private SearchList searchList = new SearchList();
    private NotificationsFragment notificationsFragment = new NotificationsFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();
    private ViewCartList viewCartList = new ViewCartList();
    private MyOrdersFragment myOrdersFragment = new MyOrdersFragment();
    private MyHistoryFragment myHistoryFragment = new MyHistoryFragment();
    private OrderList oL = new OrderList();

    private SearchView searchView;
    private MenuItem searchViewItem;
    private ImageLoader imageLoader;

    // Create search View listeners
    private SearchViewOnQueryTextListener searchViewOnQueryTextListener = new SearchViewOnQueryTextListener();
    private onSearchViewItemExpand onSearchViewItemExpand = new onSearchViewItemExpand();
    private SearchSuggestionListener searchSuggestionListener = new SearchSuggestionListener();
    private SearchDB searchDB;
    private Toolbar toolbar;
    private int oldPos = -1;
    private boolean isDrawerOpen = false;
    private DisplayImageOptions optionsWithFade;
    private DisplayImageOptions optionsWithoutFade;
    private DisplayImageOptions backdropOptionsWithFade;
    private DisplayImageOptions backdropOptionsWithoutFade;
    private int currentMovViewPagerPos;
    private int currentTVViewPagerPos;
    private boolean reAttachMovieFragments;
    private boolean reAttachTVFragments;
    private TrailerList trailerListView;
    private GalleryList galleryListView;
    private MovieDetails movieDetailsFragment;
    private MovieDetails movieDetailsSimFragment;
    private boolean saveInMovieDetailsSimFragment;
    private TVDetails tvDetailsSimFragment;
    private boolean saveInTVDetailsSimFragment;
    private CastDetails castDetailsFragment;
    private TVDetails tvDetailsFragment;
    private Bundle movieDetailsInfoBundle;
    private Bundle movieDetailsCastBundle;
    private Bundle movieDetailsOverviewBundle;
    private Bundle castDetailsInfoBundle;
    private Bundle castDetailsCreditsBundle;
    private Bundle castDetailsBiographyBundle;
    private Bundle TVDetailsInfoBundle;
    private Bundle TVDetailsCastBundle;
    private Bundle TVDetailsOverviewBundle;
    private ArrayList<Bundle> movieDetailsBundle = new ArrayList<>();
    private ArrayList<Bundle> castDetailsBundle = new ArrayList<>();
    private ArrayList<Bundle> tvDetailsBundle = new ArrayList<>();
    private boolean restoreMovieDetailsAdapterState;
    private boolean restoreMovieDetailsState;
    private int currOrientation;
    private boolean orientationChanged;
    private boolean searchViewTap;
    private boolean searchViewCount;
    private static int searchMovieDetails;
    private static int searchCastDetails;
    private static int searchTvDetails;
    private int lastVisitedSimMovie;
    private int lastVisitedSimTV;
    private int lastVisitedMovieInCredits;
    private HttpURLConnection conn;
    private SimpleCursorAdapter searchAdapter;
    private String query;
    private JSONAsyncTask request;
    private SearchImgLoadingListener searchImgLoadingListener;
    private int iconMarginConstant;
    private int iconMarginLandscape;
    private int iconConstantSpecialCase;
    private int threeIcons;
    private int threeIconsToolbar;
    private int twoIcons;
    private int twoIconsToolbar;
    private int oneIcon;
    private int oneIconToolbar;
    private boolean phone;
    private DateFormat dateFormat;

    //endregion

    private FloatingSearchView mSearchView;
    private RecyclerView mSearchResultsList;
    private String TAG;
    private LatLng latLng;
    public LatLng DlatLng;
    public static MainActivity fa;
    public SharedPreferences sp;

    /**
     * First configure the Universal Image Downloader,
     * then we set the main layout to be activity_main.xml
     * and we add the slide menu items.
     *
     * @param savedInstanceState If non-null, this activity is being re-constructed from a previous saved state as given here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this is where we check for permissions for android 6 and above
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askPermissions();
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        fa = this;
        TAG = "Spax";
        //Start the spax service
        startService(new Intent(this, SpaxService.class));
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mSearchResultsList = (RecyclerView) findViewById(R.id.search_results_list);
        setupFloatingSearch();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Navigation view header
        View header = navigationView.getHeaderView(0);
        txtName = (TextView) header.findViewById(R.id.name);
        txtWebsite = (TextView) header.findViewById(R.id.website);
        imgNavHeaderBg = (ImageView) header.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) header.findViewById(R.id.img_profile);

        imgProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDrawerLayout.closeDrawers();
                Intent profile = new Intent(MainActivity.this, ProfileSettings.class);
                startActivity(profile);
            }
        });

        txtName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDrawerLayout.closeDrawers();
                Intent profile = new Intent(MainActivity.this, ProfileSettings.class);
                startActivity(profile);
            }
        });

        txtWebsite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mDrawerLayout.closeDrawers();
                Intent profile = new Intent(MainActivity.this, ProfileSettings.class);
                startActivity(profile);
            }
        });

        // load nav menu header data
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadNavHeader();
            }
        } else {
            loadNavHeader();
        }

        // implement navitemlistener
        navigationView.setNavigationItemSelectedListener(new NavigationViewClickListener());

        //Attach Drawer to floating search menu button
        onAttachSearchViewToDrawer(mSearchView);

        //region oncreate
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.bringToFront();
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,  /* host Activity */
                mDrawerLayout,  /* DrawerLayout object */
                toolbar,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // calling onPrepareOptionsMenu() to show search view
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // calling onPrepareOptionsMenu() to hide search view
                invalidateOptionsMenu();
                syncState();
            }

            // updates the title, toolbar transparency and search view
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > .55 && !isDrawerOpen) {
                    // opening drawer
                    // mDrawerTitle is app title
                    getSupportActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu();
                    isDrawerOpen = true;
                } else if (slideOffset < .45 && isDrawerOpen) {
                    // closing drawer
                    // mTitle is title of the current view, can be movies, tv shows or movie title
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                    isDrawerOpen = false;
                }
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Get the action bar title to set padding
        TextView titleTextView = null;

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }
        if (titleTextView != null) {
            float scale = getResources().getDisplayMetrics().density;
            titleTextView.setPadding((int) scale * 15, 0, 0, 0);
        }

        phone = getResources().getBoolean(R.bool.portrait_only);

        searchDB = new SearchDB(getApplicationContext());

        if (savedInstanceState == null) {
            // Check orientation and lock to portrait if we are on phone
            if (phone) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            //Is bundle From notification check:
            String isFromNotification = getIntent().getStringExtra("isFromNotification");
            if (isFromNotification != null) {
                if (isFromNotification.equals("True")) {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    GlobalClass.setCurrentFragment("OrderList");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragmentTransaction.replace(R.id.frame_container, oL).commit();
                }
            } else
                // on first time display view for first nav item
                displayView(1);

            // Use hockey module to check for updates
            //checkForUpdates();

            // Universal Loader options and configuration.
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(false)
                    .showImageOnLoading(R.drawable.placeholder_default)
                    .showImageForEmptyUri(R.drawable.placeholder_default)
                    .showImageOnFail(R.drawable.placeholder_default)
                    .cacheOnDisk(true)
                    .build();
            Context context = this;
            File cacheDir = StorageUtils.getCacheDirectory(context);
            // Create global configuration and initialize ImageLoader with this config
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .diskCache(new UnlimitedDiscCache(cacheDir)) // default
                    .defaultDisplayImageOptions(options)
                    .build();
            ImageLoader.getInstance().init(config);


            // Check cache size
            long size = 0;
            File[] filesCache = cacheDir.listFiles();
            for (File file : filesCache) {
                size += file.length();
            }
            if (cacheDir.getUsableSpace() < MinFreeSpace || size > CacheSize) {
                ImageLoader.getInstance().getDiskCache().clear();
                searchDB.cleanSuggestionRecords();
            }


        } else {
            oldPos = savedInstanceState.getInt("oldPos");
            currentMovViewPagerPos = savedInstanceState.getInt("currentMovViewPagerPos");
            currentTVViewPagerPos = savedInstanceState.getInt("currentTVViewPagerPos");
            restoreMovieDetailsState = savedInstanceState.getBoolean("restoreMovieDetailsState");
            restoreMovieDetailsAdapterState = savedInstanceState.getBoolean("restoreMovieDetailsAdapterState");
            movieDetailsBundle = savedInstanceState.getParcelableArrayList("movieDetailsBundle");
            castDetailsBundle = savedInstanceState.getParcelableArrayList("castDetailsBundle");
            tvDetailsBundle = savedInstanceState.getParcelableArrayList("tvDetailsBundle");
            currOrientation = savedInstanceState.getInt("currOrientation");
            lastVisitedSimMovie = savedInstanceState.getInt("lastVisitedSimMovie");
            lastVisitedSimTV = savedInstanceState.getInt("lastVisitedSimTV");
            lastVisitedMovieInCredits = savedInstanceState.getInt("lastVisitedMovieInCredits");
            saveInMovieDetailsSimFragment = savedInstanceState.getBoolean("saveInMovieDetailsSimFragment");


            FragmentManager fm = getFragmentManager();
            // prevent the following bug: go to gallery preview -> swap orientation ->
            // go to movies list -> swap orientation -> action bar bugged
            // so if we are not on gallery preview we show toolbar
            if (fm.getBackStackEntryCount() == 0 || !fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().equals("galleryList")) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (getSupportActionBar() != null && !getSupportActionBar().isShowing())
                            getSupportActionBar().show();
                    }
                });
            }
        }

        // Get reference for the imageLoader
        imageLoader = ImageLoader.getInstance();

        // Options used for the backdrop image in movie and tv details and gallery
        optionsWithFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500))
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.color.black)
                .showImageForEmptyUri(R.color.black)
                .showImageOnFail(R.color.black)
                .cacheOnDisk(true)
                .build();
        optionsWithoutFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.color.black)
                .showImageForEmptyUri(R.color.black)
                .showImageOnFail(R.color.black)
                .cacheOnDisk(true)
                .build();

        // Options used for the backdrop image in movie and tv details and gallery
        backdropOptionsWithFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(500))
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.drawable.placeholder_backdrop)
                .showImageForEmptyUri(R.drawable.placeholder_backdrop)
                .showImageOnFail(R.drawable.placeholder_backdrop)
                .cacheOnDisk(true)
                .build();
        backdropOptionsWithoutFade = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888.
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(false)
                .showImageOnLoading(R.drawable.placeholder_backdrop)
                .showImageForEmptyUri(R.drawable.placeholder_backdrop)
                .showImageOnFail(R.drawable.placeholder_backdrop)
                .cacheOnDisk(true)
                .build();

        trailerListView = new TrailerList();
        galleryListView = new GalleryList();

        if (currOrientation != getResources().getConfiguration().orientation)
            orientationChanged = true;

        currOrientation = getResources().getConfiguration().orientation;

        iconConstantSpecialCase = 0;
        if (phone) {
            iconMarginConstant = 0;
            iconMarginLandscape = 0;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            if (width <= 480 && height <= 800)
                iconConstantSpecialCase = -70;

            // used in MovieDetails, CastDetails, TVDetails onMoreIconClick
            // to check whether the animation should be in up or down direction
            threeIcons = 128;
            threeIconsToolbar = 72;
            twoIcons = 183;
            twoIconsToolbar = 127;
            oneIcon = 238;
            oneIconToolbar = 182;
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                iconMarginConstant = 232;
                iconMarginLandscape = 300;

                threeIcons = 361;
                threeIconsToolbar = 295;
                twoIcons = 416;
                twoIconsToolbar = 351;
                oneIcon = 469;
                oneIconToolbar = 407;
            } else {
                iconMarginConstant = 82;
                iconMarginLandscape = 0;

                threeIcons = 209;
                threeIconsToolbar = 146;
                twoIcons = 264;
                twoIconsToolbar = 200;
                oneIcon = 319;
                oneIconToolbar = 256;
            }

        }

        dateFormat = android.text.format.DateFormat.getDateFormat(this);
        //endregion
    }

    //Attach Floating Search view to drawer
    public void onAttachSearchViewToDrawer(FloatingSearchView searchView) {
        searchView.attachNavigationDrawerToMenuButton(mDrawerLayout);
    }

    private void setupFloatingSearch() {

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                Intent i = new Intent(getApplicationContext(), MapSearchActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
                mSearchView.clearFocus();
                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {
                Log.d(TAG, "onFocusCleared()");
            }
        });


        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_location) {
                    onLocationClicked();
                }
            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        //listen for when suggestion list expands/shrinks in order to move down/up the
        //search results list
        mSearchView.setOnSuggestionsListHeightChanged(new FloatingSearchView.OnSuggestionsListHeightChanged() {
            @Override
            public void onSuggestionsListHeightChanged(float newHeight) {
                mSearchResultsList.setTranslationY(newHeight);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        try {
            //Initialize Google Play Services
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setInfoWindowAdapter(new MainActivity.CustomInfoWindowAdapter());
            //setupMapMarkers();
            new FetchMarkers().execute("");

            //setCurrentLocation();

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                /*aa= marker.getPosition().latitude;
                bb=marker.getPosition().longitude;*/
                    Intent i = new Intent(MainActivity.this, ActivityStallDetails.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("Lat", Double.toString(marker.getPosition().latitude));
                    i.putExtra("Lon", Double.toString(marker.getPosition().longitude));
                    i.putExtra("Title", marker.getTitle());
                    startActivity(i);
                /*Toast.makeText(getApplicationContext(), "Clicked a marker with title..." + marker.getTitle() + "-" +
                        marker.getPosition().latitude + "-" + marker.getPosition().longitude, Toast.LENGTH_SHORT).show();*/

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            showToastFromBackground("Error in application!");
        }

    }

    /**
     * This method can be used to show the markers on the map. Current
     * implementation of this method will show only a single Pin with title and
     * snippet. You must customize this method to show the pins as per your
     * need.
     */
    private void setupMapMarkers(Double Lat, Double Long, String StallName) {
        //mMap.clear();
        try {
            // Add a marker in Westy
            MarkerOptions opt = new MarkerOptions();
        /*LatLng l = new LatLng(-1.2640135, 36.8043942);
        opt.position(l).title("Pirates Movie Shop").snippet("");*/
            LatLng l = new LatLng(Lat, Long);
            opt.position(l).title(StallName).snippet("");
            opt.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin));
            mMap.addMarker(opt).hideInfoWindow();
            DrawPolyLines();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void DrawPolyLines() {
        try {
            if (GlobalClass.getDoc() != null) {

                GoogleDirectionAPI md = new GoogleDirectionAPI();
                ArrayList<LatLng> directionPoint = md.getDirection(GlobalClass.Doc);
                PolylineOptions rectLine = new PolylineOptions().width(10).color(
                        Color.rgb(66, 133, 244));
                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                mMap.addPolyline(rectLine);
                MarkerOptions opt = new MarkerOptions();
                opt.position(latLng);
                mMap.addMarker(opt);
                opt.position(GlobalClass.getdLatLng());
                mMap.addMarker(opt);

            }
        } catch (Exception e) {
            Log.d("MapPoly", e.getMessage());
        }
    }

    //Not called can be used as example
    public Location getCurrentLocation() {

        boolean gps_enabled = false;
        boolean network_enabled = false;
        Location myLocation = null;
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                Toast.makeText(this, "Location permission not set", Toast.LENGTH_SHORT).show();
                return null;
            } else {

                //exceptions will be thrown if provider is not permitted.
                try {
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ex) {
                }
                try {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception ex) {
                }
                if (gps_enabled)
                    myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (network_enabled)
                    myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            /*Double currentLatitude = myLocation.getLatitude();
            Double currentLongitude = myLocation.getLongitude();

            latLng = new LatLng(currentLatitude, currentLongitude);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myLocation;
    }

    //Not called can use as example Best Last location
    private Location getLastKnownBestLocation() {
        LocationManager mLocationManager;

        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void startGPSDialog() {
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Turn on GPS");  // GPS not found
                builder.setMessage("In order to have all features working properly, this app requires GPS for location. Turn on GPS?"); // Want to enable?
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                builder.setNegativeButton("NO", null);
                builder.create().show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        try {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This class creates a Custom a InfoWindowAdapter that is used to show
     * popup on map when user taps on a pin on the map. Current implementation
     * of this class will show a Title and a snippet.
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        /**
         * The contents view.
         */
        private final View mContents;

        /**
         * Instantiates a new custom info window adapter.
         */
        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.map_popup, null);
        }

        /* (non-Javadoc)
         * @see com.google.android.gms.maps.GoogleMap.InfoWindowAdapter#getInfoWindow(com.google.android.gms.maps.model.Marker)
         */
        @Override
        public View getInfoWindow(Marker marker) {

            render(marker, mContents);
            return mContents;
        }

        /* (non-Javadoc)
         * @see com.google.android.gms.maps.GoogleMap.InfoWindowAdapter#getInfoContents(com.google.android.gms.maps.model.Marker)
         */
        @Override
        public View getInfoContents(Marker marker) {

            return null;
        }

        /**
         * Render the marker content on Popup view. Customize this as per your
         * need.
         *
         * @param marker the marker
         * @param view   the content view
         */
        private void render(final Marker marker, View view) {

            String title = marker.getTitle();
            TextView titleUi = (TextView) view.findViewById(R.id.title);
            if (title != null) {
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
                        titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = (TextView) view.findViewById(R.id.snippet);
            if (snippet != null) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
                        snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }

        }
    }

    protected void onStart() {
        buildGoogleApiClient();
        //mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onResume()
	 */
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mMap != null) {
                //Initialize Google Play Services
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                //mMap.setOnInfoWindowClickListener(this);
                mMap.setInfoWindowAdapter(new MainActivity.CustomInfoWindowAdapter());
                //setupMapMarkers();
                new FetchMarkers().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String ProfImg = preferences.getString("profile_img", "profile_img.png");
            String ProfileUrl = preferences.getString("ProfilePath", "");

            // Loading profile image
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "SpaxMovieDB" + File.separator);
            File file = new File(root, ProfImg);
            //Read image from file resource
            //Was changed now reads from saved url in server
            //if (file.exists()) {
            if (!ProfileUrl.equals("")) {
                Glide.with(this).load(ProfileUrl)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfile);
            } else {
                //Read image from drawable resource
                Glide.with(this).load(R.drawable.profile_default)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToastFromBackground("An error has occurred!");
        }
    }

    //Code to show Toast message
    public void showToastFromBackground(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            startGPSDialog();
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                showToastFromBackground("Loading...");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            //mLastLocation = getLastKnownBestLocation();
            //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastLocation = new Location("Service Provider");
            mLastLocation.setLatitude(Double.parseDouble(sp.getString("mLastLatitude", "0")));
            mLastLocation.setLongitude(Double.parseDouble(sp.getString("mLastLongitude", "0")));
            if (mLastLocation != null && mLastLocation.getLatitude() != 0.0) {
                getSetLastLocation();
            } else {
                mLastLocation = new Location("Service Provider");
                mLastLocation.setLatitude(-1.2640135);
                mLastLocation.setLongitude(36.8043942);
                getSetLastLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSetLastLocation() {
        try {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        try {
            //Place current location marker
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            GlobalClass.setcLatLng(latLng);
            //save current location in shared preferences
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("mLastLatitude", Double.valueOf(location.getLatitude()).toString());
            editor.putString("mLastLongitude", Double.valueOf(location.getLongitude()).toString());
            editor.apply();
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);*/
        } catch (Exception e) {
            latLng = new LatLng(-1.2640135, 36.8043942);
        }

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void onLocationClicked() {
        try {
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            SharedPreferences.Editor editor = sp.edit();
            editor.putString("mLastLatitude", Double.valueOf(latLng.latitude).toString());
            editor.putString("mLastLongitude", Double.valueOf(latLng.longitude).toString());
            editor.apply();
            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToastFromBackground("There was an error fetching current location! Check your GPS settings");
            //latLng = new LatLng(-1.2640135, 36.8043942);
            latLng = new LatLng(Double.parseDouble(sp.getString("mLastLatitude", "-1.2640135")), Double.parseDouble(sp.getString("mLastLongitude", "36.8043942")));
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showToastFromBackground("Unable to load map, check internet Connection!");
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public void askPermissions() {
        /* All version with SDK_INT < 22 grant permissions on install time. */
        if (Build.VERSION.SDK_INT > 22) {
            ArrayList<String> arrPerm = new ArrayList<>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                arrPerm.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!arrPerm.isEmpty()) {
                String[] permissions = new String[arrPerm.size()];
                permissions = arrPerm.toArray(permissions);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                            //if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                if (mGoogleApiClient == null) {
                                    buildGoogleApiClient();
                                }
                                mMap.setMyLocationEnabled(true);
                            } else {
                                showToastFromBackground("Application might not function properly! You need to allow permissions on the app!");
                            }
                            //}
                        }
                        if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                loadNavHeader();
                            } else {
                                showToastFromBackground("Application might not function properly! You need to allow  requested permissions on the app!");
                                loadNavHeader();
                            }
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showToastFromBackground("Application might not function properly! You need to allow permissions on the app!");
                    //showToastFromBackground("Grant all permissions.");
                    finish();
                }
                break;
            }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     * But first check for file permissions
     */

    private void loadNavHeader() {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            // name, website
            txtName.setText(sp.getString("FirstName", "") + " " + sp.getString("LastName", ""));
            txtWebsite.setText(sp.getString("Email", ""));

            // loading header background image
            Glide.with(this).load(R.drawable.nav_menu_header_bg)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgNavHeaderBg);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String ProfImg = preferences.getString("profile_img", "profile_img.png");
            String ProfileUrl = preferences.getString("ProfilePath", "");

            // Loading profile image
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "SpaxMovieDB" + File.separator);
            File file = new File(root, ProfImg);
            //Read image from file resource
            //Was changed now reads from saved url in server
            //if (file.exists()) {
            if (!ProfileUrl.equals("")) {
                Glide.with(this).load(ProfileUrl)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfile);
            } else {
                //Read image from drawable resource
                Glide.with(this).load(R.drawable.profile_default)
                        .crossFade()
                        .thumbnail(0.5f)
                        .bitmapTransform(new CircleTransform(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgProfile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // showing dot next to notifications label
        //navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
    }

    /**
     * Pops the last item from the back stack.
     * If searchView is opened it hides it.
     * reAttachMovieFragments re-creates our fragments this is due to a bug in the viewPager
     * restoreMovieDetailsState -> when we press back button we would like to restore our previous (if any)
     * saved state for the current fragment. We use custom backStack since the original doesn't save fragment's state.
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawers();
        /*else if (oldPos == 1){
            finish();
        }*/
        else {
            if (searchViewItem.isActionViewExpanded())
                searchViewItem.collapseActionView();
            String currFragment = GlobalClass.getCurrentFragment();

            if (currFragment.equals("MovieFragment") || currFragment.equals("TvFragment") || currFragment.equals("GenresFragment") ||
                    currFragment.equals("AboutFragment") || currFragment.equals("PrivacyFragment") || currFragment.equals("MyOrdersFragment") ||
                    currFragment.equals("MyHistoryFragment")) {
                if (!currFragment.equals("MapFragment")) {
                    GlobalClass.setCurrentFragment("MapFragment");
                    fm.beginTransaction()
                            .remove(currentFragment)
                            .commit();
                    getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                }
            } else if (fm.getBackStackEntryCount() > 0) {
                String backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
                if (backStackEntry.equals("movieList")) {
                    GlobalClass.setCurrentFragment("MovieFragment");
                    reAttachMovieFragments = true;
                }

                if (backStackEntry.equals("TVList")) {
                    GlobalClass.setCurrentFragment("TvFragment");
                    reAttachTVFragments = true;
                }

                if (backStackEntry.equals("MyOrdersFragment")) {
                    GlobalClass.setCurrentFragment("MyOrdersFragment");
                    reAttachTVFragments = true;
                }

                if (backStackEntry.equals("MyHistoryFragment")) {
                    GlobalClass.setCurrentFragment("MyHistoryFragment");
                    reAttachTVFragments = true;
                }

                if (backStackEntry.equals("searchList:1"))
                    reAttachMovieFragments = true;


                if (backStackEntry.equals("searchList:2"))
                    reAttachTVFragments = true;

                restoreMovieDetailsState = true;
                restoreMovieDetailsAdapterState = false;
                if (orientationChanged)
                    restoreMovieDetailsAdapterState = true;
                fm.popBackStack();
            } else {
                if (currFragment.equals("CartFragment")) {
                    GlobalClass.setCurrentFragment("MapFragment");
                    fm.beginTransaction()
                            .remove(currentFragment)
                            .commit();
                    getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                } else {
                    super.onBackPressed();
                }

            }
        }

    }

    /**
     * This method is fired when the activity is paused.
     * For example if we minimize our app.
     */
    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    /**
     * This method is fired when the activity is resumed.
     */
    /*@Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
    }*/

    /**
     * This method is used by the hockey library to check for crashes.
     */
    private void checkForCrashes() {
        CrashManager.register(this, MovieDB.appId);
    }

    /**
     * This method is used by the hockey library to check for updates.
     */
    private void checkForUpdates() {
        // Remove this for store / production builds!
        UpdateManager.register(this, MovieDB.appId);
    }

    /**
     * Navigation View item click listener.
     * Fired when you click on item from the Navigation view menu.
     */
    public class NavigationViewClickListener implements
            NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            //Check to see which item was being clicked and perform appropriate action
            switch (menuItem.getItemId()) {
                //Replacing the main content with ContentFragment Which is our Inbox View;
                case R.id.nav_map:
                    displayView(1);
                    break;
                case R.id.nav_movies:
                    displayView(2);
                    break;
                case R.id.nav_tv:
                    displayView(3);
                    break;
                case R.id.nav_genres:
                    displayView(4);
                    break;
                case R.id.nav_notifications:
                    displayView(5);
                    break;
                case R.id.nav_my_orders:
                    displayView(6);
                    break;
                case R.id.nav_orders_history:
                    displayView(7);
                    break;
                case R.id.nav_settings:
                    displayView(8);
                    break;
                case R.id.nav_about_us:
                    displayView(9);
                    break;
                case R.id.nav_privacy_policy:
                    displayView(10);
                    break;
                default:
                    displayView(1);
            }

            //Checking if the item is in checked state or not, if not make it in checked state
            if (menuItem.isChecked()) {
                menuItem.setChecked(false);
            } else {
                menuItem.setChecked(true);
            }
            menuItem.setChecked(true);

            return true;
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu the options menu in which we place our items.
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchViewItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(searchViewOnQueryTextListener);
        searchView.setOnSuggestionListener(searchSuggestionListener);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchViewItemC =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchViewItemC.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


        String[] from = {SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2};
        int[] to = {R.id.posterPath, R.id.title, R.id.info};
        searchAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.suggestionrow, null, from, to, 0) {
            @Override
            public void changeCursor(Cursor cursor) {
                super.swapCursor(cursor);
            }
        };
        searchViewItemC.setSuggestionsAdapter(searchAdapter);

        MenuItemCompat.setOnActionExpandListener(searchViewItem, onSearchViewItemExpand);


        return true;
    }

    /**
     * This hook is called whenever an item in our options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.search:
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, ProfileSettings.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     * Prepare the Screen's standard options menu to be displayed.
     * This is called right before the menu is shown, every time it is shown.
     * You can use this method to efficiently enable/disable items or otherwise dynamically modify the contents.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     * If the navigation drawer is opened we hide the search view.
     * If we are on genres or about view we hide the search view.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isDrawerOpen)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 4)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 5)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 6)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 7)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 8)
            menu.findItem(R.id.search).setVisible(false);
        else if (oldPos == 10) {
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.action_settings).setVisible(false);
            oldPos = 2;
        } else menu.findItem(R.id.search).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Displays fragment view for the selected item from the slide menu.
     *
     * @param position is the position that we have selected.
     */
    public void displayView(int position) {
        // Clear history of the back stack if any
        FragmentManager fm = getFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (position != 1) {
            // update the main content by replacing fragments
            Fragment fragment = null;
            searchMovieDetails = 0;
            searchCastDetails = 0;
            searchTvDetails = 0;
            searchViewCount = false;
            resetMovieDetailsBundle();
            resetCastDetailsBundle();
            resetTvDetailsBundle();
            switch (position) {
                // Case 0 is the header we don't want to do anything with it.
                case 1:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("MapFragment");
                    fragment = null;
                    break;
                case 2:
                    reAttachMovieFragments = true;
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("MovieFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = movieSlideTab;
                    break;

                case 3:
                    reAttachTVFragments = true;
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("TvFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = tvSlideTab;
                    break;

                case 4:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("GenresFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = getFragmentManager().findFragmentByTag("genres");
                    if (fragment == null)
                        fragment = genresList;
                    if (genresList.getBackState() == 0)
                        genresList.updateList();
                    break;

                case 5:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("CartFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = viewCartList;
                    break;
                case 6:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("MyOrdersFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = myOrdersFragment;
                    break;
                case 7:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("MyHistoryFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = myHistoryFragment;
                    break;
                case 8:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    /*getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();*/
                    //fragment = settingsFragment;
                    fragment = null;
                    Intent profile = new Intent(MainActivity.this, ProfileSettings.class);
                    startActivity(profile);
                    break;
                case 9:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("AboutFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = about;
                    break;
                case 10:
                    if (oldPos == position) {
                        mDrawerLayout.closeDrawers();
                        break;
                    }
                    GlobalClass.setCurrentFragment("PrivacyFragment");
                    getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                    fragment = privacy;
                    break;
                default:
                    break;
            }
            oldPos = position;
            if (fragment != null) {
                fm.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .commit();
                currentFragment = fragment;
                // update selected item and title, then close the drawer
                /*mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);*/
                setTitle(navMenuTitles[position - 1]);
                mDrawerLayout.closeDrawers();
                try {
                    movieSlideTab.showInstantToolbar();
                    tvSlideTab.showInstantToolbar();
                } catch (NullPointerException e) {
                }
                System.gc();
            }
        } else {
            //mDrawerList.setItemChecked(oldPos, true);
            oldPos = position;
            mDrawerLayout.closeDrawers();
            if (currentFragment != null) {
                fm.beginTransaction()
                        .remove(currentFragment)
                        .commit();
                currentFragment = null;
            }
            getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
        }
    }

    /**
     * We use this method to update the action bar title.
     *
     * @param title the new title.
     *              If the searchView is opened we don't call invalidateOptionsMenu() else hides the searchView.
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(mTitle);
        // Fixes truncating shorter titles: https://code.google.com/p/android/issues/detail?id=55256
        if (!searchViewTap) {
            invalidateOptionsMenu();
        } else searchViewTap = false;
    }

    /**
     * Called when activity start-up is complete
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * Called by the system when the device configuration changes while your activity is running.
     * Note that this will only be called if you have selected configurations you would like to handle
     * with the configChanges attribute in your manifest.
     *
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Class which listens for SearchView events.
     */
    private class SearchViewOnQueryTextListener implements SearchView.OnQueryTextListener {

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any suggestions if available,
         * true if the action was handled by the listener.
         */
        @Override
        public boolean onQueryTextChange(String newText) {
            query = newText;
            query = query.replaceAll("[\\s%\"^#<>{}\\\\|`]", "%20");

            if (query.length() > 1) {
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            if (request != null)
                                request.cancel(true);

                            if (conn != null)
                                conn.disconnect();

                            request = new JSONAsyncTask();
                            request.execute(MovieDB.url + "search/multi?query=" + query + "?&api_key=" + MovieDB.key);
                            request.setQuery(query);
                        } catch (CancellationException e) {
                            if (request != null)
                                request.cancel(true);
                            // we abort the http request, else it will cause problems and slow connection later
                            if (conn != null)
                                conn.disconnect();
                        }
                    }
                }).start();
            } else {
                String[] selArgs = {query};
                searchDB.cleanAutoCompleteRecords();
                Cursor c = searchDB.getSuggestions(selArgs);
                searchAdapter.changeCursor(c);
            }
            return true;
        }

        /**
         * Called when the user submits the query.
         * This could be due to a key press on the keyboard or due to pressing a submit button.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the SearchView perform the default action.
         * We update the query, hide the keyboard and add the query to the suggestions.
         */
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchList.setQuery(query);
            searchView.clearFocus();
            return true;
        }
    }

    /**
     * Class which listens when we tap on the SearchView.
     */
    public class onSearchViewItemExpand implements MenuItemCompat.OnActionExpandListener {
        FragmentManager fm = getFragmentManager();

        /**
         * Called when the searchView icon is tapped.
         *
         * @param item our searchView;
         * @return true if the item should expand, false if expansion should be suppressed.
         * If we have navigated through items from the search view result list and we tap again,
         * we clear our backStack.
         */
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {

            // search view key
            searchViewTap = true;

            if (searchMovieDetails > 0)
                clearMovieDetailsBackStack();

            if (searchCastDetails > 0)
                clearCastDetailsBackStack();

            if (searchTvDetails > 0)
                clearTvDetailsBackStack();

            // check if we are already in the search view to prevent double adding in the back stack
            if (fm.getBackStackEntryCount() == 0 || !fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName().startsWith("searchList")) {
                // checks if the search view has been created, if it is created this method pops it from the back stack
                // also this clears back stack history until the search list
                boolean fragmentPopped = false;
                if (fm.popBackStackImmediate("searchList:1", 0))
                    fragmentPopped = true;
                if (fm.popBackStackImmediate("searchList:2", 0))
                    fragmentPopped = true;
                if (fm.popBackStackImmediate("searchList:3", 0))
                    fragmentPopped = true;

                // if getId == 0 this means that the fragment has been detached
                // we only need the current active fragment to save its state
                if (!fragmentPopped) {
                    if (movieDetailsFragment != null && movieDetailsFragment.getId() != 0) {
                        // check if the movie is already in our backStack
                        if (movieDetailsBundle.size() > 0) {
                            if (!getSupportActionBar().getTitle().equals(movieDetailsBundle.get(movieDetailsBundle.size() - 1).getString("title"))) {
                                movieDetailsFragment.setAddToBackStack(true);
                                movieDetailsFragment.onSaveInstanceState(new Bundle());
                            }
                        } else {
                            movieDetailsFragment.setAddToBackStack(true);
                            movieDetailsFragment.onSaveInstanceState(new Bundle());
                        }
                    }
                    if (castDetailsFragment != null && castDetailsFragment.getId() != 0) {
                        // check if the actor is already in our backStack
                        if (castDetailsBundle.size() > 0) {
                            if (!getSupportActionBar().getTitle().equals(castDetailsBundle.get(castDetailsBundle.size() - 1).getString("title"))) {
                                castDetailsFragment.setAddToBackStack(true);
                                castDetailsFragment.onSaveInstanceState(new Bundle());
                            }
                        } else {
                            castDetailsFragment.setAddToBackStack(true);
                            castDetailsFragment.onSaveInstanceState(new Bundle());
                        }
                    }
                    if (tvDetailsFragment != null && tvDetailsFragment.getId() != 0) {
                        // check if the tv is already in our backStack
                        if (tvDetailsBundle.size() > 0) {
                            if (!getSupportActionBar().getTitle().equals(tvDetailsBundle.get(tvDetailsBundle.size() - 1).getString("title"))) {
                                tvDetailsFragment.setAddToBackStack(true);
                                tvDetailsFragment.onSaveInstanceState(new Bundle());
                            }
                        } else {
                            tvDetailsFragment.setAddToBackStack(true);
                            tvDetailsFragment.onSaveInstanceState(new Bundle());
                        }
                    }
                    FragmentTransaction transaction = fm.beginTransaction();
                    searchList.setTitle(getResources().getString(R.string.search_title));
                    transaction.replace(R.id.frame_container, searchList);
                    // add the current transaction to the back stack:
                    transaction.addToBackStack("searchList:" + oldPos);
                    transaction.commit();
                }
            }
            return true;
        }

        /**
         * Called when click back button or by collapseSearchView() method.
         *
         * @param item our searchView;
         * @return true if the item should collapse, false if collapsing should be suppressed.
         */
        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            return true;
        }
    }

    /**
     * Fired from SearchList controller when you tap on movie.
     * Hides the searchView text area.
     */
    public void collapseSearchView() {
        searchViewItem.collapseActionView();
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We update the text value of a TextView, from runOnUiThread() because we can't update it from async task.
     *
     * @param text  the TextView to update.
     * @param value the new text value.
     */
    public void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We update the text value of a TextView, from runOnUiThread() because we can't update it from async task.
     *
     * @param text  the TextView to update.
     * @param value the new text value.
     */
    public void setTextFromHtml(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(Html.fromHtml(value));
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We use our imageLoader to display image on the given image view.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url from which we display the image.
     *            R.string.imageSize defines the size of our images.
     */
    public void setImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + url, img);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We set url tag on the imageView. So when we tap later on it we known which url to load.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url to set tag.
     *            R.string.imageSize defines the size of our images.
     */
    public void setImageTag(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setTag(url);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * We use our imageLoader to display image on the given image view.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param img the ImageView we display image on.
     * @param url the url to set tag.
     *            R.string.backDropImgSize defines the size of our backDrop images.
     *            If we load the image for first time we show fade effect, else no effect.
     */
    public void setBackDropImage(final ImageView img, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imageLoader.getDiskCache().get(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url).exists())
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithoutFade);
                else
                    imageLoader.displayImage(MovieDB.imageUrl + getResources().getString(R.string.backDropImgSize) + url, img, backdropOptionsWithFade);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * runOnUiThread() is called because we can't update it from async task.
     *
     * @param ratingBar the ratingBar we set value.
     * @param value     the value we will set on the ratingBar.
     */
    public void setRatingBarValue(final RatingBar ratingBar, final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setRating(value);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param layout the layout which we hide.
     */
    public void hideLayout(final ViewGroup layout) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param view the view which we hide.
     */
    public void hideView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param view the view which we show.
     */
    public void showView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param textView the TextView which we hide.
     */
    public void hideTextView(final TextView textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     *
     * @param ratingBar the RatingBar which we hide.
     */
    public void hideRatingBar(final RatingBar ratingBar) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ratingBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * This method is used in MovieDetails, CastDetails and TVDetails.
     * Makes a view invisible.
     *
     * @param view the View which we make invisible.
     */
    public void invisibleView(final View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * We use this to get the current Viewpager item in movies.
     */
    public int getCurrentMovViewPagerPos() {
        return currentMovViewPagerPos;
    }

    /**
     * We use this to set the current Viewpager item in movies.
     */
    public void setCurrentMovViewPagerPos(int currentMovViewPagerPos) {
        this.currentMovViewPagerPos = currentMovViewPagerPos;
    }

    /**
     * We use this to get the current Viewpager item in TVs.
     */
    public int getCurrentTVViewPagerPos() {
        return currentTVViewPagerPos;
    }

    /**
     * We use this to set the current Viewpager item in TVs.
     */
    public void setCurrentTVViewPagerPos(int currentTVViewPagerPos) {
        this.currentTVViewPagerPos = currentTVViewPagerPos;
    }

    /**
     * @return our activity_main.xml
     */
    public DrawerLayout getMDrawerLayout() {
        return mDrawerLayout;
    }

    /**
     * @return true if our ViewPager in Movies should reAttach the Fragments.
     */
    public boolean getReAttachMovieFragments() {
        return reAttachMovieFragments;
    }

    /**
     * Set if we should reAttach the Fragments in the ViewPager in Movies.
     */
    public void setReAttachMovieFragments(boolean reAttachMovieFragments) {
        this.reAttachMovieFragments = reAttachMovieFragments;
    }

    /**
     * @return true if our ViewPager in TVs should reAttach the Fragments.
     */
    public boolean getReAttachTVFragments() {
        return reAttachTVFragments;
    }

    /**
     * Set if we should reAttach the Fragments in the ViewPager in TVs.
     */
    public void setReAttachTVFragments(boolean reAttachTVFragments) {
        this.reAttachTVFragments = reAttachTVFragments;
    }

    /**
     * Fired when activity is recreated.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("oldPos", oldPos);
        outState.putInt("currentMovViewPagerPos", currentMovViewPagerPos);
        outState.putInt("currentTVViewPagerPos", currentTVViewPagerPos);
        outState.putBoolean("restoreMovieDetailsAdapterState", restoreMovieDetailsAdapterState);
        outState.putBoolean("restoreMovieDetailsState", restoreMovieDetailsState);
        outState.putParcelableArrayList("movieDetailsBundle", movieDetailsBundle);
        outState.putParcelableArrayList("castDetailsBundle", castDetailsBundle);
        outState.putParcelableArrayList("tvDetailsBundle", tvDetailsBundle);
        outState.putInt("currOrientation", currOrientation);
        outState.putInt("lastVisitedSimMovie", lastVisitedSimMovie);
        outState.putInt("lastVisitedSimTV", lastVisitedSimTV);
        outState.putInt("lastVisitedMovieInCredits", lastVisitedMovieInCredits);
        outState.putBoolean("saveInMovieDetailsSimFragment", saveInMovieDetailsSimFragment);
    }

    /**
     * Method which returns the maximum heap size of the device.
     */
    public static long getMaxMem() {
        return maxMem;
    }

    /**
     * Method which returns the TrailerList view.
     */
    public TrailerList getTrailerListView() {
        return trailerListView;
    }

    /**
     * Method which returns the GalleryList view.
     */
    public GalleryList getGalleryListView() {
        return galleryListView;
    }

    /**
     * Method which sets our movieDetails Fragment.
     */
    public void setMovieDetailsFragment(MovieDetails movieDetailsFragment) {
        this.movieDetailsFragment = movieDetailsFragment;
    }

    /**
     * Method which returns our movieDetails Fragment.
     */
    public MovieDetails getMovieDetailsFragment() {
        return movieDetailsFragment;

    }

    /**
     * Method which sets our castDetails Fragment.
     */
    public void setCastDetailsFragment(CastDetails castDetailsFragment) {
        this.castDetailsFragment = castDetailsFragment;
    }

    /**
     * Method which returns our castDetails Fragment.
     */
    public CastDetails getCastDetailsFragment() {
        return castDetailsFragment;
    }

    /**
     * Method which returns our TVDetails Fragment.
     */
    public TVDetails getTvDetailsFragment() {
        return tvDetailsFragment;
    }

    /**
     * Method which sets our TVDetails Fragment.
     */
    public void setTvDetailsFragment(TVDetails tvDetailsFragment) {
        this.tvDetailsFragment = tvDetailsFragment;
    }

    /**
     * Retrieves the config for the imageLoader with fade effect.
     */
    public DisplayImageOptions getOptionsWithFade() {
        return optionsWithFade;
    }

    /**
     * Retrieves the config for the imageLoader without fade effect.
     */
    public DisplayImageOptions getOptionsWithoutFade() {
        return optionsWithoutFade;
    }

    /**
     * Drawer backButton listener.
     */
    public class OnDrawerBackButton implements View.OnClickListener {
        public OnDrawerBackButton() {
            // keep references for your onClick logic
        }

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    }

    /**
     * Gets the saved instance in our Movie Details Info tab.
     */
    public Bundle getMovieDetailsInfoBundle() {
        return movieDetailsInfoBundle;
    }

    /**
     * Sets the saved instance in our Movie Details Info tab. Most likely sets it to null.
     */
    public void setMovieDetailsInfoBundle(Bundle movieDetailsInfoBundle) {
        this.movieDetailsInfoBundle = movieDetailsInfoBundle;
    }

    /**
     * Gets the saved instance in our Movie Details Cast tab.
     */
    public Bundle getMovieDetailsCastBundle() {
        return movieDetailsCastBundle;
    }

    /**
     * Sets the saved instance in our Movie Details Cast tab. Most likely sets it to null.
     */
    public void setMovieDetailsCastBundle(Bundle movieDetailsCastBundle) {
        this.movieDetailsCastBundle = movieDetailsCastBundle;
    }

    /**
     * Gets the saved instance in our Movie Overview Info tab.
     */
    public Bundle getMovieDetailsOverviewBundle() {
        return movieDetailsOverviewBundle;
    }

    /**
     * Sets the saved instance in our Movie Details Overview tab. Most likely sets it to null.
     */
    public void setMovieDetailsOverviewBundle(Bundle movieDetailsOverviewBundle) {
        this.movieDetailsOverviewBundle = movieDetailsOverviewBundle;
    }

    /**
     * Gets the saved instance in our Cast Details Info tab.
     */
    public Bundle getCastDetailsInfoBundle() {
        return castDetailsInfoBundle;
    }

    /**
     * Sets the saved instance in our Cast Details Info tab. Most likely sets it to null.
     */
    public void setCastDetailsInfoBundle(Bundle castDetailsInfoBundle) {
        this.castDetailsInfoBundle = castDetailsInfoBundle;
    }

    /**
     * Gets the saved instance in our Cast Details Credits tab.
     */
    public Bundle getCastDetailsCreditsBundle() {
        return castDetailsCreditsBundle;
    }

    /**
     * Sets the saved instance in our Cast Details Credits tab. Most likely sets it to null.
     */
    public void setCastDetailsCreditsBundle(Bundle castDetailsCreditsBundle) {
        this.castDetailsCreditsBundle = castDetailsCreditsBundle;
    }

    /**
     * Gets the saved instance in our Cast Details Biography tab.
     */
    public Bundle getCastDetailsBiographyBundle() {
        return castDetailsBiographyBundle;
    }

    /**
     * Sets the saved instance in our Cast Details Biography tab. Most likely sets it to null.
     */
    public void setCastDetailsBiographyBundle(Bundle castDetailsBiographyBundle) {
        this.castDetailsBiographyBundle = castDetailsBiographyBundle;
    }

    /**
     * Gets the saved instance in our TV Details Info tab.
     */
    public Bundle getTVDetailsInfoBundle() {
        return TVDetailsInfoBundle;
    }

    /**
     * Sets the saved instance in our TV Details Info tab. Most likely sets it to null.
     */
    public void setTVDetailsInfoBundle(Bundle TVDetailsInfoBundle) {
        this.TVDetailsInfoBundle = TVDetailsInfoBundle;
    }

    /**
     * Gets the saved instance in our TV Details Cast tab.
     */
    public Bundle getTVDetailsCastBundle() {
        return TVDetailsCastBundle;
    }

    /**
     * Sets the saved instance in our TV Details Cast tab. Most likely sets it to null.
     */
    public void setTVDetailsCastBundle(Bundle TVDetailsCastBundle) {
        this.TVDetailsCastBundle = TVDetailsCastBundle;
    }

    /**
     * Gets the saved instance in our TV Details Overview tab.
     */
    public Bundle getTVDetailsOverviewBundle() {
        return TVDetailsOverviewBundle;
    }

    /**
     * Sets the saved instance in our TV Details Overview tab. Most likely sets it to null.
     */
    public void setTVDetailsOverviewBundle(Bundle TVDetailsOverviewBundle) {
        this.TVDetailsOverviewBundle = TVDetailsOverviewBundle;
    }

    /**
     * Method which adds Movie Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void addMovieDetailsBundle(Bundle movieDetailsBundle) {
        this.movieDetailsBundle.add(movieDetailsBundle);
    }

    /**
     * Method which removes Movie Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void removeMovieDetailsBundle(int pos) {
        movieDetailsBundle.remove(pos);
    }

    /**
     * Method which resets Movie Details ArrayList.
     * We use it for our back navigation.
     */
    public void resetMovieDetailsBundle() {
        movieDetailsBundle = new ArrayList<>();
    }

    /**
     * Method which gets Movie Details savedState from our ArrayList.
     * We use it for our back navigation.
     */
    public ArrayList<Bundle> getMovieDetailsBundle() {
        return movieDetailsBundle;
    }

    /**
     * Method which adds Cast Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void addCastDetailsBundle(Bundle castDetailsBundle) {
        this.castDetailsBundle.add(castDetailsBundle);
    }

    /**
     * Method which removes Cast Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void removeCastDetailsBundle(int pos) {
        castDetailsBundle.remove(pos);
    }

    /**
     * Method which resets Cast Details ArrayList.
     * We use it for our back navigation.
     */
    public void resetCastDetailsBundle() {
        castDetailsBundle = new ArrayList<>();
    }

    /**
     * Method which gets Cast Details savedState from our ArrayList.
     * We use it for our back navigation.
     */
    public ArrayList<Bundle> getCastDetailsBundle() {
        return castDetailsBundle;
    }

    /**
     * Method which adds TV Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void addTvDetailsBundle(Bundle tvDetailsBundle) {
        this.tvDetailsBundle.add(tvDetailsBundle);
    }

    /**
     * Method which removes TV Details savedState to our ArrayList.
     * We use it for our back navigation.
     */
    public void removeTvDetailsBundle(int pos) {
        tvDetailsBundle.remove(pos);
    }

    /**
     * Method which resets TV Details ArrayList.
     * We use it for our back navigation.
     */
    public void resetTvDetailsBundle() {
        tvDetailsBundle = new ArrayList<>();
    }

    /**
     * Method which gets TV Details savedState from our ArrayList.
     * We use it for our back navigation.
     */
    public ArrayList<Bundle> getTvDetailsBundle() {
        return tvDetailsBundle;
    }

    /**
     * Set this to true if we should restore our Movie Details savedState when we press back button.
     */
    public void setRestoreMovieDetailsState(boolean restoreMovieDetailsState) {
        this.restoreMovieDetailsState = restoreMovieDetailsState;
    }

    /**
     * true if we should restore our Movie Details savedState when we press back button.
     */
    public boolean getRestoreMovieDetailsState() {
        return restoreMovieDetailsState;
    }

    /**
     * Set this to true if we should restore our Movie Details Adapter savedState when we press back.
     */
    public void setRestoreMovieDetailsAdapterState(boolean restoreMovieDetailsAdapterState) {
        this.restoreMovieDetailsAdapterState = restoreMovieDetailsAdapterState;
    }

    /**
     * true if we should restore our Movie Details Adapter savedState when we press back button.
     */
    public boolean getRestoreMovieDetailsAdapterState() {
        return restoreMovieDetailsAdapterState;
    }

    /**
     * Set to true if the orientation has changed.
     */
    public void setOrientationChanged(boolean orientationChanged) {
        this.orientationChanged = orientationChanged;
    }

    /**
     * Returns true if we have used our searchView and after that we have tapped on item from the searchView.
     * The fragment which is pushed checks for this. If this is true it starts counting incSearchMovieDetails() how many
     * components we have pushed so far from the searchView to the last component.
     * So if we click on the search view icon again. We clear the items we have pushed so far until the searchView,
     * but if we click backButton we have them stored and decSearchMovieDetails or Cast or TV is called.
     */
    public boolean getSearchViewCount() {
        return searchViewCount;
    }

    /**
     * Set the value searchViewCount. True if we count. Check getSearchViewCount() for more information.
     */
    public void setSearchViewCount(boolean searchViewCount) {
        this.searchViewCount = searchViewCount;
    }

    /**
     * Called from CastDetails, MovieDetails or TVDetails.
     * Check if we have used our search view and we are counting the components added to the backStack from the searchView until current.
     * Check getSearchViewCount() for more information.
     */
    public void incSearchMovieDetails() {
        searchMovieDetails++;
    }

    /**
     * Decrements the value when we restore the fragment state. This is only called if we have used our search view.
     * Check getSearchViewCount() for more information.
     */
    public void decSearchMovieDetails() {
        searchMovieDetails--;
    }

    /**
     * Same as incSearchMovieDetails()
     */
    public void incSearchCastDetails() {
        searchCastDetails++;
    }

    /**
     * Same as decSearchMovieDetails()
     */
    public void decSearchCastDetails() {
        searchCastDetails--;
    }

    /**
     * Same as incSearchMovieDetails()
     */
    public void incSearchTvDetails() {
        searchTvDetails++;
    }

    /**
     * Same as decSearchMovieDetails()
     */
    public void decSearchTvDetails() {
        searchTvDetails--;
    }

    /**
     * Called when we tap on the searchView icon.
     * Clears the item savedState we have pushed from the searchView until the end.
     * For example: MovieList -> MovieDetails -> CastDetails-> SearchView -> MovieDetails -> CastDetails -> MovieDetails.
     * This method will clear only MovieDetails savedState after the SearchView.
     * So searchMovieDetails will be 2 and will clear the last two MovieDetails savedStates.
     */
    public void clearMovieDetailsBackStack() {
        if (movieDetailsBundle.size() > 0) {
            for (int i = 0; i < searchMovieDetails; i++) {
                removeMovieDetailsBundle(movieDetailsBundle.size() - 1);
            }
        }
        searchMovieDetails = 0;
    }

    /**
     * Same as clearMovieDetailsBackStack()
     */
    public void clearCastDetailsBackStack() {
        if (castDetailsBundle.size() > 0) {
            for (int i = 0; i < searchCastDetails; i++) {
                removeCastDetailsBundle(castDetailsBundle.size() - 1);
            }
        }
        searchCastDetails = 0;
    }

    /**
     * Same as clearTvDetailsBackStack()
     */
    public void clearTvDetailsBackStack() {
        if (tvDetailsBundle.size() > 0) {
            for (int i = 0; i < searchTvDetails; i++) {
                removeTvDetailsBundle(tvDetailsBundle.size() - 1);
            }
        }
        searchTvDetails = 0;
    }

    /**
     * Returns the MovieSlideTab Fragment. This is the ViewPager parent for the MovieList.
     */
    public MovieSlideTab getMovieSlideTab() {
        return movieSlideTab;
    }

    public void setMovieSlideTab(MovieSlideTab movieSlideTab) {
        this.movieSlideTab = movieSlideTab;
    }

    /**
     * Returns the TVSlideTab Fragment. This is the ViewPager parent for the MovieList.
     */
    public TVSlideTab getTvSlideTab() {
        return tvSlideTab;
    }


    public void setTvSlideTab(TVSlideTab tvSlideTab) {
        this.tvSlideTab = tvSlideTab;
    }

    public GenresList getGenresList() {
        return genresList;
    }

    /**
     * Returns our Toolbar.
     */
    public Toolbar getToolbar() {
        return toolbar;
    }

    /**
     * Class which listens for suggestion events.
     */
    private class SearchSuggestionListener implements SearchView.OnSuggestionListener {

        /**
         * Called when a suggestion was clicked.
         *
         * @param position the position
         * @return true if the listener handles the event and wants to override the default behavior of
         * launching any intent or submitting a search query specified on that item. Return false otherwise.
         * We don't want to launch any intent, so we handle this ourselve.
         */
        @Override
        public boolean onSuggestionClick(int position) {
            Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            if (searchView.getQuery().length() > 1)
                addSuggestion(cursor);

            searchList.onSuggestionClick(cursor.getInt(4), cursor.getString(5), cursor.getString(1));
            return true;
        }

        /**
         * Called when a suggestion was selected by navigating to it.
         *
         * @param position the absolute position in the list of suggestions.
         * @return true if the listener handles the event and wants to override the default behavior of
         * launching any intent or submitting a search query specified on that item. Return false otherwise.
         */
        public boolean onSuggestionSelect(int position) {
            Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
            if (searchView.getQuery().length() > 1)
                addSuggestion(cursor);

            searchList.onSuggestionClick(cursor.getInt(4), cursor.getString(5), cursor.getString(1));
            return true;
        }

    }

    private void addSuggestion(Cursor cursor) {
        if (searchDB.getSuggestionSize() > 9) {
            searchDB.cleanSuggestionRecords();
        }

        searchDB.insertSuggestion(cursor.getInt(4), cursor.getString(1), Uri.parse(cursor.getString(3)), cursor.getString(2), cursor.getString(5));
    }

    /**
     * Called when we are on SearchView. We should clear our count.
     */
    public void clearSearchCount() {
        searchMovieDetails = 0;
        searchCastDetails = 0;
        searchTvDetails = 0;
    }

    /**
     * Returns the old position of our navigation drawer.
     */
    public int getOldPos() {
        return oldPos;
    }

    /**
     * Returns the old position of our navigation drawer.
     */
    public void setOldPos(int currPos) {
        oldPos = currPos;
    }


    public int getLastVisitedSimMovie() {
        return lastVisitedSimMovie;
    }

    public void setLastVisitedSimMovie(int lastVisitedSimMovie) {
        this.lastVisitedSimMovie = lastVisitedSimMovie;
    }

    public int getLastVisitedSimTV() {
        return lastVisitedSimTV;
    }

    public void setLastVisitedSimTV(int lastVisitedSimTV) {
        this.lastVisitedSimTV = lastVisitedSimTV;
    }

    public boolean getSaveInMovieDetailsSimFragment() {
        return saveInMovieDetailsSimFragment;
    }

    public void setSaveInMovieDetailsSimFragment(boolean saveInMovieDetailsSimFragment) {
        this.saveInMovieDetailsSimFragment = saveInMovieDetailsSimFragment;
    }

    public MovieDetails getMovieDetailsSimFragment() {
        return movieDetailsSimFragment;
    }

    public void setMovieDetailsSimFragment(MovieDetails movieDetailsSimFragment) {
        this.movieDetailsSimFragment = movieDetailsSimFragment;
    }

    public boolean getSaveInTVDetailsSimFragment() {
        return saveInTVDetailsSimFragment;
    }

    public void setSaveInTVDetailsSimFragment(boolean saveInTVDetailsSimFragment) {
        this.saveInTVDetailsSimFragment = saveInTVDetailsSimFragment;
    }

    public TVDetails getTvDetailsSimFragment() {
        return tvDetailsSimFragment;
    }

    public void setTvDetailsSimFragment(TVDetails tvDetailsSimFragment) {
        this.tvDetailsSimFragment = tvDetailsSimFragment;
    }


    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set our list data.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        private ArrayList<Integer> idsList;
        private ArrayList<String> posterPathList;
        private String queryZ;

        public void setQuery(String query) {
            this.queryZ = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                int status = conn.getResponseCode();
                if (status == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();

                    JSONObject searchData = new JSONObject(sb.toString());
                    JSONArray searchResultsArray = searchData.getJSONArray("results");
                    int length = searchResultsArray.length();
                    if (length > 10)
                        length = 10;

                    searchDB.cleanAutoCompleteRecords();
                    idsList = new ArrayList<>();
                    posterPathList = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        JSONObject object = searchResultsArray.getJSONObject(i);

                        int id = 0;
                        String title = "", posterPath = "", releaseDate = "", mediaType = "";


                        if (object.has("id") && object.getInt("id") != 0)
                            id = object.getInt("id");

                        if (object.has("title"))
                            title = object.getString("title");

                        if (object.has("name"))
                            title = object.getString("name");
                        title = title.replaceAll("'", "");

                        if (object.has("poster_path") && !object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                            posterPath = MovieDB.imageUrl + "w154" + object.getString("poster_path");


                        if (object.has("profile_path") && !object.getString("profile_path").equals("null") && !object.getString("profile_path").isEmpty())
                            posterPath = MovieDB.imageUrl + "w154" + object.getString("profile_path");

                        if (object.has("release_date") && !object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(object.getString("release_date"));
                                String formattedDate = dateFormat.format(date);
                                releaseDate = "(" + formattedDate + ")";
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (object.has("first_air_date") && !object.getString("first_air_date").equals("null") && !object.getString("first_air_date").isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            try {
                                Date date = sdf.parse(object.getString("first_air_date"));
                                String formattedDate = dateFormat.format(date);
                                releaseDate = "(" + formattedDate + ")";
                            } catch (java.text.ParseException e) {
                            }
                        }

                        if (object.has("media_type") && !object.getString("media_type").isEmpty())
                            mediaType = object.getString("media_type");


                        Uri path = Uri.parse("android.resource://movie.software.com.spax/" + R.drawable.placeholder_default);
                        if (!posterPath.isEmpty()) {
                            if (imageLoader.getDiskCache().get(posterPath).exists())
                                path = Uri.fromFile(new File(imageLoader.getDiskCache().get(posterPath).getPath()));
                            else {
                                idsList.add(id);
                                posterPathList.add(posterPath);
                            }
                        }

                        searchDB.insertAutoComplete(id, title, path, releaseDate, mediaType);


                    }

                    return true;
                }


            } catch (ParseException | IOException | JSONException e) {
                if (conn != null)
                    conn.disconnect();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (query.length() > 1) {
                searchAdapter.changeCursor(searchDB.autoComplete());

                if (posterPathList != null && posterPathList.size() > 0) {
                    for (int i = 0; i < posterPathList.size(); i++) {
                        searchImgLoadingListener = new SearchImgLoadingListener(idsList.get(i), queryZ);
                        imageLoader.loadImage(posterPathList.get(i), searchImgLoadingListener);
                    }
                }
            }


        }

    }

    private class FetchMarkers extends AsyncTask<String, Void, String> {
        String strResult, Status;

        @Override
        protected String doInBackground(String... params) {
            try {
                // On complete call either onLoginSuccess or onLoginFailed
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("Request", "5"));
                strResult = HttpPostClass.PostAsIs(nameValuePair);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Markers", strResult);
                editor.apply();
                Status = "000";
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Status = "{\\\"data\\\" :[{\\\"responsecode\\\":\\\"999\\\",\\\"description\\\":\\\"A problem occurred during connection! Cannot setup markers.\\\"}]}";
                Thread.interrupted();
            }

            //return Status;
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Setup markers here
            String responsecode, jsonMarkers, StallName, StallID;
            Double Lat, Long;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            jsonMarkers = preferences.getString("Markers", "");
            try {
                JSONObject jsonRootObject = new JSONObject(jsonMarkers);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                //Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    responsecode = jsonObject.optString("responsecode").toString();
                    if (responsecode.equals("000")) {
                        Lat = Double.parseDouble(jsonObject.optString("StallLat").toString());
                        Long = Double.parseDouble(jsonObject.optString("StallLong").toString());
                        StallName = jsonObject.optString("StallName");
                        StallID = jsonObject.optString("StallID");
                        setupMapMarkers(Lat, Long, StallName);
                    } else {
                        showToastFromBackground(jsonObject.optString("description").toString());
                    }
                }
            } catch (JSONException e) {
                showToastFromBackground("An error occurred during the operation!");
                e.printStackTrace();
            }
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private class SearchImgLoadingListener extends SimpleImageLoadingListener {
        private int currId;
        private String queryZ;

        public SearchImgLoadingListener(int currId, String query) {
            this.currId = currId;
            this.queryZ = query;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (query.equals(queryZ)) {
                Uri uriFile = Uri.fromFile(new File(imageLoader.getDiskCache().get(imageUri).getPath()));
                searchDB.updateImg(currId, uriFile);
                searchAdapter.changeCursor(searchDB.autoComplete());
            }
        }
    }

    public void setLastVisitedMovieInCredits(int lastVisitedMovieInCredits) {
        this.lastVisitedMovieInCredits = lastVisitedMovieInCredits;
    }

    public int getLastVisitedMovieInCredits() {
        return lastVisitedMovieInCredits;
    }

    public int getIconMarginConstant() {
        return iconMarginConstant;
    }

    public int getIconMarginLandscape() {
        return iconMarginLandscape;
    }

    public int getIconConstantSpecialCase() {
        return iconConstantSpecialCase;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public int getOneIcon() {
        return oneIcon;
    }

    public int getOneIconToolbar() {
        return oneIconToolbar;
    }

    public int getTwoIcons() {
        return twoIcons;
    }

    public int getTwoIconsToolbar() {
        return twoIconsToolbar;
    }

    public int getThreeIcons() {
        return threeIcons;
    }

    public int getThreeIconsToolbar() {
        return threeIconsToolbar;
    }

}
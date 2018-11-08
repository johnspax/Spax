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

package movie.software.com.spax.controller;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import movie.software.com.spax.Global.GlobalClass;
import movie.software.com.spax.MainActivity;
import movie.software.com.spax.MovieDB;
import movie.software.com.spax.R;
import movie.software.com.spax.adapter.MovieAdapter;
import movie.software.com.spax.helper.Scrollable;
import movie.software.com.spax.model.MovieModel;

/**
 * This class loads movies list.
 */
public class MovieList extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private MainActivity activity;
    private View rootView;
    private ArrayList<MovieModel> moviesList;
    private int checkLoadMore = 0;
    private int totalPages;
    private MovieAdapter movieAdapter;
    private String currentList = "";
    private int backState;
    private String title;
    private MovieModel movieModel;
    private AbsListView listView;
    private EndlessScrollListener endlessScrollListener;
    private ProgressBar spinner;
    private Toast toastLoadingMore;
    private HttpURLConnection conn;
    private boolean isLoading;
    private Bundle save;
    private boolean fragmentActive;
    private int lastVisitedMovie;
    private MovieDetails movieDetails;
    private boolean genresList;
    private float scale;
    private boolean phone;
    private int minThreshold;
    private CoordinatorLayout coordinatorLayout;
    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton fabCart;
    private onFabCartIconClick onFabCartIconClick;
    private ViewCartList viewCartList;
    private int MovieCartSize = 0;
    private TextView fabCount;

    public MovieList() {
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            save = savedInstanceState.getBundle("save");
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           sets the layout for the current view.
     * @param container          the container which holds the current view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *                           Return the View for the fragment's UI, or null.
     */
    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        movieModel = new MovieModel();

        //Fetch Movie Cart List Size
        String MovieCartJson;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        MovieCartJson = prefs.getString("MovieCart", "");
        try {
            if(!MovieCartJson.equals("")) {
                JSONObject jsonRootObject = null;
                jsonRootObject = new JSONObject(MovieCartJson);
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                MovieCartSize = jsonArray.length();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {
                case "upcoming":
                    rootView = inflater.inflate(R.layout.movieslist, container, false);
                    break;
                case "nowPlaying":
                    rootView = inflater.inflate(R.layout.nowplaying, container, false);
                    break;
                case "popular":
                    rootView = inflater.inflate(R.layout.popular, container, false);
                    break;
                case "topRated":
                    rootView = inflater.inflate(R.layout.toprated, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.movieslist, container, false);
            }
        } else
            rootView = inflater.inflate(R.layout.movieslist, container, false);


        activity = ((MainActivity) getActivity());
        viewCartList = new ViewCartList();
        toastLoadingMore = Toast.makeText(activity, R.string.loadingMore, Toast.LENGTH_SHORT);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        phone = getResources().getBoolean(R.bool.portrait_only);
        scale = getResources().getDisplayMetrics().density;
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        onFabCartIconClick = new onFabCartIconClick();

        fabCart = (FloatingActionButton) rootView.findViewById(R.id.fabCart);
        fabCart.bringToFront();
        fabCount = (TextView) rootView.findViewById(R.id.fabCount);
        fabCount.setText(String.valueOf(MovieCartSize));
        fabCount.bringToFront();
        fabCart.setOnClickListener(onFabCartIconClick);
        /*if (mSwipeRefreshLayout == null)
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);*/
        if (phone)
            minThreshold = (int) (-49 * scale);
        else
            minThreshold = (int) (-42 * scale);

        Tracker t = ((MovieDB) activity.getApplication()).getTracker();
        t.setScreenName("MovieList");
        t.send(new HitBuilders.ScreenViewBuilder().build());

        /*mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
            }
        });*/

        return rootView;
    }

    /**
     * @param savedInstanceState if the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.getArguments().getString("currentList") != null) {
            switch (this.getArguments().getString("currentList")) {

                case "upcoming":
                    listView = (AbsListView) rootView.findViewById(R.id.movieslist);
                    break;
                case "nowPlaying":
                    listView = (AbsListView) rootView.findViewById(R.id.nowplaying);
                    break;
                case "popular":
                    listView = (AbsListView) rootView.findViewById(R.id.popular);
                    break;
                case "topRated":
                    listView = (AbsListView) rootView.findViewById(R.id.toprated);
                    break;
                default:
                    // used in genres
                    activity.getMovieSlideTab().showInstantToolbar();
                    listView = (AbsListView) rootView.findViewById(R.id.movieslist);
                    listView.setPadding(0, activity.getToolbar().getHeight(), 0, 0);
                    genresList = true;
            }
        } else {
            // used when looking all credits for a specific person
            listView = (AbsListView) rootView.findViewById(R.id.movieslist);
            moviesList = new ArrayList<>();
            moviesList = this.getArguments().getParcelableArrayList("credits");
            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
            movieAdapter.sort(movieModel);
            listView.setAdapter(movieAdapter);

        }

        //Handle orientation change starts
        if (save != null) {
            checkLoadMore = save.getInt("checkLoadMore");
            totalPages = save.getInt("totalPages");
            setCurrentList(save.getString("currentListURL"));
            setTitle(save.getString("title"));
            isLoading = save.getBoolean("isLoading");
            lastVisitedMovie = save.getInt("lastVisitedMovie");
            if (save.getInt("backState") == 1) {
                backState = 1;
                moviesList = save.getParcelableArrayList("listData");
                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                endlessScrollListener = new EndlessScrollListener();
                endlessScrollListener.setCurrentPage(save.getInt("currentPage"));
                endlessScrollListener.setOldCount(save.getInt("oldCount"));
                endlessScrollListener.setLoading(save.getBoolean("loading"));
            } else {
                backState = 0;
            }
        }


        if (listView != null) {

            if (!genresList)
                ((Scrollable) listView).setScrollViewCallbacks(activity.getMovieSlideTab());

            getActivity().setTitle(getTitle());
            listView.setOnItemClickListener(this);
            listView.setOnItemLongClickListener(this);


            // checks if we have set arguments to load movies for a specific person.
            if (this.getArguments().getString("currentList") != null) {
                // check if we were on movie details and we pressed back, to prevent list reloading
                if (backState == 0) {
                    if (this.getArguments().getString("currentList").equals("upcoming") && activity.getCurrentMovViewPagerPos() == 0)
                        updateList();

                    if (this.getArguments().getString("currentList").equals("genresList"))
                        updateList();

                    if (isLoading)
                        spinner.setVisibility(View.VISIBLE);
                } else {
                    // If memory heap is higher than 20MB we use the default viewpager offscreen logic
                    // else we remove invisible list's items to save up more RAM
                    if (MainActivity.getMaxMem() / 1048576 > 20)
                        fragmentActive = true;

                    if (fragmentActive) {
                        listView.setAdapter(movieAdapter);
                        listView.setOnScrollListener(endlessScrollListener);
                    }
                    // Maintain scroll position
                    if (save != null && save.getParcelable("listViewScroll") != null) {
                        listView.onRestoreInstanceState(save.getParcelable("listViewScroll"));
                    }
                }
            }

        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        //I will set the poster path right here, to be used in MovieDetails later
        GlobalClass.setPosterPath(moviesList.get(position).getPosterPath());
        activity.setLastVisitedSimMovie(0);
        activity.resetMovieDetailsBundle();
        activity.setRestoreMovieDetailsAdapterState(true);
        activity.setRestoreMovieDetailsState(false);
        activity.setOrientationChanged(false);
        activity.resetCastDetailsBundle();
        if (movieDetails != null && lastVisitedMovie == moviesList.get(position).getId() && movieDetails.getTimeOut() == 0) {
            // Old movie details retrieve info and re-init component else crash
            movieDetails.onSaveInstanceState(new Bundle());
            Bundle bundle = new Bundle();
            bundle.putInt("id", moviesList.get(position).getId());
            bundle.putString("PosterPath", moviesList.get(position).getPosterPath());
            Bundle save = movieDetails.getSave();
            movieDetails = new MovieDetails();
            movieDetails.setTimeOut(0);
            movieDetails.setSave(save);
            movieDetails.setArguments(bundle);
        } else movieDetails = new MovieDetails();

        lastVisitedMovie = moviesList.get(position).getId();
        movieDetails.setTitle(moviesList.get(position).getTitle());
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt("id", moviesList.get(position).getId());
        bundle.putString("PosterPath", moviesList.get(position).getPosterPath());
        movieDetails.setArguments(bundle);
        transaction.replace(R.id.frame_container, movieDetails);
        // add the current transaction to the back stack:
        transaction.addToBackStack("movieList");
        transaction.commit();
        fragmentActive = true;
        activity.getMovieSlideTab().showInstantToolbar();
      /*  ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                activity.getmDrawerToggle().onDrawerSlide(activity.getmDrawerLayout(), slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
// You can change this duration to more closely match that of the default animation.
        anim.setDuration(700);
        anim.start();*/

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        int newMovieCartSize = 0;
        Boolean isInList = false;
        String MovieCartJson;
        JSONObject jsonRootObject;
        JSONArray jsonArray;
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        JSONArray req = new JSONArray();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor ed = prefs.edit();
            MovieCartJson = prefs.getString("MovieCart", "");
            if (MovieCartJson.equals("")) {
                obj1.put("Title", moviesList.get(position).getTitle());
                obj1.put("Date", moviesList.get(position).getReleaseDate());
                obj1.put("MovieID", moviesList.get(position).getId());
                obj1.put("PosterPath", moviesList.get(position).getPosterPath());
                req.put(obj1);
                newMovieCartSize = req.length();
                obj2.put("data", req);
                ed.putString("MovieCart", obj2.toString());
                ed.apply();
            } else {
                obj1.put("Title", moviesList.get(position).getTitle());
                obj1.put("Date", moviesList.get(position).getReleaseDate());
                obj1.put("MovieID", moviesList.get(position).getId());
                obj1.put("PosterPath", moviesList.get(position).getPosterPath());
                //We need to check if this item is already in cart
                jsonRootObject = new JSONObject(MovieCartJson);
                jsonArray = jsonRootObject.optJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.optString("Title").toString().equals(moviesList.get(position).getTitle())) {
                        isInList = true;
                        showToastFromBackground(moviesList.get(position).getTitle() + " is already in the cart!");
                    }
                }
                if (isInList == false) {
                    jsonArray.put(obj1);
                    obj2.put("data", jsonArray);
                    ed.putString("MovieCart", obj2.toString());
                    ed.apply();
                    newMovieCartSize = jsonArray.length();
                }
            }
            if (isInList == false) {
                final int moviePos = position;
                String dumbAssPage = String.valueOf(newMovieCartSize);
                fabCount.setText(dumbAssPage);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, moviesList.get(position).getTitle() + " added to cart", 5000)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                SharedPreferences.Editor ed = preferences.edit();
                                JSONObject obj1 = new JSONObject();
                                try {
                                    JSONObject jsonRootObject = null;
                                    jsonRootObject = new JSONObject(preferences.getString("MovieCart", ""));
                                    //Get the instance of JSONArray that contains JSONObjects
                                    JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        jsonArray.remove(jsonArray.length() - 1);
                                    } else {
                                        showToastFromBackground("This functionality is unavailable for your android version.");
                                    }
                                    obj1.put("data", jsonArray);
                                    ed.putString("MovieCart", obj1.toString());
                                    ed.apply();
                                    fabCount.setText(String.valueOf(jsonArray.length()));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Snackbar snackbar1 = Snackbar.make(coordinatorLayout, moviesList.get(moviePos).getTitle() + " removed from cart", Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                            }
                        });
                snackbar.show();
            }
        } catch (JSONException e) {
            showToastFromBackground("An error has occurred during the operation!");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Class which listens when the user has tapped on Home icon button.
     */
    public class onFabCartIconClick implements View.OnClickListener {
        public onFabCartIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String mCart = prefs.getString("MovieCart", "");
            if (!mCart.equals("")) {
                showInstantToolbar();
                GlobalClass.setCurrentFragment("myCartFragment");
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frame_container, viewCartList);
                // add the current transaction to the back stack:
                transaction.addToBackStack("movieList");
                transaction.commit();
            } else {
                showToastFromBackground("Your cart is empty!");
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        String MovieCartJson;
        MovieCartSize = 0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        MovieCartJson = prefs.getString("MovieCart", "");
        try {
            if(!MovieCartJson.equals("")) {
                JSONObject jsonRootObject = null;
                jsonRootObject = new JSONObject(MovieCartJson);
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                MovieCartSize = jsonArray.length();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fabCount.setText(String.valueOf(MovieCartSize));
        super.onResume();
    }

    /**
     * This class handles the connection to our backend server.
     * If the connection is successful we set our list data.
     */
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (checkLoadMore == 0) {
                activity.showView(spinner);
                isLoading = true;
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        toastLoadingMore.show();
                    }
                });
            }

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

                    JSONObject movieData = new JSONObject(sb.toString());
                    totalPages = movieData.getInt("total_pages");
                    JSONArray movieArray = movieData.getJSONArray("results");

                    // is added checks if we are still on the same view, if we don't do this check the program will crash
                    if (isAdded()) {
                        for (int i = 0; i < movieArray.length(); i++) {
                            JSONObject object = movieArray.getJSONObject(i);

                            MovieModel movie = new MovieModel();
                            movie.setId(object.getInt("id"));
                            movie.setTitle(object.getString("title"));
                            if (!object.getString("release_date").equals("null") && !object.getString("release_date").isEmpty())
                                movie.setReleaseDate(object.getString("release_date"));


                            if (!object.getString("poster_path").equals("null") && !object.getString("poster_path").isEmpty())
                                movie.setPosterPath(MovieDB.imageUrl + getResources().getString(R.string.imageSize) + object.getString("poster_path"));


                            moviesList.add(movie);
                        }

                        return true;
                    }

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
            // is added checks if we are still on the same view, if we don't do this check the program will cra
            if (isAdded()) {
                if (checkLoadMore == 0) {
                    activity.hideView(spinner);
                    isLoading = false;
                }

                if (!result) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.noConnection), Toast.LENGTH_LONG).show();
                    backState = 0;
                } else {
                    movieAdapter.notifyDataSetChanged();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            toastLoadingMore.cancel();
                        }
                    });
                    final View toolbarView = activity.findViewById(R.id.toolbar);
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (toolbarView.getTranslationY() == -toolbarView.getHeight() && ((Scrollable) listView).getCurrentScrollY() < minThreshold) {
                                if (phone)
                                    listView.smoothScrollBy((int) (56 * scale), 0);
                                else
                                    listView.smoothScrollBy((int) (59 * scale), 0);
                            }
                        }
                    });

                    backState = 1;
                    save = null;
                }
            }
        }

    }

    /**
     * This class listens for scroll events on the list.
     */
    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private int oldCount = 0;

        public EndlessScrollListener() {
        }


        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

            if (oldCount != totalItemCount && firstVisibleItem + visibleItemCount >= totalItemCount) {
                loading = true;
                oldCount = totalItemCount;
            }
            if (loading) {
                if (currentPage != totalPages) {
                    currentPage++;
                    checkLoadMore = 1;
                    loading = false;
                    final JSONAsyncTask request = new JSONAsyncTask();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key + "&page=" + currentPage).get(10000, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                                request.cancel(true);
                                // we abort the http request, else it will cause problems and slow connection later
                                if (conn != null)
                                    conn.disconnect();
                                toastLoadingMore.cancel();
                                currentPage--;
                                loading = true;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    if (totalPages != 1) {
                        Toast.makeText(getActivity(), R.string.nomoreresults, Toast.LENGTH_SHORT).show();
                    }
                    loading = false;

                }
            }


        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getOldCount() {
            return oldCount;
        }

        public void setOldCount(int oldCount) {
            this.oldCount = oldCount;
        }

        public boolean getLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }

    }

    public void setCurrentList(String currentList) {
        this.currentList = currentList;
    }

    public String getCurrentList() {
        return currentList;
    }

    public void setBackState(int backState) {
        this.backState = backState;
    }

    public int getBackState() {
        return backState;
    }

    /**
     * Fired when list is empty and we should update it.
     */
    public void updateList() {
        if (listView != null) {
            moviesList = new ArrayList<>();
            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
            listView.setAdapter(movieAdapter);
            endlessScrollListener = new EndlessScrollListener();
            listView.setOnScrollListener(endlessScrollListener);
            checkLoadMore = 0;
            final JSONAsyncTask request = new JSONAsyncTask();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (!isLoading) {
                            request.execute(MovieDB.url + getCurrentList() + "?&api_key=" + MovieDB.key).get(10000, TimeUnit.MILLISECONDS);
                            //mSwipeRefreshLayout.setRefreshing(false);
                        }
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        request.cancel(true);
                        toastLoadingMore.cancel();
                        if (spinner != null)
                            activity.hideView(spinner);
                        // we abort the http request, else it will cause problems and slow connection later
                        if (conn != null)
                            conn.disconnect();
                        isLoading = false;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    }

    /**
     * Update the title. We use this method to save our title and then to set it on the Toolbar.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the title.
     */
    private String getTitle() {
        return this.title;
    }

    /**
     * Called to ask the fragment to save its current dynamic state,
     * so it can later be reconstructed in a new instance of its process is restarted.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Used to avoid bug where we add item in the back stack
        // and if we change orientation twice the item from the back stack has null values
        if (save != null)
            outState.putBundle("save", save);
        else {
            Bundle send = new Bundle();
            send.putInt("checkLoadMore", checkLoadMore);
            send.putInt("totalPages", totalPages);
            send.putString("currentListURL", getCurrentList());
            send.putString("title", getTitle());
            send.putBoolean("isLoading", isLoading);
            send.putInt("lastVisitedMovie", lastVisitedMovie);
            if (backState == 1) {
                send.putInt("backState", 1);
                send.putParcelableArrayList("listData", moviesList);
                // used to restore the scroll listener variables
                send.putInt("currentPage", endlessScrollListener.getCurrentPage());
                send.putInt("oldCount", endlessScrollListener.getOldCount());
                send.putBoolean("loading", endlessScrollListener.getLoading());
                // Save scroll position
                if (listView != null) {
                    Parcelable listState = listView.onSaveInstanceState();
                    send.putParcelable("listViewScroll", listState);
                }
            } else
                send.putInt("backState", 0);

            outState.putBundle("save", send);
        }
    }

    /**
     * This method is used if the device has low heap size <=20 MB.
     * Using this method we manage to have only one active list view at a time.
     */
    public void cleanUp() {
        // WE clean unused lists to save up RAM if the heap size is less or equal to 20MB
        if (MainActivity.getMaxMem() / 1048576 <= 20) {
            if (moviesList != null) {
                listView.setAdapter(null);

            }
        }
    }

    public void setFragmentActive(boolean fragmentActive) {
        this.fragmentActive = fragmentActive;
    }

    /**
     * Used if the device has low heap size <=20 MB.
     */
    public void setAdapter() {
        if (listView != null && listView.getAdapter() == null)
            listView.setAdapter(movieAdapter);
    }

    /**
     * Returns the list with data.
     * Used when you click on search icon to get this list and set it there if the search list is empty.
     */
    public ArrayList<MovieModel> getMoviesList() {
        return moviesList;
    }

    public AbsListView getListView() {
        return listView;
    }

    /**
     * Fired when fragment is destroyed.
     */
    public void onDestroyView() {
        super.onDestroyView();
    }

    //Code to show Toast message
    public void showToastFromBackground(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    /**
     * Instant shows our toolbar. Used when click on movie details from movies list and toolbar is hidden.
     */
    public void showInstantToolbar() {
        if (activity != null) {
            View toolbarView = activity.findViewById(R.id.toolbar);

            if (toolbarView != null) {
                toolbarView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(0).start();
            }
        }
    }
}
package movie.software.com.spax.controller;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import movie.software.com.spax.MainActivity;
import movie.software.com.spax.R;
import movie.software.com.spax.adapter.MovieAdapter;
import movie.software.com.spax.custom.JSONRemoveItemClass;
import movie.software.com.spax.model.MovieModel;

/**
 * Created by John Muya on 07/04/2017.
 */

public class ViewCartList extends Fragment implements AdapterView.OnItemLongClickListener {
    private View rootView;
    private AbsListView listView;
    private MovieAdapter movieAdapter;
    private ArrayList<MovieModel> moviesList;
    private String MovieCartJson;
    private MainActivity activity;
    private CoordinatorLayout coordinatorLayout;
    private JSONObject jsonRootObject = null;
    private JSONArray jsonArray;
    private ProgressBar spinner;

    private FloatingActionButton CartCheckOut;
    private FloatingActionButton CartCheckDown;
    private TextView tvMessage;
    private onCheckOutIconClick onCheckOutIconClick;
    private onCheckDownIconClick onCheckDownIconClick;

    public ViewCartList() {

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
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           sets the layout for the current view.
     * @param container          the container which holds the current view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *                           Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.cartmovieslist, container, false);
        listView = (AbsListView) rootView.findViewById(R.id.movieslist);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        CartCheckOut = (FloatingActionButton) rootView.findViewById(R.id.CartCheckOut);
        CartCheckOut.bringToFront();
        CartCheckDown = (FloatingActionButton) rootView.findViewById(R.id.CartCheckDown);
        CartCheckDown.bringToFront();
        tvMessage = (TextView) rootView.findViewById(R.id.tvMessage);
        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);

        onCheckOutIconClick = new onCheckOutIconClick();
        onCheckDownIconClick = new onCheckDownIconClick();

        activity = ((MainActivity) getActivity());
        //activity.setOldPos(10);
        getActivity().setTitle("Movie Cart");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        MovieCartJson = prefs.getString("MovieCart", "");
        moviesList = new ArrayList<>();
        if (MovieCartJson.equals("")) {
            tvMessage.setVisibility(View.VISIBLE);
            CartCheckDown.setVisibility(View.INVISIBLE);
            CartCheckOut.setVisibility(View.INVISIBLE);
        } else {
            try {
                tvMessage.setVisibility(View.INVISIBLE);
                CartCheckDown.setVisibility(View.VISIBLE);
                CartCheckOut.setVisibility(View.VISIBLE);
                jsonRootObject = new JSONObject(MovieCartJson);
                jsonArray = jsonRootObject.optJSONArray("data");
                if (jsonArray.length() != 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        MovieModel movie = new MovieModel();
                        movie.setId(Integer.valueOf(jsonObject.optString("MovieID").toString()));
                        movie.setTitle(jsonObject.optString("Title"));
                        if (!jsonObject.optString("Date").equals("null") && !jsonObject.optString("Date").isEmpty()) {
                            String movieYear = jsonObject.optString("Date").replace(")", "").replace("(", "");
                            movie.setReleaseDate(movieYear.substring(movieYear.length() - 4, movieYear.length()));
                        }

                        if (!jsonObject.optString("PosterPath").equals("null") && !jsonObject.optString("PosterPath").isEmpty())
                            movie.setPosterPath(jsonObject.optString("PosterPath"));

                        moviesList.add(movie);
                    }
                } else {
                    tvMessage.setVisibility(View.VISIBLE);
                    CartCheckDown.setVisibility(View.INVISIBLE);
                    CartCheckOut.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CartCheckOut.setOnClickListener(onCheckOutIconClick);
            CartCheckDown.setOnClickListener(onCheckDownIconClick);

            movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
            listView.setAdapter(movieAdapter);
            listView.setOnItemLongClickListener(this);

        }
        return rootView;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        try {
            final int moviePos = position;
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Remove " + moviesList.get(position).getTitle() + "?", 5000)
                    .setAction("REMOVE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String MovieCartJson, rTitle = moviesList.get(position).getTitle();
                            JSONObject jsonRootObject;
                            JSONArray jsonArray;
                            JSONObject obj1 = new JSONObject();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor ed = prefs.edit();
                            MovieCartJson = prefs.getString("MovieCart", "");
                            try {
                                //We need to check if this item is already in cart
                                jsonRootObject = new JSONObject(MovieCartJson);
                                jsonArray = jsonRootObject.optJSONArray("data");
                                JSONRemoveItemClass jric = new JSONRemoveItemClass();
                                jsonArray = jric.remove(jsonArray, position);
                                obj1.put("data", jsonArray);
                                ed.putString("MovieCart", obj1.toString());
                                ed.apply();
                                moviesList.clear();
                                if (jsonArray.length() != 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        MovieModel movie = new MovieModel();
                                        movie.setId(Integer.valueOf(jsonObject.optString("MovieID").toString()));
                                        movie.setTitle(jsonObject.optString("Title"));
                                        if (!jsonObject.optString("Date").equals("null") && !jsonObject.optString("Date").isEmpty()) {
                                            String movieYear = jsonObject.optString("Date").replace(")", "").replace("(", "");
                                            movie.setReleaseDate(movieYear.substring(movieYear.length() - 4, movieYear.length()));
                                        }
                                        if (!jsonObject.optString("PosterPath").equals("null") && !jsonObject.optString("PosterPath").isEmpty())
                                            movie.setPosterPath(jsonObject.optString("PosterPath"));

                                        moviesList.add(movie);
                                    }
                                } else {
                                    tvMessage.setVisibility(View.VISIBLE);
                                    CartCheckDown.setVisibility(View.INVISIBLE);
                                    CartCheckOut.setVisibility(View.INVISIBLE);
                                }
                                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                                listView.setAdapter(null);
                                listView.setAdapter(movieAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Snackbar snackbar1 = Snackbar.make(coordinatorLayout, rTitle + " removed from cart", Snackbar.LENGTH_SHORT);
                            snackbar1.show();
                        }
                    });
            snackbar.show();

        } catch (Exception e) {
            showToastFromBackground("An error has occurred during the operation!");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Class which listens when the user has tapped on Check Out icon button.
     */
    public class onCheckOutIconClick implements View.OnClickListener {

        public onCheckOutIconClick() {
            // keep references for your onClick logic
        }

        @Override
        public void onClick(View v) {
            showToastFromBackground("Please wait!");
            activity.showView(spinner);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // Actions to do after 10 seconds
                    activity.displayView(1);
                    activity.hideView(spinner);
                    showToastFromBackground("Select or search movie shop to place order!");
                }
            }, 5000);


        }
    }

    /**
     * Class which listens when the user has tapped on Check Down icon button.
     */
    public class onCheckDownIconClick implements View.OnClickListener {

        public onCheckDownIconClick() {
            // keep references for your onClick logic
        }

        @Override
        public void onClick(View v) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Clear Movie Cart List?", 5000)
                    .setAction("CLEAR", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                SharedPreferences.Editor ed = preferences.edit();
                                ed.putString("MovieCart", "");
                                //ed.clear();
                                ed.apply();
                                MovieModel movieModel = new MovieModel();
                                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                                movieAdapter.sort(movieModel);
                                moviesList = null;
                                movieAdapter = null;
                                listView.setAdapter(movieAdapter);
                                tvMessage.setVisibility(View.VISIBLE);
                                CartCheckDown.setVisibility(View.INVISIBLE);
                                CartCheckOut.setVisibility(View.INVISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Movie Cart has been cleared!", Snackbar.LENGTH_SHORT);
                            snackbar1.show();
                        }
                    });
            snackbar.show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().invalidateOptionsMenu();
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

}

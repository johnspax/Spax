package movie.software.com.spax.orders;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import movie.software.com.spax.R;
import movie.software.com.spax.adapter.MovieAdapter;
import movie.software.com.spax.custom.HttpPostClass;
import movie.software.com.spax.custom.JSONRemoveItemClass;
import movie.software.com.spax.model.MovieModel;

/**
 * Created by John Muya on 10/05/2017.
 */

public class OrderList extends Fragment implements AdapterView.OnItemClickListener {
    private View rootView;
    String OrderList, OrderID, responsecode, strResult;
    int OrderPos;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;

    private Handler mHandler;
    private AbsListView listView;
    private ArrayList<MovieModel> moviesList;
    private MovieAdapter movieAdapter;
    TextView _tvMessage;
    FloatingActionButton _fabCheckDown;
    FloatingActionButton _fabCheckOut;

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
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            OrderPos = bundle.getInt("OrderPos", 0);
        }
        try {
            rootView = inflater.inflate(R.layout.cartmovieslist, container, false);
            listView = (AbsListView) rootView.findViewById(R.id.movieslist);
            coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
            _tvMessage = (TextView) rootView.findViewById(R.id.tvMessage);
            _tvMessage.setVisibility(View.INVISIBLE);
            _fabCheckDown = (FloatingActionButton) rootView.findViewById((R.id.CartCheckDown));
            _fabCheckDown.setVisibility(View.INVISIBLE);
            _fabCheckOut = (FloatingActionButton) rootView.findViewById((R.id.CartCheckOut));
            _fabCheckOut.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getActivity().setTitle("Order List");

        moviesList = new ArrayList<>();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        OrderList = preferences.getString("Orders", "");
        mHandler = new Handler();

        if (preferences.getString("ServiceOrderID", "").equals(""))
            try {
                JSONObject jsonRootObject = new JSONObject(OrderList);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                //Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    responsecode = jsonObject.optString("responsecode").toString();
                    if (i == OrderPos) {
                        OrderID = jsonObject.optString("OrderID").toString();
                    } else {
                        //showToastFromBackground(jsonObject.optString("description").toString());
                    }
                }
            } catch (JSONException e) {
                showToastFromBackground("An error occurred during the operation!");
                e.printStackTrace();
            }
        else
            OrderID = preferences.getString("ServiceOrderID", "");


        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        try {
            new PrefetchOrderList().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
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
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemChecked(position, true);
        //((ListView)parent).setItemChecked(position, true);
        try {
            final int moviePos = position;
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Remove " + moviesList.get(position).getTitle() + "?", 5000)
                    .setAction("REMOVE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String SingleOrderJson, rTitle = moviesList.get(position).getTitle();
                            JSONObject jsonRootObject;
                            JSONArray jsonArray;
                            JSONObject obj1 = new JSONObject();
                            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor ed = preferences.edit();
                            SingleOrderJson = preferences.getString("SingleOrder", "");
                            try {
                                //We need to check if this item is already in cart
                                jsonRootObject = new JSONObject(SingleOrderJson);
                                jsonArray = jsonRootObject.optJSONArray("data");
                                JSONRemoveItemClass jric = new JSONRemoveItemClass();
                                jsonArray = jric.remove(jsonArray, position);
                                obj1.put("data", jsonArray);
                                ed.putString("SingleOrder", obj1.toString());
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
                                    _tvMessage.setVisibility(View.VISIBLE);
                                }
                                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                                listView.setAdapter(null);
                                listView.setAdapter(movieAdapter);
                                UpdateOrder();
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
    }

    public void UpdateOrder() {
        progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;
                while (jumpTime < totalProgressTime) {
                    try {
                        List<NameValuePair> nameValuePair = new ArrayList<>();
                        nameValuePair.add(new BasicNameValuePair("Request", "20"));
                        nameValuePair.add(new BasicNameValuePair("OrderID", OrderID));
                        nameValuePair.add(new BasicNameValuePair("OrderJsonList", preferences.getString("SingleOrder", "")));
                        strResult = HttpPostClass.POSTJSON(nameValuePair);
                        //onSuccess();
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
            }
        };
        t.start();
    }

    private class PrefetchOrderList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            Log.e("JSON", "Pre execute");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("Request", "11"));
                nameValuePair.add(new BasicNameValuePair("OrderID", OrderID));
                strResult = HttpPostClass.PostAsIs(nameValuePair);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("SingleOrder", strResult);
                editor.putString("ServiceOrderID", "");
                editor.apply();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            try {
                try {
                    JSONObject jsonRootObject = new JSONObject(strResult);
                    //Get the instance of JSONArray that contains JSONObjects
                    JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                    //Iterate the jsonArray and print the info of JSONObjects
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
                } catch (JSONException e) {
                    showToastFromBackground("An error occurred during the operation!");
                    e.printStackTrace();
                }
                movieAdapter = new MovieAdapter(getActivity(), R.layout.row, moviesList);
                listView.setAdapter(movieAdapter);
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().invalidateOptionsMenu();

        if (listView != null) {
            listView.setOnItemClickListener(this);
        }
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

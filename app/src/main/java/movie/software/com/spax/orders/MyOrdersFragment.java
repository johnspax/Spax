package movie.software.com.spax.orders;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import movie.software.com.spax.Global.GlobalClass;
import movie.software.com.spax.MainActivity;
import movie.software.com.spax.R;
import movie.software.com.spax.custom.HttpPostClass;
import movie.software.com.spax.menu.Menu;
import movie.software.com.spax.menu.MenuAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyOrdersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyOrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyOrdersFragment extends Fragment {
    String strResult, jsonOrders, responsecode, UserID, ProfilePath = "";
    MainActivity fa;
    MenuAdapter adapter = null;
    Menu menu_data[] = null;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    private Handler mHandler;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView listView1;
    private TextView _tvMsg;

    public MyOrdersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyOrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyOrdersFragment newInstance(String param1, String param2) {
        MyOrdersFragment fragment = new MyOrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        mHandler = new Handler();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_orders_list, container, false);
        listView1 = (ListView) v.findViewById(R.id.listView1);
        _tvMsg = (TextView) v.findViewById(R.id.tvMessage);
        fa = new MainActivity();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Processing...");
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        new PrefetchData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try {
                    // Orders fragment
                    final Fragment orderList = new OrderList();
                    // Sometimes, when fragment has huge data, screen seems hanging
                    // when switching between navigation menus
                    // So using runnable, the fragment is loaded with cross fade effect
                    // This effect can be seen in GMail app
                    Runnable mPendingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putInt("OrderPos",position); // Put anything what you want
                            // update the main content by replacing fragments
                            Fragment fragment = orderList;
                            fragment.setArguments(bundle);
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                            // add the current transaction to the back stack:
                            GlobalClass.setCurrentFragment("OrderList");
                            fragmentTransaction.addToBackStack("MyOrdersFragment");
                            fragmentTransaction.replace(R.id.frame_container, fragment, "Orders List");
                            fragmentTransaction.commitAllowingStateLoss();
                        }
                    };

                    // If mPendingRunnable is not null, then add to the message queue
                    if (mPendingRunnable != null) {
                        mHandler.post(mPendingRunnable);
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        return v;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            Log.e("JSON", "Pre execute");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            UserID = preferences.getString("UserID", "");
            List<NameValuePair> nameValuePair = new ArrayList<>();
            nameValuePair.add(new BasicNameValuePair("Request", "19"));
            nameValuePair.add(new BasicNameValuePair("UserID", UserID));
            strResult = HttpPostClass.PostAsIs(nameValuePair);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Orders", strResult);
            editor.apply();
            try {
                JSONObject jsonRootObject = new JSONObject(strResult);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                //Iterate the jsonArray and print the info of JSONObjects
                menu_data = new Menu[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    responsecode = jsonObject.optString("responsecode").toString();
                    //String[] Split = jsonObject.optString("UserID").toString().split("@");
                    if (responsecode.equals("000")) {
                        ProfilePath = jsonObject.optString("ProfilePath").toString();
                        menu_data[i] = new Menu(ProfilePath,jsonObject.optString("StallName").toString(),jsonObject.optString("UserID").toString(),jsonObject.optString("MobileNumber").toString(),jsonObject.optString("OrderDate").toString());
                    } else {
                        menu_data = null;
                        //fa.showToastFromBackground(jsonObject.optString("description").toString());
                    }
                }
            } catch (JSONException e) {
                fa.showToastFromBackground("An error occurred during the operation!");
                e.printStackTrace();
            }            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and lauch main activity
            try {
                if (menu_data != null) {
                    _tvMsg.setText(null);
                    adapter = new MenuAdapter(getActivity(), R.layout.listview_item_row, menu_data);
                    listView1.setAdapter(adapter);
                } else {
                    _tvMsg.setText("You have no new orders at the moment!");
                }
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

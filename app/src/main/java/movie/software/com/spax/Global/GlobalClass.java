package movie.software.com.spax.Global;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

/**
 * Created by John Muya on 17/03/2017.
 */

public class GlobalClass extends Application {
    public static Document Doc;
    public static LatLng cLatLng, dLatLng;
    public static String APIURL = "http://104.254.246.55/ApplicationSpax/RequestProcessor.aspx";
    public static String posterPath;
    public static String currentFragment;

    @Override
    public void onCreate() {
        super.onCreate();
        Doc = null;
        cLatLng = null;
        dLatLng = null;
        posterPath = null;
        currentFragment = null;
    }

    public static Document getDoc() {
        return Doc;
    }

    public static void setDoc(Document doc) {
        Doc = doc;
    }

    public static LatLng getcLatLng() {
        return cLatLng;
    }

    public static void setcLatLng(LatLng aLatLng) {
        cLatLng = aLatLng;
    }

    public static LatLng getdLatLng() {
        return dLatLng;
    }

    public static void setdLatLng(LatLng bLatLng) {
        dLatLng = bLatLng;
    }

    public static String getPosterPath() {
        return posterPath;
    }

    public static void setPosterPath(String path) {
        posterPath = path;
    }

    public  static String getCurrentFragment(){ return currentFragment; }

    public static void setCurrentFragment(String currFragment){ currentFragment = currFragment;}

}

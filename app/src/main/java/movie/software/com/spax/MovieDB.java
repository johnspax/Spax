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

import android.app.Application;
import android.util.Base64;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MovieDB extends Application {
    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    public static final String url = "https://api.themoviedb.org/3/";// main url for the app
    public static final String key = "8dfd2d9b81820109065cd9eed3825647";//yourTMDBkey
    public static final String imageUrl = "https://image.tmdb.org/t/p/";// used to load movie, TV and actor images
    /**
     * Example URL:
     * http://i1.ytimg.com/vi/TDFAYRtrYuk/hqdefault.jpg
     * For more info:
     * http://stackoverflow.com/questions/2068344/how-do-i-get-a-youtube-video-thumbnail-from-the-youtube-api
     */
    public static final String trailerImageUrl = "http://i1.ytimg.com/vi/";// used to load trailer images
    public static final String youtube = "https://www.youtube.com/watch?v=";// used to load trailer videos
    public static final String appId = "95a38b92c5cb4bbfd779c0e2fcaef5a6";
    public static final String analyticsKey = "QUl6YVN5Q25IcVQzSFB2RnZfYW9NOUp0eDBZbVZicm9GeDBySHhB";//Your encrypted analytics API Key

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            analytics = GoogleAnalytics.getInstance(this);

            //decode analytics key
            byte[] data = Base64.decode(analyticsKey, Base64.DEFAULT);
            String ak = new String(data, "UTF-8");

            tracker = analytics.newTracker(ak);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Tracker getTracker() {
        return tracker;
    }
}
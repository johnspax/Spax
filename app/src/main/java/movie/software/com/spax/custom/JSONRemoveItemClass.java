package movie.software.com.spax.custom;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by John Muya on 28/04/2017.
 */

public class JSONRemoveItemClass extends JSONArray {

    public JSONArray remove(JSONArray jarray, int index) {

        JSONArray output = new JSONArray();
        int len = jarray.length();
        for (int i = 0; i < len; i++) {
            if (i != index) {
                try {
                    output.put(jarray.get(i));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return output;
        //return this; If you need the input array in case of a failed attempt to remove an item.
    }
}

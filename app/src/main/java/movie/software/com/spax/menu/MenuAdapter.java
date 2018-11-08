package movie.software.com.spax.menu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import movie.software.com.spax.R;


/**
 * Created by John Muya on 08/03/2017.
 */

public class MenuAdapter extends ArrayAdapter<Menu> {

    Context context;
    int layoutResourceId;
    Menu data[] = null;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public MenuAdapter(Context context, int layoutResourceId, Menu[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                // Bitmaps in RGB_565 consume 2 times less memory than in ARGB_8888. Caching images in memory else
                // flicker while toolbar hiding
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.placeholder_smaller)
                .showImageForEmptyUri(R.drawable.placeholder_smaller)
                .showImageOnFail(R.drawable.placeholder_smaller)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder = null;
        try {
            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new WeatherHolder();
                holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
                holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
                holder.txtBody = (TextView) row.findViewById(R.id.txtBody);
                holder.txtPhone = (TextView) row.findViewById(R.id.txtPhone);
                holder.txtOrderDate = (TextView) row.findViewById(R.id.txtOrderDate);

                row.setTag(holder);
            } else {
                holder = (WeatherHolder) row.getTag();
            }

            Menu weather = data[position];
            holder.txtTitle.setText(weather.title);
            //holder.imgIcon.setImageResource(weather.icon);
            holder.txtBody.setText(weather.body);
            holder.txtPhone.setText(weather.phone);
            holder.txtOrderDate.setText(weather.OrderDate);

            // if getPosterPath returns null imageLoader automatically sets default image
            imageLoader.displayImage(weather.PosterPath, holder.imgIcon, options);


            return row;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    static class WeatherHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtBody;
        TextView txtPhone;
        TextView txtOrderDate;
    }
}

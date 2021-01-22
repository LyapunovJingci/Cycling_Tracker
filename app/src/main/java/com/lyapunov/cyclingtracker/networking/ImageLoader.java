package com.lyapunov.cyclingtracker.networking;

import android.widget.ImageView;
import com.squareup.picasso.Picasso;

public class ImageLoader {

    private static ImageLoader imageLoader = new ImageLoader();
    private ImageLoader(){};
    public static ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void loadImage (String latlng, ImageView view) {
        StringBuffer bf = new StringBuffer();
        bf.append("https://maps.googleapis.com/maps/api/staticmap?path=color:0x007870|weight:5");
        bf.append(latlng);
        bf.append("&size=240x150&key=");
        bf.append(Keys.getMapstaticKey());
        String url = bf.toString();
        Picasso.get().load(url).resize(240, 150).into(view);
    }

}

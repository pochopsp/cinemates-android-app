package it.unina.cinemates.ui.common.utils;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import it.unina.cinemates.R;

public class GlideUtils {

    public static void loadAndSetCircleImageWithUserPlaceholder(String imageUrl, ImageView imageView, Context context) {
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.no_profilepic_placeholder)
                .fallback(R.drawable.no_profilepic_placeholder)
                .circleCrop()
                .into(imageView);
    }

    public static void loadAndSetCircleImage(String imageUrl, ImageView imageView, Context context) {
        Glide.with(context)
                .load(imageUrl)
                .circleCrop()
                .into(imageView);
    }

    public static void loadAndSetImage(String imageUrl, ImageView imageView, Context context) {
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);
    }

    public static void loadAndSetImage(Integer resourceId, ImageView imageView, Context context) {
        Glide.with(context)
                .load(resourceId)
                .into(imageView);
    }

}

package it.unina.cinemates.ui.search.viewpager;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

public class FadePageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(View view, float position) {
        view.setTranslationX(view.getWidth() * -position);

        if (position <= -1.0f || position >= 1.0f) {
            view.setAlpha(0.0f);
            view.setVisibility(View.GONE);
        } else if (position == 0.0f) {
            view.setAlpha(1.0f);
            view.setVisibility(View.VISIBLE);
        } else {
            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            view.setAlpha(1.0f - Math.abs(position));
            view.setVisibility(View.VISIBLE);
        }
    }
}
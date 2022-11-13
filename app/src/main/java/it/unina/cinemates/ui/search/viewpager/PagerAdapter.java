package it.unina.cinemates.ui.search.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.unina.cinemates.ui.search.SearchTabFragment;

public class PagerAdapter extends FragmentStateAdapter {

    public PagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new SearchTabFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

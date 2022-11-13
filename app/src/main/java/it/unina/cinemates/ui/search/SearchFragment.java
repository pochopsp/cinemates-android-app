package it.unina.cinemates.ui.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Objects;

import it.unina.cinemates.MainActivity;
import it.unina.cinemates.R;
import it.unina.cinemates.retrofit.backend.enums.BackendOperationResult;
import it.unina.cinemates.ui.common.utils.DatePickerDialogUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.ui.search.viewpager.FadePageTransformer;
import it.unina.cinemates.ui.search.viewpager.PagerAdapter;
import it.unina.cinemates.viewmodels.SearchViewModel;
import it.unina.cinemates.viewmodels.enums.SearchType;
import it.unina.cinemates.viewmodels.user.movielist.logged.AddMovieToMovieListViewModel;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private SearchParameters lastSearchParameters;

    private AddMovieToMovieListViewModel addMovieToMovieListViewModel;

    private Snackbar addToListSuccessSnackbar;
    private Snackbar addToListErrorSnackbar;
    private Snackbar checkYourConnectionErrorSnackbar;

    private ConstraintLayout filterLayout;
    private TextInputEditText textInputEditText;

    private SearchType selectedSearch;
    private TextView yearFilterPickerTextView;
    private SwitchMaterial byActorSwitch;

    public SearchFragment() {
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        addMovieToMovieListViewModel = new ViewModelProvider(requireActivity()).get(AddMovieToMovieListViewModel.class);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        View bottomNavigationView = ((MainActivity) requireActivity()).getBottomNavigationView();

        this.addToListSuccessSnackbar = SnackbarUtils.successAnchorSnackbar(requireActivity(), getString(R.string.movie_add_to_list_success), bottomNavigationView);
        this.addToListErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.movie_already_in_list_error), bottomNavigationView);
        this.checkYourConnectionErrorSnackbar = SnackbarUtils.failureAnchorSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable), bottomNavigationView);


        addMovieToMovieListViewModel.getInsertMovieInMovieListResult().observe(getViewLifecycleOwner(), insertListResult -> {

            if (insertListResult == BackendOperationResult.IDLE)
                return;

            switch (insertListResult) {
                case SUCCESS:
                    this.addToListSuccessSnackbar.show();
                    break;
                case ALREADY_PRESENT:
                    this.addToListErrorSnackbar.show();
                    break;
                case SERVER_UNREACHABLE:
                    this.checkYourConnectionErrorSnackbar.show();
                    break;
            }

            addMovieToMovieListViewModel.resetInsertMovieInMovielistResult();
        });

        filterLayout = view.findViewById(R.id.filter_constraint_layout);
        MaterialButton filterButton = view.findViewById(R.id.search_filter_icon);
        filterButton.setOnClickListener(v -> {
            if (filterLayout.getVisibility() == View.VISIBLE)
                filterLayout.setVisibility(View.GONE);
            else
                filterLayout.setVisibility(View.VISIBLE);
        });

        TextView yearFilterTextView = view.findViewById(R.id.year_filter_textview);
        yearFilterPickerTextView = view.findViewById(R.id.year_filter_edit_text);
        yearFilterPickerTextView.requestFocus();
        ImageView clearYearImageView = view.findViewById(R.id.clear_year_imageview);
        clearYearImageView.setOnClickListener(v -> yearFilterPickerTextView.setText(""));

        view.findViewById(R.id.year_filter_textview).setOnClickListener(v -> yearPickerShow());

        yearFilterPickerTextView.setOnClickListener(v -> yearPickerShow());


        yearFilterPickerTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    yearFilterTextView.setVisibility(View.VISIBLE);
                    yearFilterTextView.setText(s.toString());
                } else
                    yearFilterTextView.setVisibility(View.GONE);

                if (s.toString().equals(""))
                    searchViewModel.setSearchByYearLiveData(null);
                else
                    searchViewModel.setSearchByYearLiveData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextView byActorTextView = view.findViewById(R.id.by_actor_textview);
        byActorSwitch = view.findViewById(R.id.search_by_actor_switch);
        byActorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                byActorTextView.setVisibility(View.VISIBLE);
            else
                byActorTextView.setVisibility(View.GONE);

            searchViewModel.setSearchedByActorLiveData(byActorSwitch.isChecked());
        });

        Button removeAllFiltersTextButton = view.findViewById(R.id.search_remove_all_filters);
        removeAllFiltersTextButton.setOnClickListener(v -> {
            removeAllFilters();
            filterLayout.setVisibility(View.GONE);
        });

        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(this));
        viewPager.setPageTransformer(new FadePageTransformer());

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0)
                        tab.setText(getString(R.string.search_movies_tab_title));
                    else
                        tab.setText(getString(R.string.search_users_tab_title));
                }
        ).attach();


        textInputEditText = view.findViewById(R.id.search_edit_text);
        Bundle bundle = getArguments();
        if (bundle != null) {
            Boolean fromDiscoverLists = (Boolean) bundle.get("fromDiscoverLists");
            if (fromDiscoverLists != null && fromDiscoverLists) {
                Objects.requireNonNull(tabLayout.getTabAt(1)).select();
                searchViewModel.setSearchTypeLiveData(SearchType.USERS);
                textInputEditText.setHint(R.string.search_users_hint);
            }
        }

        selectedSearch = SearchType.MOVIES;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (Objects.requireNonNull(tab.getText()).toString().equals(requireContext().getString(R.string.search_movies_tab_title))) {

                    applyMovieFilters();
                    filterButton.setEnabled(true);
                    filterButton.setIconTintResource(R.color.white);
                    FirebaseAnalytics.getInstance(requireContext()).logEvent("MovieSearch", null);
                    selectedSearch = SearchType.MOVIES;
                    searchViewModel.setSearchTypeLiveData(SearchType.MOVIES);
                    textInputEditText.setHint(R.string.search_movies_hint);
                    textInputEditText.setSelection(Objects.requireNonNull(textInputEditText.getText()).length());
                } else {
                    hideFiltersGui(filterButton, yearFilterTextView, byActorTextView);
                    FirebaseAnalytics.getInstance(requireContext()).logEvent("UserSearch", null);
                    selectedSearch = SearchType.USERS;
                    searchViewModel.setSearchTypeLiveData(SearchType.USERS);
                    textInputEditText.setHint(R.string.search_users_hint);
                    textInputEditText.setSelection(Objects.requireNonNull(textInputEditText.getText()).length());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (searchViewModel.getSearchTypeLiveData().getValue() == SearchType.MOVIES)
            Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        else
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();

        View verticalLine = view.findViewById(R.id.search_vertical_line);
        Button clearButton = view.findViewById(R.id.search_clear_button);
        clearButton.setOnClickListener(v -> {
            verticalLine.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
            textInputEditText.setText("");
        });

        Button searchButton = view.findViewById(R.id.send_icon_button);
        searchButton.setOnClickListener(v -> {
            prepareSearch(yearFilterTextView, byActorSwitch, textInputEditText);
            ((MainActivity) requireActivity()).closeKeyboard(view);
            textInputEditText.clearFocus();
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    verticalLine.setVisibility(View.VISIBLE);
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    verticalLine.setVisibility(View.GONE);
                    clearButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textInputEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                prepareSearch(yearFilterTextView, byActorSwitch, textInputEditText);
                ((MainActivity) requireActivity()).closeKeyboard(view);
                textInputEditText.clearFocus();
                return true;
            }
            return false;
        });

        searchViewModel.getSearchByActorLiveData().observe(getViewLifecycleOwner(), byActor -> {
            byActorSwitch.setChecked(byActor);
            if (byActor && byActorTextView.getVisibility() == View.GONE && tabLayout.getSelectedTabPosition() == 0)
                byActorTextView.setVisibility(View.VISIBLE);
        });

        searchViewModel.getSearchByYearLiveData().observe(getViewLifecycleOwner(), year -> {
            if (year == null || yearFilterTextView.getVisibility() == View.VISIBLE)
                return;

            if (tabLayout.getSelectedTabPosition() == 0)
                yearFilterPickerTextView.setText(year);
        });


        searchViewModel.getSearchedTextLiveData().observe(getViewLifecycleOwner(), searchParameters -> {
            if (searchParameters != null)
                textInputEditText.setText(searchParameters.getSearchedText());
        });

        return view;
    }

    private void yearPickerShow() {

        String year = searchViewModel.getSearchByYearLiveData().getValue();
        if (year == null || year.isEmpty())
            DatePickerDialogUtils.yearDialogWithOutputTextView(
                    1888,
                    10,
                    R.string.year_filter_title,
                    yearFilterPickerTextView,
                    requireActivity()
            ).show();
        else
            DatePickerDialogUtils.yearDialogWithOutputTextView(
                    1888,
                    10,
                    Integer.parseInt(year),
                    R.string.year_filter_title,
                    yearFilterPickerTextView,
                    requireActivity()
            ).show();
        textInputEditText.requestFocus();
        textInputEditText.setSelection(Objects.requireNonNull(textInputEditText.getText()).toString().length());
    }

    private void hideFiltersGui(MaterialButton filterButton, TextView yearFilterTextView, TextView byActorTextView) {

        filterButton.setEnabled(false);
        filterButton.setIconTintResource(R.color.gray_line);
        filterLayout.setVisibility(View.GONE);
        yearFilterTextView.setVisibility(View.GONE);
        byActorTextView.setVisibility(View.GONE);
    }


    private void removeAllFilters() {
        yearFilterPickerTextView.setText("");
        byActorSwitch.setChecked(false);
    }

    private void applyMovieFilters() {
        searchViewModel.setSearchByYearLiveData(searchViewModel.getSearchByYearLiveData().getValue());
        searchViewModel.setSearchedByActorLiveData(searchViewModel.getSearchByActorLiveData().getValue());
    }

    private void prepareSearch(TextView yearFilterTextView, SwitchMaterial byActorSwitch, TextInputEditText textInputEditText) {
        String year = null;

        String searchedString = Objects.requireNonNull(textInputEditText.getText()).toString().trim();
        textInputEditText.setText(searchedString);

        filterLayout.setVisibility(View.GONE);
        if (yearFilterTextView.getVisibility() == View.VISIBLE)
            year = yearFilterTextView.getText().toString();

        SearchParameters parameters = new SearchParameters(searchedString, byActorSwitch.isChecked(), year);
        if (parameters.equals(lastSearchParameters)) {
            return;
        }
        lastSearchParameters = parameters;

        if (!textInputEditText.getText().toString().isEmpty()) {
            searchViewModel.resetMovieSearchList();
            searchViewModel.setSearchedTextLiveData(parameters);
        }

        searchViewModel.setIsNewSearchLiveData(true);

        if (selectedSearch == SearchType.MOVIES)
            FirebaseAnalytics.getInstance(requireContext()).logEvent("MovieSearch", null);
        else
            FirebaseAnalytics.getInstance(requireContext()).logEvent("UserSearch", null);
    }
}
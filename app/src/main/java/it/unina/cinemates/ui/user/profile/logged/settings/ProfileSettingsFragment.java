package it.unina.cinemates.ui.user.profile.logged.settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import it.unina.cinemates.R;
import it.unina.cinemates.cloudservices.cloudinary.CloudinaryHelper;
import it.unina.cinemates.cloudservices.cognito.LoggedUser;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.ui.common.utils.GlideUtils;
import it.unina.cinemates.ui.common.utils.SnackbarUtils;
import it.unina.cinemates.utils.IntegerUtils;
import it.unina.cinemates.viewmodels.user.profile.logged.ProfileSettingsViewModel;

public class ProfileSettingsFragment extends Fragment {

    private static final String TAG = "LOGGEDUSERPROFILESETTINGS_FRAGMENT";

    private ImageView backIcon;

    private ProfileSettingsViewModel profileSettingsViewModel;

    // --------------------- CHANGE PROFILE PIC ---------------------

    private ImageView profilePicImageView;
    private Button changeProfilePicButton;
    private Button changeProfilePicCancelButton;
    private Button changeProfilePicConfirmButton;
    private boolean changingProfilePicMode = false;

    private Uri imageRetrievedFromStorageUri;

    private Integer currentIdPhoto;
    private Integer newIdPhoto;

    private LinearLayout dimWindow;
    private CircularProgressIndicator progressIndicator;

    // --------------------- EDIT USER BIO ---------------------
    private String userBio;
    private String previousUserBio;

    private RelativeLayout cancelConfirmEditBioLayout;
    private TextInputEditText bioEditText;
    private Button editBioButton;

    // --------------------- SNACKBARS ---------------------
    private Snackbar serverErrorSnackbar;
    private Snackbar editBioSuccessSnackbar;
    private Snackbar changeProfilePicSuccessSnackbar;
    private Snackbar deleteProfilePicSuccessSnackbar;


    private Button changePasswordButton;
    private Button deleteAccountButton;

    private FloatingActionButton deleteProfilePicFab;


    public ProfileSettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.profileSettingsViewModel = new ViewModelProvider(requireActivity()).get(ProfileSettingsViewModel.class);
        overrideBackPressedBehavior();
    }

    private void overrideBackPressedBehavior() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                ProfileSettingsFragment.this.goBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goBack() {
        if (!this.changingProfilePicMode) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
            profileSettingsViewModel.resetBioAndIdPhotoUpdateResultLiveData();
            profileSettingsViewModel.resetAllData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged_user_profile_settings, container, false);

        setupBackIcon(view);

        setupSnackbars();

        setupDeleteProfilePicFAB(view);

        setupProfilePicImageView(view);

        setupChangeProfilePicButtonsAndLogic(view);

        setupEmailTextView(view);

        setupEditBioButtonsAndLogic(view);

        setupChangePasswordButton(view);

        setupDeleteAccountButton(view);

        profileSettingsViewModel.getUpdateIdPhotoResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            switch (backendOperationResult) {
                case SERVER_UNREACHABLE:
                    this.serverErrorSnackbar.show();
                    setupProfilePicImageView(view);
                    break;
                case SUCCESS:
                    this.exitLoadingMode();
                    this.exitChangeProfilePicMode();

                    deleteProfilePicFab.setVisibility(View.VISIBLE);

                    this.changeProfilePicSuccessSnackbar.show();

                    CloudinaryHelper.deleteImageWithIntegerId(this.currentIdPhoto, TAG);

                    this.currentIdPhoto = this.newIdPhoto;

                    LoggedUser.getInstance().setIdPhoto(this.currentIdPhoto);
            }
        });


        profileSettingsViewModel.getUpdateBioResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            switch (backendOperationResult) {
                case SERVER_UNREACHABLE:
                    userBio = previousUserBio;
                    resetBioTextInputEditText();
                    this.serverErrorSnackbar.show();
                    break;
                case SUCCESS:
                    resetBioTextInputEditText();
                    this.editBioSuccessSnackbar.show();
            }
        });


        profileSettingsViewModel.getDeleteProfilePicResult().observe(getViewLifecycleOwner(), backendOperationResult -> {
            switch (backendOperationResult) {
                case SERVER_UNREACHABLE:
                    serverErrorSnackbar.show();
                    break;
                case SUCCESS:
                    deleteProfilePicSuccessSnackbar.show();
                    profileSettingsViewModel.resetDeleteProfilePicResult();
                    GlideUtils.loadAndSetCircleImageWithUserPlaceholder("", profilePicImageView, requireContext());
                    LoggedUser.getInstance().setIdPhoto(null);
                    deleteProfilePicFab.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }

    private void setupDeleteAccountButton(View view) {
        deleteAccountButton = view.findViewById(R.id.logged_user_profile_settings_delete_account_button);
        deleteAccountButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_logged_user_delete_account);
        });
    }

    private void setupChangePasswordButton(View view) {
        changePasswordButton = view.findViewById(R.id.logged_user_profile_settings_change_password_button);
        changePasswordButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_logged_user_change_password);
        });
    }

    private void setupEditBioButtonsAndLogic(View view) {
        cancelConfirmEditBioLayout = view.findViewById(R.id.logged_user_profile_settings_cancel_confirm_edit_bio_layout);

        Bundle bundle = getArguments();
        assert bundle != null;
        this.userBio = (String) bundle.get("userBio");

        bioEditText = view.findViewById(R.id.logged_user_profile_settings_bio_textinput);
        if (this.userBio != null)
            bioEditText.setText(this.userBio);

        editBioButton = view.findViewById(R.id.logged_user_profile_settings_edit_bio_button);
        editBioButton.setOnClickListener(v -> {
            cancelConfirmEditBioLayout.setVisibility(View.VISIBLE);
            bioEditText.setEnabled(true);
            if (this.userBio == null) {
                bioEditText.setText("");
            } else
                bioEditText.setSelection(Objects.requireNonNull(bioEditText.getText()).length());
            bioEditText.requestFocus();
            editBioButton.setVisibility(View.GONE);
        });


        Button confirmEditBioButton = view.findViewById(R.id.logged_user_profile_settings_confirm_edit_bio_button);
        confirmEditBioButton.setOnClickListener(v -> {
            ProfileSettingsFragment.this.cancelOrConfirmEditBioPressed(view);

            String newUserBio = Objects.requireNonNull(bioEditText.getText()).toString();

            newUserBio = newUserBio.trim();

            if(newUserBio.length() > 0)
                bioEditText.setText(newUserBio);

            if (newUserBio.equals(""))
                newUserBio = null;

            if (!Objects.equals(newUserBio, userBio)) {
                this.previousUserBio = userBio;
                userBio = newUserBio;
                profileSettingsViewModel.requestBioUpdate(userBio);
            }

            if (newUserBio == null)
                bioEditText.setText(R.string.userprofile_biotext);
        });


        Button cancelEditBioButton = view.findViewById(R.id.logged_user_profile_settings_cancel_edit_bio_button);
        cancelEditBioButton.setOnClickListener(v -> {
            ProfileSettingsFragment.this.cancelOrConfirmEditBioPressed(view);
            resetBioTextInputEditText();
        });
    }


    private void setupChangeProfilePicButtonsAndLogic(View view) {
        dimWindow = view.findViewById(R.id.logged_user_profile_settings_dim_window);
        progressIndicator = view.findViewById(R.id.logged_user_profile_settings_profile_pic_progress_indicator);

        setupChangeProfilePicButton(view);

        setupChangeProfilePicCancelButton(view);

        setupChangeProfilePicConfirmButton(view);
    }

    private void setupChangeProfilePicConfirmButton(View view) {
        changeProfilePicConfirmButton = view.findViewById(R.id.change_profile_pic_confirm);
        changeProfilePicConfirmButton.setOnClickListener(v -> {
            if (!this.isNetworkAvailable(requireContext())) {
                serverErrorSnackbar.show();
                setupProfilePicImageView(view);
                exitChangeProfilePicMode();
                return;
            }
            newIdPhoto = IntegerUtils.randomInteger();
            profileSettingsViewModel.requestProfilePicUpdate(imageRetrievedFromStorageUri, newIdPhoto);
            enterLoadingMode();
        });
    }

    private void setupChangeProfilePicCancelButton(View view) {
        changeProfilePicCancelButton = view.findViewById(R.id.change_profile_pic_cancel);
        changeProfilePicCancelButton.setOnClickListener(v -> {
            setupProfilePicImageView(view);
            exitChangeProfilePicMode();
        });
    }

    private void setupChangeProfilePicButton(View view) {
        changeProfilePicButton = view.findViewById(R.id.logged_user_profile_settings_change_profile_pic_button);
        ActivityResultLauncher<String> imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    imageRetrievedFromStorageUri = uri;
                    if (uri != null) { //User picked an image
                        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(imageRetrievedFromStorageUri.toString(), profilePicImageView, requireContext());
                        enterChangeProfilePicMode();
                    }
                });

        changeProfilePicButton.setOnClickListener(v -> imagePicker.launch("image/*"));
    }

    private void setupDeleteProfilePicFAB(View view) {
        deleteProfilePicFab = view.findViewById(R.id.user_profile_settings_delete_profile_pic_floating_action_button);
        deleteProfilePicFab.setOnClickListener(
                v -> AlertDialogUtils.alertDialog(
                        requireContext(),
                        R.string.delete_profile_pic_dialog_title,
                        R.string.delete_profile_pic_dialog_message,
                        R.string.delete,
                        (dialog, which) -> profileSettingsViewModel.requestDeleteProfilePic(currentIdPhoto)
                ).show());
    }

    private void enterChangeProfilePicMode() {
        changeProfilePicButton.setVisibility(View.INVISIBLE);
        deleteProfilePicFab.setVisibility(View.INVISIBLE);
        backIcon.setVisibility(View.INVISIBLE);
        editBioButton.setVisibility(View.INVISIBLE);
        changePasswordButton.setEnabled(false);
        deleteAccountButton.setEnabled(false);
        changeProfilePicCancelButton.setVisibility(View.VISIBLE);
        changeProfilePicConfirmButton.setVisibility(View.VISIBLE);

        changingProfilePicMode = true;
    }

    private void exitChangeProfilePicMode() {
        changingProfilePicMode = false;

        changeProfilePicCancelButton.setVisibility(View.INVISIBLE);
        changeProfilePicConfirmButton.setVisibility(View.INVISIBLE);
        changeProfilePicButton.setVisibility(View.VISIBLE);
        deleteProfilePicFab.setVisibility(View.VISIBLE);
        backIcon.setVisibility(View.VISIBLE);
        editBioButton.setVisibility(View.VISIBLE);
        changePasswordButton.setEnabled(true);
        deleteAccountButton.setEnabled(true);
    }

    private void setupProfilePicImageView(View view) {
        profilePicImageView = view.findViewById(R.id.logged_user_profile_settings_pic_image_view);
        currentIdPhoto = LoggedUser.getInstance().getIdPhoto();
        String idPhoto = currentIdPhoto == null ? "" : currentIdPhoto.toString();

        GlideUtils.loadAndSetCircleImageWithUserPlaceholder(CloudinaryHelper.imagePath(idPhoto), profilePicImageView, this.requireContext());
        if (idPhoto.equals(""))
            deleteProfilePicFab.setVisibility(View.INVISIBLE);
    }

    private void setupSnackbars() {
        this.editBioSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.edit_bio_success));
        this.changeProfilePicSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.change_profile_pic_success));
        this.serverErrorSnackbar = SnackbarUtils.failureSnackbar(requireActivity(), getString(R.string.snackbar_server_unreachable));
        this.deleteProfilePicSuccessSnackbar = SnackbarUtils.successSnackbar(requireActivity(), getString(R.string.delete_profile_pic_success));
    }

    private void resetBioTextInputEditText() {
        if (userBio == null)
            bioEditText.setText(this.getString(R.string.userprofile_biotext));
        else
            bioEditText.setText(userBio);
    }

    private void cancelOrConfirmEditBioPressed(View view) {
        cancelConfirmEditBioLayout.setVisibility(View.GONE);
        bioEditText.setEnabled(false);
        view.requestFocus();
        editBioButton.setVisibility(View.VISIBLE);
    }


    private void exitLoadingMode() {
        progressIndicator.setVisibility(View.GONE);
        dimWindow.setVisibility(View.GONE);
        this.requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void enterLoadingMode() {
        dimWindow.setVisibility(View.VISIBLE);
        progressIndicator.setVisibility(View.VISIBLE);
        ProfileSettingsFragment.this.requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    private void setupEmailTextView(View view) {
        TextView emailTextView = view.findViewById(R.id.logged_user_profile_settings_email_textview);
        emailTextView.setText(LoggedUser.getInstance().getEmail());
    }


    private void setupBackIcon(View view) {
        this.backIcon = view.findViewById(R.id.logged_user_profile_settings_back_icon);
        this.backIcon.setOnClickListener(v -> ProfileSettingsFragment.this.goBack());
    }


    @SuppressWarnings("deprecation")
    private boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // For 29 api or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities == null)
                return false;
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true;
            else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                return true;
            else return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        }
        // For below 29 api
        else {
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
        }
    }


}
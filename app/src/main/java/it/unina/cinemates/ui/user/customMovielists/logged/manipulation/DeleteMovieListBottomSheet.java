package it.unina.cinemates.ui.user.customMovielists.logged.manipulation;


import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import it.unina.cinemates.R;
import it.unina.cinemates.ui.common.utils.AlertDialogUtils;
import it.unina.cinemates.viewmodels.user.customMovielists.CustomListsViewModel;

public class DeleteMovieListBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "DELETEMOVIELIST_BOTTOMSHEET";
    private final ExtendedFloatingActionButton createListFAB;
    private final boolean isListEmpty;
    private Integer movieListId;
    private ImageView movieListOverlay;

    private CustomListsViewModel customListsViewModel;
    private boolean requestedDelete = false;

    AlertDialog deleteMovieListDialog;

    public DeleteMovieListBottomSheet(int movieListId, boolean isListEmpty, ImageView movieListOverlay, ExtendedFloatingActionButton createListFAB) {
        this.movieListId = movieListId;
        this.isListEmpty = isListEmpty;
        this.movieListOverlay = movieListOverlay;
        this.createListFAB = createListFAB;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CineMates_BottomSheetWithoutDim);
        customListsViewModel = new ViewModelProvider(requireActivity()).get(CustomListsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_movie_list_bottom_sheet, container, false);


        deleteMovieListDialog = AlertDialogUtils.alertDialogWithBothButtonListeners(
                requireContext(),
                R.string.delete_movie_list_dialog_title,
                R.string.irreversible_action_dialog_message,
                R.string.delete,
                (dialog, which) -> {
                    requestedDelete = true;
                    customListsViewModel.requestDeleteMovieList(this.movieListId);
                },
                (dialog, which) -> DeleteMovieListBottomSheet.this.dismiss()
        );


        Button deleteListButton = view.findViewById(R.id.delete_list_button);
        deleteListButton.setOnClickListener(v -> {
            Log.e(TAG, "deleting list with id " + movieListId + "...");
            if(isListEmpty){
                requestedDelete = true;
                customListsViewModel.requestDeleteMovieList(this.movieListId);
            }else{
                deleteMovieListDialog.show();
            }
        });

        return view;
    }

    @Override
    public void dismiss() {
        this.onBottomSheetClosing();
        super.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.onBottomSheetClosing();
        super.onCancel(dialog);
    }
    
    
    private void onBottomSheetClosing(){
        if(!requestedDelete) {
            this.createListFAB.setVisibility(View.VISIBLE);
        }
        this.movieListOverlay.setVisibility(View.INVISIBLE);
    }
    
}



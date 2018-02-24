package com.nerdgeeks.cinetodo.todolist.ui;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.nerdgeeks.cinetodo.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddMovieFragment extends DialogFragment {

    private EditText editTextMovieName;
    public AddMovieFragment() {
        // Required empty public constructor
    }

    public static AddMovieFragment newInstance(){
        AddMovieFragment fragment = new AddMovieFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_movie, null);
        editTextMovieName = (EditText) rootView.findViewById(R.id.edit_text_list_name);

        editTextMovieName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == event.ACTION_DOWN){

                }
                return true;
            }
        });

        builder.setView(rootView)
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddNewMovie activity = (AddNewMovie) getActivity();
                        activity.AddNewTask(editTextMovieName.getText().toString());
                    }
                });

        return builder.create();
    }

    public interface AddNewMovie{
        void AddNewTask(String title);
    }
}

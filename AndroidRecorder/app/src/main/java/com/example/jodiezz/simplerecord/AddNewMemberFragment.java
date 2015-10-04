package com.example.jodiezz.simplerecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class AddNewMemberFragment extends DialogFragment {

    public interface AddNewMemberListener {
        public void onDialogPositiveClick(DialogFragment dialog, String nameRecorded);
        public void onDialogNegativeClick(DialogFragment dialog);
        //public void getFromUser(String message);
    }
    AddNewMemberListener mListener;

    // Override the Fragment.onAttach() method to instantiate the AddNewMemberListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AddNewMemberListener so we can send events to the host
            mListener = (AddNewMemberListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AddNewMemberListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        builder.setTitle("User Info");
        builder.setMessage("Please key in your name below:");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.activity_add_new_user, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //proceedToRecord();
                        System.out.println("dialog proceed clicked");
                        //System.out.println(((EditText)dialogView.findViewById(R.id.first_name)).getText().toString());
                        //System.out.println(((EditText)dialogView.findViewById(R.id.last_name)).getText().toString());
                        String nameRecord = ((EditText)dialogView.findViewById(R.id.first_name)).getText().toString() +
                                ((EditText)dialogView.findViewById(R.id.last_name)).getText().toString();
                        mListener.onDialogPositiveClick(AddNewMemberFragment.this, nameRecord);

                    }
                })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //LoginDialogFragment.this.getDialog().cancel();
                        mListener.onDialogNegativeClick(AddNewMemberFragment.this);
                    }
                });
        return builder.create();
    }
    /*
    public void getFromUser(View v) {
        if (mListener != null) {
            EditText edit = (EditText)findViewById(R.id.first_name);
            mListener.getFromUser(edit.getText().toString());
        }
    }*/


}

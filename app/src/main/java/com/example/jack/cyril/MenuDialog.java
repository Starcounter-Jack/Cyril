package com.example.jack.cyril;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ResourceBundle;

public class MenuDialog extends DialogFragment {


    private Controller Controller;

    public MenuDialog( Controller c ) {
        Controller = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Controller c = Controller;
        String[] choices = {"Recover using okey-dokey","Recover using all","Reset Trip","Set Trip Manually","Speed simulator","Calling ahead"};
        builder.setTitle("Menu")
                .setItems(choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 2) { // RESET TRIP
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setTitle("Reset Trip");
                            builder.setMessage("The trip will be set to Zero. Are you sure?");

                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing but close the dialog

                                    dialog.dismiss();
                                    c.Reset();
                                }
                            });

                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // Do nothing
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
    }
}
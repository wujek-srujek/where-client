package com.where.client.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import com.where.client.R;


public class MapTypePickerDialog extends DialogFragment {

    public interface MapTypeCallback {

        int currentMapType();

        void onMapTypeChanged(int newType);
    }


    private MapTypeCallback mapTypeCallback;

    private String[] itemNames;

    public static MapTypePickerDialog newInstance() {
        return new MapTypePickerDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mapTypeCallback = (MapTypeCallback) context;

        itemNames = new String[] {
                getString(R.string.map_type_normal),
                getString(R.string.map_type_satellite),
                getString(R.string.map_type_terrain),
                getString(R.string.map_type_hybrid)
        };
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // use the fact that map types are public constants values 1-4 (ignore NONE)
        // this way if we preserve the same ordering we can do without item->type mapping
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.map_type_dialog_title)
                .setSingleChoiceItems(
                        itemNames,
                        mapTypeCallback.currentMapType() - 1,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                mapTypeCallback.onMapTypeChanged(item + 1);
                                dialog.dismiss();
                            }
                        })
                .create();
    }
}

package org.dhappy.habits;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DescriptorWeightDialog extends DialogFragment {
	private String TAG = "DescriptorWeightDialog";
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.weight_dialog, null);
        
        final TextView weight = (TextView) view.findViewById(R.id.weight);
        SeekBar weightSelect = (SeekBar) view.findViewById(R.id.weight_select);
        weightSelect.setMax(200);
        weightSelect.setProgress(100);
        weightSelect.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
            	position -= 100; // shift from 0-200
            	
                weight.setText(Integer.toString(position));
                float pos = position / 100f;
                int color = Color.rgb((int) (255 * Math.max(0, -pos)),
			                          (int) (255 * Math.max(0, pos)),
			                          (int) (255 * (1 - Math.abs(pos))));
                weight.setTextColor(color);
            }

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {}
        });
        
        builder.setView(view)
               .setMessage(R.string.habit_edit_cancel)
               .setPositiveButton(R.string.weight_confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                   }
               })
               .setNegativeButton(R.string.weight_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        return builder.create();
    }
}

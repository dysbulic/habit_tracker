package org.dhappy.habits;

import org.dhappy.habits.database.ReadingTable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DescriptorWeightDialog extends DialogFragment {
	private String TAG = "DescriptorWeightDialog";
	public final static String DESCRIPTOR_NAME = "org.dhappy.habits.mood.descriptor.name";
	
	public interface DescriptorWeightDialogListener {
        public void onRecordWeight(DialogFragment dialog);
    }

	DescriptorWeightDialogListener mListener;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (DescriptorWeightDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.weight_dialog, null);
        
        final TextView weight = (TextView) view.findViewById(R.id.weight);
        SeekBar weightSelect = (SeekBar) view.findViewById(R.id.weight_select);
        weightSelect.setMax(200); // -100–100
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

        String descriptor = this.getArguments().getString(DESCRIPTOR_NAME, "Unset");
        final DescriptorWeightDialog self = this;
        
        builder.setView(view)
               .setMessage(descriptor)
               .setPositiveButton(R.string.weight_confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   //mListener.onRecordWeight(self);
                       ContentValues values = new ContentValues();
                       //values.put(ReadingTable.COLUMN_DESCRIPTOR_ID, id);
                       values.put(ReadingTable.COLUMN_TIME, Math.floor(System.currentTimeMillis() / 1000));

                       //getActivity().getContentResolver().insert(HabitContentProvider.READINGS_URI, values);

                     	Toast.makeText(getActivity(), "Added new event", Toast.LENGTH_LONG).show();

                     	((MainActivity) getActivity()).setActiveTab(2);
                   }
               })
               .setNegativeButton(R.string.weight_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   }
               });
        return builder.create();
    }
}

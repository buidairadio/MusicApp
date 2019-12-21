package com.uet.teamduan.musicplayer.screens.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
import com.uet.teamduan.musicplayer.R;


public class TimePickerDialogFragment extends AppCompatDialogFragment {
    private MyTimePickerListener listener;
    private TimePicker timePicker ;
    private CardView cardView_Ok;
    private CardView cardView_Cancel;

    private int mHour;
    private int mMinute;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_timepicker,null);

        timePicker = view.findViewById(R.id.timePicker);
        cardView_Ok = view.findViewById(R.id.cardView_Ok);
        cardView_Cancel = view.findViewById(R.id.cardView_Cancel);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(mHour);
        timePicker.setCurrentMinute(mMinute);
        builder.setView(view);

        final Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        cardView_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.setTime(timePicker.getCurrentHour(),timePicker.getCurrentMinute());
                dialog.dismiss();
            }
        });
        cardView_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTimePickerCancel();
                dialog.dismiss();
            }
        });

        return dialog;
    }
    public void initTime(int hour, int minute){
        mHour = hour;
        mMinute = minute;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (MyTimePickerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must implement FindFriendListener");
        }
    }

    public interface MyTimePickerListener{
        void setTime(int hour, int minute);
        void onTimePickerCancel();
    }
}

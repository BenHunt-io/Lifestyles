package theappfoundry.lifestyles;

/**
 * Created by Ben on 6/8/2017.
 */

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by Ben on 6/6/2017.
 */
/**
 * This class creates the dialog. Has to be static. Nested classes that are declared static are
 * called static nested classes. Non-static nested classes are called inner classes. Static
 * nested classes can't access the enclosing classes members.
 *
 * The builder convience class builds the Alert Dialog. returns it in the onCreateDialog Callback
 * OnClick handles the buttons in the dialog.. The three buttons allowed.. Positive,negative, and
 * Neutral are represented by ints. See documentation.
 *
 */
public class DialogWindowPopUp extends DialogFragment implements View.OnClickListener, NumberPicker.OnValueChangeListener, RadioGroup.OnCheckedChangeListener {

    // Need to declare this so I can have a reference to timeText across the class
    TextView timeText;

    String timeLimitHour = "00";  // default to zero
    String timeLimitMinute = "00"; // default to zero


    /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    DialogWindowPopUp.NoticeDialogListener mListener;

    String TAG = "DialogWindowPopUp";

    //                    .setTitle("Ben's Dialog Window")
//                    .setPositiveButton("Create", this)
//                    .setNegativeButton("Cancel", this)
//                    .getContext().getTheme().applyStyle(R.style.Theme_Window_NoMinWidth, true)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {




        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get layout inflater
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Set layout by setting view that is returned from inflating the XML layout
        builder.setView(layoutInflater.inflate(R.layout.dialog_window_layout, null));

        AlertDialog dialog = builder.create();

        // Set a theme to this dialog that requires no min width.
        dialog.getContext().setTheme(R.style.Theme_Window_NoMinWidth);


        return dialog;
    }

//        /**
//         * Default positive/negative/neutral onClick
//         * @param dialog
//         * @param which
//         */
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            switch(which){
//                case Dialog.BUTTON_POSITIVE:
//                    break;
//                case Dialog.BUTTON_NEGATIVE:
//                    break;
//            }
//        }

    @Override
    public void onStart(){
        super.onStart();


        if(getDialog() == null){
            return;
        }
        else {

            /**
             * Have to make sure I access the buttons that are being created. So I use the
             * Dialog. Have to make sure that the dialog has actually been created. So I do it
             * in the onStart() callback.
             */
            ImageButton closeButton = (ImageButton)getDialog().findViewById(R.id.closeButton);
            Button createButton = (Button)getDialog().findViewById(R.id.createGeo_button);
            timeText = (TextView)getDialog().findViewById(R.id.timeText);

            EditText nameField = (EditText)getDialog().findViewById(R.id.nameField);
            nameField.setMaxLines(1); // Don't need a name longer than a line.

            NumberPicker hourPicker = (NumberPicker)getDialog().findViewById(R.id.hourPicker);
            NumberPicker minutePicker = (NumberPicker)getDialog().findViewById(R.id.minutePicker);


            // This radio group is nested inside a horizontal scrollview. This acts as ImagePicker
            RadioGroup imagePicker = (RadioGroup)getDialog().findViewById(R.id.imagePicker);
            imagePicker.setOnCheckedChangeListener(this);

            // Set Max Min Values for NumberPicker
            hourPicker.setMaxValue(24);
            minutePicker.setMaxValue(60);
            hourPicker.setMinValue(0);
            minutePicker.setMinValue(0);

            hourPicker.setOnValueChangedListener(this);
            minutePicker.setOnValueChangedListener(this);

            closeButton.setOnClickListener(this);
            createButton.setOnClickListener(this);

        }


    }

    /**
     * Now that mListener has a reference to the activites NoticeDialogListener that was
     * implemented. When mListener makes calls onDialogNegativeClick, it's really the activities
     * NoticeDialogListener and it's passing the current dialog instance as an argument
     * @param v - The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(v == getDialog().findViewById(R.id.closeButton)) {
            // Send the positive button event back to the host activity
            mListener.onDialogNegativeClick(DialogWindowPopUp.this);
            getDialog().dismiss();
        }
        else if(v == getDialog().findViewById(R.id.createGeo_button)) {
            // Send the negative button event back to the host activity
            mListener.onDialogPositiveClick(DialogWindowPopUp.this);
            getDialog().dismiss();
        }
//            else if(v == getDialog().findViewById(R.id.r))
//                getDialog().findViewById(R.id.imageButton3).setBackgroundResource(R.drawable.image_button_scroll_selected);

    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override public void onAttach(Activity activity){
        super.onAttach(activity);
        // Verify the host activity implements the callback interface
        try{
            mListener = (NoticeDialogListener)activity;
        }
        catch (ClassCastException e){
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    /**
     * When numberPicker changes values as users scrolls and selects values.. Update time text
     * with new values
     * @param picker - which numberPicker was changed.. either hour or minute
     * @param oldVal
     * @param newVal  - new value that is selected
     */
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        String sentence = new String();
        Integer newValue;
        if(picker.getId() == R.id.hourPicker){
            newValue = newVal;
            if(newVal < 10){
                timeLimitHour = 0 + newValue.toString();
                sentence = "Time Limit: " + timeLimitHour + ":" +timeLimitMinute;
            }
            else {
                timeLimitHour = newValue.toString();
                sentence = "Time Limit: " + timeLimitHour + ":" + timeLimitMinute;
            }
        }
        else if(picker.getId() == R.id.minutePicker){
            newValue = newVal;
            if(newVal < 10){
                timeLimitMinute = 0 + newValue.toString();
                sentence = "Time Limit: " + timeLimitHour + ":" +timeLimitMinute;
            }
            else {
                timeLimitMinute = newValue.toString();
                sentence = "Time Limit: " + timeLimitHour + ":" + timeLimitMinute;
            }
        }

        timeText.setText(sentence);


    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

        Log.d(TAG, "onCheckedChanged: " + getDialog().findViewById(checkedId).toString());
    }
}

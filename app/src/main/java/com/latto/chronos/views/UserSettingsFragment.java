package com.latto.chronos.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.latto.chronos.MainActivity;
import com.latto.chronos.R;


public class UserSettingsFragment extends Fragment {

    private CardView ringToneCardView, reminderTimeCardView, reminderFrequencyCardView, appThemeCardView;
    private TextView ringtoneTextView, reminderTimeTextView, reminderFrequencyTextView, appThemeTextView;
    private AlertDialog ringtoneAlertDialog, reminderTimeAlertDialog, reminderFrequencyAlertDialog, appThemeAlertDialog;
    private boolean isChanged;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        defineViews(view);
        initViews();
        createAlertDialogs();
        defineListeners();

        return view;
    }

    private void defineViews(View view) {
        ringToneCardView = view.findViewById(R.id.UserSettingsFragment_CardView_RingTone);
        reminderTimeCardView = view.findViewById(R.id.UserSettingsFragment_CardView_ReminderTime);
        reminderFrequencyCardView = view.findViewById(R.id.UserSettingsFragment_CardView_ReminderFrequency);
        //appThemeCardView = view.findViewById(R.id.UserSettingsFragment_CardView_AppTheme);

        ringtoneTextView = view.findViewById(R.id.UserSettingsFragment_TextView_DefaultRingtone);
        reminderTimeTextView = view.findViewById(R.id.UserSettingsFragment_TextView_DefaultReminderTime);
        reminderFrequencyTextView = view.findViewById(R.id.UserSettingsFragment_TextView_DefaultReminderFrequency);
        //appThemeTextView = view.findViewById(R.id.UserSettingsFragment_TextView_AppTheme);
    }

    private void initViews() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ringtoneTextView.setText(sp.getString("ringtone", "Consequence"));
        reminderTimeTextView.setText(sp.getString("reminder", getResources().getString(R.string.at_the_time_of_event)));
        reminderFrequencyTextView.setText(sp.getString("frequency", "One-Time"));
        //appThemeTextView.setText(sp.getString("theme", "Indigo"));
    }

    private void createAlertDialogs() {
        ringtoneAlertDialog = createRadioGroupDialog(R.layout.layout_alert_dialog_ringtone, "ringtone", ringtoneTextView, false);
        reminderTimeAlertDialog = createRadioGroupDialog(R.layout.layout_alert_dialog_notification, "reminder", reminderTimeTextView, false);
        reminderFrequencyAlertDialog = createRadioGroupDialog(R.layout.layout_alert_dialog_repeat, "frequency", reminderFrequencyTextView, true);
        appThemeAlertDialog = createRadioGroupDialog(R.layout.layout_alert_dialog_apptheme, "theme", appThemeTextView, true);
    }

    private AlertDialog createRadioGroupDialog(int layoutResId, String key, TextView targetTextView, boolean isFlagged) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getContext()).inflate(layoutResId, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.AlertDialogLayout_RadioGroup);

        // Créer le dialogue ici
        AlertDialog dialog = builder.setView(dialogView).create();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedValue = ((RadioButton) dialogView.findViewById(checkedId)).getText().toString();
            if (!targetTextView.getText().toString().equalsIgnoreCase(selectedValue)) {
                save(key, selectedValue);
                if (isFlagged) {
                    saveFlag("isChanged", true);
                    isChanged = true;
                }
            }
            targetTextView.setText(selectedValue);
            //if (key.equals("theme")) changeTheme();

            // Ici on peut utiliser 'dialog' car il est déjà défini
            dialog.dismiss();
        });

        Button backButton = dialogView.findViewById(R.id.AlertDialogLayout_Button_Back);
        backButton.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void defineListeners() {
        ringToneCardView.setOnClickListener(v -> ringtoneAlertDialog.show());
        reminderTimeCardView.setOnClickListener(v -> reminderTimeAlertDialog.show());
        reminderFrequencyCardView.setOnClickListener(v -> reminderFrequencyAlertDialog.show());
       // appThemeCardView.setOnClickListener(v -> appThemeAlertDialog.show());
    }

    private void changeTheme() {
        if (isChanged) {
            restartApp();
        }
    }

    private void save(String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putString(key, value).apply();
    }

    private void saveFlag(String key, boolean flag) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.edit().putBoolean(key, flag).apply();
    }

    private void restartApp() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
}

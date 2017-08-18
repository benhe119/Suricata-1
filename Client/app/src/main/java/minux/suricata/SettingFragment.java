package minux.suricata;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingFragment extends PreferenceFragment {
    private Context context;
    private DatabaseReference databaseReference;
    private Vibrator vibrator;
    private Preference pEmail, pUpdateTime, pVersion;
    private SwitchPreference spNotification, spVibration;
    private RingtonePreference rpRingtone;
    private ListPreference lpUpdateDay;
    private Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "notification":
                    notification = (Boolean) newValue;
                    if (notification && vibration) {
                        vibrator.vibrate(375);
                    }
                    break;
                case "ringtone":
                    ringtone = (String) newValue;
                    break;
                case "vibration":
                    vibration = (Boolean) newValue;
                    if (vibration) {
                        vibrator.vibrate(375);
                    }
                    break;
                case "updateday":
                    updateDay = (String) newValue;
                    break;
            }
            databaseReference.setValue(new PreferencesClass(email, notification, ringtone, vibration, updateTime, updateDay));
            updateSummary();
            return true;
        }
    };
    private Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(final Preference preference) {
            switch (preference.getKey()) {
                case "updatetime":
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hour = String.valueOf(hourOfDay);
                            String min = String.valueOf(minute);
                            if (hourOfDay < 10) {
                                hour = "0" + hour;
                            }
                            if (minute < 10) {
                                min = "0" + min;
                            }
                            updateTime = hour + ":" + min;
                            preference.setSummary(updateTime);
                            databaseReference.setValue(new PreferencesClass(email, notification, ringtone, vibration, updateTime, updateDay));
                        }
                    }, 0, 0, false).show();
                    break;
                case "reset":
                    new AlertDialog.Builder(context)
                            .setTitle("설정을 초기화하시겠습니까?")
                            .setMessage("초기화 이후 설정을 다시 복원할 수 없습니다.")
                            .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    notification = true;
                                    ringtone = null;
                                    vibration = true;
                                    updateTime = null;
                                    updateDay = null;
                                    databaseReference.setValue(new PreferencesClass(email, true, null, true, null, null));
                                    vibrator.vibrate(375);
                                    updateSummary();
                                }
                            })
                            .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                    break;
                case "version":
                    Toast.makeText(context, "만든이 : 이민욱", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    };
    private ValueEventListener valueEventListener = new ValueEventListener() { // Firebase로 부터 설정을 받아오는 리스너
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            PreferencesClass preferences = dataSnapshot.getValue(PreferencesClass.class);
            email = preferences.getEmail();
            notification = preferences.getNotification();
            vibration = preferences.getVibration();
            ringtone = preferences.getRingtone();
            updateTime = preferences.getUpdateTime();
            updateDay = preferences.getUpdateDay();
            updateSummary();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private String email, ringtone, updateTime, updateDay;
    private Boolean notification, vibration;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        context = SettingFragment.this.getContext();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(getUid()).child("preference");
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        findPreferences();
        setOnPreferenceListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
    }

    private String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void findPreferences() {
        pEmail = getPreferenceScreen().findPreference("email");
        spNotification = (SwitchPreference) getPreferenceScreen().findPreference("notification");
        rpRingtone = (RingtonePreference) getPreferenceScreen().findPreference("ringtone");
        spVibration = (SwitchPreference) getPreferenceScreen().findPreference("vibration");
        pUpdateTime = getPreferenceScreen().findPreference("updatetime");
        lpUpdateDay = (ListPreference) getPreferenceScreen().findPreference("updateday");
        pVersion = getPreferenceScreen().findPreference("version");
    }

    private void setOnPreferenceListeners() {
        spNotification.setOnPreferenceChangeListener(onPreferenceChangeListener);
        rpRingtone.setOnPreferenceClickListener(onPreferenceClickListener);
        rpRingtone.setOnPreferenceChangeListener(onPreferenceChangeListener);
        spVibration.setOnPreferenceChangeListener(onPreferenceChangeListener);
        pUpdateTime.setOnPreferenceClickListener(onPreferenceClickListener);
        pUpdateTime.setOnPreferenceChangeListener(onPreferenceChangeListener);
        lpUpdateDay.setOnPreferenceChangeListener(onPreferenceChangeListener);
        pVersion.setOnPreferenceClickListener(onPreferenceClickListener);

        getPreferenceScreen().findPreference("reset").setOnPreferenceClickListener(onPreferenceClickListener);
    }

    private void updateSummary() {
        pEmail.setSummary(email);
        if (notification == null) {
            notification = false;
        }
        spNotification.setChecked(notification);
        if (ringtone == null) {
            rpRingtone.setSummary("설정 안됨");
        } else if (ringtone.equals("")) {
            rpRingtone.setSummary("무음");
        } else {
            rpRingtone.setSummary(RingtoneManager.getRingtone(context, Uri.parse(ringtone)).getTitle(context));
        }
        if (vibration == null) {
            vibration = false;
        }
        spVibration.setChecked(vibration);
        if (updateTime == null) {
            pUpdateTime.setSummary("설정 안됨");
        } else {
            pUpdateTime.setSummary(updateTime);
        }
        if (updateDay == null) {
            lpUpdateDay.setSummary("설정 안됨");
        } else {
            lpUpdateDay.setSummary(change2Korean(updateDay));
        }
        try {
            pVersion.setSummary(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }

    private String change2Korean(String dayOfEnglish) {
        switch (dayOfEnglish) {
            case "sunday":
                return "일요일";
            case "monday":
                return "월요일";
            case "tuesday":
                return "화요일";
            case "wednesday":
                return "수요일";
            case "thursday":
                return "목요일";
            case "friday":
                return "금요일";
            default:
                return "토요일";
        }
    }
}

class PreferencesClass {
    private String email;
    private Boolean notification;
    private String ringtone;
    private Boolean vibration;
    private String updateTime;
    private String updateDay;

    public PreferencesClass() {

    }

    public PreferencesClass(String email, Boolean notification, String ringtone, Boolean vibration, String updateTime, String updateDay) {
        this.email = email;
        this.notification = notification;
        this.ringtone = ringtone;
        this.vibration = vibration;
        this.updateTime = updateTime;
        this.updateDay = updateDay;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getNotification() {
        return notification;
    }

    public String getRingtone() {
        return ringtone;
    }

    public Boolean getVibration() {
        return vibration;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getUpdateDay() {
        return updateDay;
    }
}
package minux.suricata;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class BaseFragment extends Fragment {
    public DatabaseReference databaseReference;
    public ValueEventListener velPreference = new ValueEventListener() { // 알림 설정을 확인하는 리스너
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            PreferencesClass preferencesClass = dataSnapshot.getValue(PreferencesClass.class);
            notification = preferencesClass.getNotification();
            ringtone = preferencesClass.getRingtone();
            vibration = preferencesClass.getVibration();
            if (notification == null) {
                notification = false;
            }
            if (ringtone == null) {
                ringtone = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_NOTIFICATION).toString();
            }
            if (vibration == null) {
                vibration = false;
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private Boolean notification, vibration;
    private String ringtone;
    public ChildEventListener celMessage = new ChildEventListener() { // 새로운 메시지 수신 시 Fragment에 메시지를 추가하는 리스너
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            MessageClass messageClass = dataSnapshot.getValue(MessageClass.class);
            String date, time, priority, title, content;
            date = messageClass.getDate();
            time = messageClass.getTime();
            priority = messageClass.getPriority();
            title = messageClass.getTitle();
            content = messageClass.getContent();
            try {
                addMessage(date, time, priority, title, content);
                showNotification(setFragmentName(priority));
            } catch (Exception ignored) {

            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private int fragmentNameFlag;
    public ScrollView scrollView;
    public LinearLayout linearLayout;

    private void addMessage(String date, String time, String priority, String title, String content) {
        LayoutInflater inflaterLayout = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflaterLayout.inflate(R.layout.class_message, null);

        ((ImageView) view.findViewById(R.id.iv_message_priority)).setImageDrawable(setIcon(priority, title));
        ((TextView) view.findViewById(R.id.tv_message_title)).setText(title);
        ((TextView) view.findViewById(R.id.tv_message_time)).setText(setMeridiem(time));
        ((TextView) view.findViewById(R.id.tv_message_content)).setText(content);

        scrollView.removeView(linearLayout);
        linearLayout.addView(view, 0);
        scrollView.addView(linearLayout);
    }

    private Drawable setIcon(String priority, String title) {
        switch (priority) {
            case "0": // 시스템 메시지
                if (title.equals("Notice"))
                    return ContextCompat.getDrawable(getActivity(), R.drawable.ic_black_notification);
                if (title.equals("Error"))
                    return ContextCompat.getDrawable(getActivity(), R.drawable.ic_black_error);
            case "1":
                return ContextCompat.getDrawable(getActivity(), R.drawable.ic_black_one);
            case "2":
                return ContextCompat.getDrawable(getActivity(), R.drawable.ic_black_two);
            case "3":
                return ContextCompat.getDrawable(getActivity(), R.drawable.ic_black_three);
        }
        return null;
    }

    private String setMeridiem(String time) { // 오전, 오후 판단하는 함수
        int hour = Integer.parseInt(time.substring(0, 2));
        String minute = time.substring(3, 5);
        if (hour > 12) {
            hour = hour - 12;
            return "오후 " + String.valueOf(hour) + ":" + minute;
        } else {
            return "오전 " + String.valueOf(hour) + ":" + minute;
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void showNotification(String fragmentName) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_white_notification)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(fragmentName)
                .setContentText("새로운 메시지가 도착했습니다.")
                .setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        if (notification) {
            notificationBuilder.setSound(Uri.parse(ringtone));
            if (vibration) {
                notificationBuilder.setVibrate(new long[]{625, 375}); // delay, vibrate
            }
        }
        ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).notify(fragmentNameFlag, notificationBuilder.build());
    }

    private String setFragmentName(String priority) { // 시스템 메시지인지 공격 탐지 메시지인지 판단하는 함수
        if (priority.equals("0")) {
            fragmentNameFlag = 0;
            return "시스템";
        } else {
            fragmentNameFlag = 1;
            return "공격 탐지";
        }
    }
}

class MessageClass {
    private String date;
    private String time;
    private String priority;
    private String title;
    private String content;

    public MessageClass() {

    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
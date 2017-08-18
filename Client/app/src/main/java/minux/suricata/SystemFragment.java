package minux.suricata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.firebase.database.FirebaseDatabase;

public class SystemFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("message").child("system").addChildEventListener(celMessage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_system, container, false);
        scrollView = (ScrollView) view.findViewById(R.id.sv_system);
        linearLayout = (LinearLayout) view.findViewById(R.id.ll_system);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseReference.child("user").child(getUid()).child("preference").addListenerForSingleValueEvent(velPreference);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.child("message").child("system").removeEventListener(celMessage);
        databaseReference.child("user").child(getUid()).child("preference").removeEventListener(velPreference);
    }
}
package minux.suricata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;

public class StatisticsFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("message").child("detection").addChildEventListener(celMessage);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.child("message").child("detection").removeEventListener(celMessage);
    }
}
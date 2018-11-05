package top.geek_studio.chenlongcould.musicplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PublicFragment extends Fragment {

    //实例化一个fragment
    public static PublicFragment newInstance(int index) {
        PublicFragment myFragment = new PublicFragment();

        Bundle bundle = new Bundle();
        //传递参数
        // TODO: 2018/9/28
        bundle.putInt(Values.INDEX, index);
        myFragment.setArguments(bundle);
        return myFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_fragment_layout, container, false);

        TextView textView = view.findViewById(R.id.text_view_frag);

        if (this.getArguments() != null) {
            textView.setText(String.valueOf(this.getArguments().getInt(Values.INDEX, -1)));
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

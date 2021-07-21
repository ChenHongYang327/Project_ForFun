package idv.tfp10105.project_forfun.discussionboard.disboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.bean.Post;

public class discussionBoard_RentSeeking_ListFragment extends Fragment {
    private Post post;
    private RecyclerView rv_rentseekinglist;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        post = (Post) (getArguments() != null ? getArguments().getSerializable("post") : null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discussion_board__rent_seeking__list, container, false);
        rv_rentseekinglist = view.findViewById(R.id.rv_rentseekinglist);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
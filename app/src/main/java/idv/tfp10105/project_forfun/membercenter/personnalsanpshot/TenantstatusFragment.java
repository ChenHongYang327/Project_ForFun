package idv.tfp10105.project_forfun.membercenter.personnalsanpshot;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.common.Common;
import idv.tfp10105.project_forfun.common.RemoteAccess;
import idv.tfp10105.project_forfun.common.bean.Member;
import idv.tfp10105.project_forfun.common.bean.PersonEvaluation;
import idv.tfp10105.project_forfun.membercenter.personnalsanpshot.adapter.PersonnalAdapter;

public class TenantstatusFragment extends Fragment {
    private Activity activity;
    private List<PersonEvaluation> personEvaluations  =new ArrayList<>();
    private Member user,selectUser;
    private RecyclerView rvTen;
    private final String url = Common.URL +"personalSnapshot";
    public TenantstatusFragment(Member user, Member selectUser) {
        this.user = user;
        this.selectUser = selectUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=getActivity();
        //假資料
//        selectUser=new Member();
//        selectUser.setMemberId(2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_tenantstatus, container, false);
        findView(view);
        fakedata();
        handleTenData();
        return view;
    }

    private void fakedata() {
        PersonEvaluation fake1=new PersonEvaluation();
        PersonEvaluation fake2=new PersonEvaluation();
        PersonEvaluation fake3=new PersonEvaluation();
        fake1.setPersonComment("還不錯");
        fake1.setPersonStar(3);
        fake1.setCommented(3);
        fake1.setCommentedBy(2);
        fake1.setCreateTime(new Timestamp(System.currentTimeMillis()));
        fake2.setPersonComment("還行");
        fake2.setPersonStar(4);
        fake2.setCommented(3);
        fake2.setCommentedBy(2);
        fake2.setCreateTime(new Timestamp(System.currentTimeMillis()));
        fake3.setPersonComment("很棒");
        fake3.setPersonStar(2);
        fake3.setCommented(3);
        fake3.setCommentedBy(2);
        fake3.setCreateTime(new Timestamp(System.currentTimeMillis()));
        personEvaluations.add(fake1);
        personEvaluations.add(fake2);
        personEvaluations.add(fake3);
        rvTen.setLayoutManager(new LinearLayoutManager(activity));
        rvTen.setAdapter(new PersonnalAdapter(activity, activity, personEvaluations));

    }

    private void findView(View view) {
        rvTen=view.findViewById(R.id.rvTen);
    }

    private void handleTenData() {
        //連線
//        if (RemoteAccess.networkCheck(activity)) {
//            JsonObject client = new JsonObject();
//            client.addProperty("action", "TenantStatusEvaluation");
//            client.addProperty("commentedID",selectUser.getMemberId());
//            String resp = RemoteAccess.getJsonData(url,client.toString());
//            if(resp!=null){
//                if (personEvaluations.size() > 0) {
//                    Type listType= new TypeToken<List<PersonEvaluation>>(){}.getType();
//                    personEvaluations=new Gson().fromJson(resp,listType);
//                    rvTen.setLayoutManager(new LinearLayoutManager(activity));
//                    rvTen.setAdapter(new PersonnalAdapter(activity, activity, personEvaluations));
//                } else {
//                    //沒資料要執行的動作
//                }
//            }
//            else{
//                //沒資料要執行的動作
//            }
//
//        }
    }
}
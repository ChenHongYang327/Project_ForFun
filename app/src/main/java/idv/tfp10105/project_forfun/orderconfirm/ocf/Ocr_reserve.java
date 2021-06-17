package idv.tfp10105.project_forfun.orderconfirm.ocf;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import idv.tfp10105.project_forfun.R;

public class Ocr_reserve extends Fragment {
    private AppCompatActivity activity;
    private RecyclerView recyclerView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ocr_reserve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycleview_ocr_reserve);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new MyAdaptor(activity,getlist()));
    }

    private class MyAdaptor extends RecyclerView.Adapter<MyAdaptor.Holder>{
        private List<Book> booksList;
        private Context context;

        public MyAdaptor(Context context,List<Book> booksList){
            this.context = context;
            this.booksList = booksList;
        }


        class Holder extends RecyclerView.ViewHolder{
            ImageView imgPic;
            TextView tvTitle, tvArea, tvControlText;
            Button btClick;

            public Holder(@NonNull @NotNull View itemView) {
                super(itemView);
                imgPic = itemView.findViewById(R.id.img_ocrItrmRV_pic);
                tvTitle = itemView.findViewById(R.id.tv_ocrItrmRV_Title);
                tvArea = itemView.findViewById(R.id.tv_ocrItrmRV_Area);
                tvControlText = itemView.findViewById(R.id.tv_ocrItrmRV_con1);
                tvControlText.setText("待確認");
                itemView.findViewById(R.id.bt_ocrItrmRV_con1).setOnClickListener(v->{
                    Bundle bundle = new Bundle();
                    bundle.putInt("ocr",1);
                    Navigation.findNavController(v).navigate(R.id.action_orderconfirm_mainfragment_to_orderconfirm_houseSnapshot,bundle);
                });
            }

        }

        @Override
        public int getItemCount() {
            return booksList == null ? 0 : booksList.size();
        }

        @NonNull
        @NotNull
        @Override
        public Holder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View itemview = LayoutInflater.from(activity).
                    inflate(R.layout.ocr_item_rvview,parent,false);
            return new Holder(itemview);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull Ocr_reserve.MyAdaptor.Holder holder, int position) {

            final Book book = booksList.get(position);
            if(book.getImageId() == null){
                holder.imgPic.setImageResource(R.drawable.bt_9044blank);
            }else {
                holder.imgPic.setImageResource(R.drawable.bt_9044blank);
            }

            holder.tvTitle.setText(book.getTitle());
            holder.tvArea.setText(book.getTitle());
        }
    }

    //fake
    private List<Book> getlist(){
        List<Book> books = new ArrayList<>();
        books.add(new Book(R.drawable.bt_9044blank,"222"));
        books.add(new Book(R.drawable.bt_14050blank,"456"));
        books.add(new Book(R.drawable.bt_36045blank,"789"));
        return books;
    }

}
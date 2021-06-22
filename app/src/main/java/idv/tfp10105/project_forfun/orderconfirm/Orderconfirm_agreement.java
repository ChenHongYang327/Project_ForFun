package idv.tfp10105.project_forfun.orderconfirm;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import idv.tfp10105.project_forfun.R;

public class Orderconfirm_agreement extends Fragment {
    private AppCompatActivity activity;

    private TextView tvDateStart, tvDateEnd, tvRent, tvSignHO, tvSignCus;
    private ImageView btConfirm, btCancel, imgSignHO, imgSignCus;
    private Bitmap bitmapSignHO, bitmapSign;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orderconfirm_agreement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //參照元件
        tvDateStart = view.findViewById(R.id.tv_ocrAgreement_dateStart);
        tvDateEnd = view.findViewById(R.id.tv_ocrAgreement_dateEnd);
        tvRent = view.findViewById(R.id.tv_ocrAgreement_rent);
        tvSignHO = view.findViewById(R.id.tv_ocrAgreement_HOCanvas_Text);
        tvSignCus = view.findViewById(R.id.tv_ocrAgreement_Canvas_Text);
        btCancel = view.findViewById(R.id.bt_ocrAgreement_cancel);
        btConfirm = view.findViewById(R.id.bt_ocrAgreement_confirm);
        imgSignHO = view.findViewById(R.id.imgview_ocrAgreement_HOCanvas);
        imgSignCus = view.findViewById(R.id.imgview_ocrAgreement_Canvas);

        imgSignCus.setEnabled(false);


        //選擇日期 (對話框)
        handleDate();

        //電子簽名
        handleSign();

        //確認按鈕(存擋到外面)
        handleBtConfirm();

        //取消按鈕，返回上一頁（房屋快照OR瀏覽）
        btCancel.setOnClickListener(v->{
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void handleDate() {

        tvDateStart.setOnClickListener(v->{
            Toast.makeText(activity,"DateEnd",Toast.LENGTH_SHORT).show();
        });

        tvDateEnd.setOnClickListener(v->{
            Toast.makeText(activity,"DateEnd",Toast.LENGTH_SHORT).show();
        });
    }

    private void handleSign() {

        //房東簽名
        imgSignHO.setOnClickListener(v->{
            final SignatureView signatureView = new SignatureView(activity, null);
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MaterialAlertDialog_MaterialComponents_Title_Icon);
            builder.setTitle("AgreementSign")
                    .setView(signatureView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bitmapSignHO = signatureView.getContentDataURI();
                            imgSignHO.setImageBitmap(bitmapSignHO);
                            tvSignHO.setText("");
                        }
                    })
                    .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

        });

        //房客簽名
        imgSignCus.setOnClickListener(v->{
            final SignatureView signatureView = new SignatureView(activity, null);
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.MaterialAlertDialog_MaterialComponents_Title_Icon);
            builder.setTitle("AgreementSign")
                    .setView(signatureView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bitmapSign = signatureView.getContentDataURI();
                            imgSignCus.setImageBitmap(bitmapSign);
                        }
                    })
                    .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        });

    }

    private void handleBtConfirm() {

        btConfirm.setOnClickListener(v->{
            Toast.makeText(activity,"comfirm",Toast.LENGTH_SHORT).show();
        });

    }


}
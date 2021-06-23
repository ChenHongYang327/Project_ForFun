package idv.tfp10105.project_forfun.orderconfirm;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        findView(view);

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

    private void findView(View view) {
        tvDateStart = view.findViewById(R.id.tv_ocrAgreement_dateStart);
        tvDateEnd = view.findViewById(R.id.tv_ocrAgreement_dateEnd);
        tvRent = view.findViewById(R.id.tv_ocrAgreement_rent);
        tvSignHO = view.findViewById(R.id.tv_ocrAgreement_HOCanvas_Text);
        tvSignCus = view.findViewById(R.id.tv_ocrAgreement_Canvas_Text);
        btCancel = view.findViewById(R.id.bt_ocrAgreement_cancel);
        btConfirm = view.findViewById(R.id.bt_ocrAgreement_confirm);
        imgSignHO = view.findViewById(R.id.imgview_ocrAgreement_HOCanvas);
        imgSignCus = view.findViewById(R.id.imgview_ocrAgreement_Canvas);
    }

    private void handleDate() {

        tvDateStart.setOnClickListener(v->{
            Calendar m_Calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            DatePickerDialog.OnDateSetListener datepicker = (vew, year, month, dayOfMonth) -> {
                m_Calendar.set(Calendar.YEAR, year);
                m_Calendar.set(Calendar.MONTH, month);
                m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvDateStart.setText(sdf.format(m_Calendar.getTime()));
            };
            DatePickerDialog dialog = new DatePickerDialog(activity,
                    datepicker,
                    m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH),
                    m_Calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);

        });

        tvDateEnd.setOnClickListener(v->{
            Calendar m_Calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN);
            DatePickerDialog.OnDateSetListener datepicker = (vew, year, month, dayOfMonth) -> {
                m_Calendar.set(Calendar.YEAR, year);
                m_Calendar.set(Calendar.MONTH, month);
                m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvDateEnd.setText(sdf.format(m_Calendar.getTime()));
            };
            DatePickerDialog dialog = new DatePickerDialog(activity,
                    datepicker,
                    m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH),
                    m_Calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        });
    }

    private void handleSign() {

        //房東簽名
        imgSignHO.setOnClickListener(v->{
            final SignatureView signatureView = new SignatureView(activity, null);
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_Design_BottomSheetDialog);
            builder.setTitle("簽名板")
                    .setView(signatureView)
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bitmapSignHO = signatureView.getContentDataURI();
                            imgSignHO.setImageBitmap(bitmapSignHO);
                            tvSignHO.setText("");
                        }
                    })
                    .setNeutralButton("返回", new DialogInterface.OnClickListener() {
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
            androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_Design_BottomSheetDialog);
            builder.setTitle("簽名板")
                    .setView(signatureView)
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bitmapSign = signatureView.getContentDataURI();
                            imgSignCus.setImageBitmap(bitmapSign);
                        }
                    })
                    .setNeutralButton("返回", new DialogInterface.OnClickListener() {
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
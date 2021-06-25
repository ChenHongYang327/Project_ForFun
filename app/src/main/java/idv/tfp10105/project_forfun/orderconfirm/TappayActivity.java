package idv.tfp10105.project_forfun.orderconfirm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import idv.tfp10105.project_forfun.R;
import tech.cherri.tpdirect.api.TPDGooglePay;

public class TappayActivity extends AppCompatActivity {

    //自訂識別碼
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 101;
    private static final String TAG = "TAG_TapPayActivity";

    public static final String TAPPAY_DOMAIN_SANDBOX = "https://sandbox.tappaysdk.com/";
    public static final String TAPPAY_PAY_BY_PRIME_URL = "tpc/payment/pay-by-prime";
    public ProgressDialog mProgressDialog;
    private TPDGooglePay tpdGooglePay;

    private ImageView btBuy, btCancel, btReturn, btConfirm, imgPic;
    private TextView tvAccount, tvNotes, tvCardInfo, tvResult;
    private TextView tvReturnText, tvConfirmText, tvancelText;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tappay);
        btBuy = findViewById(R.id.bt_ocrTapPay_toBuy);
        btCancel = findViewById(R.id.bt_ocrTapPay_cancel);
        btConfirm = findViewById(R.id.bt_ocrTapPay_confirm);
        btReturn = findViewById(R.id.bt_ocrTapPay_return);
        imgPic = findViewById(R.id.img_ocrTapPay_pic);
        tvAccount = findViewById(R.id.tv_ocrTapPay_account);
        tvCardInfo = findViewById(R.id.tv_ocrTapPay_cardinfo);
        tvNotes = findViewById(R.id.tv_ocrTapPay_notes);
        tvResult = findViewById(R.id.tv_ocrTapPay_result);
        tvancelText = findViewById(R.id.tv_ocrTapPay_cancelText);
        tvConfirmText = findViewById(R.id.tv_ocrTapPay_confirmText);
        tvReturnText = findViewById(R.id.tv_ocrTapPay_returnText);





    }


}
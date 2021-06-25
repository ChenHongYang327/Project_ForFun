package idv.tfp10105.project_forfun.orderconfirm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10105.project_forfun.R;
import idv.tfp10105.project_forfun.orderconfirm.tappayNetwork.RemoteAccess_TapPay;
import tech.cherri.tpdirect.api.TPDCard;
import tech.cherri.tpdirect.api.TPDConsumer;
import tech.cherri.tpdirect.api.TPDGooglePay;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;

public class TappayActivity extends AppCompatActivity {
    private static final String TAG = "TAG_TapPayActivity";

    //自訂識別碼
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 101;

    //google＆tappay 相關宣告
    public static final String TAPPAY_DOMAIN_SANDBOX = "https://sandbox.tappaysdk.com/";
    public static final String TAPPAY_PAY_BY_PRIME_URL = "tpc/payment/pay-by-prime";
    public ProgressDialog mProgressDialog;
    private TPDGooglePay tpdGooglePay;
    private PaymentData paymentData;

    private ImageView btBuy, btCancel, btReturn, btConfirm, imgPic; //button用圖片表示
    private TextView tvAccount, tvNotes, tvCardInfo, tvResult;
    private TextView tvReturnText, tvConfirmText, tvancelText;

    //設定信用卡類別
    public static final TPDCard.CardType[] CARD_TYPES = new TPDCard.CardType[]{
            TPDCard.CardType.Visa
            , TPDCard.CardType.MasterCard
            , TPDCard.CardType.JCB
            , TPDCard.CardType.AmericanExpress
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tappay);
        //宣告元件參照
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

        // TODO: 判斷誰登入


        handleViews();

        TPDSetup.initInstance(this,
                Integer.parseInt(getString(R.string.TapPay_AppID)),
                getString(R.string.TapPay_AppKey),
                TPDServerType.Sandbox);

        prepareGooglePay();

    }

    private void handleViews() {
        btBuy.setEnabled(false);
        btBuy.setOnClickListener(v->{
            // 跳出user資訊視窗讓user確認，確認後會呼叫onActivityResult()
            tpdGooglePay.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    // 消費總金額
                    .setTotalPrice("10")
                    // 設定幣別
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
        });

        //確認按鈕事件
        btConfirm.setEnabled(false);
        btConfirm.setOnClickListener(v->{
            getPrimeFromTapPay(paymentData);
        });

        //取消按鈕事件
        btCancel.setOnClickListener(v->{
            Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
            //TODO: cancel navugation
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MSG", String.valueOf(requestCode));

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    btConfirm.setEnabled(true);
                    // 取得支付資訊
                    paymentData = PaymentData.getFromIntent(data);
                    if (paymentData != null) {
                        // 取得支付資訊中的信用卡資訊並顯示
                        showCardInfo(paymentData);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    btConfirm.setEnabled(false);
                    // tvResult.setText(R.string.textCanceled);
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    btConfirm.setEnabled(false);
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    if (status != null) {
                        String text = "status code: " + status.getStatusCode() +
                                " , message: " + status.getStatusMessage();
                        Log.d(TAG, text);
                        tvResult.setText(text);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void prepareGooglePay() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        // 設定商店名稱
        tpdMerchant.setMerchantName(getString(R.string.TapPay_MerchantName));
        // 設定允許的信用卡種類
        tpdMerchant.setSupportedNetworks(CARD_TYPES);
        // 設定客戶填寫項目
        TPDConsumer tpdConsumer = new TPDConsumer();
        // 不需要電話號碼
        tpdConsumer.setPhoneNumberRequired(false);
        // 不需要運送地址
        tpdConsumer.setShippingAddressRequired(false);
        // 不需要Email
        tpdConsumer.setEmailRequired(false);

        tpdGooglePay = new TPDGooglePay(this, tpdMerchant, tpdConsumer);
        // 檢查user裝置是否支援Google Pay
        tpdGooglePay.isGooglePayAvailable((isReadyToPay, msg) -> {
            Log.d(TAG, "Pay with Google availability : " + isReadyToPay);
            if (isReadyToPay) {
                btBuy.setEnabled(true);
            } else {
                btBuy.setEnabled(false);
                //       tvResult.setText(R.string.textCannotUseGPay);
            }
        });
    }

    /**
     * 只取得支付資訊當中的信用卡資訊並顯示
     */
    private void showCardInfo(PaymentData paymentData) {
        Gson gson = new Gson();
        //可以把 paymentData.toJson() 列印出來，看裡面有哪些值，此範例只印出新用卡資訊
        JsonObject paymentDataJO = gson.fromJson(paymentData.toJson(), JsonObject.class);
        String cardDescription = paymentDataJO.get("paymentMethodData").getAsJsonObject()
                .get("description").getAsString();
        tvCardInfo.setText(cardDescription);
    }

    private void getPrimeFromTapPay(PaymentData paymentData) {
        //繞圈圈ｕｉ元件
        showProgressDialog();
        /* 呼叫getPrime()只將支付資料提交給TapPay以取得prime (代替卡片的一次性字串，此字串的時效為 30 秒)，
            參看https://docs.tappaysdk.com/google-pay/zh/reference.html#prime */
        /* 一般而言，手機提交支付、信用卡資料給TapPay後，TapPay會將信用卡等資訊送至Bank確認是否合法，
               Bank會回一個暫時編號給TapPay方便後續支付確認，TapPay儲存該編號後再回一個自編prime給手機，
               手機再傳給server，server再呼叫payByPrime方法提交給TapPay，以確認這筆訂單，
               此時server就可發訊息告訴user訂單成立。
               參看圖示 https://docs.tappaysdk.com/google-pay/zh/home.html#home 向下捲動即可看到 */
        tpdGooglePay.getPrime(
                paymentData,
                //called back 成功
                (prime, tpdCardInfo, referenceInfo) -> {

                    Log.d("Prime",prime);

                    hideProgressDialog();

                    String text = "Your prime is " + prime + "\n\n"
                            /* 手機得到prime後，一般會傳給商家server端再呼叫payByPrime方法提交給TapPay，以確認這筆訂單
                               現在為了方便，手機直接提交給TapPay */
                            + generatePayByPrimeForSandBox(prime,
                            getString(R.string.TapPay_PartnerKey),
                            getString(R.string.TapPay_MerchantID));
                    Log.d(TAG, text);
                    tvResult.setText(text);
                },
                //called back 失敗
                (status, reportMsg) -> {
                    Log.d("PrimeNG",reportMsg);

                    hideProgressDialog();
                    String text = "TapPay getPrime failed. status: " + status + ", message: " + reportMsg;
                    Log.d(TAG, text);
                    //tvResult.setText(text);
                });
    }

    // 將交易資訊送至TapPay測試區
    public static String generatePayByPrimeForSandBox(String prime, String partnerKey, String merchantId) {
        JsonObject paymentJO = new JsonObject();
        paymentJO.addProperty("partner_key", partnerKey);
        paymentJO.addProperty("prime", prime);
        paymentJO.addProperty("merchant_id", merchantId);
        paymentJO.addProperty("amount", 10);
        paymentJO.addProperty("currency", "TWD");
        paymentJO.addProperty("order_number", "SN0001");
        paymentJO.addProperty("details", "茶葉蛋1顆");

        //自己輸入的，目的把一些資訊抓下來，幫使用者少填一些東西
        JsonObject cardHolderJO = new JsonObject();
        cardHolderJO.addProperty("name", "Ron");
        cardHolderJO.addProperty("phone_number", "+886912345678");
        cardHolderJO.addProperty("email", "ron@email.com");

        paymentJO.add("cardholder", cardHolderJO);

        // TapPay測試區網址
        String url = TAPPAY_DOMAIN_SANDBOX + TAPPAY_PAY_BY_PRIME_URL;
        return RemoteAccess_TapPay.getRemoteData(url, paymentJO.toString(), partnerKey);
    }


    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
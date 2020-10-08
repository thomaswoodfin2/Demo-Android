package com.pinpoint.appointment.helper;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.utils.MyLifecycleHandler;


public class ToastHelper {
    private static int duration = Toast.LENGTH_SHORT;
    private static Toast toast = null;

    public static void displayInfo(final String message) {
        if (!message.isEmpty()) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(BaseApplication.getInstance(), message, duration);
            toast.show();

        }
    }

    public static void displayCustomToast(String message)
    {
        if (message != null && !message.isEmpty() && MyLifecycleHandler.isApplicationVisible())
        {
            View mView = LayoutInflater.from(BaseApplication.getInstance()).inflate(R.layout.item_custom_toast, null);
            TextView mTxtMessage = GenericView.findViewById(mView, R.id.tv_txtMessage);
            mTxtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
            mTxtMessage.setText(message);
            toast = new Toast(BaseApplication.getInstance());
            toast.setView(mView);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static void hideToast()
    {
        try {
            if (toast != null) {
                toast.cancel();

            }
        }catch (Exception ex)
        {

        }
    }

//    public static void displayCustomDialog(Activity mContext, String message) {
//        if (message != null && !message.isEmpty()) {
//            final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
//            View view = LayoutInflater.from(mContext).inflate(R.layout.item_custom_dialog_toast, null);
//
//            TextView mTxtTitle = GenericView.findViewById(view, R.id.tv_txtTitle);
//            TextView mTxtMessage = GenericView.findViewById(view, R.id.tv_txtMessage);
//
//            Button mBtnButton2 = GenericView.findViewById(view, R.id.bt_btnButton2);
//            LinearLayout mLinearButton = GenericView.findViewById(view, R.id.ll_linearButton);
//
//            mTxtTitle.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//            mTxtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//
//            mBtnButton2.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//            mTxtTitle.setText(MessageHelper.getInstance().getAppMessage(mContext.getString(R.string.str_custom_toast_title)));
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                mTxtMessage.setText(Html.fromHtml("<font >" + message + "</font>", Html.FROM_HTML_MODE_LEGACY));
//            } else {
//                mTxtMessage.setText(Html.fromHtml("<font >" + message + "</font>"));
//            }
//
//            mTxtMessage.setText(message);
//            mBtnButton2.setText(MessageHelper.getInstance().getAppMessage(mContext.getString(R.string.str_ok)));
//
//            mBtnButton2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    dialog.dismiss();
//                }
//            });
//            dialog.setCanceledOnTouchOutside(true);
//            dialog.setContentView(view);
//            dialog.show();
//        }
//    }


//    public static void displayCustomDialogForValidation(final Activity mContext, String message, final View mView) {
//
//        final Dialog dialog = new Dialog(mContext, R.style.DialogTheme);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.item_custom_dialog_toast, null);
//
//        TextView mTxtTitle = GenericView.findViewById(view, R.id.tv_txtTitle);
//        TextView mTxtMessage = GenericView.findViewById(view, R.id.tv_txtMessage);
//
//
//        Button mBtnButton2 = GenericView.findViewById(view, R.id.bt_btnButton2);
//        LinearLayout mLinearButton = GenericView.findViewById(view, R.id.ll_linearButton);
//
//        mTxtTitle.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//        mTxtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
//
//        mBtnButton2.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
//        mTxtTitle.setText(MessageHelper.getInstance().getAppMessage(mContext.getString(R.string.str_custom_toast_title)));
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//
//            mTxtMessage.setText(Html.fromHtml("<font >" + message + "</font>", Html.FROM_HTML_MODE_LEGACY));
//        } else {
//
//            mTxtMessage.setText(Html.fromHtml("<font >" + message + "</font>"));
//
//        }
//        mTxtMessage.setText(message);
//        mBtnButton2.setText(MessageHelper.getInstance().getAppMessage(mContext.getString(R.string.str_ok)));
//
//        mBtnButton2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.dismiss();
//                if (mView != null) {
//                    mView.requestFocus();
//                    Utils.getInstance().launchKeyboard(mContext, mView);
//                }
//            }
//        });
//        dialog.setCanceledOnTouchOutside(true);
//        dialog.setContentView(view);
//        dialog.show();
//
//    }
}
package com.pinpoint.appointment.customviews;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.enumeration.CalendarDateSelection;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.DataObserver;

import java.util.Calendar;

public class CustomDialog {

    private static CustomDialog instance;
    private Dialog dialog;
    private static final int PREV_MONTH = 1;

    //constructor
    private CustomDialog() {
    }

    public static CustomDialog getInstance() {
        if (instance == null) {
            instance = new CustomDialog();
        }
        return instance;
    }

    public void showProgressBar(Context context) {
        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.custom_progressbar);

        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        dialog = new Dialog(context, R.style.DialogTheme);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setContentView(R.layout.custom_progressbar);
//
//        try {
//            if (dialog != null) {
//                if (!dialog.isShowing()) {
//                    dialog.show();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void hide() {
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    public boolean isDialogShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void showAlert(final Context context, String msg, String header, String positiveButton, String negativeButton, boolean isShowNegative) {

        dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
        TextView txtHeader = view.findViewById(R.id.txtHeader);
        TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
        TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
        Button btn_positive = view.findViewById(R.id.btn_positive);
        txtHeader.setText(header);
        txtMessage.setText(msg);
        btn_positive.setText(positiveButton);
        txtNegative.setText(negativeButton);
        if (isShowNegative) {
            txtNegative.setVisibility(View.VISIBLE);
        }
//        mTxtYes.setTag(buttonText);*/
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        txtNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {
        try {
            dialog = new Dialog(context, R.style.DialogTheme);
            @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
            TextView txtHeader = view.findViewById(R.id.txtHeader);
            TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
            TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
            ImageView iv_header = (ImageView) view.findViewById(R.id.iv_header);

            txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
            txtNegative.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

            if (header.equalsIgnoreCase("Alert")) {
                iv_header.setImageResource(R.drawable.ic_call_black_24dp);
            } else if (header.equalsIgnoreCase("Logout")) {
                iv_header.setImageResource(R.drawable.ic_info_outline_black_24dp);
            }
            Button btn_positive = view.findViewById(R.id.btn_positive);
            btn_positive.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtHeader.setText(header);
            txtMessage.setText(msg);
            btn_positive.setText(positiveButton);
            txtNegative.setText(negativeButton);
            if (isShowNegative) {
                txtNegative.setVisibility(View.VISIBLE);
            }
//        mTxtYes.setTag(buttonText);*/
            btn_positive.setOnClickListener(onClickListener1);
            txtNegative.setOnClickListener(onClickListener2);

            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(view);
            try {
                if (dialog != null) {
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
        }
    }

    public void showAlert(final Context context, String msg, String buttonText) {

        dialog = new Dialog(context, R.style.DialogTheme);
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null);
        TextView txtAlert = view.findViewById(R.id.txtAlert);
        TextView txtYes = view.findViewById(R.id.txtYes);
        txtYes.setText(buttonText);
        txtAlert.setText(msg);
//        mTxtYes.setTag(buttonText);*/
        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlert(Context context, String msg, String positiveButtonText, String negativeButtonText) {

        dialog = new Dialog(context, R.style.DialogTheme);

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null);

       /* mTxtYes.setText(positiveButtonText);
        mTxtNo.setText(negativeButtonText);
        mTxtAlert.setText(msg);
        mTxtYes.setTag(positiveButtonText);
        mTxtNo.setTag(negativeButtonText);*/

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlert(Context context, String msg, String positiveButtonText, String negativeButtonText, final RequestCode requestCode, final DataObserver dataObserver) {

        dialog = new Dialog(context, R.style.DialogTheme);

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null);

       /* mTxtYes.setText(positiveButtonText);
        mTxtNo.setText(negativeButtonText);
        mTxtAlert.setText(msg);
        mTxtYes.setTag(positiveButtonText);*/

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(view);
        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* mTxtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dataObserver.onRetryRequest(requestCode);
            }
        });

        mTxtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });*/
    }

    public static void showDatePickerDialog(final Context context, DatePickerDialog.OnDateSetListener dateSetListener,
                                            final CalendarDateSelection calendarDateSelection, int year, int month, int day) {

        final Calendar mCurrentDate = Calendar.getInstance();

        final Calendar minDate = Calendar.getInstance();

        int mYear = mCurrentDate.get(Calendar.YEAR);
        int mMonth = mCurrentDate.get(Calendar.MONTH);
        int mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog mDatePicker = new DatePickerDialog(context, R.style.DatePickerDialogTheme, dateSetListener, mYear, mMonth, mDay);

        switch (calendarDateSelection) {

            case CALENDAR_WITH_ALL_DATE:

                break;
            case CALENDAR_WITH_PAST_DATE:

                minDate.set(Calendar.YEAR, year);
                minDate.set(Calendar.MONTH, month - PREV_MONTH);
                minDate.set(Calendar.DAY_OF_MONTH, day);

//                mDatePicker.getDatePicker().setMinDate(minDate.getTimeInMillis());
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                break;

            case CALENDAR_WITH_PAST_OR_FUTURE_DATE_INTERVAL:

                minDate.set(Calendar.YEAR, year);
                minDate.set(Calendar.MONTH, month - PREV_MONTH);
                minDate.set(Calendar.DAY_OF_MONTH, day);

                mDatePicker.getDatePicker().setMinDate(minDate.getTimeInMillis());
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                break;
            case CALENDAR_WITH_CURRENT_TO_FUTUREDATE:

                minDate.set(Calendar.YEAR, year);
                minDate.set(Calendar.MONTH, month - PREV_MONTH);
                minDate.set(Calendar.DAY_OF_MONTH, day);

                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                mDatePicker.getDatePicker().setMaxDate(minDate.getTimeInMillis());
                break;


            case CALENDAR_WITH_FUTURE_DATE:

                minDate.set(Calendar.YEAR, year);
                minDate.set(Calendar.MONTH, month - PREV_MONTH);
                minDate.set(Calendar.DAY_OF_MONTH, day);
                mDatePicker.getDatePicker().setMinDate(minDate.getTimeInMillis());
//                mDatePicker.getDatePicker().setMaxDate(minDate.getTimeInMillis());
//                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
////                mDatePicker.getDatePicker().setMaxDate(minDate.getTimeInMillis());
                break;
        }

        mDatePicker.setTitle("");
        try {
            if (!mDatePicker.isShowing()) {
                mDatePicker.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showTimePickerDialog(final Context context, TimePickerDialog.OnTimeSetListener timeSetListener) {

        final Calendar mCurrentDate = Calendar.getInstance();
        int hour = mCurrentDate.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentDate.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, timeSetListener, hour, minute, false);
        timePickerDialog.setTitle("");
        try {
            if (!timePickerDialog.isShowing()) {
                timePickerDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog(Context mContext, String title, String message, String buttonText1, View.OnClickListener onClickListener1,
                           String buttonText2, View.OnClickListener onClickListener2, boolean isDualButton) {
        if (message != null && !message.isEmpty()) {
            dialog = new Dialog(mContext, R.style.DialogTheme);
            View view = LayoutInflater.from(mContext).inflate(R.layout.diag_alert, null);

            TextView mTxtTitle = GenericView.findViewById(view, R.id.tv_txtTitle);
            TextView mTxtMessage = GenericView.findViewById(view, R.id.tv_txtMessage);

            Button mBtnButton1 = GenericView.findViewById(view, R.id.bt_btnButton1);
            Button mBtnButton2 = GenericView.findViewById(view, R.id.bt_btnButton2);
            Button mBtnButton3 = GenericView.findViewById(view, R.id.bt_btnButton3);
            ImageView iv_ImgClose = GenericView.findViewById(view, R.id.iv_ImgClose);
//            String cancelText = MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string
//                    .str_cancel));
            String cancelText = "Canel";
            if (!(buttonText1.equalsIgnoreCase(cancelText) || buttonText2.equalsIgnoreCase(cancelText))) {
                iv_ImgClose.setVisibility(View.VISIBLE);
            }

            LinearLayout mLinearButton = GenericView.findViewById(view, R.id.ll_linearButton);

            mTxtTitle.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            mTxtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_LIGHT));

            mBtnButton1.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            mBtnButton2.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            mBtnButton3.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

            mTxtTitle.setText(title);
            mTxtMessage.setText(message);
            mBtnButton1.setText(buttonText1);
            mBtnButton2.setText(buttonText2);

            if (isDualButton) {
                mLinearButton.setVisibility(View.VISIBLE);
                mBtnButton3.setVisibility(View.GONE);
            } else {
                mLinearButton.setVisibility(View.GONE);
                mBtnButton3.setVisibility(View.VISIBLE);
            }

            mBtnButton1.setOnClickListener(onClickListener1);
            mBtnButton2.setOnClickListener(onClickListener2);
            iv_ImgClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(view);
            dialog.show();
        }
    }

}
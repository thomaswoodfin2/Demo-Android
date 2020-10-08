package com.pinpoint.appointment.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.CustomOverlay;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.customviews.TouchImageView;
import com.pinpoint.appointment.graphicsUtils.GraphicsUtil;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.IViewClick;
import com.pinpoint.appointment.utils.Debug;

import java.io.File;

/**
 * TODO stub is generated but developer or programmer need to add code as required
 */
public class CropImageActivityLandscape extends AppCompatActivity implements IViewClick {

    // xml component declaration
    private FrameLayout frameLayout;
    private TextView mTxtdesc;
    private Button btnCancel;
    private Button btnDone;
    private ImageView ivZoomInst;
    private ImageView ivRotate;
    private RelativeLayout relParent;
    private ProgressBar pBarCropAct;

    // variable declaration
    /**
     * shape: 1 - circle, 2 - square, 3 - rectangle
     */
    private int shape = 1;
    private int radius = 100, length, breadth;
    private int durationInst = 3000; /* In milli second */
    private float aspectRatio = BaseConstants.VALUE_IMAGE_RATIO;
    private float bitmapRatio;
    private int[] screenWH;
    private boolean rotate = true;

    private String directoryPath;
    public static final String IMAGE_PATH = "imagePath";
    public static final String SHAPE = "shape";
    public static final String IS_ROTATE = "isRotate";
    public static final String DIRECTORY_PATH = "directoryPath";
    public static final String IMAGE_RATIO = "imageRatio";
    public static final String BITMAP_RATIO = "bitmapRatio";

    // class object declaration
    private DisplayMetrics metrics = new DisplayMetrics();
    private Bitmap mBitmap = null;
    private TouchImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        init();

        screenWH = GraphicsUtil.getScreenWidthHeight();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            String imagePath = bundle.getString(IMAGE_PATH);
            shape = bundle.getInt(SHAPE);
            bitmapRatio = bundle.getFloat(BITMAP_RATIO);
            if (bundle.containsKey(IS_ROTATE)) {
                rotate = bundle.getBoolean(IS_ROTATE);
            }
            //  aspectRatio = bundle.getFloat(IMAGE_RATIO);
            directoryPath = bundle.getString(DIRECTORY_PATH);
            radius = GraphicsUtil.convertToPixel(this, radius);
            image = new TouchImageView(this);

            if (!rotate) {
                ivRotate.setVisibility(View.GONE);
            } else {
                ivRotate.setVisibility(View.VISIBLE);
            }

            PrefHelper.setBoolean(PrefHelper.KEY_ZOOM_INST, true);
            if (PrefHelper.getBoolean(PrefHelper.KEY_ZOOM_INST, true)) {

                ivZoomInst.setVisibility(View.VISIBLE);
                PrefHelper.setBoolean(PrefHelper.KEY_ZOOM_INST, false);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after #durationInst#ms
                        ivZoomInst.setVisibility(View.GONE);
                    }
                }, durationInst);
            } else {
                ivZoomInst.setVisibility(View.GONE);
            }

            frameLayout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            /*
             * Crop image Resolution 1080x1360 1360/1080 = 1.26 Aspect ratio=
             * 1.26
             */
            int screenWH[] = GraphicsUtil.getScreenWidthHeight();
            aspectRatio = screenWH[0] / screenWH[1];

            switch (shape) {
                case GraphicsUtil.SHAPE_CIRCLE:
                    radius = (screenWH[1] / 2)-100;
                    break;
                case GraphicsUtil.SHAPE_SQUARE:
                    length = ((screenWH[1] - 40));
                    breadth = ((screenWH[1] - 40));
                    break;
                case GraphicsUtil.SHAPE_RECTANGLE:
                    length = screenWH[0];
                    breadth = (int) (length * aspectRatio);
                    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // Calculate inSampleSize
            options.inSampleSize = GraphicsUtil.getInstance().calculateInSampleSize(options, screenWH[0], (int) (screenWH[0] * aspectRatio));
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            mBitmap = BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath(), options);


            image.setLength(length);
            image.setBreadth(breadth);
            image.setRadius(radius);
            image.setShape(shape);
            image.setImageBitmap(mBitmap);


            CustomOverlay customOverlay = new CustomOverlay(this);
            switch (shape) {
                case GraphicsUtil.SHAPE_CIRCLE:
                    customOverlay.setRadius(radius);
                    customOverlay.setLength(0);
                    customOverlay.setBreadth(0);
                    break;
                case GraphicsUtil.SHAPE_SQUARE:
                    customOverlay.setRadius(0);
                    customOverlay.setLength(length);
                    customOverlay.setBreadth(breadth);
                    break;
                case GraphicsUtil.SHAPE_RECTANGLE:
                    customOverlay.setRadius(0);
                    customOverlay.setLength(length);
                    customOverlay.setBreadth(breadth);
                    break;
            }

            customOverlay.setShape(shape);
            customOverlay.setTemplateImage(imagePath);
            frameLayout.addView(image);
            frameLayout.addView(customOverlay);
        }

    }

    /**
     * Initialise view of xml component here
     * eg. textView, editText
     * and initialisation of required class objects
     */
    public void init() {

        relParent = GenericView.findViewById(this, R.id.activity_crop_image);

        ivRotate = GenericView.findViewById(this, R.id.iv_rotate);

        mTxtdesc = GenericView.findViewById(this, R.id.txt_desc);
        btnDone = GenericView.findViewById(this, R.id.btn_done);
        btnCancel = GenericView.findViewById(this, R.id.btn_cancel);
        ivZoomInst = GenericView.findViewById(this, R.id.iv_zoomInst);
        frameLayout = GenericView.findViewById(this, R.id.frame_lay);
        pBarCropAct = GenericView.findViewById(this, R.id.pb_cropAct);

        mTxtdesc.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        btnDone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        btnCancel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        mTxtdesc.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_resize_desc)));
        btnDone.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_done)));
        btnCancel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)));
    }

    @Override
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
//                Uti ls.clickEffect(view);
                finish();
                break;
            case R.id.iv_rotate:
                try {
                    Bitmap mBitmap1 = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    mBitmap = GraphicsUtil.makeRotate(mBitmap1, 90);
                    image.setImageBitmap(mBitmap);
                    mBitmap1.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_done:

                image.setDrawingCacheEnabled(true);
                mBitmap = Bitmap.createScaledBitmap(image.getDrawingCache(), length/2, length/2, true);
                image.setDrawingCacheEnabled(false); // clear drawing cache
                if (mBitmap != null) {
                    try {
//                    Bitmap finalBitmap = getCroppedCircleImage(x, y, radius * 2, radius * 2);
                        final Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, length, length);
                        String picPath = GraphicsUtil.getInstance().saveImage(mBitmap, directoryPath);
                        mBitmap.recycle();
                        Intent intent = new Intent();
                        intent.putExtra(IMAGE_PATH, picPath);
                        setResult(RESULT_OK, intent);
                        finish();
                    }catch (Exception ex)
                    {
                        Debug.trace(ex.toString());
                    }
                }
                try
                {
                    processBitmapImage(mBitmap);
                }
                catch (Exception ex)
                {
                    Debug.trace("Error"+ex.toString());
                }
                break;
        }
    }

    /**
     * To give required shape to the bitmap.
     *
     * @param bitmap : selected image
     */
    private void processBitmapImage(Bitmap bitmap) {

        Debug.trace("SHAPE:" + shape);

        int x, y, width, height;

        switch (shape) {

            case GraphicsUtil.SHAPE_CIRCLE:
                mBitmap = bitmap;

                width = image.getWidth();
                height = image.getHeight();
                x = width / 2 - (radius);
                y = height / 2 - (radius);

                if (mBitmap != null) {
                    Bitmap finalBitmap = getCroppedCircleImage(x, y, radius * 2, radius * 2);
                    String picPath = GraphicsUtil.getInstance().saveImage(finalBitmap, directoryPath);
                    finalBitmap.recycle();
                    Intent intent = new Intent();
                    intent.putExtra(IMAGE_PATH, picPath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;

            case GraphicsUtil.SHAPE_SQUARE:
                mBitmap = bitmap;
                width = image.getWidth();
                height = image.getHeight();
                y = width / 2 - (length / 2);
                x = height / 2 - (length / 2);

                if (mBitmap != null) {
                    if((x+width)>=mBitmap.getWidth())
                    {
                        x=0;
                        length=mBitmap.getWidth();
                    }
                    Bitmap finalBitmap = getCroppedImage(x, y, length, length);

                    String picPath = GraphicsUtil.getInstance().saveImage(finalBitmap, directoryPath);
                    finalBitmap.recycle();
                    Intent intent = new Intent();
                    intent.putExtra(IMAGE_PATH, picPath);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Debug.trace("System out", "original");
                }
                break;

            case GraphicsUtil.SHAPE_RECTANGLE:
                mBitmap = bitmap;
                width = image.getWidth();
                height = image.getHeight();
                x = width / 2 - (length / 2);
                y = height / 2 - (breadth / 2);

                if (mBitmap != null) {
                    Bitmap finalBitmap = getCroppedImage(x, y, length, breadth);

                    String picPath = GraphicsUtil.getInstance().saveImage(finalBitmap, directoryPath);
                    finalBitmap.recycle();
                    Intent intent = new Intent();
                    intent.putExtra(IMAGE_PATH, picPath);
                    setResult(RESULT_OK, intent);
                    finish();

                } else {
                    Debug.trace("System out", "original");
                }
                break;
        }
    }

    /**
     * To get modified bitmap. Refer {@link Bitmap#createBitmap(Bitmap, int, int, int, int)} method
     * for more information.
     *
     * @param x      The x coordinate of the first pixel in source
     * @param y      The y coordinate of the first pixel in source
     * @param width  The number of pixels in each row
     * @param height The number of rows
     */
    public Bitmap getCroppedImage(int x, int y, int width, int height) {
        // Crop the subset from the original Bitmap.
        final Bitmap croppedBitmap = Bitmap.createBitmap(mBitmap, x, y, width, height);
        return croppedBitmap;
    }

    /**
     * Gets the cropped circle image based on the current crop selection.
     *
     * @param x      The x coordinate of the first pixel in source
     * @param y      The y coordinate of the first pixel in source
     * @param width  The number of pixels in each row
     * @param height The number of rows
     * @return output (Bitmap) : a new Circular Bitmap representing the cropped image
     */
    public Bitmap getCroppedCircleImage(int x, int y, int width, int height) {
        Bitmap bitmap = getCroppedImage(x, y, width, height);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}

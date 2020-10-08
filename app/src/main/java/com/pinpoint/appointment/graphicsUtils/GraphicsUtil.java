package com.pinpoint.appointment.graphicsUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;


import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.utils.Debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * TODO stub is generated but developer or programmer need to add code as required.
 * This class use for graphics related stub like image processing, to get device's screen resolution
 * and etc.
 */

public class GraphicsUtil {

    // variable declaration
    public static final int SHAPE_CIRCLE = 1;
    public static final int SHAPE_SQUARE = 2;
    public static final int SHAPE_RECTANGLE = 3;

    public static final int MEDIA_AUDIO = 1;
    public static final int MEDIA_VIDEO = 2;
    public static final int MEDIA_IMAGE = 3;

    public static final String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory() + File.separator + "PinPoint";
    public static final String CONTEST_DIRECTORY = STORAGE_DIRECTORY + File.separator + "Images";
    public static final String DOWNLOAD_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Shipper_images";
    public static final String CAPTURED_DIRECTORY_NAME = STORAGE_DIRECTORY + File.separator + "Camera";

    // class object declaration
    private static GraphicsUtil instance;


    // constructor
    private GraphicsUtil() {

    }

    /**
     * @return instance (GraphicsUtil) : it return class instance
     */
    public static GraphicsUtil getInstance() {
        if (instance == null) {
            instance = new GraphicsUtil();
        }
        return instance;
    }


    /**
     * To calculate screen width and height.
     *
     * @param options   : To get screen width and height
     * @param reqWidth  : Required screen width
     * @param reqHeight : Required screen height
     * @return inSampleSize (int) :
     * @see <a href="http://voidcanvas.com/whatsapp-like-image-compression-in-android/"> Image compression </a>
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate heightRatio and widthRatio with height and width
            // to requested reqHeight and reqWidth.

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * To save image with specified name and root directory
     *
     * @param finalBitmap (Bitmap) : bitmap that to be saved in External storage
     * @param rootDir     (String) : directory path
     */
    public String saveImage(Bitmap finalBitmap, String rootDir) {
        String timeStamp, imageName;
        File mediaStorageDir;
        if (!TextUtils.isEmpty(rootDir)) {
            mediaStorageDir = new File(rootDir);
        } else {
            mediaStorageDir = new File(rootDir, "temp");
        }

        // make directory in External storage
        mediaStorageDir.mkdirs();


        if (rootDir.equalsIgnoreCase(CONTEST_DIRECTORY)) {

            imageName = "IMG_" + System.currentTimeMillis() + ".jpg";
        } else {

            // define required image name
            imageName = "IMG_contest.jpg";
        }

        File file = new File(mediaStorageDir, imageName);
        if (file.exists()) {
            file.delete();
        }
        try {

            if (finalBitmap != null) {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getPath();
    }

    /**
     * To remove directory or file from user's external storage.
     * <br><strong>Note:</strong> Make sure before use this method you must ask for runtime permission
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} to perform delete operation</br>
     *
     * @param filePath : file or directory path e.g. Refer {@link GraphicsUtil#CONTEST_DIRECTORY}
     *                 //     * @see com.example.mvc.codebase.permissionUtils.PermissionClass
     */
    public static boolean removeDirectoryOrFile(String filePath) {
        File fileOrDirectory = new File(filePath);
        if (fileOrDirectory.exists()) {
            File[] files = fileOrDirectory.listFiles();
            if (files == null) {
                return true;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    // call this method if you want to remove directory also
                    // removeDirectoryOrFile(filePath);
                } else {
                    file.delete();
                }
            }
        }
        return (fileOrDirectory.delete());
    }


    /**
     * To get captured image path
     *
     * @param dirName   : directory name. e.g. {@link GraphicsUtil#STORAGE_DIRECTORY}
     * @param mediaType : media type <ul><li>mediaType 1 = audio</li>
     *                  <li>mediaType 2 = video</li>
     *                  <li>mediaType 3 = image</li></ul>
     */
    public static Uri getOutputMediaFileUri(String dirName, int mediaType) {
        File mediaStorageDir = new File(dirName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Debug.trace(dirName, "Oops! Failed create " + dirName + " directory");
                return null;
            }
        }

        String extension = "";
        String fileType = "";

        switch (mediaType) {

            case MEDIA_AUDIO:
                fileType = "AUD";
                /*mp3 format cause a problem in android and iphone to play audio so we put .m4a format
                form server .m4a format must be enable other wise its fire file not found error in iphone
                or from web browser while playing URL*/
                extension = ".m4a";
                break;
            case MEDIA_VIDEO:
                fileType = "VID";
                extension = ".3gp";
                break;
            case MEDIA_IMAGE:
                fileType = "IMG";
                extension = ".jpg";
                break;
        }

        // Create a media file name
        File mediaFile;
        int n = 10000;
        Random generator = new Random();

        n = generator.nextInt(n);

        if (dirName.equalsIgnoreCase(CONTEST_DIRECTORY)) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileType + "_contest" + extension);
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileType + n + extension);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            BaseConstants.CURRENTPHOTOPATH = mediaFile.getAbsolutePath();
            return FileProvider.getUriForFile(BaseApplication.getInstance(), BaseApplication.getInstance().getPackageName() + ".provider", mediaFile);
        } else {
            return Uri.fromFile(mediaFile);
        }
//
    }

    /**
     * Convert Bitmap to ByteArray
     *
     * @param bitmap : bitmap that to be saved in ByteArray
     */
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        try {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method that accept bitmap, apply rotation on bitmap.
     *
     * @param src    : source bitmap.
     * @param degree : at degree bitmap is rotated e.g. 90,180.
     * @return Bitmap : final processed bitmap.
     */
    public static Bitmap makeRotate(Bitmap src, float degree) {

        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);

        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    /**
     * To get device screen width and height
     *
     * @return widthAndHeight (int[]) : it return screen width and height in an array
     * e.g. int[] {screenWidth, screenHeight}
     */
    public static int[] getScreenWidthHeight() {
        WindowManager wm = (WindowManager) BaseApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        return new int[]{screenWidth, screenHeight};
    }

    /**
     * // TODO need to add code for file path validation
     * it convert gallery or camera image in bitmap from it's reference
     * <br><strong> Note: </strong> selectedImagePath should be like this</br>
     * <pre>{@code
     * selectedImagePath = "/storage/emulated/0/1483959236587.jpg" // camera image path
     * selectedImagePath = "/storage/sdcard/Download/ic_abbacus.png" // gallery image path
     * }
     *
     * </pre>
     *
     * @param selectedImagePath (String) : gallery or camera image destination path
     */
    public static Bitmap convertImageInBitmap(String selectedImagePath) {
        Bitmap thumbnail;
        thumbnail = BitmapFactory.decodeFile(selectedImagePath);
        return thumbnail;
    }

    /**
     * To convert value in pixel.
     *
     * @param context : context
     * @param value   : value that to be converted in pixel
     */
    public static int convertToPixel(Context context, int value) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
        return (int) px;
    }


}

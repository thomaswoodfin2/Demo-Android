package com.pinpoint.appointment.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;

import java.io.File;


public class CustomOverlay extends androidx.appcompat.widget.AppCompatImageView {
	public CustomOverlay(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomOverlay(Context context) {
		super(context);
	}

	private int shape = 0;
	private int length;
	private int breadth;
	private int radius;
	private int templateimageWidth = 0;
	private int templateimageHeight = 0;
	private String templateImage = "";
	//	int cropBorderColor=Color.parseColor("#D4D419");
	private int cropBorderColor= Color.parseColor("#FFFFFF");


	public String getTemplateImage() {
		return templateImage;
	}

	public void setTemplateImage(String templateImage) {
		this.templateImage = templateImage;
	}

	public int getTemplateimageWidth() {
		return templateimageWidth;
	}

	public void setTemplateimageWidth(int templateimageWidth) {
		this.templateimageWidth = templateimageWidth;
	}

	public int getTemplateimageHeight() {
		return templateimageHeight;
	}

	public void setTemplateimageHeight(int templateimageHeight) {
		this.templateimageHeight = templateimageHeight;
	}

	public int getShape() {
		return shape;
	}

	public void setShape(int shape) {
		this.shape = shape;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int lenght) {
		this.length = lenght;
	}

	public int getBreadth() {
		return breadth;
	}

	public void setBreadth(int breadth) {
		this.breadth = breadth;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	@Override
	@SuppressWarnings("deprecation")
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {

		try {
			int midX, midY;
			midX = this.getWidth() / 2;
			midY = this.getHeight() / 2;
			if (templateimageWidth > 0 && templateimageHeight > 0) {// template
				BitmapFactory.Options options = new BitmapFactory.Options();
				// options.inSampleSize=2;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;

//				DiscCacheAware discCacheAware = MyApplication.imageLoader.getDiscCache();
				Log.d("System out", "templateImage: " + templateImage);
				Log.d("System out", "templateImage.hashCode(): "+ templateImage.hashCode());
//				templateImage = discCacheAware.get(templateImage).getAbsolutePath();
				Log.d("System out", "url file path on sdcard: " + templateImage);
				Bitmap bitmap2 = BitmapFactory.decodeFile(new File(
						templateImage).getAbsolutePath(), options);
				Bitmap bitmap = Bitmap.createScaledBitmap(bitmap2,
						templateimageWidth, templateimageHeight, true);
				float left = midX - templateimageWidth / 2, top = midY
						- templateimageHeight / 2, right = midX
						+ templateimageWidth / 2, bottom = midY
						+ templateimageHeight / 2;
				canvas.drawBitmap(bitmap, left, top, new Paint());
			} else {
				if (shape == 1) {// circle
					// Bitmap bitmap = Bitmap.createBitmap((radius*2),
					// (radius*2),
					// Bitmap.Config.ARGB_8888);
					Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(),
							canvas.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas temp = new Canvas(bitmap);
					Paint paint = new Paint();
					paint.setColor(0xcc000000);
					// paint.setColor(0x88808080);
					/*
					 * paint.setColor(Color.parseColor("#D4D419"));
					 * paint.setStrokeWidth(5); paint.setStyle(Style.STROKE);
					 * paint.setStrokeJoin(Join.ROUND);
					 */
					temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(),
							paint);

					Paint transparentPaint = new Paint();

					// transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
					transparentPaint.setXfermode(new PorterDuffXfermode(
							PorterDuff.Mode.CLEAR));
					temp.drawCircle(midX, midY, radius, transparentPaint);

					Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					borderPaint.setColor(cropBorderColor);
					borderPaint.setStrokeWidth(5);
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setStrokeJoin(Join.ROUND);
					temp.drawCircle(midX, midY, radius, borderPaint);

					canvas.drawBitmap(bitmap, 0, 0, new Paint());
				} else if (shape == 2) {// square
					Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(),
							canvas.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas temp = new Canvas(bitmap);
					Paint paint = new Paint();
					paint.setColor(0xcc000000);
					// paint.setColor(0x88808080);
					temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(),
							paint);

					Paint transparentPaint = new Paint();
					transparentPaint.setColor(getResources().getColor(
							android.R.color.transparent));
					transparentPaint.setXfermode(new PorterDuffXfermode(
							PorterDuff.Mode.CLEAR));
					// temp.drawCircle(this.getWidth()/2 , this.getHeight()/2 ,
					// radius, transparentPaint);
					float left = midX - length / 2, top = midY - length / 2, right = midX
							+ length / 2, bottom = midY + length / 2;
					temp.drawRect(left, top, right, bottom, transparentPaint);

					Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					borderPaint.setColor(cropBorderColor);
					borderPaint.setStrokeWidth(5);
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setStrokeJoin(Join.ROUND);
					temp.drawRect(left, top, right, bottom, borderPaint);

					canvas.drawBitmap(bitmap, 0, 0, new Paint());
				} else if (shape == 3) {// rectangle
					Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(),
							canvas.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas temp = new Canvas(bitmap);
					Paint paint = new Paint();
					// paint.setColor(0xcc000000);
					// paint.setColor(0x88808080);
					paint.setColor(0xcc000000);

					/*
					 * paint.setColor(Color.parseColor("#D4D419"));
					 * paint.setStrokeWidth(5); paint.setStyle(Style.STROKE);
					 * paint.setStrokeJoin(Join.ROUND);
					 */

					temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(),
							paint);

					Paint transparentPaint = new Paint();
					// transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
					transparentPaint.setXfermode(new PorterDuffXfermode(
							PorterDuff.Mode.CLEAR));
					// temp.drawCircle(this.getWidth()/2 , this.getHeight()/2 ,
					// radius, transparentPaint);
					float left = midX - length / 2, top = midY - breadth / 2, right = midX
							+ length / 2, bottom = midY + breadth / 2;
					temp.drawRect(left, top, right, bottom, transparentPaint);

					Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
					borderPaint.setColor(cropBorderColor);
					borderPaint.setStrokeWidth(5);
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setStrokeJoin(Join.ROUND);
					temp.drawRect(left, top, right, bottom, borderPaint);

					canvas.drawBitmap(bitmap, 0, 0, new Paint());

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

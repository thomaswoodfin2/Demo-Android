package com.pinpoint.appointment.customviews;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class ItemDecoration extends RecyclerView.ItemDecoration {

   private int mItemOffsetL;
    private int mItemOffsetR;
    private int mItemOffsetT;
    private int mItemOffsetB;

    public ItemDecoration(int itemOffset) {
        mItemOffsetL = itemOffset;
        mItemOffsetR = itemOffset;
        mItemOffsetT = itemOffset;
        mItemOffsetB = itemOffset;
    }


    public ItemDecoration(int itemOffsetL, int itemOffsetR, int itemOffsetT, int itemOffsetB) {
       mItemOffsetL = itemOffsetL;
       mItemOffsetR = itemOffsetR;
       mItemOffsetT = itemOffsetT;
       mItemOffsetB = itemOffsetB;
   }

//   public ItemOffsetDecoration(Context context, int itemOffsetId) {
//       this(context.getResources().getDimensionPixelSize(itemOffsetId));
//       mItemOffset = itemOffsetId;
//   }

   @Override
   public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                              RecyclerView.State state) {
       super.getItemOffsets(outRect, view, parent, state);
       outRect.set(mItemOffsetL, mItemOffsetT, mItemOffsetR, mItemOffsetB);
   }
}
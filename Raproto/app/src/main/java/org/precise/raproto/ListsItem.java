package org.precise.raproto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ListsItem {
    private final String mItemName;
    private final String mItemName2;
    private final Class mClass;
    private final String mItemType;

    public ListsItem(String itemName, Class<? extends Activity> clazz, String itemType) {
        mItemName = itemName;
        mItemName2 = null;
        mClass = clazz;
        mItemType = itemType;
    }

    public ListsItem(String itemName, String itemName2, Class<? extends Activity> clazz, String itemType) {
        mItemName = itemName;
        mItemName2 = itemName2;
        mClass = clazz;
        mItemType = itemType;
    }

    public ListsItem(String itemName, String itemType) {
        mItemName = itemName;
        mItemName2 = null;
        mClass = null;
        mItemType = itemType;
    }

    public ListsItem(String itemName, String itemName2, String itemType) {
        mItemName = itemName;
        mItemName2 = itemName2;
        mClass = null;
        mItemType = itemType;
    }

    public String getItemName() {
        return mItemName;
    }

    public String getItemName2() {
        return mItemName2;
    }

    public String getItemType() {
        return mItemType;
    }

    public void launchActivity(Context context) {
        if (mClass != null) {
            Intent intent = new Intent(context, mClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
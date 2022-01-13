/*
 * Copyright (C) 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.precise.raproto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class ListsItem {
    private final String mItemName;
    private String mItemName2;
    private final Class mClass;
    private final String mItemType;

    public ListsItem(String itemName, Class<? extends Activity> clazz, String itemType) {
        mItemName = itemName;
        mItemName2 = null;
        mClass = clazz;
        mItemType = itemType;
    }

    public ListsItem(String itemName,String itemName2, Class<? extends Activity> clazz, String itemType) {
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

    public void updateItemName2(String newItemName) { mItemName2 =  newItemName;}

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

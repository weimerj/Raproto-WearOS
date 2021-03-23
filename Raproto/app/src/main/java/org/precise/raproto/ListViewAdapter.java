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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ListViewAdapter<T extends Item> extends ArrayAdapter<T> {
    private final LayoutInflater mInflater;
    private List<T> mItems;

    public ListViewAdapter(@NonNull Context context, @NonNull List<T> items) {
        super(context, R.layout.list_item_layout, items);
        mInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        int type = mItems.get(position).getItemType();
        if (convertView == null) {
            holder = new Holder();
            Log.d("Type", String.valueOf(type));
            switch(type) {
                case 0:
                    convertView = mInflater.inflate(R.layout.list_item_layout_text_only, parent, false);
                    holder.mTextView = convertView.findViewById(R.id.item_text);
                    break;
                case 1:
                    convertView = mInflater.inflate(R.layout.list_item_layout, parent, false);
                    holder.mTextView = convertView.findViewById(R.id.item_text);
                    holder.mImageView = convertView.findViewById(R.id.item_image);
                    break;
                case 2:
                    convertView = mInflater.inflate(R.layout.list_item_layout_toggle, parent, false);
                    holder.mTextView = convertView.findViewById(R.id.item_text);
                    break;
            }
            convertView.setTag(holder); // Cache the holder for future use.
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mTextView.setText(mItems.get(position).getItemId());
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    private static class Holder {
        TextView mTextView;
        ImageView mImageView;
    }
}

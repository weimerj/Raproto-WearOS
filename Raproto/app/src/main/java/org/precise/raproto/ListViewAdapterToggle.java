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
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListViewAdapterToggle extends ArrayAdapter<ListsItem> {

    // For custom listener.
    public interface SwitchChangeListener {

        void onChange(boolean switchOn);
    }

    private final LayoutInflater mInflater;
    private ArrayList<ListsItem> mItems = new ArrayList<ListsItem>();
    private SwitchChangeListener mSwitchChangeListener;
    private Switch mSwitchWidget;
    private Context mContext;
    private List<Holder> holderList = new ArrayList<Holder>();


    public ListViewAdapterToggle(Context context, ArrayList<ListsItem> items, SwitchChangeListener switchChangeListener) {
        super(context, R.layout.list_item_arrow, items);
        mInflater = LayoutInflater.from(context);
        mItems = items;
        mContext = context;

        // Forces activity to implement a SwitchChangeListener
        mSwitchChangeListener = switchChangeListener;
    }

    public void updateHolderTextView(int holderListPosition, int textViewIdx) {
        Holder holder = holderList.get(holderListPosition);
        if (holder == null) {
            Log.d("holder", "null");
        }else {
            if (holder.mTextViewList.get(textViewIdx) != null) {
                holder.mTextViewList.get(textViewIdx).setText(mItems.get(holderListPosition).getItemName2());
            }else {
                Log.d("holder textview", "null");
            }
        }


    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        String type = mItems.get(position).getItemType();
        convertView = null;
        if (convertView == null) {
            holder = new Holder();
            switch(type) {
                case "text_only":
                    convertView = mInflater.inflate(R.layout.list_item_text_only, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    break;
                case "arrow":
                    convertView = mInflater.inflate(R.layout.list_item_arrow, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    holder.mImageView = convertView.findViewById(R.id.item_image);
                    break;
                case "toggle":
                    convertView = mInflater.inflate(R.layout.list_item_toggle, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    mSwitchWidget = convertView.findViewById(R.id.switch_widget);
                    // Set the OnClickListener (Observer pattern used here).
                    convertView.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mSwitchWidget.setChecked(!(mSwitchWidget.isChecked()));
                                    if (mSwitchChangeListener != null) {
                                        mSwitchChangeListener.onChange(mSwitchWidget.isChecked());
                                    }
                                }
                            });
                    break;
                case "2_rows":
                    convertView = mInflater.inflate(R.layout.list_item_2_rows, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text2));
                    break;
                case "2_rows_centered":
                    convertView = mInflater.inflate(R.layout.list_item_2_rows_centered, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text2));
                    break;
                case "2_rows_arrow":
                    convertView = mInflater.inflate(R.layout.list_item_2_rows_arrow, parent, false);
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text));
                    holder.mTextViewList.add(convertView.findViewById(R.id.item_text2));
                    holder.mImageView = convertView.findViewById(R.id.item_image);
                    break;
            }

            holderList.add(holder);
            convertView.setTag(holder); // Cache the holder for future use.
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.mTextViewList.get(0).setText(mItems.get(position).getItemName());
        if (holder.mTextViewList.size() > 1) {
            holder.mTextViewList.get(1).setText(mItems.get(position).getItemName2());
        }
        return convertView;
    }

    public String getSwitchToggleString(boolean isChecked) {
        return isChecked ? mContext.getString(R.string.on) : mContext.getString(R.string.off);
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    private class Holder {
        ArrayList<TextView> mTextViewList = new ArrayList<TextView>();
        ImageView mImageView;

    }

}

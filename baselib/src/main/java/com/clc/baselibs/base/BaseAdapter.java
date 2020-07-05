package com.clc.baselibs.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import java.util.List;

/**
 * @author tanlei
 * @date 2019/8/9
 * @describe listview配合databinding通用的适配器
 */

public class BaseAdapter<T> extends android.widget.BaseAdapter {
    private Context context;
    private List<T> list;
    private int layoutId;
    private int variableId;

    public BaseAdapter(Context context, List<T> list, int layoutId, int variableId) {
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
        this.variableId = variableId;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewDataBinding binding = null;
        if (view == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, viewGroup, false);
        } else {
            binding = DataBindingUtil.getBinding(view);
        }
        binding.setVariable(variableId, list.get(i));
        return binding.getRoot();
    }
}

package com.smartneasy.hargahponline.adapter;

import java.util.ArrayList;
import java.util.List;

import com.smartneasy.hargahponline.R;
import com.smartneasy.hargahponline.model.Handphone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ListAdapterHandphone extends BaseAdapter implements Filterable {
    private Context context;
    private List<Handphone> list;
    private List<Handphone> filteredList;

    public ListAdapterHandphone(Context context, List<Handphone> list) {
        this.context = context;
        this.list = list;
        this.filteredList = list;
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_row, parent, false);

            holder = new ViewHolder();
            holder.textNama = convertView.findViewById(R.id.text_nama);
            holder.textHarga = convertView.findViewById(R.id.text_harga);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Handphone hp = filteredList.get(position);
        holder.textNama.setText(hp.getNama());
        holder.textHarga.setText(hp.getHarga());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new HandphoneFilter();
    }

    private class ViewHolder {
        TextView textNama;
        TextView textHarga;
    }

    private class HandphoneFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Handphone> filteredData = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredData.addAll(list);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Handphone hp : list) {
                    if (hp.getNama().toLowerCase().contains(filterPattern)) {
                        filteredData.add(hp);
                    }
                }
            }

            results.values = filteredData;
            results.count = filteredData.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (List<Handphone>) results.values;
            notifyDataSetChanged();
        }
    }
}

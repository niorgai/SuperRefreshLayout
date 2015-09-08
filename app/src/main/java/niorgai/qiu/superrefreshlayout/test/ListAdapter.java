package niorgai.qiu.superrefreshlayout.test;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import niorgai.qiu.superrefreshlayout.R;

/**
 * 简单的ListAdapter
 * Created by qiu on 9/8/15.
 */
public class ListAdapter extends ArrayAdapter<Integer> {

    public ListAdapter(Context context) {
        super(context, R.layout.list_item);
    }

    @Override
    public int getCount() {
        return 30;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.list_item, null);
        }
        ((TextView)convertView).setText(String.valueOf(position));
        return convertView;
    }
}

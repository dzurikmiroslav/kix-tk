package org.kixlabs.tk.activities.adapters;

import java.util.List;

import org.kixlabs.tk.service.so.TableCellSO;
import org.kixlabs.tk.service.so.TableRowSO;
import org.kixlabs.tk.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TableRowAdapter extends ArrayAdapter<TableRowSO> {

	private List<TableRowSO> values;

	private Context context;

	private String formatNumber(int n) {
		if (n < 10)
			return "0" + String.valueOf(n);
		else
			return String.valueOf(n);
	}

	public TableRowAdapter(Context context, List<TableRowSO> values) {
		super(context, R.layout.table_item, values);
		this.values = values;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.table_item, null);
		}
		TableRowSO r = values.get(position);
		((TextView) v.findViewById(R.id.label_hour)).setText(formatNumber(r
				.getHour()));
		if (values.get(position).getCells() != null) {
			StringBuilder builder = new StringBuilder();
			for (TableCellSO c : values.get(position).getCells()) {
				builder.append(formatNumber(c.getMinute()));
				if (c.getNote() != 0)
					builder.append(c.getNote());
				builder.append(' ');
			}
			((TextView) v.findViewById(R.id.label_minutes)).setText(builder
					.toString());
		}
		return v;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

}
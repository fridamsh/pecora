package msh.frida.mapapp.Other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import msh.frida.mapapp.Models.HikeModel;
import msh.frida.mapapp.R;

public class HistoryArrayAdapter extends ArrayAdapter<HikeModel> {

    private final Context context;
    private final List<HikeModel> values;

    public HistoryArrayAdapter(Context context, List<HikeModel> values) {
        super(context, R.layout.history_listview_layout, values);
        this.context = context;
        this.values = values;
    }

    static class ViewHolder {
        protected TextView textMonth;
        protected TextView textDay;
        protected TextView text;
        protected TextView text2;
        protected TextView text3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            view = inflater.inflate(R.layout.history_listview_layout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.textMonth = (TextView) view.findViewById(R.id.label_month);
            viewHolder.textDay = (TextView) view.findViewById(R.id.label_day);
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.text2 = (TextView) view.findViewById(R.id.label2);
            viewHolder.text3 = (TextView) view.findViewById(R.id.label3);

            view.setTag(viewHolder);

        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.textMonth.setText(getMonth(values.get(position).getDateStart()));
        holder.textDay.setText(getDay(values.get(position).getDateStart()));
        holder.text.setText("Kl. " + getTime(values.get(position).getDateStart()));
        holder.text2.setText(values.get(position).getTitle());
        holder.text3.setText("Gjeter: " + values.get(position).getName());
        return view;
    }

    private String getMonth(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        switch (dateArray[1]) {
            case ("May"):
                return "Mai";
            case ("Oct"):
                return "Okt";
            case ("Dec"):
                return "Des";
        }
        return dateArray[1];
    }
    private String getDay(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");

        return dateArray[2];
    }

    private String getTime(Long dateInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        String date = c.getTime().toString();
        String[] dateArray = date.split(" ");
        String[] timeArray = dateArray[3].split(":");

        return timeArray[0] + ":" + timeArray[1];
    }
}
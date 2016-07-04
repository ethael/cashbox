package protect.cashbox;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import protect.cashbox.category.Category;

public class DashboardCategoryStatsAdapter extends BaseAdapter {

    private final String FRACTION_FORMAT;
    public List<Category> categories;
    public List<Category> filteredCategories = new ArrayList<>();
    public Context context;

    public DashboardCategoryStatsAdapter(Context context, List<Category> categories) {
        this.categories = categories;
        this.context = context;
        filteredCategories.addAll(categories);
        FRACTION_FORMAT = context.getResources().getString(R.string.fraction);
    }

    @Override
    public int getCount() {
        return filteredCategories.size();
    }

    @Override
    public Category getItem(int position) {
        return filteredCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView categoryName;
        ProgressBar categoryBar;
        TextView categoryValue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dashboard_category_list, parent, false);
            holder = new ViewHolder();
            holder.categoryName = (TextView) convertView.findViewById(R.id.category_name);
            holder.categoryBar = (ProgressBar) convertView.findViewById(R.id.category_bar);
            holder.categoryValue = (TextView) convertView.findViewById(R.id.category_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category item = getItem(position);
        holder.categoryName.setText(item.getName());
        holder.categoryValue.setText(String.format(FRACTION_FORMAT, item.getCurrent(), item.getMax()));
        holder.categoryBar.setMax(item.getMax() == 0 ? 1 : item.getMax());
        holder.categoryBar.setProgress(item.getCurrent());

        if (item.getMax() == 0) {
            holder.categoryBar.getProgressDrawable().setColorFilter(Color.parseColor("#79ceff"), PorterDuff.Mode.SRC_IN);
        } else if ((item.getMax() <= item.getCurrent()) && (((float) item.getCurrent() / item.getMax()) > 1.3)) {
            holder.categoryBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        } else if (item.getMax() <= item.getCurrent()) {
            holder.categoryBar.getProgressDrawable().setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
        } else {
            holder.categoryBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        }
        return convertView;
    }

    public void reset(List<Category> categories) {
        this.categories = categories;
        filteredCategories.clear();
        filteredCategories.addAll(categories);
        notifyDataSetChanged();
    }
}


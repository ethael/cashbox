package protect.cashbox.category;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import protect.cashbox.R;

public class CategoryListAdapter extends BaseAdapter {

    public List<Category> categories;
    public List<Category> filteredCategories = new ArrayList<>();
    public Context context;

    public CategoryListAdapter(Context context, List<Category> categories) {
        this.categories = categories;
        this.context = context;
        filteredCategories.addAll(categories);
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
        TextView categoryMax;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.category_list, parent, false);
            holder = new ViewHolder();
            holder.categoryName = (TextView) convertView.findViewById(R.id.category_name);
            holder.categoryMax = (TextView) convertView.findViewById(R.id.category_max);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category item = getItem(position);
        holder.categoryName.setText(item.getName());
        holder.categoryMax.setText(String.valueOf(item.getMax()));
        return convertView;
    }

    public void filter(String query) {

        query = query.toLowerCase(Locale.getDefault());

        filteredCategories.clear();
        if (query.length() == 0) {
            filteredCategories.addAll(categories);

        } else {
            for (Category category : categories) {
                if (query.length() != 0 && category.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredCategories.add(category);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reset(List<Category> categories) {
        this.categories = categories;
        filteredCategories.clear();
        filteredCategories.addAll(categories);
        notifyDataSetChanged();
    }

    public void reset() {
        filteredCategories.clear();
        filteredCategories.addAll(categories);
        notifyDataSetChanged();
    }
}


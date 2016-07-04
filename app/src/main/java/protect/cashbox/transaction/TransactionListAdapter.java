package protect.cashbox.transaction;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import protect.cashbox.R;
import protect.cashbox.util.Constants;

public class TransactionListAdapter extends BaseAdapter {

    private final DateFormat DATE_FORMATTER = SimpleDateFormat.getDateInstance();

    public List<Transaction> transactions;
    public Map<String, Integer> accountIdToCurrencySymbol;
    public List<Transaction> filteredTransactions = new ArrayList<>();
    public Context context;

    public TransactionListAdapter(Context context, List<Transaction> transactions) {
        this.transactions = transactions;
        this.context = context;
        filteredTransactions.addAll(transactions);
    }

    @Override
    public int getCount() {
        return filteredTransactions.size();
    }

    @Override
    public Transaction getItem(int position) {
        return filteredTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView nameField;
        TextView valueField;
        TextView dateField;
        TextView categoryField;
        ImageView receiptIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.transaction_list, parent, false);
            holder = new ViewHolder();
            holder.nameField = (TextView) convertView.findViewById(R.id.name);
            holder.valueField = (TextView) convertView.findViewById(R.id.transactionFragmentValue);
            holder.dateField = (TextView) convertView.findViewById(R.id.date);
            holder.categoryField = (TextView) convertView.findViewById(R.id.category);
            holder.receiptIcon = (ImageView) convertView.findViewById(R.id.receiptIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = getItem(position);
        String currencySymbol = Constants.CURRENCY_SYMBOLS.get(accountIdToCurrencySymbol.get(transaction.getAccount()));
        holder.nameField.setText(transaction.getName());
        holder.valueField.setText(String.format(Locale.getDefault(), "%.2f ", transaction.getValue()) + currencySymbol);
        holder.categoryField.setText(transaction.getCategory());
        holder.dateField.setText(DATE_FORMATTER.format(transaction.getDate()));
        holder.receiptIcon.setVisibility((transaction.getReceipt() == null || transaction.getReceipt().isEmpty()) ? View.GONE : View.VISIBLE);
        return convertView;
    }

    public void filter(String query) {

        query = query.toLowerCase(Locale.getDefault());

        filteredTransactions.clear();
        if (query.length() == 0) {
            filteredTransactions.addAll(transactions);
        } else {
            for (Transaction transaction : transactions) {
                if (transaction.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredTransactions.add(transaction);
                } else if (transaction.getCategory().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredTransactions.add(transaction);
                } else if (transaction.getNote().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reset(List<Transaction> transactions, Map<String, Integer> accountIdToCurrencySymbol) {
        this.transactions = transactions;
        this.accountIdToCurrencySymbol = accountIdToCurrencySymbol;
        filteredTransactions.clear();
        filteredTransactions.addAll(transactions);
        notifyDataSetChanged();
    }

    public void reset() {
        filteredTransactions.clear();
        filteredTransactions.addAll(transactions);
        notifyDataSetChanged();
    }
}

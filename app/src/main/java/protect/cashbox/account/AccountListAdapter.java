package protect.cashbox.account;

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
import protect.cashbox.util.Util;

public class AccountListAdapter extends BaseAdapter {

    public List<Account> accounts;
    public List<Account> filteredAccounts = new ArrayList<>();
    public Context context;

    public AccountListAdapter(Context context, List<Account> accounts) {
        this.accounts = accounts;
        this.context = context;
        filteredAccounts.addAll(accounts);
    }

    @Override
    public int getCount() {
        return filteredAccounts.size();
    }

    @Override
    public Account getItem(int position) {
        return filteredAccounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView accountName;
        TextView accountCurrency;
        TextView accountType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.account_list, parent, false);
            holder = new ViewHolder();
            holder.accountName = (TextView) convertView.findViewById(R.id.account_name);
            holder.accountType = (TextView) convertView.findViewById(R.id.account_type);
            holder.accountCurrency = (TextView) convertView.findViewById(R.id.account_currency);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Account account = getItem(position);
        holder.accountName.setText(account.getName());
        holder.accountType.setText(context.getString(Util.accountTypeToLocaleId(account.getType())));
        holder.accountCurrency.setText(context.getString(Util.currencyIdToLocaleId(account.getCurrency())));
        return convertView;
    }

    public void filter(String query) {

        query = query.toLowerCase(Locale.getDefault());

        filteredAccounts.clear();
        if (query.length() == 0) {
            filteredAccounts.addAll(accounts);

        } else {
            for (Account account : accounts) {
                if (query.length() != 0 && account.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    filteredAccounts.add(account);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void reset(List<Account> accounts) {
        this.accounts = accounts;
        filteredAccounts.clear();
        filteredAccounts.addAll(accounts);
        notifyDataSetChanged();
    }

    public void reset() {
        filteredAccounts.clear();
        filteredAccounts.addAll(accounts);
        notifyDataSetChanged();
    }
}

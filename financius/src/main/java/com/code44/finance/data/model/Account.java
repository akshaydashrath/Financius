package com.code44.finance.data.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.code44.finance.App;
import com.code44.finance.backend.endpoint.accounts.model.AccountEntity;
import com.code44.finance.common.model.AccountOwner;
import com.code44.finance.common.utils.Preconditions;
import com.code44.finance.data.Query;
import com.code44.finance.data.db.Column;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.providers.AccountsProvider;
import com.code44.finance.utils.IOUtils;

public class Account extends BaseModel<AccountEntity> {
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    private static Account systemAccount;

    private Currency currency;
    private String title;
    private String note;
    private long balance;
    private AccountOwner accountOwner;
    private boolean includeInTotals;

    public Account() {
        super();
        setCurrency(Currency.getDefault());
        setTitle(null);
        setNote(null);
        setBalance(0);
        setAccountOwner(AccountOwner.USER);
        setIncludeInTotals(true);
    }

    public Account(Parcel in) {
        super(in);
    }

    public static Account getSystem() {
        if (systemAccount == null) {
            final Cursor cursor = Query.create()
                    .projectionLocalId(Tables.Accounts.LOCAL_ID)
                    .projection(Tables.Accounts.PROJECTION)
                    .selection(Tables.Accounts.OWNER.getName() + "=?", AccountOwner.SYSTEM.asString())
                    .from(App.getContext(), AccountsProvider.uriAccounts())
                    .execute();

            systemAccount = Account.from(cursor);
            IOUtils.closeQuietly(cursor);
        }
        return systemAccount;
    }

    public static Account from(Cursor cursor) {
        final Account account = new Account();
        if (cursor.getCount() > 0) {
            account.updateFrom(cursor, null);
        }
        return account;
    }

    public static Account fromAccountFrom(Cursor cursor) {
        final Account account = new Account();
        if (cursor.getCount() > 0) {
            account.updateFrom(cursor, Tables.Accounts.TEMP_TABLE_NAME_FROM_ACCOUNT);
        }
        return account;
    }

    public static Account fromAccountTo(Cursor cursor) {
        final Account account = new Account();
        if (cursor.getCount() > 0) {
            account.updateFrom(cursor, Tables.Accounts.TEMP_TABLE_NAME_TO_ACCOUNT);
        }
        return account;
    }

    public static Account from(AccountEntity entity, Currency currency) {
        final Account account = new Account();
        account.updateFrom(entity);
        account.setCurrency(currency);
        return account;
    }

    @Override protected Column getLocalIdColumn() {
        return Tables.Accounts.LOCAL_ID;
    }

    @Override protected Column getIdColumn() {
        return Tables.Accounts.ID;
    }

    @Override protected Column getModelStateColumn() {
        return Tables.Accounts.MODEL_STATE;
    }

    @Override protected Column getSyncStateColumn() {
        return Tables.Accounts.SYNC_STATE;
    }

    @Override protected void toValues(ContentValues values) {
        values.put(Tables.Accounts.CURRENCY_ID.getName(), currency.getId());
        values.put(Tables.Accounts.TITLE.getName(), title);
        values.put(Tables.Accounts.NOTE.getName(), note);
        values.put(Tables.Accounts.BALANCE.getName(), balance);
        values.put(Tables.Accounts.OWNER.getName(), accountOwner.asInt());
        values.put(Tables.Accounts.INCLUDE_IN_TOTALS.getName(), includeInTotals);
    }

    @Override protected void toParcel(Parcel parcel) {
        parcel.writeParcelable(currency, 0);
        parcel.writeString(title);
        parcel.writeString(note);
        parcel.writeLong(balance);
        parcel.writeInt(accountOwner.asInt());
        parcel.writeInt(includeInTotals ? 1 : 0);
    }

    @Override protected void toEntity(AccountEntity entity) {
        entity.setCurrencyId(currency.getId());
        entity.setTitle(title);
        entity.setNote(note);
        entity.setAccountOwner(accountOwner.toString());
        entity.setIncludeInTotals(includeInTotals);
    }

    @Override protected AccountEntity createEntity() {
        return new AccountEntity();
    }

    @Override protected void fromParcel(Parcel parcel) {
        setCurrency((Currency) parcel.readParcelable(Currency.class.getClassLoader()));
        setTitle(parcel.readString());
        setNote(parcel.readString());
        setBalance(parcel.readLong());
        setAccountOwner(AccountOwner.fromInt(parcel.readInt()));
        setIncludeInTotals(parcel.readInt() != 0);
    }

    @Override protected void fromCursor(Cursor cursor, String columnPrefixTable) {
        int index;

        // Currency
        Currency currency;
        if (TextUtils.isEmpty(columnPrefixTable)) {
            currency = Currency.from(cursor);
        } else if (columnPrefixTable.equals(Tables.Accounts.TEMP_TABLE_NAME_FROM_ACCOUNT)) {
            currency = Currency.fromCurrencyFrom(cursor);
        } else if (columnPrefixTable.equals(Tables.Accounts.TEMP_TABLE_NAME_TO_ACCOUNT)) {
            currency = Currency.fromCurrencyTo(cursor);
        } else {
            throw new IllegalArgumentException("Table prefix " + columnPrefixTable + " is not supported.");
        }
        currency.setLocalId(0);
        setCurrency(currency);

        // Title
        index = cursor.getColumnIndex(Tables.Accounts.TITLE.getName(columnPrefixTable));
        if (index >= 0) {
            setTitle(cursor.getString(index));
        }

        // Note
        index = cursor.getColumnIndex(Tables.Accounts.NOTE.getName(columnPrefixTable));
        if (index >= 0) {
            setNote(cursor.getString(index));
        }

        // Balance
        index = cursor.getColumnIndex(Tables.Accounts.BALANCE.getName(columnPrefixTable));
        if (index >= 0) {
            setBalance(cursor.getInt(index));
        }

        // Owner
        index = cursor.getColumnIndex(Tables.Accounts.OWNER.getName(columnPrefixTable));
        if (index >= 0) {
            setAccountOwner(AccountOwner.fromInt(cursor.getInt(index)));
        }

        // Include in totals
        index = cursor.getColumnIndex(Tables.Accounts.INCLUDE_IN_TOTALS.getName(columnPrefixTable));
        if (index >= 0) {
            setIncludeInTotals(cursor.getInt(index) != 0);
        }
    }

    @Override protected void fromEntity(AccountEntity entity) {
        setTitle(entity.getTitle());
        setNote(entity.getNote());
        setAccountOwner(AccountOwner.valueOf(entity.getAccountOwner()));
        setIncludeInTotals(entity.getIncludeInTotals());
    }

    @Override public void checkValues() throws IllegalStateException {
        super.checkValues();
        Preconditions.checkNotNull(currency, "Currency cannot be null.");
        Preconditions.checkNotNull(accountOwner, "Owner cannot be null.");
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public AccountOwner getAccountOwner() {
        return accountOwner;
    }

    public void setAccountOwner(AccountOwner accountOwner) {
        this.accountOwner = accountOwner;
    }

    public boolean includeInTotals() {
        return includeInTotals;
    }

    public void setIncludeInTotals(boolean includeInTotals) {
        this.includeInTotals = includeInTotals;
    }
}
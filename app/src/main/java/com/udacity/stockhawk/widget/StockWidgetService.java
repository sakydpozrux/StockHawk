package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.util.StockFormatUtils;

import timber.log.Timber;

/**
 * Created by sakydpozrux on 20/05/2017.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor;

            @Override
            public void onCreate() {
                // This method intentionally left blank.
            }

            @Override
            public void onDataSetChanged() {
                onDestroy();

                // We have to clear calling identity because this service is not exported
                // so it doesn't have access to the app's data.
                final long identityToken = Binder.clearCallingIdentity();

                mCursor = getContentResolver().query(
                        Contract.Quote.URI,
                        null,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                if (mCursor != null)
                    return mCursor.getCount();

                return 0;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION
                        || mCursor == null
                        || !mCursor.moveToPosition(position)) {
                    Timber.d("Couldn't get view at position: " + position);
                    return null;
                }

                final String symbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
                final float price = mCursor.getFloat(Contract.Quote.POSITION_PRICE);
                final float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                views.setTextViewText(R.id.symbol, symbol);
                views.setTextViewText(R.id.price, StockFormatUtils.getDollarFormat(price, true));
                views.setTextViewText(R.id.change, StockFormatUtils.getPercentageFormat(percentageChange / 100));



                final int backgroundResource =
                        (percentageChange >= 0.0)
                        ? R.drawable.percent_change_pill_green
                        : R.drawable.percent_change_pill_red;

                views.setInt(R.id.change, "setBackgroundResource", backgroundResource);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(Contract.Quote.POSITION_ID);

                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

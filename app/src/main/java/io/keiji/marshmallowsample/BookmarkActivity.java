package io.keiji.marshmallowsample;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkActivity extends AppCompatActivity {

    private Cursor mCursor;

    public Cursor getBookmarkCursor() {
//        compileSdkVersion 23ではビルドできない
//        return Browser.getAllBookmarks(getContentResolver());
        return null;
    }

    private Cursor getBookmarkCursor2() {
//        compileSdkVersion 23ではビルドできない
//        String[] strBookmarkProjection = new String[]{
//                Browser.BookmarkColumns.BOOKMARK,
//                Browser.BookmarkColumns.CREATED,
//                Browser.BookmarkColumns.DATE,
//                Browser.BookmarkColumns.TITLE,
//                Browser.BookmarkColumns.URL,
//                Browser.BookmarkColumns.VISITS
//        };
//        return getContentResolver().query(Browser.BOOKMARKS_URI,
//                strBookmarkProjection, Browser.BookmarkColumns.BOOKMARK + "=1", null, null);
        return null;
    }

    private class Adapter extends BaseAdapter {

        private final Cursor mCursor;

        private Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            mCursor.moveToPosition(position);
            return mCursor;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(BookmarkActivity.this);
            }

            Cursor c = (Cursor) getItem(position);

            TextView textView = (TextView) convertView;
//            compileSdkVersion 23ではビルドできない
//            textView.setText(c.getString(c.getColumnIndex(Browser.BookmarkColumns.URL)));
            return textView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView bookmarkList = new ListView(this);
        setContentView(bookmarkList);

        mCursor = getBookmarkCursor2();
        bookmarkList.setAdapter(new Adapter(mCursor));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCursor.close();
    }
}

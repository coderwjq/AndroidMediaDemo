package com.coderwjq.mediaplayer.db;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import com.coderwjq.mediaplayer.ui.fragment.VideoListAdapter;
import com.coderwjq.mediaplayer.utils.CursorUtils;

/**
 * @Created by coderwjq on 2017/5/4 16:51.
 * @Desc
 */

public class MediaAsyncQueryHandler extends AsyncQueryHandler {
    private static final String TAG = "MediaAsyncQueryHandler";

    public MediaAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    /**
     * 主线程返回查询结果
     *
     * @param token
     * @param cookie
     * @param cursor
     */
    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // empty
        super.onQueryComplete(token, cookie, cursor);
        CursorUtils.printCursor(cursor);

        // TODO:instanceof和父类比较
        if (cookie instanceof VideoListAdapter) {
            VideoListAdapter cursorAdapter = (VideoListAdapter) cookie;
            cursorAdapter.swapCursor(cursor);
        }
    }
}

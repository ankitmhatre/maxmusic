/*
 * Copyright (C) 2011 Christopher Eby <kreed@kreed.org>
 * Copyright (C) 2015 Adrian Ulrich <adrian@blinkenlights.ch>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dragonide.maxmusic;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.provider.MediaStore.Audio.Playlists.Members;

/**
 * CursorAdapter backed by MediaStore playlists.
 */
public class PlaylistAdapter extends CursorAdapter implements Handler.Callback {
	private static final String[] PROJECTION = new String[] {
		MediaStore.Audio.Playlists.Members._ID,
		MediaStore.Audio.Playlists.Members.TITLE,
		MediaStore.Audio.Playlists.Members.ARTIST,
		MediaStore.Audio.Playlists.Members.AUDIO_ID,
		MediaStore.Audio.Playlists.Members.ALBUM_ID,
		MediaStore.Audio.Playlists.Members.PLAY_ORDER,
	};

	private final Context mContext;
	private final Handler mWorkerHandler;
	private final Handler mUiHandler;
	private final LayoutInflater mInflater;

	private long mPlaylistId;

	private boolean mEditable;

	/**
	 * Create a playlist adapter.
	 *
	 * @param context A context to use.
	 * @param worker A looper running a worker thread (to run queries on).
	 */
	public PlaylistAdapter(Context context, Looper worker)
	{
		super(context, null, false);

		mContext = context;
		mUiHandler = new Handler(this);
		mWorkerHandler = new Handler(worker, this);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Set the id of the backing playlist.
	 *
	 * @param id The MediaStore id of a playlist.
	 */
	public void setPlaylistId(long id)
	{
		mPlaylistId = id;
		mWorkerHandler.sendEmptyMessage(MSG_RUN_QUERY);
	}

	/**
	 * Enabled or disable edit mode. Edit mode adds a drag grabber to the left
	 * side a views and a delete button to the right side of views.
	 *
	 * @param editable True to enable edit mode.
	 */
	public void setEditable(boolean editable)
	{
		mEditable = editable;
		notifyDataSetInvalidated();
	}

	/**
	 * Update the values in the given view.
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		DraggableRow dview = (DraggableRow)view;
		dview.setupLayout(DraggableRow.LAYOUT_DRAGGABLE);
		dview.showDragger(mEditable);

		TextView textView = dview.getTextView();
		textView.setText(cursor.getString(1));
		textView.setTag(cursor.getLong(3));

		LazyCoverView cover = dview.getCoverView();
		cover.setCover(MediaUtils.TYPE_ALBUM, cursor.getLong(4), null);
	}

	/**
	 * Generate a new view.
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return mInflater.inflate(R.layout.draggable_row, parent, false);
	}

	/**
	 * Re-run the query. Should be run on worker thread.
	 */
	public static final int MSG_RUN_QUERY = 1;
	/**
	 * Update the cursor. Must be run on UI thread.
	 */
	public static final int MSG_UPDATE_CURSOR = 2;

	@Override
	public boolean handleMessage(Message message)
	{
		switch (message.what) {
		case MSG_RUN_QUERY: {
			Cursor cursor = runQuery(mContext.getContentResolver());
			mUiHandler.sendMessage(mUiHandler.obtainMessage(MSG_UPDATE_CURSOR, cursor));
			break;
		}
		case MSG_UPDATE_CURSOR:
			changeCursor((Cursor)message.obj);
			break;
		default:
			return false;
		}

		return true;
	}

	/**
	 * Query the playlist songs.
	 *
	 * @param resolver A ContentResolver to query with.
	 * @return The resulting cursor.
	 */
	private Cursor runQuery(ContentResolver resolver)
	{
		QueryTask query = MediaUtils.buildPlaylistQuery(mPlaylistId, PROJECTION, null);
		return query.runQuery(resolver);
	}

	/**
	 * Moves a song in the playlist
	 * @param from original position of item
	 * @param to destination of item
	 **/
	public void moveItem(int from, int to)
	{
		if (from == to)
			// easy mode
			return;

		int count = getCount();
		if (to >= count || from >= count)
			// this can happen when the adapter changes during the drag
			return;

		// The Android API contains a method to move a playlist item, however,
		// it has only been available since Froyo and doesn't seem to work
		// after a song has been removed from the playlist (I think?).

		ContentResolver resolver = mContext.getContentResolver();
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId);
		Cursor cursor = getCursor();

		int start = Math.min(from, to);
		int end = Math.max(from, to);

		long order;
		if (start == 0) {
			order = 0;
		} else {
			cursor.moveToPosition(start - 1);
			order = cursor.getLong(5) + 1;
		}

		cursor.moveToPosition(end);
		long endOrder = cursor.getLong(5);

		// clear the rows we are replacing
		String[] args = new String[] { Long.toString(order), Long.toString(endOrder) };
		resolver.delete(uri, "play_order >= ? AND play_order <= ?", args);

		// create the new rows
		ContentValues[] values = new ContentValues[end - start + 1];
		for (int i = start, j = 0; i <= end; ++i, ++j, ++order) {
			cursor.moveToPosition(i == to ? from : i > to ? i - 1 : i + 1);
			ContentValues value = new ContentValues(2);
			value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Long.valueOf(order));
			value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(3));
			values[j] = value;
		}

		// insert the new rows
		resolver.bulkInsert(uri, values);

		changeCursor(runQuery(resolver));
	}

	public void removeItem(int position)
	{
		ContentResolver resolver = mContext.getContentResolver();
		Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId);
		resolver.delete(ContentUris.withAppendedId(uri, getItemId(position)), null, null);
		mUiHandler.sendEmptyMessage(MSG_RUN_QUERY);
	}


}

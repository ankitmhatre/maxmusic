/*
 * Copyright (C) 2011 Christopher Eby <kreed@kreed.org>
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

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Overrides ListPreference to show the selected value as the summary.
 * <p/>
 * (ListPreference should supposedly be able to do this itself if %s is in the
 * summary, but as far as I can tell that behavior is broken.)
 */
public class ListPreferenceSummary extends ListPreference {
    public ListPreferenceSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        return getEntry();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        notifyChanged();
    }

    @Override
    protected void onBindView(View view) {
        /*((TextView) view.findViewById(android.R.id.title))
				.setTextColor(PrefUtils.getInt(getContext(),PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
		((TextView) view.findViewById(android.R.id.summary))
				.setTextColor(PrefUtils.getInt(getContext(),PrefKeys.ACCENT_INT, PrefDefaults.ACCENT_DEF));
*/
        super.onBindView(view);
    }

    @Override
    protected void onBindDialogView(View view) {

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, outValue, true);
        final int themeForegroundColor = outValue.data;
        view.findViewById(android.R.id.content).setBackgroundColor(themeForegroundColor);
        super.onBindDialogView(view);
    }
}

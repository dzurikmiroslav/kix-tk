package org.kixlabs.tk.activities.browse;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class StaticViewPager extends ViewPager {

	public StaticViewPager(Context context) {
		super(context);
	}

	public StaticViewPager(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return onInterceptTouchEvent(event);
	}

}

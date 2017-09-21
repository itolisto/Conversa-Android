package ee.app.conversa.view;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by edgargomez on 4/19/17.
 * From: http://stackoverflow.com/a/4463535/5349296
 */

@SuppressLint("ParcelCreator")
public class URLSpanNoUnderline extends URLSpan {

    public URLSpanNoUnderline(String url) {
        super(url);
    }

    @Override public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

}
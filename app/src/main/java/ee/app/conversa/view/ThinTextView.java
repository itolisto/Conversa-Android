package ee.app.conversa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import ee.app.conversa.ConversaApp;

/**
 * Created by edgargomez on 5/11/15.
 */
public class ThinTextView extends TextView {

    public ThinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!this.isInEditMode())
            this.setTypeface(ConversaApp.getInstance(context).getTfRalewayThin());
    }

}

package ee.app.conversa.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface OnCompleteFileFunction {
    void OnCompleteFileFunction(@Nullable byte[] bytes, @NonNull Exception exception);
}
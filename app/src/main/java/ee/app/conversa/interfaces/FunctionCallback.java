package ee.app.conversa.interfaces;

import ee.app.conversa.networking.FirebaseCustomException;

public interface FunctionCallback<T> extends FirebaseCallback<T, FirebaseCustomException> {
    @Override
    void done(T object, FirebaseCustomException e);
}
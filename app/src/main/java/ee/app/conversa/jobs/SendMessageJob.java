package ee.app.conversa.jobs;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import ee.app.conversa.ConversaApp;
import ee.app.conversa.delivery.DeliveryStatus;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.networking.FirebaseCustomException;
import ee.app.conversa.networking.NetworkingManager;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;

/**
 * Created by edgargomez on 9/5/16.
 */
public class SendMessageJob extends Job {

    private final String TAG = SendMessageJob.class.getSimpleName();
    private final long id;

    public SendMessageJob(long id, String group) {
        // Order of messages matter, we don't want to send two in parallel
        super(new Params(Priority.CRITICAL).requireNetwork().persist().groupBy(group));
        // We have to set variables so they get serialized into job
        this.id = id;
    }

    @Override
    public void onAdded() {
        // Job has been secured to disk
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRun() throws Throwable {
        dbMessage message = ConversaApp.getInstance(getApplicationContext())
                .getDB()
                .getMessageById(id);

        final HashMap<String, Object> params = new HashMap<>(9);
        params.put("toId", message.getFromUserId());
        params.put("fromId", message.getToUserId());
        params.put("fromCustomer", true);
        params.put("messageType", Integer.valueOf(message.getMessageType()));

        if (AblyConnection.getInstance() != null && AblyConnection.getInstance()
                .getPublicConnectionId() != null)
        {
            params.put("connectionId", AblyConnection.getInstance().getPublicConnectionId());
        }

        switch (message.getMessageType()) {
            case Const.kMessageTypeAudio:
            case Const.kMessageTypeImage:
            case Const.kMessageTypeVideo: {
                Uri file = Uri.fromFile(new File(message.getLocalUrl()));
                // Create FirebaseStorage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                // Reference to messages images
                StorageReference riversRef = storageRef.child("messages/" + file.getLastPathSegment());
                // Create upload task
                UploadTask uploadTask = riversRef.putFile(file);

                try {
                    com.google.android.gms.tasks.Tasks.await(
                            // Register observers to listen for when the download is done or if it fails
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();//TODO: REVIEW
                                }
                            })
                    );
                } catch (ExecutionException|InterruptedException e) {
                    Logger.error("SendMessageJob", "File couldn't be added to message " + e.getMessage());
                    message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusParseError);
                    return;
                }

                params.put("file", file);
                params.put("width", message.getWidth());
                params.put("height", message.getHeight());
                params.put("size", message.getBytes());
                break;
            }
            case Const.kMessageTypeLocation: {
                params.put("latitude", message.getLatitude());
                params.put("longitude", message.getLongitude());
                break;
            }

            case Const.kMessageTypeText: {
                params.put("text", message.getBody());
                break;
            }
        }

        try {
            NetworkingManager.getInstance().postSync(getApplicationContext(),"message/sendUserMessage", params);
            message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusAllDelivered);
        } catch (FirebaseCustomException e) {
            if (AppActions.validateParseException(e)) {
                AppActions.appLogout(getApplicationContext(), true);
            } else {
                message.updateMessageStatus(getApplicationContext(), DeliveryStatus.statusParseError);
            }
        }
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Logger.error(TAG, "onCancel called. Reason: " + cancelReason);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.CANCEL;
    }

}
package ee.app.conversa;

import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.events.MessagePressedEvent;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.messageshandling.SendMessageAsync;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.LightTextView;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.MyBottomSheetDialogFragment;

public class ActivityChatWall extends ConversaActivity implements View.OnClickListener,
		View.OnTouchListener, TextWatcher {

	private dbBusiness businessObject;
	private MessagesAdapter gMessagesAdapter;

	private boolean addAsContact;
	private boolean loading;
	private boolean loadMore;
	private boolean newMessagesFromNewIntent;

	private LightTextView mSubTitleTextView;
	private RecyclerView mRvWallMessages;
	private TextView mTvNoMessages;
	private EditText mEtMessageText;
	private BottomSheetDialogFragment myBottomSheet;
	private ImageButton mBtnWallSend;
	private Bitmap bmAvatar;

	private Timer timer;

	public ActivityChatWall() {
		this.loading = true;
		this.loadMore = true;
		this.newMessagesFromNewIntent = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_wall);

		// Deactivate check internet connection
		checkInternetConnection = false;

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if(extras == null) {
				finish();
			} else {
				businessObject = extras.getParcelable(Const.kClassBusiness);
				addAsContact = extras.getBoolean(Const.kYapDatabaseName);
			}
		} else {
			businessObject = savedInstanceState.getParcelable(Const.kClassBusiness);
			addAsContact = savedInstanceState.getBoolean(Const.kYapDatabaseName);
		}

		initialization();
		dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), 20, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stoptimertask();
	}

	public void startTimer() {
		if (loading || timer != null) {
			return;
		}

		//set a new Timer
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (gMessagesAdapter != null && mRvWallMessages != null) {
					final int firstVisibleItem = ((LinearLayoutManager)mRvWallMessages.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
					final int lastVisibleItem = ((LinearLayoutManager)mRvWallMessages.getLayoutManager()).findLastCompletelyVisibleItemPosition();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (firstVisibleItem >= 0) {
								gMessagesAdapter.updateTime(firstVisibleItem, (lastVisibleItem - firstVisibleItem) + 1);
							}
						}
					});
				}
			}
		}, 0, 60000);
	}

	public void stoptimertask() {
		//stop the timer, if it's not already null
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				navigateUp();
				return true;
		}

		return false;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void initialization() {
		super.initialization();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		MediumTextView mTitleTextView = (MediumTextView) toolbar.findViewById(R.id.tvChatName);
		FrameLayout mBackButton = (FrameLayout) toolbar.findViewById(R.id.flBack);
		ImageButton mIbBackButton = (ImageButton) toolbar.findViewById(R.id.ibBack);
		mSubTitleTextView = (LightTextView) toolbar.findViewById(R.id.tvChatStatus);
		SimpleDraweeView ivContactAvatar = (SimpleDraweeView) toolbar.findViewById(R.id.ivAvatarChat);

		Uri uri = Utils.getUriFromString(businessObject.getAvatarThumbFileId());
		Postprocessor redMeshPostprocessor = new BasePostprocessor() {
			@Override
			public String getName() {
				return "redMeshPostprocessor";
			}

			@Override
			public void process(Bitmap bitmap) {
				if (bitmap != null) {
					bmAvatar = bitmap;
				}
			}
		};

		ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
				.setPostprocessor(redMeshPostprocessor)
				.build();

		PipelineDraweeController controller = (PipelineDraweeController)
				Fresco.newDraweeControllerBuilder()
						.setImageRequest(request)
						.setOldController(ivContactAvatar.getController())
						.build();
		ivContactAvatar.setController(controller);

		mIbBackButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
		ivContactAvatar.setOnClickListener(this);
		mTitleTextView.setText(businessObject.getDisplayName());

		setSupportActionBar(toolbar);

		mRvWallMessages = (RecyclerView) findViewById(R.id.rvWallMessages);
		mTvNoMessages = (TextView) findViewById(R.id.tvNoMessages);
		mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
		mBtnWallSend = (ImageButton) findViewById(R.id.btnWallSend);

		myBottomSheet = MyBottomSheetDialogFragment.newInstance(businessObject.getBusinessId(), this);

		ImageButton mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface(ConversaApp.getInstance(this).getTfRalewayRegular());
		mEtMessageText.addTextChangedListener(this);

		gMessagesAdapter = new MessagesAdapter(this, businessObject.getBusinessId());
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		manager.setReverseLayout(true);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setOnTouchListener(this);
		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				final int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
				final int totalItemCount = recyclerView.getLayoutManager().getItemCount();

				// 1. Check if app isn't checking for new messages and last visible item is on the top
				if (!loading && lastVisibleItem == (totalItemCount - 1)) {
					// 2. If load more is true retrieve more messages otherwise skip
					if (loadMore) {
						gMessagesAdapter.addLoad(true);
						mRvWallMessages.scrollToPosition(totalItemCount);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								dbMessage.getAllMessageForChat(getApplicationContext(), businessObject.getBusinessId(), 20, totalItemCount);
							}
						}, 1900);
						loading = true;
					}
				}
			}
		});
		mRvWallMessages.setAdapter(gMessagesAdapter);

		mBtnWallSend.setOnClickListener(this);
		mBtnOpenSlidingDrawer.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		navigateUp();
	}

	private void navigateUp() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}

		Intent upIntent = NavUtils.getParentActivityIntent(this);
		if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is NOT part of this app's task, so create a new task
			// when navigating up, with a synthesized back stack.
			TaskStackBuilder.create(this)
					// Add all of this activity's parents to the back stack
					.addNextIntentWithParentStack(upIntent)
					// Navigate up to the closest parent
					.startActivities();
		} else {
			// This activity is part of this app's task, so simply
			// navigate up to the logical parent activity.
			NavUtils.navigateUpTo(this, upIntent);
		}
	}

	@Override
	protected void openFromNotification(Intent intent) {
		Log.e("openFromNotification", "New intent with flags " + intent.getFlags());
		dbBusiness business = intent.getParcelableExtra(Const.kClassBusiness);

		if (business == null) {
			super.onBackPressed();
		} else {
			addAsContact = intent.getBooleanExtra(Const.kYapDatabaseName, false);

			if (business.getBusinessId().equals(businessObject.getBusinessId())) {
				// Call for new messages
				int count = intent.getIntExtra(Const.kAppVersionKey, 1);
				newMessagesFromNewIntent = true;
				dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), count, 0);
			} else {
				// Set new business reference
				businessObject = business;
				// Clean list of current messages and get new messages
				dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), 20, 0);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.rvWallMessages:
				Utils.hideKeyboard(this);
				return false;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Make sure the request was successful
		if (resultCode == RESULT_OK) {
			// Check which request we're responding to
			switch (requestCode) {
				case ActivityCameraCrop.PICK_CAMERA_REQUEST:
				case ActivityCameraCrop.PICK_GALLERY_REQUEST: {
					SendMessageAsync.sendImageMessage(
							this,
							data.getStringExtra("result"),
							data.getIntExtra("width", 0),
							data.getIntExtra("height", 0),
							data.getIntExtra("bytes", 0),
							addAsContact,
							businessObject);
					break;
				}
				case ActivityLocation.PICK_LOCATION_REQUEST: {
					SendMessageAsync.sendLocationMessage(
							this,
							data.getDoubleExtra("lat", 0),
							data.getDoubleExtra("lon", 0),
							addAsContact,
							businessObject);
					break;
				}
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessagePressedEvent(MessagePressedEvent event) {
		dbMessage message = event.getMessage();

		switch (message.getMessageType()) {
			case Const.kMessageTypeText: {

				break;
			}
			case Const.kMessageTypeLocation: {
				Intent intent = new Intent(this, ActivityLocation.class);
				intent.putExtra(Const.LOCATION, "userLocation");
				intent.putExtra(Const.LATITUDE, message.getLatitude());
				intent.putExtra(Const.LONGITUDE, message.getLongitude());
				startActivity(intent);
				break;
			}
			case Const.kMessageTypeImage: {
				showImage(message);
				break;
			}
			case Const.kMessageTypeVideo: {
				break;
			}
			case Const.kMessageTypeAudio: {
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivAvatarChat:
				// Llamar a Servidor por foto y actualizar
				break;
			case R.id.ibBack:
			case R.id.flBack:
				onBackPressed();
				break;
			case R.id.btnSlideButton:
				myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());
				break;
			case R.id.btnWallSend:
				String body = mEtMessageText.getText().toString().trim();

				if (body.length() > 0) {
					mEtMessageText.setText("");
					SendMessageAsync.sendTextMessage(
							this,
							body,
							addAsContact,
							businessObject);
				}
				break;
		}
	}

	/* ****************************************************************************************** */

	@Override
	public void noInternetConnection() {
		super.noInternetConnection();
		mBtnWallSend.setEnabled(false);
	}

	@Override
	public void yesInternetConnection() {
		super.yesInternetConnection();
		mBtnWallSend.setEnabled(true);
	}

	public void showImage(final dbMessage m) {
		final Intent i = new Intent(this, ActivityImageDetail.class);
		i.putExtra(ActivityImageDetail.EXTRA_IMAGE, m.getFileId());
//			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
//					v, 0, 0, v.getWidth(), v.getHeight());
		ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
				mBtnWallSend, 0, 0, mBtnWallSend.getWidth(), mBtnWallSend.getHeight());
		startActivity(i, options.toBundle());
	}

	@Override
	public void MessagesGetAll(List<dbMessage> messages) {
		// 1. Add messages
		if (mRvWallMessages.getLayoutManager().getItemCount() == 0) {
			// If messages size is zero there's no need to do anything
			if (messages.size() > 0) {
				// Update unread incoming messages
				dbMessage.updateUnreadMessages(this, businessObject.getBusinessId());
				// Set messages
				gMessagesAdapter.setMessages(messages);
				// As this is the first time we load messages, change visibility
				mTvNoMessages.setVisibility(View.GONE);
				mRvWallMessages.setVisibility(View.VISIBLE);
			}

			// Check if we need to load more messages
			if (messages.size() < 20) {
				loadMore = false;
			}
		} else {
			if (newMessagesFromNewIntent) {
				newMessagesFromNewIntent = false;
				gMessagesAdapter.addMessages(messages, 0);
				mRvWallMessages.scrollToPosition(0);
			} else {
				gMessagesAdapter.addLoad(false);

				// No need to check visibility, only add messages to adapter
				gMessagesAdapter.addMessages(messages);
				// Check if we need to load more messages
				if (messages.size() < 20) {
					loadMore = false;
				}
			}
		}

		// 2. Set loading as completed
		loading = false;
		startTimer();
	}

	@Override
	public void MessageSent(final dbMessage response) {
		// 1. Check visibility
		if (mTvNoMessages.getVisibility() == View.VISIBLE) {
			mTvNoMessages.setVisibility(View.GONE);
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if(addAsContact) {
			addAsContact = false;
		}

		// 3. Add message to adapter
		gMessagesAdapter.addMessage(response);
		mRvWallMessages.scrollToPosition(0);
	}

	@Override
	public void MessageDeleted(dbMessage r) {

	}

	@Override
	public void MessageUpdated(dbMessage r) {
		if (r == null) {
			return;
		}

		// 1. Get visible items and first visible item position
		int visibleItemCount = mRvWallMessages.getChildCount();
		int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
		// 2. Update message
		gMessagesAdapter.updateMessage(r, firstVisibleItem, visibleItemCount);
	}

	@Override
	public void MessageReceived(dbMessage message) {
		// 1. Check if this message belongs to this conversation
		if (message.getFromUserId().equals(businessObject.getBusinessId())) {
			// 2. Check visibility
			if (mTvNoMessages.getVisibility() == View.VISIBLE) {
				mTvNoMessages.setVisibility(View.GONE);
				mRvWallMessages.setVisibility(View.VISIBLE);
			}

			// 3. Add to adapter
			gMessagesAdapter.addMessage(message);
			mRvWallMessages.scrollToPosition(0);
		} else {
			super.MessageReceived(message);
		}
	}

	@Override
	public void ContactAdded(dbBusiness response) {
		Utils.saveToInternalStorage(this, bmAvatar, response.getId());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().isEmpty()) {
			mBtnWallSend.setEnabled(false);
		} else {
			mBtnWallSend.setEnabled(true);
		}
	}

}
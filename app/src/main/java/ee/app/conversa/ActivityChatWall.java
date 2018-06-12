package ee.app.conversa;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flurry.android.FlurryAgent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.camera.ImagePickerDemo;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.FunctionCallback;
import ee.app.conversa.interfaces.OnMessageClickListener;
import ee.app.conversa.management.AblyConnection;
import ee.app.conversa.messaging.MessageDeleteReason;
import ee.app.conversa.messaging.MessageUpdateReason;
import ee.app.conversa.messaging.SendMessageAsync;
import ee.app.conversa.model.database.dbBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.networking.FirebaseCustomException;
import ee.app.conversa.networking.NetworkingManager;
import ee.app.conversa.utils.AppActions;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Logger;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.MediumTextView;
import ee.app.conversa.view.MyBottomSheetDialogFragment;
import ee.app.conversa.view.RegularTextView;

public class ActivityChatWall extends ConversaActivity implements View.OnClickListener, OnMessageClickListener {

	private dbBusiness businessObject;
	private MessagesAdapter gMessagesAdapter;

	private boolean typingFlag = false;
	private boolean addAsContact;
	private boolean loading;
	private boolean loadMore;
	private boolean newMessagesFromNewIntent;
	private int itemPosition;

	private Handler isUserTypingHandler = new Handler();
	private RegularTextView mSubTitleTextView;
	private RecyclerView mRvWallMessages;
	private EditText mEtMessageText;
	private MyBottomSheetDialogFragment myBottomSheet;
	private ImageButton mBtnWallSend;
	private SimpleDraweeView ivContactAvatar;
	private MediumTextView mTitleTextView;

	private Timer typingTimer;
	private String lastStatus;

	public final static int PERMISSION_RQ = 84;

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
		// EventBus should not unregister on onStop
		unregisterListener = false;

		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			if (extras == null) {
				finish();
			} else {
				businessObject = extras.getParcelable(Const.iExtraBusiness);
				addAsContact = extras.getBoolean(Const.iExtraAddBusiness);
				itemPosition = extras.getInt(Const.iExtraPosition, -1);
			}
		} else {
			businessObject = savedInstanceState.getParcelable(Const.iExtraBusiness);
			addAsContact = savedInstanceState.getBoolean(Const.iExtraAddBusiness);
			itemPosition = savedInstanceState.getInt(Const.iExtraPosition, -1);
		}

		initialization();
		dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), 20, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Map<String, String> articleParams = new HashMap<>(1);
		String customer_id = ConversaApp.getInstance(this).getPreferences().getAccountCustomerId();
		articleParams.put("user", (customer_id == null) ? "" : customer_id);
		FlurryAgent.logEvent("user_chat_duration", articleParams, true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FlurryAgent.endTimedEvent("user_chat_duration");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(Const.iExtraBusiness, businessObject);
		outState.putBoolean(Const.iExtraAddBusiness, addAsContact);
		outState.putInt(Const.iExtraPosition, itemPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void openFromNotification(Intent intent) {
		Logger.error("openFromNotification", "New intent: " + intent.toString());
		dbBusiness business = intent.getParcelableExtra(Const.iExtraBusiness);

		if (business == null) {
			finish();
		} else {
			addAsContact = intent.getBooleanExtra(Const.iExtraAddBusiness, false);
			itemPosition = intent.getIntExtra(Const.iExtraPosition, -1);
			typingFlag = false;

			if (business.getBusinessId().equals(businessObject.getBusinessId())) {
				// Call for new messages
				int count = intent.getIntExtra(Const.kAppVersionKey, 1);
				newMessagesFromNewIntent = true;
				dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), count, 0);
			} else {
				// Change name and avatar
				Uri uri = Utils.getUriFromString(business.getAvatarThumbFileId());

				if (uri == null) {
					uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
				}

				lastStatus = getString(R.string.app_name);
				ivContactAvatar.setImageURI(uri);
				mTitleTextView.setText(business.getDisplayName());
				mSubTitleTextView.setText(lastStatus);
				loadLastStatus();
				// Set new business reference
				businessObject = business;
				// Clean list of current messages and get new messages
				loadMore = true;
				gMessagesAdapter.clearMessages();
				mRvWallMessages.setEnabled(false);
				dbMessage.getAllMessageForChat(this, business.getBusinessId(), 20, 0);
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	private Runnable isUserTypingRunnable = new Runnable() {
		@Override
		public void run() {
			Logger.error("isUserTypingRunnable", "Try to send typing ended update");
			AblyConnection.getInstance().userHasEndedTyping(businessObject.getBusinessId());
			typingFlag = false;
		}
	};

	private TextWatcher isUserTypingTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public void afterTextChanged(Editable s) {
			isUserTypingHandler.removeCallbacks(isUserTypingRunnable);

			if (s.toString().isEmpty()) {
				mBtnWallSend.setEnabled(false);
				isUserTypingHandler.postDelayed(isUserTypingRunnable, 0);
			} else {
				mBtnWallSend.setEnabled(true);

				if (!typingFlag) {
					Logger.error("isUserTypingRunnable", "Try to send typing started update");
					AblyConnection.getInstance().userHasStartedTyping(businessObject.getBusinessId());
					typingFlag = true;
				}

				isUserTypingHandler.postDelayed(isUserTypingRunnable, 3500);
			}
		}
	};

	@Override
	@SuppressWarnings("ConstantConditions")
	protected void initialization() {
		super.initialization();

		Toolbar toolbar = findViewById(R.id.toolbar);
		mTitleTextView = toolbar.findViewById(R.id.tvChatName);
		FrameLayout mBackButton = toolbar.findViewById(R.id.flBack);
		ImageButton mIbBackButton = toolbar.findViewById(R.id.ibBack);
		mSubTitleTextView = toolbar.findViewById(R.id.tvChatStatus);
		ivContactAvatar = toolbar.findViewById(R.id.ivAvatarChat);

		Uri uri = Utils.getUriFromString(businessObject.getAvatarThumbFileId());

		if (uri == null) {
			uri = Utils.getDefaultImage(this, R.drawable.ic_business_default);
		}

		lastStatus = getString(R.string.app_name);

		ivContactAvatar.setImageURI(uri);
		mTitleTextView.setText(businessObject.getDisplayName());
		toolbar.findViewById(R.id.rlChatHeader).setOnClickListener(this);

		setSupportActionBar(toolbar);

		mRvWallMessages = findViewById(R.id.rvWallMessages);
		mEtMessageText = findViewById(R.id.etWallMessage);
		mBtnWallSend = findViewById(R.id.btnWallSend);

		mBtnWallSend.setEnabled(false);

		myBottomSheet = MyBottomSheetDialogFragment.newInstance(businessObject.getBusinessId(), this);

		ImageButton mBtnOpenSlidingDrawer = findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface(ConversaApp.getInstance(this).getTfRalewayRegular());
		mEtMessageText.addTextChangedListener(isUserTypingTextWatcher);

		gMessagesAdapter = new MessagesAdapter(this,
				ConversaApp.getInstance(this).getPreferences().getAccountCustomerId(),
				this);
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setAdapter(gMessagesAdapter);
		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				// 1. If load more is true retrieve more messages otherwise skip
				if (loadMore) {
					final int lastVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager())
							.findFirstCompletelyVisibleItemPosition();
					final int totalItemCount = recyclerView.getLayoutManager().getItemCount();

					// 2. Check if app isn't checking for new messages and last visible item is on the top
					if (!loading && lastVisibleItem == 0) {
						gMessagesAdapter.addLoad(true);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								dbMessage.getAllMessageForChat(getApplicationContext(),
										businessObject.getBusinessId(), 20, totalItemCount);
							}
						}, 1900);
						loading = true;
					}
				}
			}
		});
		mRvWallMessages.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v,
									   int left, int top, int right, final int bottom,
									   int oldLeft, int oldTop, int oldRight, final int oldBottom) {
				if (bottom < oldBottom) {
					mRvWallMessages.postDelayed(new Runnable() {
						@Override
						public void run() {
							mRvWallMessages.smoothScrollToPosition(
									gMessagesAdapter.getItemCount() - 1
							);
						}
					}, 100);
				}
			}
		});

		mBtnWallSend.setOnClickListener(this);
		mBtnOpenSlidingDrawer.setOnClickListener(this);
		mIbBackButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
		ivContactAvatar.setOnClickListener(this);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Request permission to save videos in external storage
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
		}

		loadLastStatus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				navigateUp();
				return true;
		}

		return false;
	}

	@Override
	public void onBackPressed() {
		navigateUp();
	}

	private void navigateUp() {
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
	public void onMessageClick(dbMessage message, View view, int position) {
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
				Intent i = new Intent(this, ActivityImageDetail.class);
				i.putExtra(ActivityImageDetail.EXTRA_IMAGE, message.getLocalUrl());
				ActivityOptions options = ActivityOptions.makeScaleUpAnimation(
						view, 0, 0, view.getWidth(), view.getHeight());
				startActivity(i, options.toBundle());
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
			case R.id.rlChatHeader: {
				Intent intent = new Intent(this, ActivityProfile.class);
				intent.putExtra(Const.iExtraBusiness, businessObject);
				startActivity(intent);
				overridePendingTransition(0, 0);
				break;
			}
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
					//insert here the code to send the images.
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && data != null) {
			switch (requestCode) {
				case ActivityLocation.PICK_LOCATION_REQUEST: {
					SendMessageAsync.sendLocationMessage(
							this,
							data.getDoubleExtra("lat", 0),
							data.getDoubleExtra("lon", 0),
							addAsContact,
							businessObject);
					break;
				}
				case Const.CAPTURE_MEDIA: {
					/*
					String path = ImageFilePath.getPath(this, Uri.parse(data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH)));
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(path, options);

					SendMessageAsync.sendImageMessage(
							this,
							path,
							options.outWidth,
							options.outHeight,
							new File(path == null ? "" : path).length(),
							addAsContact,
							businessObject);
					*/
					break;
				}
				case Const.CAPTURE_VIDEO: {
					/*
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					retriever.setDataSource(data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));
					int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
					int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
					int duration = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
					retriever.release();
					*/
					break;

                }
                case ImagePickerDemo.CAMERA_CODE_ACTIVITY: {
					String uri = data.getStringExtra("imageUri");
					SendMessageAsync.sendImageMessage(this,uri,200, 200, 200 ,addAsContact, businessObject);
					break;
                }
			}
		} else {
			Logger.error("onActivityResult", "Error");
		}
	}

	/* ****************************************************************************************** */

	private void loadLastStatus() {
		final WeakReference<ActivityChatWall> wActivity = new WeakReference<>(this);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

				final HashMap<String, String> params = new HashMap<>(1);
				params.put("businessId", businessObject.getBusinessId());

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						NetworkingManager.getInstance().post("getBusinessLastStatus", params, new FunctionCallback<Long>() {
							@Override
							public void done(Long last, FirebaseCustomException e) {
								if (e != null) {
									if (AppActions.validateParseException(e)) {
										AppActions.appLogout(getApplicationContext(), true);
									}
								} else {
									ActivityChatWall activity = wActivity.get();
									if (activity != null) {
										long now = System.currentTimeMillis();

										if (last <= now) {
											long diff = now - last;
											long diffm = diff / (1000 * 60);

											if (diffm < 63) {
												if (diffm <= 3) {
													lastStatus = getString(R.string.chat_status_now);
												} else {
													diffm = diffm - 3;
													if (diffm == 1) {
														lastStatus = getString(R.string.chat_status, 1, getString(R.string.chat_status_minute));
													} else {
														lastStatus = getString(R.string.chat_status, diffm, getString(R.string.chat_status_minutes));
													}
												}
											} else {
												diff = diff - (3 * 60 * 1000);
												long diffh = diff / (1000 * 60 * 60);
												long diffd = diff / (1000 * 60 * 60 * 24);
												long diffw = diff / (1000 * 60 * 60 * 24 * 7);

												if (diffh < 24) {
													if (diffh == 1) {
														lastStatus = getString(R.string.chat_status, 1, getString(R.string.chat_status_hour));
													} else {
														lastStatus = getString(R.string.chat_status, diffh, getString(R.string.chat_status_hours));
													}
												} else if (diffd < 31) {
													if (diffd == 1) {
														lastStatus = getString(R.string.chat_status, 1, getString(R.string.chat_status_day));
													} else {
														lastStatus = getString(R.string.chat_status, diffd, getString(R.string.chat_status_days));
													}
												} else if (diffw < 52) {
													if (diffw == 1) {
														lastStatus = getString(R.string.chat_status, 1, getString(R.string.chat_status_week));
													} else {
														lastStatus = getString(R.string.chat_status, diffw, getString(R.string.chat_status_weeks));
													}
												} else {
													diffw = diffw / 52;
													if (diffw == 1) {
														lastStatus = getString(R.string.chat_status, 1, getString(R.string.chat_status_year));
													} else {
														lastStatus = getString(R.string.chat_status, diffw, getString(R.string.chat_status_years));
													}
												}
											}
										}

										if (!typingFlag)
											mSubTitleTextView.setText(lastStatus);
									}
								}
							}
						});
					}
				});
			}
		}, (long) (2.5 * 1000));
	}

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

	@Override
	public void MessagesGetAll(List<dbMessage> messages) {
		// 1. Add messages
		if (mRvWallMessages.getLayoutManager().getItemCount() == 0) {
			mRvWallMessages.setEnabled(true);

			// If messages size is zero there's no need to do anything
			if (messages.size() > 0) {
				// Update unread incoming messages
				dbMessage.updateViewMessages(this, businessObject.getBusinessId());
				// Set messages
				gMessagesAdapter.setMessages(messages);
				// As this is the first time we load messages, change visibility
				mRvWallMessages.setVisibility(View.VISIBLE);
				mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);
			}

			// Check if we need to load more messages
			if (messages.size() < 20) {
				loadMore = false;
			}

		} else {
			if (newMessagesFromNewIntent) {
				newMessagesFromNewIntent = false;
				gMessagesAdapter.addMessages(messages);
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
	}

	@Override
	public void MessageSent(dbMessage message) {
		// 1. Check visibility
		if (mRvWallMessages.getVisibility() == View.GONE) {
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if (addAsContact) {
			addAsContact = false;
		}

		if (ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenSending()) {
			playSound(true);
		}

		// 3. Add message to adapter
		int visibleItemCount = mRvWallMessages.getChildCount();
		int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
		gMessagesAdapter.addMessage(message, firstVisibleItem, visibleItemCount);
		mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);
	}

	@Override
	public void MessageDeleted(List<String> response, MessageDeleteReason reason) {

	}

	@Override
	public void MessageUpdated(dbMessage message, MessageUpdateReason reason) {
		// 1. Get visible items and first visible item position
		int visibleItemCount = mRvWallMessages.getChildCount();
		int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
		// 2. Update message
		switch (reason) {
			case FILE_DOWNLOAD:
				gMessagesAdapter.updateImageView(message, firstVisibleItem, visibleItemCount);
				break;
			case VIEW:
				break;
			case STATUS:
				gMessagesAdapter.updateStatus(message, firstVisibleItem, visibleItemCount);
				break;
		}
	}

	@Override
	public void MessageReceived(dbMessage message) {
		// 1. Check if this message belongs to this conversation
		if (message.getFromUserId().equals(businessObject.getBusinessId())) {
			dbMessage.updateViewMessages(this, businessObject.getBusinessId());

			// 2. Check visibility
			if (mRvWallMessages.getVisibility() == View.GONE) {
				mRvWallMessages.setVisibility(View.VISIBLE);
			}

			if (ConversaApp.getInstance(this).getPreferences().getPlaySoundWhenReceiving()) {
				playSound(false);
			}

			if (!lastStatus.equals(getString(R.string.chat_status_now))) {
				lastStatus = getString(R.string.chat_status_now);
			}

			// 3. Add to adapter
			int visibleItemCount = mRvWallMessages.getChildCount();
			int firstVisibleItem = ((LinearLayoutManager) mRvWallMessages.getLayoutManager()).findFirstVisibleItemPosition();
			gMessagesAdapter.addMessage(message, firstVisibleItem, visibleItemCount);
			mRvWallMessages.scrollToPosition(gMessagesAdapter.getItemCount() - 1);

			// 4. Update is typing
			onTypingMessage(businessObject.getBusinessId(), false);
		} else {
			super.MessageReceived(message);
		}
	}

	@Override
	public void onTypingMessage(String from, boolean isTyping) {
		if (from.equals(businessObject.getBusinessId())) {
			if (typingTimer != null) {
				typingTimer.cancel();
				typingTimer = null;
			}

			if (isTyping) {
				typingTimer = new Timer();
				typingTimer.schedule(new TimerTask() {
					public void run() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showIsTyping(false);
							}
						});
					}
				}, 6000);
			}

			showIsTyping(isTyping);
		}
	}

	public void showIsTyping(boolean show) {
		if (show) {
			mSubTitleTextView.setText(R.string.is_typing);
		} else {
			mSubTitleTextView.setText(lastStatus);
		}
	}

	private void playSound(final boolean sound) {
		final SoundPool sounds;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AudioAttributes attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_NOTIFICATION)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();
			sounds = new SoundPool.Builder()
					.setAudioAttributes(attributes)
					.build();
		} else {
			sounds = new SoundPool(15, AudioManager.STREAM_NOTIFICATION, 0);
		}

		sounds.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
				/** soundId for Later handling of sound pool **/
				// in 2nd param u have to pass your desire ringtone
				sounds.play(soundId, 0.99f, 0.99f, 1, 0, 0.99f);
			}
		});

		if (sound) {
			sounds.load(getApplicationContext(), R.raw.message_sent, 1);
		} else {
			sounds.load(getApplicationContext(), R.raw.message_received, 1);
		}
	}

}
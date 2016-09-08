package ee.app.conversa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.events.MessagePressedEvent;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.messageshandling.SendMessageAsync;
import ee.app.conversa.model.database.Location;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.TouchImageView;


public class ActivityChatWall extends ConversaActivity implements View.OnClickListener,
		View.OnTouchListener, TextWatcher {

	private dBusiness businessObject;
	private boolean addAsContact;

	public List<Location> gLocations;
	public MessagesAdapter gMessagesAdapter;
	public ChatsAdapter mChatsAdapter;

	private boolean loading;
	private boolean loadMore;
	private boolean newMessagesFromNewIntent;

	public static RecyclerView mRvWallMessages;
	private static TouchImageView mTivPhotoImage;
	public static TextView mTvNoMessages;
	private EditText mEtMessageText;
	private BottomSheetBehavior mBottomSheetBehavior;
	private RelativeLayout rlImageDisplay;
	private Button mBtnWallSend;

	public ActivityChatWall() {
		this.mChatsAdapter = null;
		this.gLocations = new ArrayList<>();
		this.loading = false;
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
				businessObject = null;
				addAsContact = true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				ActionBar actionBar = getSupportActionBar();
				if (actionBar != null) {
					actionBar.setDisplayHomeAsUpEnabled(false);
				}

				super.onBackPressed();
				return true;
		}

		return false;
	}

	@Override
	protected void initialization() {
		super.initialization();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		TextView mTitleTextView = (TextView) toolbar.findViewById(R.id.tvChatName);
		ImageView imageButton = (ImageView) toolbar.findViewById(R.id.ivAvatarChat);
		imageButton.setOnClickListener(this);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		mRvWallMessages = (RecyclerView) findViewById(R.id.rvWallMessages);
		mTvNoMessages = (TextView) findViewById(R.id.tvNoMessages);
		mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
		mBtnWallSend = (Button) findViewById(R.id.btnWallSend);
		rlImageDisplay = (RelativeLayout) findViewById(R.id.rlImageDisplay);
		mTivPhotoImage = (TouchImageView) findViewById(R.id.tivPhotoImage);

		View bottomSheet = findViewById(R.id.bottom_sheet);
		mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

		Button mBtnBack	= (Button) findViewById(R.id.btnCloseImage);
//		Button mBtnProfile = (Button) findViewById(R.id.btnProfile);
//		Button mBtnLocations = (Button) findViewById(R.id.btnLocations);
//		Button mBtnCloseLocts = (Button) findViewById(R.id.btnCloseLocation);
//		Button mBtnReloadLocts = (Button) findViewById(R.id.btnReloadLocation);
		ImageButton mBtnCamera = (ImageButton) findViewById(R.id.btnCamera);
		ImageButton mBtnGallery = (ImageButton) findViewById(R.id.btnGallery);
		ImageButton mBtnLocation = (ImageButton) findViewById(R.id.btnLocation);
		ImageButton mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface( ConversaApp.getInstance(this).getTfRalewayRegular());
//		mBtnBlockUser.setTypeface(ConversaApp.getTfRalewayRegular());
//		mBtnProfile.setTypeface(    ConversaApp.getTfRalewayRegular());
		mBtnWallSend.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());
		mBtnBack.setTypeface(ConversaApp.getInstance(this).getTfRalewayMedium());

		mEtMessageText.addTextChangedListener(this);

		gMessagesAdapter = new MessagesAdapter(this, businessObject.getBusinessId());
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		manager.setReverseLayout(true);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setOnTouchListener(this);
		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				int lastVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
				int totalItemCount = recyclerView.getLayoutManager().getItemCount();

				// 1. Check if app isn't checking for new messages and last visible item is on the top
				if (!loading && lastVisibleItem == (totalItemCount - 1)) {
					// 2. If load more is true retrieve more messages otherwise skip
					if (loadMore) {
						dbMessage.getAllMessageForChat(getApplicationContext(), businessObject.getBusinessId(), 20, totalItemCount);
						loading = true;
					}
				}
			}
		});
		mRvWallMessages.setAdapter(gMessagesAdapter);

		mBtnWallSend.setOnClickListener(this);
		mBtnOpenSlidingDrawer.setOnClickListener(this);

		mBtnCamera.setOnClickListener(this);
		mBtnGallery.setOnClickListener(this);
		mBtnLocation.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);
//		mBtnBlockUser.setOnClickListener(this);
//		mBtnLocations.setOnClickListener(this);
//		mBtnCloseLocts.setOnClickListener(this);
//		mBtnReloadLocts.setOnClickListener(this);
//		mBtnProfile.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		if(closeThisFirst()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void openFromNotification(Intent intent) {
		Log.e("openFromNotification", "New intent with flags " + intent.getFlags());
		dBusiness business = intent.getParcelableExtra(Const.kClassBusiness);

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
				closeThisFirst();
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
		if(v instanceof ImageButton) {
			switch (v.getId()) {
				case R.id.btnCamera: {
					Intent intent = new Intent(this, ActivityCameraCrop.class);
					intent.putExtra("type", "camera");
					startActivityForResult(intent, ActivityCameraCrop.PICK_CAMERA_REQUEST);
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					break;
				}
				case R.id.btnGallery: {
					Intent intent = new Intent(this, ActivityCameraCrop.class);
					intent.putExtra("type", "gallery");
					startActivityForResult(intent, ActivityCameraCrop.PICK_GALLERY_REQUEST);
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					break;
				}
				case R.id.btnLocation: {
					Intent intent = new Intent(getApplicationContext(), ActivityLocation.class);
					intent.putExtra(Const.LOCATION, businessObject.getBusinessId());
					startActivityForResult(intent, ActivityLocation.PICK_LOCATION_REQUEST);
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
					break;
				}
				case R.id.btnSlideButton:
					mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
					break;
			}
		} else if (v instanceof ImageView) {
			switch (v.getId()) {
				case R.id.ivAvatarChat:
					// Llamar a Servidor por foto y actualizar
					break;
			}
		} else if (v instanceof Button) {
			switch (v.getId()) {
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
				case R.id.btnCloseImage:
					closeImage();
					break;
			}
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

	private boolean closeThisFirst() {
		if(rlImageDisplay.getVisibility() == View.VISIBLE) {
			closeImage();
			return false;
		} if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
			return false;
		} else {
			return true;
		}
	}

	public void showImage(final dbMessage m) {
		//Utils.hideKeyboard(this);

		try {
			//File image = new File(m.getFileId());
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			Bitmap bitmap = BitmapFactory.decodeFile(m.getFileId(), bmOptions);
			bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
			mTivPhotoImage.setImageBitmap(bitmap);
			final Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
					R.anim.slide_in);
			rlImageDisplay.startAnimation(slidein);
			rlImageDisplay.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			// Couldn't open image, get default image
		}
	}

	public void closeImage() {
		final Animation slideout = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out);
		rlImageDisplay.startAnimation(slideout);
		rlImageDisplay.setVisibility(View.GONE);
		mTivPhotoImage.setImageBitmap(null);
		mTivPhotoImage.resetZoom();
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
				mRvWallMessages.getLayoutManager().smoothScrollToPosition(mRvWallMessages, null, messages.size() - 1);
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
				// No need to check visibility, only add messages to adapter
				mRvWallMessages.scrollToPosition(mRvWallMessages.getLayoutManager().getChildCount() - 1);
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
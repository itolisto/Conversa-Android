package ee.app.conversa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.messageshandling.SaveUserAsync;
import ee.app.conversa.messageshandling.SendMessageAsync;
import ee.app.conversa.model.database.Location;
import ee.app.conversa.model.database.dBusiness;
import ee.app.conversa.model.database.dbMessage;
import ee.app.conversa.receivers.FileUploadingReceiver;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.TouchImageView;


public class ActivityChatWall extends ConversaActivity implements OnClickListener, FileUploadingReceiver.Receiver {

	public static ActivityChatWall sInstance;

	private dBusiness businessObject;
	private boolean addAsContact;

	public List<Location> gLocations;
	public MessagesAdapter gMessagesAdapter;
	public ChatsAdapter mChatsAdapter;
	public FileUploadingReceiver mReceiver;

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

	public static boolean gIsVisible = false;

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
		mReceiver = new FileUploadingReceiver(new Handler());
		mReceiver.setReceiver(this);
		dbMessage.getAllMessageForChat(this, businessObject.getBusinessId(), 20, 0);

		sInstance = this;
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

	private boolean closeThisFirst() {
		if(rlImageDisplay.getVisibility() == View.VISIBLE) {
			closeImage();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onBackPressed() {
		if(closeThisFirst()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void openFromNotification(Intent intent) {
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
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		gIsVisible = hasFocus;
	}

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
		ImageButton mBtnMore = (ImageButton) findViewById(R.id.btnMore);
		ImageButton mBtnLocation = (ImageButton) findViewById(R.id.btnLocation);
		ImageButton mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);

		mEtMessageText.setTypeface( ConversaApp.getTfRalewayRegular());
//		mBtnBlockUser.setTypeface(ConversaApp.getTfRalewayRegular());
//		mBtnProfile.setTypeface(    ConversaApp.getTfRalewayRegular());
		mBtnWallSend.setTypeface(ConversaApp.getTfRalewayMedium());
//		mBtnBack.setTypeface(ConversaApp.getTfRalewayMedium());

		gMessagesAdapter = new MessagesAdapter(this);
		LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		manager.setReverseLayout(true);
		mRvWallMessages.setLayoutManager(manager);
		mRvWallMessages.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Utils.hideKeyboard(sInstance);
				closeThisFirst();
				return false;
			}
		});

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

//		mRvLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

		//MultiSelector mMultiSelector = new SingleSelector();
//		gLocationsAdapter = new LocationsAdapter(this, gLocations);//, mMultiSelector);
//		mRvLocation.setAdapter(gLocationsAdapter);

		mBtnWallSend.setOnClickListener(this);
		mBtnOpenSlidingDrawer.setOnClickListener(this);

		mBtnCamera.setOnClickListener(this);
		mBtnGallery.setOnClickListener(this);
		mBtnLocation.setOnClickListener(this);
		mBtnMore.setOnClickListener(this);
//		mBtnBack.setOnClickListener(this);
//		mBtnBlockUser.setOnClickListener(this);
//		mBtnLocations.setOnClickListener(this);
//		mBtnCloseLocts.setOnClickListener(this);
//		mBtnReloadLocts.setOnClickListener(this);
//		mBtnProfile.setOnClickListener(this);
	}

	/* ****************************************************************************************** */
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

	@Override
	protected void handlePushNotification(Intent intent) {
		Bundle i = intent.getExtras();
		dbMessage m = (dbMessage) i.get("message");
		showImage(m);
	}

	public void showImage(final dbMessage m){
		Utils.hideKeyboard(this);
		final Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_in);
		rlImageDisplay.startAnimation(slidein);
		rlImageDisplay.setVisibility(View.VISIBLE);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Make sure the request was successful
		if (resultCode == RESULT_OK) {
			// Check which request we're responding to
			switch (requestCode) {
				case ActivityCameraCrop.PICK_CAMERA_REQUEST: {

					break;
				}
				case ActivityCameraCrop.PICK_GALLERY_REQUEST: {

					break;
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		if(v instanceof ImageButton) {
			switch (v.getId()) {
				case R.id.btnCamera:
					Intent intent = new Intent(this, ActivityCameraCrop.class);
					intent.putExtra("type", "camera");
					startActivityForResult(intent, ActivityCameraCrop.PICK_CAMERA_REQUEST);
					break;
				case R.id.btnGallery:
					Intent intent1 = new Intent(this, ActivityCameraCrop.class);
					intent1.putExtra("type", "gallery");
					startActivityForResult(intent1, ActivityCameraCrop.PICK_GALLERY_REQUEST);
					break;
				case R.id.btnLocation:
					Intent intent2 = new Intent(getApplicationContext(), ActivityLocation.class);
					intent2.putExtra(Const.LOCATION, businessObject.getBusinessId());
					startActivity(intent2);
					break;
				case R.id.btnMore:
					// Definir si habrán más opciones
					break;
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

					if (!body.isEmpty()) {
						mEtMessageText.setText("");
						SendMessageAsync.sendTextMessage(this, businessObject.getBusinessId(), body);
					}
					break;
				case R.id.btnCloseImage:
					closeImage();
					break;
			}
		}
	}

	@Override
	public void messagesGetAll(List<dbMessage> messages) {
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
	public void messageSent(final dbMessage response, ParseFile file) {
		// 1. Check visibility
		if (mTvNoMessages.getVisibility() == View.VISIBLE) {
			mTvNoMessages.setVisibility(View.GONE);
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if(addAsContact) {
			SaveUserAsync.saveBusinessAsContact(this, businessObject);
			addAsContact = false;
		}

		// 3. Add message to adapter
		gMessagesAdapter.addMessage(response);
		mRvWallMessages.scrollToPosition(0);
	}

	@Override
	public void messageDeleted(dbMessage r) {

	}

	@Override
	public void messageUpdated(dbMessage r) {
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
	public void onReceiveResult(dbMessage message, int percentage) {

	}

}
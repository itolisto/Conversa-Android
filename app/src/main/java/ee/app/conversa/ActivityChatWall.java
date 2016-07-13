package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.messageshandling.SaveUserAsync;
import ee.app.conversa.messageshandling.SendMessageAsync;
import ee.app.conversa.model.Database.Location;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.response.MessageResponse;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.TouchImageView;


public class ActivityChatWall extends ConversaActivity implements OnClickListener {

	public static ActivityChatWall sInstance;

	private dBusiness businessObject;
	private boolean addAsContact;

	public List<Location> gLocations;
	public MessagesAdapter gMessagesAdapter;
	public ChatsAdapter mChatsAdapter;

	private boolean loading;
	private int previousTotal;

	public static RecyclerView mRvWallMessages;
	private static TouchImageView mTivPhotoImage;
	public static TextView mTvNoMessages;
	private EditText mEtMessageText;
	private BottomSheetBehavior mBottomSheetBehavior;
	private RelativeLayout rlImageDisplay;
	private Button mBtnWallSend;

	public static boolean gIsVisible = false;
	private final IntentFilter mPushFilter = new IntentFilter(MessagesAdapter.PUSH);

	public ActivityChatWall() {
		this.mChatsAdapter = null;
		this.gLocations = new ArrayList<>();
		this.previousTotal = 0;
		this.loading = false;
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

		Message.getAllMessageForChat(businessObject.getBusinessId(), previousTotal);

		sInstance = this;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
			getIntent().removeExtra(Const.PUSH_INTENT);
			openWallFromNotification(intent);
		}

		addAsContact = getIntent().getBooleanExtra(Const.kYapDatabaseName, true);
	}

	@Override
	public void onStart() {
		super.onStart();
		ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);
		ConversaApp.getLocalBroadcastManager().registerReceiver(receiver, newMessageFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(receiver);
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

	private void openWallFromNotification(Intent intent) {
		String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
//		User fromUser     = ConversaApp.getDB().isContact(fromUserId);

//		if(fromUser == null) {
//			try {
//				fromUser = new ConversaAsyncTask<Void, Void, User>(
//						new CouchDB.FindBusinessById(fromUserId), null, getApplicationContext(), true
//				).execute().get();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}

//		if(fromUser != null) {
		//UsersManagement.setToUser(fromUser);
		//SettingsManager.ResetSettings();
//			if (ActivityChatWall.gCurrentMessages != null)
//				ActivityChatWall.gCurrentMessages.clear();

//			setWallMessages();
//		}
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
				int visibleItemCount = recyclerView.getChildCount();
				int totalItemCount = recyclerView.getLayoutManager().getItemCount();
				int firstVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

				// 1. Check if app isn't checking for new messages and first visible item is on the top
				if (!loading && firstVisibleItem == 0) {
					// 2. If total item count is equal to a multiply of 20, retrieve more messages
					if (totalItemCount == (20 * previousTotal)) {
						Log.e("...", "messages called" + visibleItemCount + "," + totalItemCount + "," + firstVisibleItem);
						Message.getAllMessageForChat(businessObject.getBusinessId(), previousTotal);
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

	private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Bundle i = intent.getExtras();
			Message m = (Message) i.get("message");
			showImage(m);
		}
	};

	public void showImage(final Message m){
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
	public void onClick(View v) {
		if(v instanceof ImageButton) {
			switch (v.getId()) {
				case R.id.btnCamera:
					Intent intent = new Intent(getApplicationContext(), ActivityCameraCrop.class);
					intent.putExtra("type", "camera");
					ActivityChatWall.sInstance.startActivity(intent);
					break;
				case R.id.btnGallery:
					Intent intent1 = new Intent(getApplicationContext(), ActivityCameraCrop.class);
					intent1.putExtra("type", "gallery");
					ActivityChatWall.sInstance.startActivity(intent1);
					break;
				case R.id.btnLocation:
					Intent intent2 = new Intent(getApplicationContext(), ActivityLocation.class);
					intent2.putExtra(Const.LOCATION, "myLocation");
					ActivityChatWall.sInstance.startActivity(intent2);
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
						SendMessageAsync.sendTextMessage(businessObject.getBusinessId(), body);
					}
					break;
				case R.id.btnCloseImage:
					closeImage();
					break;
			}
		}
	}

	@Override
	public void MessagesGetAll(MessageResponse r) {
		List<Message> messages = r.getMessages();
		// 1. Add messages
		if (previousTotal == 0) {
			// If messages size is zero there's no need to do anything
			if (messages.size() > 0) {
				gMessagesAdapter.addMessages(messages, 0);
				mRvWallMessages.getLayoutManager().smoothScrollToPosition(mRvWallMessages, null, messages.size() - 1);
				// Check visibility only if this is the first time we load messages
				mTvNoMessages.setVisibility(View.GONE);
				mRvWallMessages.setVisibility(View.VISIBLE);
			}
		} else {
			// No need to check visibility, only add messages to adapter
			gMessagesAdapter.addMessages(messages, 0);
		}

		// 2. Increase number of page retrieve
		previousTotal++;
		// 3. Set loading as completed
		loading = false;
	}

	@Override
	public void MessageSent(MessageResponse r) {
		final Message response = r.getMessage();

		// 1. Check visibility
		if (mTvNoMessages.getVisibility() == View.VISIBLE) {
			mTvNoMessages.setVisibility(View.GONE);
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Check if user needs to be added
		if(addAsContact) {
			SaveUserAsync.saveBusinessAsContact(businessObject);
			addAsContact = false;
		}

		// 3. Add message to adapter
		gMessagesAdapter.addMessage(response);
		mRvWallMessages.scrollToPosition(mRvWallMessages.getLayoutManager().getChildCount());
		// 4. Save to Parse. Set parameters accordingly to message type
		HashMap<String, String> params = new HashMap<>();
		params.put("user", response.getFromUserId());
		params.put("business", response.getToUserId());
		params.put("text", response.getBody());
		params.put("fromUser", String.valueOf(true));
		ParseCloud.callFunctionInBackground("sendUserMessage", params, new FunctionCallback<Boolean>() {
			@Override
			public void done(Boolean result, ParseException e) {
				// 4.1. Update local db delivery
				if (e == null) {
					response.updateDelivery(Message.statusAllDelivered);
				} else {
					response.updateDelivery(Message.statusParseError);
				}
			}
		});
	}

	@Override
	public void MessageDeleted(MessageResponse r) {

	}

	@Override
	public void MessageUpdated(MessageResponse r) {

	}

	@Override
	public void MessageReceived(Message message) {
		// 1. Check visibility
		if (mTvNoMessages.getVisibility() == View.VISIBLE) {
			mTvNoMessages.setVisibility(View.GONE);
			mRvWallMessages.setVisibility(View.VISIBLE);
		}

		// 2. Add to adapter
		gMessagesAdapter.addMessage(message);
		mRvWallMessages.scrollToPosition(mRvWallMessages.getLayoutManager().getChildCount());
	}

}
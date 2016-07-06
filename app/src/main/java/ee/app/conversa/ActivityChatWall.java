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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversa.adapters.ChatsAdapter;
import ee.app.conversa.adapters.LocationsAdapter;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.interfaces.OnMessageTaskCompleted;
import ee.app.conversa.messageshandling.SaveUserAsync;
import ee.app.conversa.messageshandling.SendMessageAsync;
import ee.app.conversa.model.Database.Location;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.dBusiness;
import ee.app.conversa.responses.MessageResponse;
import ee.app.conversa.utils.Const;
import ee.app.conversa.utils.Utils;
import ee.app.conversa.view.TouchImageView;


public class ActivityChatWall extends ConversaActivity implements OnClickListener, OnMessageTaskCompleted {

    public static ActivityChatWall sInstance;

	private dBusiness businessObject;
	private boolean addAsContact;

	//public static List<Message> gCurrentMessages;
	public List<Location> gLocations;
	public MessagesAdapter gMessagesAdapter;
	public ChatsAdapter mChatsAdapter;
	public LocationsAdapter gLocationsAdapter;

	private boolean loading;
	private int previousTotal;

    public static RecyclerView mRvWallMessages;
	public static RecyclerView mRvLocation;
	private static TouchImageView mTivPhotoImage;
	private ImageButton mBtnOpenSlidingDrawer;
	public static TextView mTvNoMessages;
	private EditText mEtMessageText;
	private static ProgressBar mPbLoadingForImage;
	private BottomSheetBehavior mBottomSheetBehavior;

	private ScrollView mRlBottom;
	private RelativeLayout rlImageDisplay;
	private RelativeLayout rlLocations;
	private RelativeLayout mRlOptionsLayout;

    private Button mBtnBlockUser;
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

		// Deactivate broadcast and check internet connection
		checkInternetConnection = false;

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		TextView mTitleTextView = (TextView) toolbar.findViewById(R.id.tvChatName);
		ImageView optionButton = (ImageView) toolbar.findViewById(R.id.ivMoreOptions);
		ImageView imageButton = (ImageView) toolbar.findViewById(R.id.ivAvatarChat);
		optionButton.setOnClickListener(this);
		imageButton.setOnClickListener(this);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

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
		Message.getAllMessageForChat(this, businessObject.getBusinessId(), previousTotal);
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
    protected void onDestroy() {
        super.onDestroy();
        gMessagesAdapter = null;
    }

    @Override
	public void onResume() {
		super.onResume();
        ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		ConversaApp.getLocalBroadcastManager().unregisterReceiver(mPushReceiver);
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
		return true;
//		if(rlLocations.getVisibility() == View.VISIBLE) {
//			closeLocations();
//			return false;
//		} else if(rlImageDisplay.getVisibility() == View.VISIBLE) {
//			closeImage();
//			return false;
//		}  else if(mRlOptionsLayout.getVisibility() == View.VISIBLE) {
//			closeOptions();
//			return false;
//		} else {
//			return true;
//		}
	}

	@Override
	public void onBackPressed() {
		if(closeThisFirst()) {
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(false);
			}

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

    /**
     * This method is call when a new message is receive for the current open chat
     */
    @Override
    protected void refreshWallMessages(Message message) {
        //UpdateMessages.reload(message);
    }

	protected void initialization() {
		mRvWallMessages = (RecyclerView) findViewById(R.id.rvWallMessages);
//		mRvLocation = (RecyclerView) findViewById(R.id.rvLocations);
		mTvNoMessages = (TextView) findViewById(R.id.tvNoMessages);
        mEtMessageText = (EditText) findViewById(R.id.etWallMessage);
        mBtnWallSend = (Button) findViewById(R.id.btnWallSend);
//        mBtnBlockUser = (Button) findViewById(R.id.btnBlockUser);
//		rlImageDisplay = (RelativeLayout) findViewById(R.id.rlImageDisplay);
//		rlLocations = (RelativeLayout) findViewById(R.id.rlChatLocations);
//		mRlOptionsLayout = (RelativeLayout) findViewById(R.id.rlOptionsLayout);
//		mRlBottom = (ScrollView) findViewById(R.id.myScrollView);
		mBtnOpenSlidingDrawer = (ImageButton) findViewById(R.id.btnSlideButton);
        mPbLoadingForImage = (ProgressBar) findViewById(R.id.pbLoadingForImage);
		mTivPhotoImage = (TouchImageView) findViewById(R.id.tivPhotoImage);

		View bottomSheet = findViewById( R.id.bottom_sheet );
		mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

//		Button mBtnBack	= (Button) findViewById(R.id.btnCloseImage);
//		Button mBtnProfile = (Button) findViewById(R.id.btnProfile);
//		Button mBtnLocations = (Button) findViewById(R.id.btnLocations);
//		Button mBtnCloseLocts = (Button) findViewById(R.id.btnCloseLocation);
//		Button mBtnReloadLocts = (Button) findViewById(R.id.btnReloadLocation);
		ImageButton mBtnCamera = (ImageButton) findViewById(R.id.btnCamera);
		ImageButton mBtnGallery = (ImageButton) findViewById(R.id.btnGallery);
		ImageButton mBtnMore = (ImageButton) findViewById(R.id.btnMore);
		ImageButton mBtnLocation = (ImageButton) findViewById(R.id.btnLocation);

//		mEtMessageText.setTypeface( ConversaApp.getTfRalewayRegular());
//		mBtnBlockUser.setTypeface(ConversaApp.getTfRalewayRegular());
//		mBtnProfile.setTypeface(    ConversaApp.getTfRalewayRegular());
		mBtnWallSend.setTypeface(ConversaApp.getTfRalewayMedium());
//		mBtnBack.setTypeface(ConversaApp.getTfRalewayMedium());

		gMessagesAdapter = new MessagesAdapter(this);
		mRvWallMessages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
						Message.getAllMessageForChat(sInstance, businessObject.getBusinessId(), previousTotal);
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

	/***********************************************************************************/
	/***********************************************************************************/
	/***********************************************************************************/
	/***********************************************************************************/

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

	private void closeImage() {
		final Animation slideout = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out);
		rlImageDisplay.startAnimation(slideout);
		rlImageDisplay.setVisibility(View.GONE);
		mTivPhotoImage.setImageBitmap(null);
		mTivPhotoImage.resetZoom();
	}

	private void closeLocations() {
		final Animation slideout = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_out);
		rlLocations.startAnimation(slideout);
		rlLocations.setVisibility(View.GONE);
	}

	private void closeOptions() {
		mRlOptionsLayout.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		if(v instanceof ImageButton) {
			switch (v.getId()) {
				case R.id.btnCamera:
					Intent intent = new Intent(getApplicationContext(), ActivityCameraCrop.class);
					intent.putExtra("type", "camera");
					intent.putExtra("folder", Const.IMAGE_FOLDER);
					ActivityChatWall.sInstance.startActivity(intent);
					break;
				case R.id.btnGallery:
					Intent intent1 = new Intent(getApplicationContext(), ActivityCameraCrop.class);
					intent1.putExtra("type", "gallery");
					intent1.putExtra("folder", Const.IMAGE_FOLDER);
					ActivityChatWall.sInstance.startActivity(intent1);
					break;
				case R.id.btnLocation:
					Intent intent2 = new Intent(getApplicationContext(), ActivityLocation.class);
					intent2.putExtra(Const.LOCATION, "myLocation");
					ActivityChatWall.sInstance.startActivity(intent2);
					break;
				case R.id.btnMore:

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
				case R.id.ivMoreOptions:
					if (mRlOptionsLayout.getVisibility() == View.GONE) {
						mRlOptionsLayout.setVisibility(View.VISIBLE);
					} else {
						mRlOptionsLayout.setVisibility(View.GONE);
					}
					break;
			}
		} else if (v instanceof Button) {
			switch (v.getId()) {
				case R.id.btnWallSend:
					String body = mEtMessageText.getText().toString().trim();

					if (checkInternetConnection()) {
						if (!body.equals("")) {
							mEtMessageText.setText("");
                        	SendMessageAsync.sendTextMessage(gMessagesAdapter, businessObject.getBusinessId(), body, this);
						}
					}
					break;
				case R.id.btnCloseImage:
					closeImage();
					break;
//				case R.id.btnBlockUser:
//					if (mBtnWallSend.isEnabled()) {
//						//Call server for blocking
//						mBtnWallSend.setEnabled(false);
//						mEtMessageText.setEnabled(false);
//						mBtnBlockUser.setText(getResources().getString(R.string.action_unblock));
//					} else {
//						//Call server for unblocking
//						mBtnWallSend.setEnabled(true);
//						mEtMessageText.setEnabled(true);
//						mBtnBlockUser.setText(getResources().getString(R.string.action_block));
//					}
//					break;
//				case R.id.btnProfile:
//					//new HookUpDialog(ActivityChatWall.sInstance).showOnlyOK(getResources().getString(R.string.profiles_to_be_implemented));
//					break;
//				case R.id.btnLocations:
//					Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
//							R.anim.slide_in);
//					rlLocations.setVisibility(View.VISIBLE);
//					rlLocations.startAnimation(slidein);
//					break;
				case R.id.btnCloseLocation:
					closeLocations();
					break;
				case R.id.btnReloadLocation:
//					CouchDB.getLocationsAsync(UsersManagement.getToUser().getId(),null,sInstance,false);
					break;
			}
		}
	}

	@Override
	public void OnMessageTaskCompleted(MessageResponse response) {
		if (response != null) {
			switch (response.getActionCode()) {
				case Message.ACTION_MESSAGE_SAVE:
					if (response.getResponse().size() > 0) {
						// 1. Add to adapter
						final Message message = response.getResponse().get(0);
						message.addMessageToAdapter();
						// 2. Save to Parse
						HashMap<String, String> params = new HashMap<>();
						params.put("user", ConversaApp.getPreferences().getCustomerId());
						params.put("business", message.getToUserId());
						params.put("text", message.getBody());
						params.put("fromUser", String.valueOf(true));
						ParseCloud.callFunctionInBackground("sendUserMessage", params, new FunctionCallback<Boolean>() {
							@Override
							public void done(Boolean result, ParseException e) {
								// 2.1. Update local db delivery
								if (e == null) {
									message.updateDelivery(Message.statusAllDelivered);
								} else {
									message.updateDelivery(Message.statusParseError);
								}
							}
						});
					}
					// 3. Check if user needs to be added
					if(addAsContact) {
						SaveUserAsync.saveBusinessAsContact(mChatsAdapter, businessObject, null);
						addAsContact = false;
					}
					break;
				case Message.ACTION_MESSAGE_UPDATE:
					break;
				case Message.ACTION_MESSAGE_DELETE:
					break;
				case Message.ACTION_MESSAGE_RETRIEVE_ALL:
					if (response.getResponse().size() > 0) {
						// 1. Define if messages need to be set or added
						if (previousTotal == 0) {
							gMessagesAdapter.setMessages(response.getResponse());
							//recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, recyclerAdapter.getItemCount() - 1);
							mRvWallMessages.scrollToPosition(mRvWallMessages.getLayoutManager().getChildCount() + response.getResponse().size() - 1);
							mTvNoMessages.setVisibility(View.GONE);
							mRvWallMessages.setVisibility(View.VISIBLE);
						} else {
							gMessagesAdapter.addMessages(response.getResponse(), 0);
						}
					}
					// 1. Increase number of page retrieve
					previousTotal++;
					// 2. Set loading as completed
					loading = false;
					break;
			}
		}
	}
}
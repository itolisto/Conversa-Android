package ee.app.conversa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.app.conversa.adapters.LocationsAdapter;
import ee.app.conversa.adapters.MessagesAdapter;
import ee.app.conversa.extendables.ConversaActivity;
import ee.app.conversa.management.SettingsManager;
import ee.app.conversa.messageshandling.UpdateMessages;
import ee.app.conversa.model.Database.Location;
import ee.app.conversa.model.Database.Message;
import ee.app.conversa.model.Database.User;
import ee.app.conversa.utils.Const;
import ee.app.conversa.view.TouchImageView;


public class ActivityChatWall extends ConversaActivity {

    public static ActivityChatWall sInstance;

	public static List<Message> gCurrentMessages;
	public static List<Location> gLocations;
	public static MessagesAdapter gMessagesAdapter;
	public static LocationsAdapter gLocationsAdapter;

	private boolean loading;
	private int previousTotal = 0;
	private int firstVisibleItem, visibleItemCount, totalItemCount;

    //public static SwipeRefreshLayout mSwipeRefreshLayout;
    public static RecyclerView mRvWallMessages;
	public static RecyclerView mRvLocation;
	private static TouchImageView mTivPhotoImage;
	private ImageButton mBtnOpenSlidingDrawer;
	public static TextView mTvNoMessages;
	private EditText mEtMessageText;
	private static ProgressBar mPbLoadingForImage;

	private ScrollView mRlBottom;
	private RelativeLayout rlImageDisplay;
	private RelativeLayout rlLocations;
    private LinearLayout mButtonsLayout;
    private LinearLayout mMoreLayout;

	private SlidingDrawer mSlidingDrawer;
	private Button mBtnBack;
	private ImageButton mBtnCamera;
	private ImageButton mBtnGallery;
	private ImageButton mBtnMore;
	private ImageButton mBtnLocation;
    private Button mBtnBlockUser;
    private Button mBtnProfile;
    private Button mBtnLocations;
	private Button mBtnWallSend;
	private Button mBtnCloseLocts;
	private Button mBtnReloadLocts;
	private Toolbar toolbar;

	private RelativeLayout.LayoutParams mParamsOpened;
	private RelativeLayout.LayoutParams mParamsClosed;

	private static final int OPENED = 1003;
	private static final int CLOSED = 1004;

    public static boolean gIsVisible = false;
    private final IntentFilter mPushFilter = new IntentFilter(MessagesAdapter.PUSH);

    public ActivityChatWall() {
		gCurrentMessages = new ArrayList<>();
		gLocations = new ArrayList<>();
		previousTotal = 0;
		loading = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_wall);

		// Deactivate broadcast and check internet connection
		checkInternetConnection = false;

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		TextView mTitleTextView  = (TextView)  toolbar.findViewById(R.id.tvChatName);
		ImageView imageButton    = (ImageView) toolbar.findViewById(R.id.ivAvatarChat);
//		Utils.displayImage(UsersManagement.getToUser().getAvatarThumbFileId(), Const.BUSINESS_FOLDER, imageButton, null,
//				ImageLoader.SMALL, R.drawable.business_default, false);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Llamar a Servidor por foto y actualizar
				//if (UsersManagement.getToUser() != null) {
//					CouchDB.findAvatarIdAndDisplay(
//							UsersManagement.getToUser().getId(),
//							(ImageView) view,
//							getApplicationContext());
				//}
			}
		});
//		mTitleTextView.setText(UsersManagement.getToUser().getName());

		initialization();
		onClickListeners();
        sInstance = this;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
		if (getIntent().getBooleanExtra(Const.PUSH_INTENT, false)) {
			getIntent().removeExtra(Const.PUSH_INTENT);
			openWallFromNotification(intent);
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
		//UsersManagement.setToUser(null);
        //Necesario para limpiar los mensajes que se despliegan
        gMessagesAdapter = null;
    }

    @Override
	public void onResume() {
		super.onResume();

		this.previousTotal = 0;
		this.loading = true;

		runOnUiThread(new Runnable() {
			public void run() {
				final String id = "";//UsersManagement.getToUser().getId();
				// Recibir localizaciones
				gLocations = ConversaApp.getDB().getLocations(id);
				gLocationsAdapter.setLocations(gLocations);

				if (ConversaApp.getDB().hasPendingMessages(id)) {
					List<Message> newMessages;
					//Llamar al Servidor por todos los mensajes
					try {
						String lastId = String.valueOf(ConversaApp.getDB().messageCountForContact(id));
						newMessages = new ArrayList<Message>();
//						newMessages = new GetMessagesByIdAsync(getApplicationContext()).
//								execute(id, lastId).get(6500, TimeUnit.MILLISECONDS);
						//Actualizar para que ya no llame por mensajes
						ConversaApp.getDB().setHasPendingMessages(id, 0);
						Thread.sleep(500);
						//Desplegar resultados
						if (newMessages != null && newMessages.size() > 0) {
							int countBefore = gCurrentMessages.size();
							int countAfter = countBefore + newMessages.size();

							if (mRvWallMessages != null) {
//								mRvWallMessages.smoothScrollToPosition(countAfter - 1);
							}

							gMessagesAdapter.addMessages(newMessages, countBefore - 1);

							if (countBefore == 0)
								checkMessagesCount();
						}
					} catch (IllegalStateException | InterruptedException  e) {
						e.printStackTrace();
					}
				}

				if (ConversaApp.getDB().hasUnreadMessages(id)) {
//					CouchDB.setReadAtAsync(id, new ReadAtFinish(), sInstance, false);
				}
			}
		});

        ConversaApp.getLocalBroadcastManager().registerReceiver(mPushReceiver, mPushFilter);

		if(mSlidingDrawer.isOpened())
			setSlidingDrawer(CLOSED);
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
				getSupportActionBar().setDisplayHomeAsUpEnabled(false);
				super.onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if(rlLocations.getVisibility() == View.VISIBLE) {
			closeLocations();
		} else if(rlImageDisplay.getVisibility() == View.VISIBLE) {
			closeImage();
		} else if (moreActionsLayoutIsOpened()) {
			setMoreActionsLayout(CLOSED);
		} else if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		} else {
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			super.onBackPressed();
		}
	}

	private void openWallFromNotification(Intent intent) {
		String fromUserId = intent.getStringExtra(Const.PUSH_FROM_USER_ID);
		User fromUser     = ConversaApp.getDB().isContact(fromUserId);

		if(fromUser == null) {
//			try {
//				fromUser = new ConversaAsyncTask<Void, Void, User>(
//						new CouchDB.FindBusinessById(fromUserId), null, getApplicationContext(), true
//				).execute().get();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
		}

		if(fromUser != null) {
			//UsersManagement.setToUser(fromUser);
			SettingsManager.ResetSettings();
			if (ActivityChatWall.gCurrentMessages != null)
				ActivityChatWall.gCurrentMessages.clear();

			setWallMessages();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		gIsVisible = hasFocus;
	}

    public void setWallMessages() {
        List<Message> newMessages = new ArrayList<>();

//        try {
//            newMessages = new GetMessageByIdAsync(this).
//                    execute(UsersManagement.getToUser().getId()).get(4500, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException|ExecutionException|TimeoutException e) {
//            e.printStackTrace();
//        }

        if (newMessages != null && newMessages.size() > 0) {

            int countBefore = gCurrentMessages.size();
            Collections.reverse(newMessages);
            gCurrentMessages.addAll(0, newMessages);
            int countAfter = gCurrentMessages.size();

            if(mRvWallMessages != null) {
                if (countBefore == 0) {
					mRvWallMessages.scrollToPosition(countAfter - 1);
                } else {
					((LinearLayoutManager) mRvWallMessages.getLayoutManager()).scrollToPositionWithOffset(countAfter - countBefore - 1, 0);
				}
            }

			if(countBefore == 0)
            	checkMessagesCount();
        }
    }

    /**
     * This method is call when a new message is receive for the current open chat
     */
    @Override
    protected void refreshWallMessages(Message message) {
        super.refreshWallMessages(message);
        UpdateMessages.reload(message);
    }

	public void checkMessagesCount() {
		if (gCurrentMessages.size() > 0) {
			mTvNoMessages.setVisibility(View.GONE);
            mRvWallMessages.setVisibility(View.VISIBLE);
			//mSwipeRefreshLayout.setEnabled(true);
		} else {
			mTvNoMessages.setVisibility(View.VISIBLE);
            mRvWallMessages.setVisibility(View.GONE);
			//mSwipeRefreshLayout.setEnabled(false);
		}
	}

	private void onClickListeners() {
		mBtnWallSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String body = mEtMessageText.getText().toString().trim();

                if (ConversaApp.hasNetworkConnection()) {
                    if (!body.equals("")) {
                        mEtMessageText.setText("");
                        setSlidingDrawer(CLOSED);
//                        new SendMessageAsync(sInstance, SendMessageAsync.TYPE_TEXT)
//                                .execute(body);
                    }
                }
			}
		});

		mBtnOpenSlidingDrawer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSlidingDrawer.isOpened())
					setSlidingDrawer(CLOSED);
				else
					setSlidingDrawer(OPENED);
			}
		});

		mBtnCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ActivityCameraCrop.class);
				intent.putExtra("type", "camera");
				intent.putExtra("folder", Const.IMAGE_FOLDER);
				ActivityChatWall.sInstance.startActivity(intent);
			}
		});

		mBtnGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ActivityCameraCrop.class);
				intent.putExtra("type", "gallery");
				intent.putExtra("folder", Const.IMAGE_FOLDER);
				ActivityChatWall.sInstance.startActivity(intent);
			}
		});

        mBtnMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(moreActionsLayoutIsOpened())
                    setMoreActionsLayout(CLOSED);
                else
                    setMoreActionsLayout(OPENED);
            }
        });

		mBtnLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityLocation.class);
                intent.putExtra(Const.LOCATION, "myLocation");
                ActivityChatWall.sInstance.startActivity(intent);
			}
		});

        mBtnBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				closeImage();
			}
		});

        mBtnBlockUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnWallSend.isEnabled()) {
                    //Call server for blocking
                    mBtnWallSend.setEnabled(false);
                    mEtMessageText.setEnabled(false);
                    mBtnBlockUser.setText(getResources().getString(R.string.action_unblock));
                } else {
                    //Call server for unblocking
                    mBtnWallSend.setEnabled(true);
                    mEtMessageText.setEnabled(true);
                    mBtnBlockUser.setText(getResources().getString(R.string.action_block));
                }
            }
        });

		mBtnLocations.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
						R.anim.slide_in);
				rlLocations.setVisibility(View.VISIBLE);
				rlLocations.startAnimation(slidein);
			}
		});

		mBtnCloseLocts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeLocations();
			}
		});

		mBtnReloadLocts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				CouchDB.getLocationsAsync(UsersManagement.getToUser().getId(),null,sInstance,false);
			}
		});

        mBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new HookUpDialog(ActivityChatWall.sInstance).showOnlyOK(getResources().getString(R.string.profiles_to_be_implemented));
            }
        });
	}

	protected void initialization() {
		mRvWallMessages = (RecyclerView) findViewById(R.id.lvWallMessages);
		mRvLocation     = (RecyclerView) findViewById(R.id.rvLocations);
		mTvNoMessages 	= (TextView) 	 findViewById(R.id.tvNoMessages);
        mEtMessageText 	= (EditText) 	 findViewById(R.id.etWallMessage);
        mBtnWallSend 	= (Button) 	 	 findViewById(R.id.btnWallSend);
        mBtnBack	 	= (Button) 	 	 findViewById(R.id.btnCloseImage);
        mBtnBlockUser 	= (Button) 	 	 findViewById(R.id.btnBlockUser);
        mBtnProfile 	= (Button) 	 	 findViewById(R.id.btnProfile);
        mBtnLocations   = (Button)  	 findViewById(R.id.btnLocations);
		mBtnCloseLocts  = (Button)  	 findViewById(R.id.btnCloseLocation);
		mBtnReloadLocts = (Button)  	 findViewById(R.id.btnReloadLocation);
		rlImageDisplay 	= (RelativeLayout) 	findViewById(R.id.rlImageDisplay);
		rlLocations 	= (RelativeLayout) 	findViewById(R.id.rlChatLocations);
		mRlBottom 		= (ScrollView) 		findViewById(R.id.myScrollView);
		mBtnCamera 		= (ImageButton) 	findViewById(R.id.btnCamera);
		mBtnGallery 	= (ImageButton) 	findViewById(R.id.btnGallery);
		mBtnMore 		= (ImageButton) 	findViewById(R.id.btnMore);
		mBtnLocation 	= (ImageButton) 	findViewById(R.id.btnLocation);
		mBtnOpenSlidingDrawer = (ImageButton)	findViewById(R.id.btnSlideButton);
		mSlidingDrawer 		  = (SlidingDrawer) findViewById(R.id.slDrawer);
		mButtonsLayout 		  = (LinearLayout) 	findViewById(R.id.llButtonsLayout);
        mMoreLayout 		  = (LinearLayout) 	findViewById(R.id.llMoreActions);
        mPbLoadingForImage    = (ProgressBar) 	findViewById(R.id.pbLoadingForImage);
		mTivPhotoImage 		  = (TouchImageView)	 findViewById(R.id.tivPhotoImage);
		//mSwipeRefreshLayout	  = (SwipeRefreshLayout) findViewById(R.id.srlMessages);

		mEtMessageText.setTypeface( ConversaApp.getTfRalewayRegular());
		mBtnBlockUser.setTypeface(ConversaApp.getTfRalewayRegular());
		mBtnProfile.setTypeface(    ConversaApp.getTfRalewayRegular());
		mBtnWallSend.setTypeface(ConversaApp.getTfRalewayMedium());
		mBtnBack.setTypeface(ConversaApp.getTfRalewayMedium());

		mRvWallMessages.setHasFixedSize(false);
		mRvWallMessages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		mRvWallMessages.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideKeyboard();
				return false;
			}
		});

		mRvWallMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				visibleItemCount = recyclerView.getChildCount();
				totalItemCount   = ((LinearLayoutManager)recyclerView.getLayoutManager()).getItemCount();
				firstVisibleItem = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

				if (loading) {
					if (totalItemCount > previousTotal) {
						loading = false;
						previousTotal = totalItemCount;
					}
				}

//				if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 5)) { //When reaches bottom }
				if(!loading && firstVisibleItem == 0) {
					Log.e("...", "messages called" + visibleItemCount + "," + totalItemCount + "," + firstVisibleItem);
					int value = SettingsManager.getsVisibleMessageCount() + SettingsManager.getsMessageCount();
					SettingsManager.setsVisibleMessageCount(value);
					value = SettingsManager.getsPage() + 1;
					SettingsManager.setsPage(value);
					setWallMessages();

					loading = true;
				}
			}
		});

		mRvLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

//        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.orange, R.color.blue);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//			@Override
//			public void onRefresh() {
//				int value = SettingsManager.getsVisibleMessageCount() + SettingsManager.getsMessageCount();
//				SettingsManager.setsVisibleMessageCount(value);
//				value = SettingsManager.getsPage() + 1;
//				SettingsManager.setsPage(value);
//				setWallMessages();
//			}
//		});

		mParamsClosed = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParamsOpened = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mParamsClosed.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mParamsOpened.addRule(RelativeLayout.ABOVE, mSlidingDrawer.getId());

        gMessagesAdapter = new MessagesAdapter(this, gCurrentMessages);
        mRvWallMessages.setAdapter(gMessagesAdapter);

		//MultiSelector mMultiSelector = new SingleSelector();
		gLocationsAdapter = new LocationsAdapter(this, gLocations);//, mMultiSelector);
		mRvLocation.setAdapter(gLocationsAdapter);

		setWallMessages();
		checkMessagesCount();
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
		hideKeyboard();
//		Utils.displayImageTouch(m.getImageFileId(), Const.IMAGE_FOLDER, mTivPhotoImage, mPbLoadingForImage,
//				ImageLoader.SMALL, R.drawable.image_stub, false);
		final Animation slidein = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.slide_in);
		rlImageDisplay.startAnimation(slidein);
		rlImageDisplay.setVisibility(View.VISIBLE);
	}

	private void setSlidingDrawer(int state) {
		mBtnOpenSlidingDrawer.setScaleType(ImageView.ScaleType.FIT_XY);
		switch (state) {
			case OPENED:
				InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEtMessageText.getWindowToken(), 0);
				mEtMessageText.clearFocus();
				mBtnOpenSlidingDrawer.setImageResource(R.drawable.hide_more_btn_off);
				mSlidingDrawer.open();
				mRlBottom.setLayoutParams(mParamsOpened);
				mButtonsLayout.setVisibility(View.VISIBLE);
				break;
			case CLOSED:
				mBtnOpenSlidingDrawer.setImageResource(R.drawable.more_btn_off);
				mSlidingDrawer.close();
				mRlBottom.setLayoutParams(mParamsClosed);
				if (moreActionsLayoutIsOpened())
					setMoreActionsLayout(CLOSED);
				break;
			default:
				break;
		}
	}

    private void setMoreActionsLayout(int state) {
        switch (state) {
            case OPENED:
                mMoreLayout.setVisibility(View.VISIBLE);
                break;
            case CLOSED:
                mMoreLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private boolean moreActionsLayoutIsOpened() {
        return (mMoreLayout.getVisibility() == View.VISIBLE);
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
		setMoreActionsLayout(CLOSED);
	}

    public void hideKeyboard() {
		AppCompatActivity activity = this;
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        View cur_focus = activity.getCurrentFocus();
        if (cur_focus != null) {
            inputMethodManager.hideSoftInputFromWindow(cur_focus.getWindowToken(), 0);
        }
    }

//    private class ReadAtFinish implements ResultListener<String> {
//        @Override
//        public void onResultsSuccess(String result) {
//            if(result != null) {
//				ConversaApp.getDB().updateReadMessages(result);
//                if(FragmentUsersChat.mUserListAdapter != null)
//                    FragmentUsersChat.mUserListAdapter.notifyItemRangeChanged(0, FragmentUsersChat.mUserListAdapter.getItemCount());
//            }
//        }
//
//        @Override
//        public void onResultsFail() {}
//    }
//
//    private class GetMessageByIdAsync extends ConversaAsync<String, Void, List<Message>> {
//        @Override
//        protected List<Message> backgroundWork(String... params) throws JSONException, IOException, ConversaException, IllegalStateException, ConversaForbiddenException {
//            List<Message> newMessages;
//            String userId = params[0];
//            newMessages = CouchDB.findMessagesForUser(userId, SettingsManager.getsPage(), 15);
//            return newMessages;
//        }
//
//        protected GetMessageByIdAsync(Context context) { super(context); }
//        @Override
//        protected void onPreExecute() { super.onPreExecute(); }
//        @Override
//        protected void onPostExecute(List<Message> result) {
//            super.onPostExecute(result);
////            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing())
////                mSwipeRefreshLayout.setRefreshing(false);
//        }
//    }
//
//	private class GetMessagesByIdAsync extends ConversaAsync<String, Void, List<Message>> {
//		@Override
//		protected List<Message> backgroundWork(String... params) throws JSONException, IOException, ConversaException, IllegalStateException, ConversaForbiddenException {
//			List<Message> newMessages;
//			String userId = params[0];
//			String lastId = params[1];
//			newMessages = CouchDB.findNewMessagesForUser(userId, lastId);
//			return newMessages;
//		}
//
//		protected GetMessagesByIdAsync(Context context) { super(context); }
//		@Override
//		protected void onPreExecute() { super.onPreExecute(); }
//		@Override
//		protected void onPostExecute(List<Message> result) { super.onPostExecute(result); }
//	}
}
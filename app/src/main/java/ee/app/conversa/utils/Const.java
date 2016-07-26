/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversa.utils;
/**
 * Const
 * 
 * This class contains all of the constant variables used through the
 * application.
 */
public class Const {

	/* SendBird */
	public static final String sbAppId = "17FB6C41-7B2E-41C2-B481-C2B168633616";

	/* Debug/Production variables */
	public static final boolean IS_DEBUG = true;

    /* Parse */
	// Category class
	public static final String kClassCategory = "Category";
	public static final String kCategoryThumbnail = "thumbnail";
	public static final String kCategoryRelevance = "relevance";
	public static final String kCategoryPosition = "position";

	// Message class
		// pMessage
		public static final String kClassMessage       = "Message";
		public static final String kMessageUserKey = "user";
		public static final String kMessageBusinessKey = "business";
		public static final String kMessageFromUserKey = "fromUser";
		public static final String kMessageSizeInBytesKey = "size";
		public static final String kMessageWidthKey    = "width";
		public static final String kMessageHeightKey   = "height";
		public static final String kMessageThumbKey    = "thumbnail";
		public static final String kMessageFileKey     = "file";
		public static final String kMessageTextKey     = "text";
		public static final String kMessageDurationKey = "duration";
		public static final String kMessageLocationKey = "location";
		public static final String kMessageReadAtKey = "reatAt";
		// Message
		public static final String kMessageTypeText = "1";
		public static final String kMessageTypeAudio = "2";
		public static final String kMessageTypeVideo = "3";
		public static final String kMessageTypeImage = "4";
		public static final String kMessageTypeLocation = "5";


	// User class
	public static final String kUserAvatarKey   = "avatar";
	public static final String kUserUsernameKey = "username";
	public static final String kUserEmailKey    = "email";
	public static final String kUserPasswordKey = "password";
	public static final String kUserDisplayNameKey  = "displayName";
	public static final String kUserTypeKey     = "userType";
	public static final String kUserBirthday = "birthday";
	public static final String kUserGender = "gender";







	// Options class
	public static final String kClassOptions           = "Options";
	public static final String kOptionsCodeKey         = "code";
	public static final String kOptionsDefaultValueKey = "defaultValue";

	// BusinessOptions class
	public static final String kClassBusinessOptions       = "BusinessOptions";
	public static final String kBusinessOptionsBusinessKey = "business";
	public static final String kBusinessOptionsOptionKey   = "option";
	public static final String kBusinessOptionsValueKey    = "value";
	public static final String kBusinessOptionsActiveKey   = "active";

	// BusinessCategory class
	public static final String kClassBusinessCategory        = "BusinessCategory";
	public static final String kBusinessCategoryCategoryKey  = "category";
	public static final String kBusinessCategoryBusinessKey  = "business";
	public static final String kBusinessCategoryRelevanceKey = "relevance";
	public static final String kBusinessCategoryPositionKey  = "position";
	public static final String kBusinessCategoryActiveKey    = "active";

	// Customer class
	public static final String kClassCustomer       = "Customer";
	public static final String kCustomerUserInfoKey = "userInfo";

	// dBusiness class
	public static final String kClassBusiness           = "dBusiness";
	public static final String kBusinessBusinessInfoKey = "businessInfo";
	public static final String kBusinessDisplayNameKey = "displayName";
	public static final String kBusinessConversaIdKey   = "conversaID";
	public static final String kBusinessAboutKey   = "about";
	public static final String kBusinessStatusKey   = "status";
	public static final String kBusinessAvatarKey   = "avatar";
	public static final String kBusinessActiveKey       = "active";
	public static final String kBusinessCountryKey      = "country";
	public static final String kBusinessVerifiedKey     = "verified";
	public static final String kBusinessBusinessKey     = "business";
	public static final String kBusinessTagTagKey       = "tags";

// Contact class
	public static final String kClassContact         = "UserContact";
	public static final String kContactFromUserKey   = "fromUser";
	public static final String kContactToBusinessKey = "toBusiness";
	public static final String kContactActiveChatKey = "activeChat";

// Favorite class
	public static final String kClassFavorite         = "UserFavorite";
	public static final String kFavoriteFromUserKey   = "fromUser";
	public static final String kFavoriteToBusinessKey = "toBusiness";
	public static final String kFavoriteIsFavoriteKey = "isCurrentlyFavorite";

// PubNubMessage class
	public static final String kPubNubMessageTextKey = "message";
	public static final String kPubNubMessageFromKey = "from";
	public static final String kPubNubMessageTypeKey = "type";

// Messages media location
	public static final String kMessageMediaImageLocation = "/image";
	public static final String kMessageMediaVideoLocation = "/video";
	public static final String kMessageMediaAudioLocation = "/audio";

// Statistics class
	public static final String kClassStatistics = "Statistics";
	public static final String kStatisticsBusinessKey  = "business";
	public static final String kStatisticsCriteria1Key = "messagesReceived";
	public static final String kStatisticsCriteria2Key = "numberOfFollowers";
	public static final String kStatisticsCriteria3Key = "numberOfProfileViews";
	public static final String kStatisticsCriteria4Key = "numberOfSearches";
	public static final String kStatisticsCriteria5Key = "numberOfComplaints";
	public static final String kStatisticsCriteria6Key = "numberOfMutesByUser";

// General
	public static final String kObjectRowObjectIdKey  = "objectId";
	public static final String kObjectRowCreatedAtKey = "createdAt";

// Other
	public static final String kAccountAvatarName          = "user_avatar.png";
	public static final String kNSDictionaryBusiness       = "objectBusiness";
	public static final String kNSDictionaryChangeValue    = "hasChangeValue";
	public static final String kSettingKeyLanguage         = "userSelectedSetting";
	public static final String kAppVersionKey              = "kAppVersionKey";
	public static final String kYapDatabaseServiceName     = "ee.app.Conversa";
	public static final String kYapDatabaseName            = "ConversaYap.sqlite";
	public static final String kYapDatabasePassphraseAccountName = "YapDatabasePassphraseAccountName";
	public static final String kMuteUserNotificationName   = "kMuteUserNotificationName";
	

    /* General */
	public static final String APP_NAME					= "Conversa";
    public static final String ROBOTO                   = "fonts/Roboto_v1_2/Roboto/";
    
	/* User constants */
	public static final String LOCATIONS            = "locations";
	public static final String CONTACTS             = "contacts";
	public static final String NAME                 = "name";
	public static final String PASSWORD             = "password";
	public static final String EMAIL                = "email";
    public static final String ACTION               = "action";

	/* pMessage constants */
	public static final String MESSAGE_TYPE         = "message_type";
	public static final String LOCATION             = "location";
	public static final String MODIFIED             = "modified";
	public static final String FROM_USER_ID         = "from_user_id";
	public static final String VALID                = "valid";
	public static final String MESSAGE_TARGET_TYPE  = "message_target_type";
	public static final String CREATED              = "created";
	public static final String TO_USER_ID           = "to_user_id";
    public static final String TO_USER_TYPE         = "to_user_type";
    public static final String FROM_USER_TYPE       = "from_user_type";
	public static final String BODY                 = "body";
	public static final String LATITUDE             = "latitude";
	public static final String LONGITUDE            = "longitude";

	/* General constants */
	public static final String ID           = "id";
	public static final String _REV         = "_rev";
	
	/* Push notification constants */
    public static final String PUSH_SENDER_ID       = "753901434477";
	public static final String PUSH_MESSAGE         = "messageId";
	public static final String PUSH_READ            = "read";

	public static final String PUSH_FROM_USER_ID    = "fromUser";
	public static final String PUSH_FROM_NAME       = "fromUserName";
	public static final String PUSH_MESSAGE_CONTENT = "messageContent";
	public static final String PUSH_TO_USER_ID      = "toUser";
	public static final String PUSH_INTENT          = "push_intent";
}

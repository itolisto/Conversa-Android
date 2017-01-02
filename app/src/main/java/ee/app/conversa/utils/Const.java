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

    /* Parse */
	// Category class
	public static final String kClassCategory = "Category";

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
	public static final String kUserUsernameKey = "username";
	public static final String kUserEmailKey    = "email";
	public static final String kUserPasswordKey = "password";
	public static final String kUserTypeKey     = "userType";
	public static final String kUserBirthday = "birthday";
	public static final String kUserGender = "gender";

	// BusinessCategory class
	public static final String kClassBusinessCategory        = "BusinessCategory";
	public static final String kBusinessCategoryCategoryKey  = "category";
	public static final String kBusinessCategoryBusinessKey  = "business";
	public static final String kBusinessCategoryRelevanceKey = "relevance";
	public static final String kBusinessCategoryPositionKey  = "position";
	public static final String kBusinessCategoryActiveKey    = "active";

	// dbBusiness class
	public static final String kBusinessBusinessInfoKey = "businessInfo";
	public static final String kBusinessDisplayNameKey = "displayName";
	public static final String kBusinessConversaIdKey   = "conversaID";
	public static final String kBusinessAboutKey   = "about";
	public static final String kBusinessAvatarKey   = "avatar";
	public static final String kBusinessActiveKey       = "active";
	public static final String kBusinessCountryKey      = "country";
	public static final String kBusinessVerifiedKey     = "verified";
	public static final String kBusinessBusinessKey     = "business";

	// General
	public static final String kObjectRowObjectIdKey  = "objectId";

	// Other
	public static final String kAppVersionKey              = "kAppVersionKey";

    /* General */
    public static final String ROBOTO                   = "fonts/Roboto_v1_2/Roboto/";
    
	/* User constants */
    public static final String ACTION               = "action";

	/* pMessage constants */
	public static final String LOCATION             = "location";
	public static final String LATITUDE             = "latitude";
	public static final String LONGITUDE            = "longitude";

	/* INTENTS */
	public static final String iExtraBusiness = "iExtraBusiness";
	public static final String iExtraAddBusiness = "iExtraAddBusiness";
	public static final String iExtraPosition = "iExtraAddBusiness";

}

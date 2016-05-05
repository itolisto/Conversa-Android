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

package ee.app.conversa.management;

import ee.app.conversa.model.Database.Message;
import ee.app.conversa.utils.Const;

/**
 * MessageManagement
 * 
 * Creates message object.
 */

public class MessageManagement {

	public static Message createMessage(
            int messageType, String messageTargetType, String body, String latitude, String longitude,
            String imageFileId) {

		long created = System.currentTimeMillis() / 1000;
		long modified = created;

		String fromUserId = "";//UsersManagement.getLoginUser().getId();
		String toUserId = "";//UsersManagement.getToUser().getId();
		String type = String.valueOf(Const.C_TYPE);

        boolean valid = true;

		Message message = new Message(
                Const._REV, type, messageType,
				messageTargetType, body, fromUserId,
                toUserId, created, modified,
                valid, latitude, longitude,
                imageFileId);

		return message;
	}

}

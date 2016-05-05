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

package ee.app.conversa.messageshandling;

import android.view.View;

import ee.app.conversa.ActivityChatWall;
import ee.app.conversa.model.Database.Message;

/**
 * UpdateMessages
 * 
 * Executes AsyncTask for fetching messages from CouchDB.
 */

public class UpdateMessages {

    public static void reload(Message message) {
        if (ActivityChatWall.gMessagesAdapter != null) {
            if(message != null) {
                int size = ActivityChatWall.gMessagesAdapter.getItemCount();
                ActivityChatWall.gMessagesAdapter.addMessage(size, message);

                if (ActivityChatWall.mRvWallMessages != null) {
                    if(size > 0)
                        ActivityChatWall.mRvWallMessages.smoothScrollToPosition(size);

                    if(size == 0 && ActivityChatWall.mTvNoMessages != null){
                        ActivityChatWall.mTvNoMessages.setVisibility(View.GONE);
                        ActivityChatWall.mRvWallMessages.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                //Actualizar mensajes
                ActivityChatWall.gMessagesAdapter.updateMessages();
            }
        }
    }
	
}

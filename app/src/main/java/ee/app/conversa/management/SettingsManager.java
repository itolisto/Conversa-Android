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

/**
 * SettingsManager
 * 
 * Holds reference to wall messages parameters.
 */

public class SettingsManager {
	
	private static final int sMessageCount = 15;
	private static int sVisibleMessageCount = 15;
	private static int sPage = 0;
	
	public static void ResetSettings(){
        SettingsManager.sVisibleMessageCount = 15;
        SettingsManager.sPage = 0;
	}

    public static int getsPage() { return SettingsManager.sPage; }
	public static int getsMessageCount() { return SettingsManager.sMessageCount; }
	public static int getsVisibleMessageCount() { return SettingsManager.sVisibleMessageCount; }

	public static void setsVisibleMessageCount(int sVisibleMessageCount) { SettingsManager.sVisibleMessageCount = sVisibleMessageCount; }
	public static void setsPage(int sPage) { SettingsManager.sPage = sPage; }
}

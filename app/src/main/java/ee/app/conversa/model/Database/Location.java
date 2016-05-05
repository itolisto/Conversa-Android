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

package ee.app.conversa.model.Database;

/**
 * Location
 * 
 * Model class for Location.
 */

public class Location {

	private String mId; // Used only when retrieving from internal database to chat wall
	private int mLocationId;
    private String mBusinessId;
	private String mName;
    private String mAddress;

	public Location(String mBusinessId, String mName, String mAddress) {
		super();
        this.mBusinessId = mBusinessId;
		this.mName       = mName;
		this.mAddress    = mAddress;
	}

    public Location(){}
	/* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
	public String getId() { return mId; }
	public String getBusinessId() { return mBusinessId; }
	public int getLocationId() { return mLocationId; }
	public String getName() { return mName; }
	public String getAddress() { return mAddress; }
	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
	public void setId(String mId) { this.mId = mId; }
	public void setBusinessId(String mBusinessId) { this.mBusinessId = mBusinessId; }
	public void setLocationId(int mLocationId) { this.mLocationId = mLocationId; }
	public void setName(String mName) { this.mName = mName; }
	public void setAddress(String mAddress) { this.mAddress = mAddress; }
}

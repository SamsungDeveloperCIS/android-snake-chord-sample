/*
 * Copyright (C) 2013 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Media Solution Center Russia, 
 * Service Development, Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.snake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SnakeMessage {

	private static final String TAG = "[Chord][ApiTest]";
    private static final String TAGClass = "SnakeMessage : ";

    private String KEY_DIRECTION = "direction";
    private String KEY_ACTION = "action";
    private String KEY_PLAYER_NAME = "player_name";
    private String KEY_PLAYER_TRAILS = "player_trails";
    private String KEY_PLAYER_MOVE_DELAY = "player_move_delay";
    private String KEY_PLAYER_DIRECTION = "player_direction";
    private String KEY_PLAYER_COLOR = "player_color";
    
    final public static int ACTION_START = 1;
    final public static int ACTION_MOVE = 2;
    final public static int ACTION_EAT = 3;
    final public static int ACTION_READY = 4;
    final public static int ACTION_REFUSE = 5;
    
    private JSONObject mJSONObject;
    
    public SnakeMessage() {
    	mJSONObject = new JSONObject();
		reset();
    }
    
    public SnakeMessage setPlayer(String name, int[] trails, int color, long moveDelay, int direction) {
    	try {
    		if (name != null && !name.isEmpty()) {
    			mJSONObject.put(KEY_PLAYER_NAME, name);
    			JSONArray t = new JSONArray();
    			for (int i=0; i<trails.length; i++) {
    				t.put(trails[i]);
    			}
    			mJSONObject.put(KEY_PLAYER_TRAILS, t);
    			mJSONObject.put(KEY_PLAYER_COLOR, color);
    			mJSONObject.put(KEY_PLAYER_MOVE_DELAY, moveDelay);
				mJSONObject.put(KEY_PLAYER_DIRECTION, direction);
    		}
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	return this;
    }

    public SnakeMessage setDirection(int code) {
    	try {
    		if (0 <= code && code <= 3) {
    			mJSONObject.put(KEY_DIRECTION, code);
    			mJSONObject.put(KEY_ACTION, ACTION_MOVE);
    		}
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	return this;
    }
    
    public SnakeMessage setAction(int action) {
    	try {
			mJSONObject.put(KEY_ACTION, action);
    	} catch (JSONException e) {
			e.printStackTrace();
		}
    	return this;
    }
    
    public void load(String message) {
    	try {
			mJSONObject = new JSONObject(message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    public SnakePlayer getPlayer() {
    	try {
    		JSONArray trs = mJSONObject.getJSONArray(KEY_PLAYER_TRAILS);
    		int[] trails = new int[trs.length()];
    		for (int i=0; i<trs.length(); i++) {
    			trails[i] = trs.getInt(i);
    		}
    		return new SnakePlayer(
				mJSONObject.getString(KEY_PLAYER_NAME),
				trails,
				mJSONObject.getInt(KEY_PLAYER_COLOR),
				mJSONObject.getLong(KEY_PLAYER_MOVE_DELAY),
				mJSONObject.getInt(KEY_PLAYER_DIRECTION)
			);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public int getAction() {
    	try {
			return mJSONObject.getInt(KEY_ACTION);
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		}
    }
    
    public int getDirection() {
    	try {
			return mJSONObject.getInt(KEY_DIRECTION);
		} catch (JSONException e) {
			e.printStackTrace();
			return Snake.MOVE_UP;
		}
    }
    
    public SnakeMessage reset() {
    	try {
			mJSONObject.put(KEY_ACTION, -1);
			mJSONObject.put(KEY_DIRECTION, Snake.MOVE_UP);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return this;
    }
    
    public String toString() {
    	return mJSONObject.toString();
    }
}

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

import java.util.ArrayList;
import java.util.Locale;

class SnakePlayer {
	public String name;
	public int color;
	public boolean ready;
	public long moveDelay;
	public int direction;
	public int[] trail;
	public ArrayList<Coordinate> mSnakeTrail;

	public SnakePlayer(String name, int[] trail, int color, long moveDelay, int direction) {
		this.name = name;
		this.trail = trail;
		this.moveDelay = moveDelay;
		this.direction = direction;
		this.color = color;
		this.ready = false;
		mSnakeTrail = new ArrayList<Coordinate>();
		int length = trail.length;
		for (int i=0; i<length; i +=2) {
			mSnakeTrail.add(new Coordinate(trail[i], trail[i+1]));
		}
	}

	public String toString() {
		String trailsString = "";
        int length = trail.length;
		for (int i=0; i<length; i +=2) {
			trailsString += String.format("(%d,%d) ", trail[i], trail[i+1]);
		}
		return String.format(Locale.ENGLISH, "name=%s trails=[ %s] color=%d ready=%s direction=%d moveDelay=%d", 
			name, trailsString, color, ready, direction, moveDelay);
	}
}
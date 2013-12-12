/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.chord.InvalidInterfaceException;
import com.samsung.android.sdk.chord.Schord;
import com.samsung.android.sdk.chord.SchordChannel;
import com.samsung.android.sdk.chord.SchordManager;
import com.samsung.android.sdk.chord.SchordManager.NetworkListener;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a serpent roaming
 * around the garden looking for apples. Be careful, though, because when you catch one, not only
 * will you become longer, but you'll move faster. Running into yourself or the walls will end the
 * game.
 * 
 */
public class Snake extends Activity {

    /**
     * Constants for desired direction of moving the snake
     */
    public static int MOVE_LEFT = 0;
    public static int MOVE_UP = 1;
    public static int MOVE_DOWN = 2;
    public static int MOVE_RIGHT = 3;

    private static String ICICLE_KEY = "snake-view";

    private static final String TAG = "[Chord][ApiTest]";
    private static final String TAGClass = "SnakeActivity : ";
    private String mChannelName = "";
    private SnakeView mSnakeView;
    private SnakeMessage mSnakeMessage;
    private CheckBox mIamReadyCheckbox;
    private RelativeLayout mPlayersContainer;
    private LinearLayout mPlayersLinearLayout;
	private HashMap<String, SnakePlayer> mPlayers;
	private String mName;
	private Integer mColor;
	
    /**
     * Called when Activity is first created. Turns off the title bar, sets up the content views,
     * and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayers = new HashMap<String, SnakePlayer>();
        setContentView(R.layout.snake_layout);
        mName = getActivity().getSharedPreferences("snake", MODE_PRIVATE).getString("name", "Anonymouse");
        
        mPlayersContainer = (RelativeLayout)findViewById(R.id.playersContainer);
        mPlayersLinearLayout = (LinearLayout)findViewById(R.id.playersList);
        
        mIamReadyCheckbox = (CheckBox) findViewById(R.id.iamready);
        mIamReadyCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sendState(isChecked);
				checkStateAndStartGame();
			}
		});
        
        mSnakeView = (SnakeView) findViewById(R.id.snake);
        Random r = new Random();
		mColor = r.nextInt(7) + 1;
        ImageView mycolor = (ImageView)findViewById(R.id.mycolor);
        Bitmap[] bitmaps = mSnakeView.getTileArray();
        mycolor.setImageBitmap(bitmaps[mColor]);
        mSnakeView.color = mColor;
        mSnakeView.setDependentViews((TextView) findViewById(R.id.text),
                findViewById(R.id.arrowContainer), findViewById(R.id.background));
        mSnakeMessage = new SnakeMessage();
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(SnakeView.PAUSE);
            }
        }
        mSnakeView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSnakeView.getGameState() == SnakeView.RUNNING) {
                    // Normalize x,y between 0 and 1
                    float x = event.getX() / v.getWidth();
                    float y = event.getY() / v.getHeight();

                    // Direction will be [0,1,2,3] depending on quadrant
                    int direction = 0;
                    direction = (x > y) ? 1 : 0;
                    direction |= (x > 1 - y) ? 2 : 0;

                    // Direction is same as the quadrant which was clicked
                    mSnakeView.moveSnake(direction);
                    mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                    sendData();
                } else {
                    // If the game is not running then on touching any part of the screen
                    // we start the game by sending MOVE_UP signal to SnakeView
                    mSnakeView.moveSnake(MOVE_UP);
                    mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                    sendData();
                }
                return false;
            }
        });
        
//        startService();
//        bindChordService();
    }
    
    private Activity getActivity() {
    	return Snake.this;
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * [A] Initialize Chord!
         */
        if (mChordManager == null) {
        	Log.d(TAG, TAGClass + "\n[A] Initialize Chord!");
            initChord();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        mSnakeView.setMode(SnakeView.PAUSE);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        sendState(false);
    }

    @Override
    public void onDestroy() {
        /**
         * [D] Release Chord!
         */
        if (mChordManager != null) {
        	mChordManager.close();
            mChordManager = null;
        }

        super.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Store the game state
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    /**
     * Handles key events in the game. Update the direction our snake is traveling based on the
     * DPAD.
     *
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
		switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mSnakeView.moveSnake(MOVE_UP);
                mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                sendData();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mSnakeView.moveSnake(MOVE_RIGHT);
                mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                sendData();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSnakeView.moveSnake(MOVE_DOWN);
                mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                sendData();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mSnakeView.moveSnake(MOVE_LEFT);
                mSnakeMessage.setAction(SnakeMessage.ACTION_MOVE);
                sendData();
                break;
        }

        return super.onKeyDown(keyCode, msg);
    }

    private void checkStateAndStartGame() {
    	if (mPlayers.size() == 0) {
    		return;
    	}
    	boolean startGame = mIamReadyCheckbox.isChecked();
		for (String n : mPlayers.keySet()) {
    		SnakePlayer p = mPlayers.get(n);
    		startGame &= p.ready;
    	}
		if (startGame) {
			mPlayersContainer.setVisibility(View.GONE);
		}
    }
    
    private void updateViewWithPlayersList() {
    	mPlayersLinearLayout.removeAllViews();
    	for (String node : mPlayers.keySet()) {
    		SnakePlayer p = mPlayers.get(node);
    		Log.d(TAG, TAGClass + "Player="+p.toString());
    		TextView text = new TextView(getActivity(), null, android.R.attr.textAppearanceLarge);
    		text.setText(String.format("%s is ready - %s", p.name, p.ready ? "OK" : "NOT"));
    		text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    		mPlayersLinearLayout.addView(text);
    	}
    }
    
    private void sendState(boolean isChecked) {
		if (isChecked) {
			mSnakeMessage.setAction(SnakeMessage.ACTION_READY);
		} else {
			mSnakeMessage.setAction(SnakeMessage.ACTION_REFUSE);
		}
		sendData();
		Log.d(TAG, TAGClass + "onCheckedChanged() message="+mSnakeMessage.toString());
    }
    
    private void sendData() {
    	mSnakeMessage.setPlayer(mName, mSnakeView.getSnakeTrailCoordinates(), mColor, mSnakeView.getMoveDelay(), mSnakeView.getDirection());
    	Log.d(TAG, TAGClass + "sendData() message="+mSnakeMessage.toString());
    	
    	byte[][] payload = new byte[1][];
        payload[0] = mSnakeMessage.toString().getBytes();

        SchordChannel channel = mChordManager.getJoinedChannel(CHORD_HELLO_TEST_CHANNEL);
        channel.sendDataToAll(CHORD_SAMPLE_MESSAGE_TYPE, payload);
        
    	mSnakeMessage.reset();
    }
    
    //Chord specific code, copied from BasicChordSample (Samsung Mobile SDK 1.0.3)
    private static final String CHORD_HELLO_TEST_CHANNEL = "com.samsung.android.sdk.chord.example.HELLOTESTCHANNEL";
	private static final String CHORD_SAMPLE_MESSAGE_TYPE = "com.samsung.android.sdk.chord.example.MESSAGE_TYPE";
	private SchordManager mChordManager = null;
	private int mSelectedInterface = -1;
	
    private void initChord() {

        /****************************************************
         * 1. GetInstance
         ****************************************************/
        
        Schord chord = new Schord();        
        try {
            chord.initialize(getActivity());
        } catch (SsdkUnsupportedException e) {
            if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED) {
                // Vendor is not SAMSUNG
                return;
            }
        }        
        mChordManager = new SchordManager(getActivity());
        Log.d(TAG, TAGClass +"    getInstance");

        /****************************************************
         * 2. Set some values before start If you want to use secured channel,
         * you should enable SecureMode. Please refer
         * UseSecureChannelFragment.java mChordManager.enableSecureMode(true);
         * 
         *
         * Once you will use sendFile or sendMultiFiles, you have to call setTempDirectory  
         * mChordManager.setTempDirectory(Environment.getExternalStorageDirectory().getAbsolutePath()
         *       + "/Chord");
         ****************************************************/
        Log.d(TAG, TAGClass +"    setLooper");
        mChordManager.setLooper(getActivity().getMainLooper());

        /**
         * Optional. If you need listening network changed, you can set callback
         * before starting chord.
         */
        mChordManager.setNetworkListener(new NetworkListener() {

            @Override
            public void onDisconnected(int interfaceType) {
                if (interfaceType == mSelectedInterface) {
                    Toast.makeText(getActivity(),
                            getInterfaceName(interfaceType) + " is disconnected",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onConnected(int interfaceType) {
                if (interfaceType == mSelectedInterface) {
                    Toast.makeText(getActivity(),
                            getInterfaceName(interfaceType) + " is connected",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        //auto start connection
        startChord();

    }

    private String getInterfaceName(int interfaceType) {
        if (SchordManager.INTERFACE_TYPE_WIFI == interfaceType)
            return "Wi-Fi";
        else if (SchordManager.INTERFACE_TYPE_WIFI_AP == interfaceType)
            return "Mobile AP";
        else if (SchordManager.INTERFACE_TYPE_WIFI_P2P == interfaceType)
            return "Wi-Fi Direct";

        return "UNKNOWN";
    }

    private void startChord() {
        /**
         * 3. Start Chord using the first interface in the list of available
         * interfaces.
         */
        List<Integer> infList = mChordManager.getAvailableInterfaceTypes();
        if(infList.isEmpty()){
            Log.d(TAG, TAGClass +"    There is no available connection.");
            return;
        }
        
        int interfaceType = 0;
        for (int interfaceValue : mChordManager.getAvailableInterfaceTypes()) {
            Log.d(TAG, TAGClass + "Available interface : " + interfaceValue);
            if (interfaceValue == SchordManager.INTERFACE_TYPE_WIFI) {
            	interfaceType = SchordManager.INTERFACE_TYPE_WIFI;
            	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFI");
            	break;
            } else if (interfaceValue == SchordManager.INTERFACE_TYPE_WIFI_AP) {
            	interfaceType = SchordManager.INTERFACE_TYPE_WIFI_AP;
            	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFI_AP");
            	break;
            } else if (interfaceValue == SchordManager.INTERFACE_TYPE_WIFI_P2P) {
            	interfaceType = SchordManager.INTERFACE_TYPE_WIFI_P2P;
            	Log.d(TAG, TAGClass + "ChordManager.INTERFACE_TYPE_WIFI_P2P");
            	break;
            }
        }
        
        try {
            mChordManager.start(interfaceType, mManagerListener);
            mSelectedInterface = interfaceType;
            Log.d(TAG, TAGClass +"    start(" + getInterfaceName(interfaceType) + ")");
        } catch (IllegalArgumentException e) {
            Log.d(TAG, TAGClass +"    Fail to start -" + e.getMessage());
        } catch (InvalidInterfaceException e) {
            Log.d(TAG, TAGClass +"    There is no such a connection.");
        } catch (Exception e) {
            Log.d(TAG, TAGClass +"    Fail to start -" + e.getMessage());
        }
    }

    /**
     * ChordManagerListener
     */
    SchordManager.StatusListener mManagerListener = new SchordManager.StatusListener() {

        @Override
        public void onStarted(String nodeName, int reason) {
            /**
             * 4. Chord has started successfully
             */
            if (reason == STARTED_BY_USER) {
                // Success to start by calling start() method
                Log.d(TAG, TAGClass +"    >onStarted(" + nodeName + ", STARTED_BY_USER)");
                joinTestChannel();
            } else if (reason == STARTED_BY_RECONNECTION) {
                // Re-start by network re-connection.
                Log.d(TAG, TAGClass +"    >onStarted(" + nodeName + ", STARTED_BY_RECONNECTION)");
            }

        }

        @Override
        public void onStopped(int reason) {
            /**
             * 8. Chord has stopped successfully
             */
            if (STOPPED_BY_USER == reason) {
                // Success to stop by calling stop() method
                Log.d(TAG, TAGClass +"    >onStopped(STOPPED_BY_USER)");
            } else if (NETWORK_DISCONNECTED == reason) {
                // Stopped by network disconnected
                Log.d(TAG, TAGClass +"    >onStopped(NETWORK_DISCONNECTED)");
            }
        }
    };

    private void joinTestChannel() {
        SchordChannel channel = null;
        
        /**
         * 5. Join my channel
         */
        Log.d(TAG, TAGClass +"    joinChannel");
        channel = mChordManager.joinChannel(CHORD_HELLO_TEST_CHANNEL, mChannelListener);

        if (channel == null) {
            Log.d(TAG, TAGClass +"    Fail to joinChannel");
        }
    }

    private void stopChord() {
        if (mChordManager == null)
            return;

        /**
         * If you registered NetworkListener, you should unregister it.
         */
        mChordManager.setNetworkListener(null);

        /**
         * 7. Stop Chord. You can call leaveChannel explicitly.
         * mChordManager.leaveChannel(CHORD_HELLO_TEST_CHANNEL);
         */
        Log.d(TAG, TAGClass +"    stop");
        mChordManager.stop();
    }

    // ***************************************************
    // ChordChannelListener
    // ***************************************************
    private SchordChannel.StatusListener mChannelListener = new SchordChannel.StatusListener() {

        /**
         * Called when a node leave event is raised on the channel.
         */
        @Override
        public void onNodeLeft(String fromNode, String fromChannel) {
            Log.d(TAG, TAGClass +"    >onNodeLeft(" + fromNode + ")");
        }

        /**
         * Called when a node join event is raised on the channel
         */
        @Override
        public void onNodeJoined(String fromNode, String fromChannel) {
            Log.d(TAG, TAGClass +"    >onNodeJoined(" + fromNode + ")");

            /**
             * 6. Send data to joined node
             */
            Log.v(TAG, TAGClass + "onNodeJoined node="+fromNode+" channel="+fromChannel+" bJoined=true");
			sendState(mIamReadyCheckbox.isChecked());
        }

        /**
         * Called when the data message received from the node.
         */
        @Override
        public void onDataReceived(String fromNode, String fromChannel, String payloadType,
                byte[][] payload) {
            /**
             * 6. Received data from other node
             */
        	String message = new String(payload[0]);
        	Log.d(TAG, TAGClass +"    >onDataReceived(" + fromNode + ", " + message + ")");
            if(!payloadType.equals(CHORD_SAMPLE_MESSAGE_TYPE)){
                return;
            }
        	String node = fromNode;
        	String channel = fromChannel;
        	Log.v(TAG, TAGClass + "onReceiveMessage node="+node+" channel="+channel+" message="+message);
    		mSnakeMessage.load(message);
    		SnakePlayer p = mSnakeMessage.getPlayer();
    		if (p == null) {
    			return;
    		}
    		switch (mSnakeMessage.getAction()) {
    		case SnakeMessage.ACTION_MOVE:
    			p.ready = true;
    			mPlayers.put(node, p);
    			updateViewWithPlayersList();
    			break;
    		case SnakeMessage.ACTION_READY:
    			p.ready = true;
    			mPlayers.put(node, p);
    			updateViewWithPlayersList();
    			checkStateAndStartGame();
    			break;
    		case SnakeMessage.ACTION_REFUSE:
    			mPlayers.put(node, p);
    			updateViewWithPlayersList();
    			break;
    		default:
    			mPlayers.put(node, p);
    			break;
    		}
    		mSnakeView.updatePlayers(mPlayers);
    		
        }

        /**
         * The following callBacks are not used in this Fragment. Please refer
         * to the SendFilesFragment.java
         */
        @Override
        public void onMultiFilesWillReceive(String fromNode, String fromChannel, String fileName,
                String taskId, int totalCount, String fileType, long fileSize) {

        }

        @Override
        public void onMultiFilesSent(String toNode, String toChannel, String fileName,
                String taskId, int index, String fileType) {

        }

        @Override
        public void onMultiFilesReceived(String fromNode, String fromChannel, String fileName,
                String taskId, int index, String fileType, long fileSize, String tmpFilePath) {

        }

        @Override
        public void onMultiFilesFinished(String node, String channel, String taskId, int reason) {

        }

        @Override
        public void onMultiFilesFailed(String node, String channel, String fileName, String taskId,
                int index, int reason) {

        }

        @Override
        public void onMultiFilesChunkSent(String toNode, String toChannel, String fileName,
                String taskId, int index, String fileType, long fileSize, long offset,
                long chunkSize) {

        }

        @Override
        public void onMultiFilesChunkReceived(String fromNode, String fromChannel, String fileName,
                String taskId, int index, String fileType, long fileSize, long offset) {

        }

        @Override
        public void onFileWillReceive(String fromNode, String fromChannel, String fileName,
                String hash, String fileType, String exchangeId, long fileSize) {

        }

        @Override
        public void onFileSent(String toNode, String toChannel, String fileName, String hash,
                String fileType, String exchangeId) {

        }

        @Override
        public void onFileReceived(String fromNode, String fromChannel, String fileName,
                String hash, String fileType, String exchangeId, long fileSize, String tmpFilePath) {

        }

        @Override
        public void onFileFailed(String node, String channel, String fileName, String hash,
                String exchangeId, int reason) {

        }

        @Override
        public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash,
                String fileType, String exchangeId, long fileSize, long offset, long chunkSize) {

        }

        @Override
        public void onFileChunkReceived(String fromNode, String fromChannel, String fileName,
                String hash, String fileType, String exchangeId, long fileSize, long offset) {

        }

    };
    
}

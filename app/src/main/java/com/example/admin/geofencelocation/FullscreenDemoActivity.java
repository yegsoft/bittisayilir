/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.example.admin.geofencelocation;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Sample activity showing how to properly enable custom fullscreen behavior.
 * <p>
 * This is the preferred way of handling fullscreen because the default fullscreen implementation
 * will cause re-buffering of the video.
 */
public class FullscreenDemoActivity extends YouTubeFailureRecoveryActivity implements
    View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    YouTubePlayer.OnFullscreenListener {

  private static final int PORTRAIT_ORIENTATION = Build.VERSION.SDK_INT < 9
      ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

  private LinearLayout baseLayout;
  private YouTubePlayerView playerView;
  private YouTubePlayer player;
  private Button fullscreenButton;
  private CompoundButton checkbox;
  private View otherViews;

  private boolean fullscreen;

    String oynat;
    ArrayList<String> myArrayList=new ArrayList<String>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.fullscreen_demo);
    baseLayout = (LinearLayout) findViewById(R.id.layout);
    playerView = (YouTubePlayerView) findViewById(R.id.player);
    fullscreenButton = (Button) findViewById(R.id.fullscreen_button);
    checkbox = (CompoundButton) findViewById(R.id.landscape_fullscreen_checkbox);
    otherViews = findViewById(R.id.other_views);

    checkbox.setOnCheckedChangeListener(this);
    // You can use your own button to switch to fullscreen too
    fullscreenButton.setOnClickListener(this);

    playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

    doLayout();
  }

  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player,
                                      final boolean wasRestored) {


      final String location = getIntent().getExtras().getString("location");
      Log.d("Yusuf", "onCreate: location is " + location);
      this.player=player;




      DatabaseReference oku = FirebaseDatabase.getInstance().getReference().child("konumlar");
      ValueEventListener listener = new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              long size = dataSnapshot.getChildrenCount();
              int kayit=1;

              for (int i = 1; i <= size; i++) {
                  String holdName = dataSnapshot.child("" + i).child("isim").getValue(String.class);
                  if (holdName.equals(location))  {
                      kayit=i;
                      break;
                  }
              }

              String youtube = dataSnapshot.child("" + kayit).child("link").getValue(String.class);
              oynat=youtube;
              Log.d("Yusuf", "BBBBBBBBBBBBBBBBB " + youtube);


              setControlsEnabled();
              // Specify that we want to handle fullscreen behavior ourselves.
              player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
              player.setOnFullscreenListener(FullscreenDemoActivity.this);

                  player.cueVideo(oynat);


              myArrayList.add(oynat);
              int uzunluk=myArrayList.size();

              SharedPreferences sPrefs= PreferenceManager.getDefaultSharedPreferences(FullscreenDemoActivity.this);
              SharedPreferences.Editor sEdit=sPrefs.edit();

              String a = myArrayList.get(uzunluk-1);

              Log.d("Yusuf", "uuuuuuuuu " + a);

              sEdit.putString("deger",myArrayList.get(uzunluk-1));
              sEdit.putInt("size",myArrayList.size());
              sEdit.commit();


          };

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      };oku.addListenerForSingleValueEvent(listener);



  }

  @Override
  protected YouTubePlayer.Provider getYouTubePlayerProvider() {
    return playerView;
  }

  @Override
  public void onClick(View v) {
    player.setFullscreen(!fullscreen);
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    int controlFlags = player.getFullscreenControlFlags();
    if (isChecked) {
      // If you use the FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE, your activity's normal UI
      // should never be laid out in landscape mode (since the video will be fullscreen whenever the
      // activity is in landscape orientation). Therefore you should set the activity's requested
      // orientation to portrait. Typically you would do this in your AndroidManifest.xml, we do it
      // programmatically here since this activity demos fullscreen behavior both with and without
      // this flag).
      setRequestedOrientation(PORTRAIT_ORIENTATION);
      controlFlags |= YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
      controlFlags &= ~YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE;
    }
    player.setFullscreenControlFlags(controlFlags);
  }

  private void doLayout() {
    LinearLayout.LayoutParams playerParams =
        (LinearLayout.LayoutParams) playerView.getLayoutParams();
    if (fullscreen) {
      // When in fullscreen, the visibility of all other views than the player should be set to
      // GONE and the player should be laid out across the whole screen.
      playerParams.width = LayoutParams.MATCH_PARENT;
      playerParams.height = LayoutParams.MATCH_PARENT;

      otherViews.setVisibility(View.GONE);
    } else {
      // This layout is up to you - this is just a simple example (vertically stacked boxes in
      // portrait, horizontally stacked in landscape).
      otherViews.setVisibility(View.VISIBLE);
      ViewGroup.LayoutParams otherViewsParams = otherViews.getLayoutParams();
      if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        playerParams.width = otherViewsParams.width = 0;
        playerParams.height = WRAP_CONTENT;
        otherViewsParams.height = MATCH_PARENT;
        playerParams.weight = 1;
        baseLayout.setOrientation(LinearLayout.HORIZONTAL);
      } else {
        playerParams.width = otherViewsParams.width = MATCH_PARENT;
        playerParams.height = WRAP_CONTENT;
        playerParams.weight = 0;
        otherViewsParams.height = 0;
        baseLayout.setOrientation(LinearLayout.VERTICAL);
      }
      setControlsEnabled();
    }
  }

  private void setControlsEnabled() {
    checkbox.setEnabled(player != null
        && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    fullscreenButton.setEnabled(player != null);
  }

  @Override
  public void onFullscreen(boolean isFullscreen) {
    fullscreen = isFullscreen;
    doLayout();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    doLayout();
  }

}

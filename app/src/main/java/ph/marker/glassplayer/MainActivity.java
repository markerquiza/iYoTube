package ph.marker.glassplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.media.AudioManager;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
 private static final int VOICE_SEARCH = 41;
 private static final String LAST_URL = "last_url";
 private static final int ACCENT = 0xffb7ffbf;
 private ScrollView menu;
 private Button playLast;
 private FrameLayout playbackStage;
 private boolean fullscreen;
 private boolean browsing;
 private String browseControls;
 private String resultsUrl;
 private final List<String> browseQueue = new ArrayList<>();
 private int queueIndex = -1;
 private ExoPlayer player;
 private PlayerView playerView;
 private WebView web;
 private TextView status;
 private boolean youtube=false;
 private boolean playlist;
 private final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
 private boolean closed=false;
 private final Runnable poll=new Runnable() { public void run() {
  if(closed) return;
  if(youtube) web.evaluateJavascript("window.glassStatus || 'Loading YouTube…'",value->{
   if(!closed && youtube) try { showStatus(new org.json.JSONTokener(value).nextValue().toString()); } catch(Exception ignored) {}
  });

  handler.postDelayed(this,1000);
 }};
 @androidx.media3.common.util.UnstableApi
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  getWindow().getDecorView().setSystemUiVisibility(5894);
  FrameLayout root = new FrameLayout(this);
  root.setBackgroundColor(Color.BLACK);
  FrameLayout stage = new FrameLayout(this);
  root.addView(stage, new FrameLayout.LayoutParams(-1, -1));

  menu = new ScrollView(this);
  menu.setFillViewport(true);
  menu.setVerticalScrollBarEnabled(false);
  LinearLayout home = new LinearLayout(this);
  home.setOrientation(LinearLayout.VERTICAL);
  home.setPadding(dp(20), dp(24), dp(20), dp(20));
  menu.addView(home);

  TextView title = new TextView(this);
  title.setText("IyoTube");
  title.setTextSize(30);
  title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
  title.setTextColor(ACCENT);
  home.addView(title);
  TextView subtitle = new TextView(this);
  subtitle.setText("Videos & playlists");
  subtitle.setTextSize(14);
  subtitle.setTextColor(0xff9bd5a2);
  subtitle.setPadding(0, dp(4), 0, dp(20));
  home.addView(subtitle);

  Button open = addMenuButton(home, "Search or open link", true);
  open.setOnClickListener(v -> input());
  playLast = addMenuButton(home, "Play last video", false);
  playLast.setOnClickListener(v -> play(VideoSource.parse(getPreferences(MODE_PRIVATE)
    .getString(LAST_URL, ""))));
  playLast.setVisibility(getPreferences(MODE_PRIVATE).contains(LAST_URL) ? View.VISIBLE : View.GONE);
  Button voice = addMenuButton(home, "Voice search", false);
  voice.setOnClickListener(v -> startVoiceSearch());
  Button browse = addMenuButton(home, "Browse YouTube", false);
  browse.setOnClickListener(v -> browseYouTube("https://m.youtube.com/"));
  Button sample = addMenuButton(home, "Play a sample video", false);
  sample.setOnClickListener(v -> play(VideoSource.parse("https://youtu.be/M7lc1UVf-VE")));
  Button wifi = addMenuButton(home, "Wi-Fi settings", false);
  wifi.setOnClickListener(v -> {
   try {
    startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
   } catch (ActivityNotFoundException | SecurityException e) {
    new AlertDialog.Builder(this)
      .setMessage("The glasses did not open Wi-Fi settings.")
      .setPositiveButton("OK", null)
      .show();
   }
  });
  root.addView(menu, new FrameLayout.LayoutParams(-1, -1));
  player=new ExoPlayer.Builder(this).build(); playerView=new PlayerView(this); playerView.setPlayer(player); stage.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
  web=new WebView(this); web.setBackgroundColor(Color.BLACK); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.getSettings().setMediaPlaybackRequiresUserGesture(false); web.getSettings().setAllowFileAccess(false); web.getSettings().setAllowContentAccess(false);
  web.setWebChromeClient(new WebChromeClient());
  try {
   browseControls = asset("browse-controls.js");
  } catch (IOException error) {
   throw new IllegalStateException("Missing browse controls", error);
  }
  setVolumeControlStream(AudioManager.STREAM_MUSIC);
  web.setWebViewClient(new WebViewClient() {
   @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    if (!request.isForMainFrame()) return false;
    Uri destination = request.getUrl();
    String host = destination.getHost();
    if (!browsing || !"https".equals(destination.getScheme())
      || !("m.youtube.com".equals(host) || "www.youtube.com".equals(host) || "youtube.com".equals(host))) {
     return true;
    }
    String path = destination.getPath();
    if ("/watch".equals(path) || (path != null && path.startsWith("/shorts/"))) {
     try {
      VideoSource source = VideoSource.parse(destination.toString());
      String page = view.getUrl();
      view.evaluateJavascript(browseControls + "('queue')", value -> {
       if (!closed && browsing && Objects.equals(page, view.getUrl())) {
        openBrowseVideo(source, value);
       }
      });
      return true;
     } catch (IllegalArgumentException error) {
      return false;
     }
    }
    return false;
   }
  });
  stage.addView(web,new FrameLayout.LayoutParams(-1,-1)); web.setVisibility(View.GONE);
  playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);
  stage.setVisibility(View.INVISIBLE);
  menu.setVisibility(View.VISIBLE);
  status = new TextView(this);
  status.setTextColor(Color.GREEN);
  status.setBackgroundColor(Color.BLACK);
  status.setPadding(8, 8, 8, 8);
  status.setVisibility(View.GONE);
  FrameLayout.LayoutParams statusLayout = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
  stage.addView(status, statusLayout);
  playbackStage = stage;
  setContentView(root);
  open.requestFocus();
  player.addListener(new Player.Listener(){ @Override public void onPlayerError(PlaybackException error) { showStatus("Playback failed: "+error.getErrorCodeName()+" • check media URL/network"); handler.removeCallbacks(poll); } });
  playerView.setUseController(false);
  playerView.setOnClickListener(v->command("toggle"));
  final float[] touchStart=new float[2];
  playerView.setOnTouchListener((v,e)->{
   if(e.getActionMasked()==MotionEvent.ACTION_DOWN){touchStart[0]=e.getX();touchStart[1]=e.getY();return true;}
   if(e.getActionMasked()==MotionEvent.ACTION_UP){
    float dx=e.getX()-touchStart[0],dy=e.getY()-touchStart[1];
    if(Math.abs(dx)<30 && Math.abs(dy)<30)v.performClick();
    else if(Math.abs(dx)>60 && Math.abs(dx)>Math.abs(dy))command(dx>0?"forward":"back");
    return true;
   }
   return true;
  });
  handler.post(poll);
 }
 private int dp(int value) {
  return Math.round(value * getResources().getDisplayMetrics().density);
 }

 private GradientDrawable menuBackground(int fill, int outline, int stroke) {
  GradientDrawable background = new GradientDrawable();
  background.setColor(fill);
  background.setCornerRadius(dp(12));
  background.setStroke(dp(stroke), outline);
  return background;
 }

 private Button addMenuButton(LinearLayout home, String label, boolean primary) {
  Button button = new Button(this);
  button.setText(label);
  button.setAllCaps(false);
  button.setTextSize(17);
  button.setTextColor(ACCENT);
  button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
  button.setPadding(dp(16), dp(8), dp(16), dp(8));
  button.setMinHeight(dp(52));
  button.setFocusable(true);
  StateListDrawable background = new StateListDrawable();
  background.addState(new int[]{android.R.attr.state_pressed}, menuBackground(0xff325d38, ACCENT, 2));
  background.addState(new int[]{android.R.attr.state_focused}, menuBackground(0xff183c21, ACCENT, 2));
  background.addState(new int[]{}, menuBackground(primary ? 0xff102b18 : 0xff07160b, 0xff44724b, 1));
  button.setBackground(background);
  LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(-1, -2);
  layout.bottomMargin = dp(10);
  home.addView(button, layout);
  return button;
 }

 private String asset(String name) throws IOException { try(InputStream in=getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) {byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);return out.toString("UTF-8");} }
 private void startVoiceSearch() {
  Intent intent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
  intent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
  intent.putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say a YouTube video title");
  try {
   startActivityForResult(intent, VOICE_SEARCH);
  } catch (ActivityNotFoundException | SecurityException error) {
   new AlertDialog.Builder(this)
     .setMessage("Voice input is unavailable on these glasses. You can still type a title.")
     .setPositiveButton("Type title", (dialog, button) -> input())
     .setNegativeButton("Cancel", null)
     .show();
  }
 }

 @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if (requestCode != VOICE_SEARCH || resultCode != RESULT_OK || data == null) return;
  List<String> results = data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
  if (results == null || results.isEmpty()) return;
  String query = results.get(0).trim();
  if (!query.isEmpty()) searchYouTube(query);
 }

 private void searchYouTube(String query) {
  browseYouTube(Uri.parse("https://m.youtube.com/results").buildUpon()
    .appendQueryParameter("search_query", query).build().toString());
 }

 private void input() {
  EditText input = new EditText(this);
  input.setSingleLine();
  input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
    | android.text.InputType.TYPE_TEXT_VARIATION_URI);
  input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_GO);
  input.setHint("Video title or URL");

  AlertDialog dialog = new AlertDialog.Builder(this)
    .setTitle("Search or open link")
    .setView(input)
    .setPositiveButton("Go", null)
    .setNegativeButton("Cancel", null)
    .create();

  dialog.setOnShowListener(ignored -> {
   Button playButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
   playButton.setOnClickListener(v -> {
    String query = input.getText().toString().trim();
    if (query.isEmpty()) {
     input.setError("Enter a video title or link.");
     return;
    }
    if (!query.contains("://") && !query.matches("(?i)^(www\\.|m\\.)?(youtube\\.com|youtu\\.be)/.*")) {
     dialog.dismiss();
     searchYouTube(query);
     return;
    }
    if (!query.contains("://")) query = "https://" + query;
    VideoSource source;
    try {
     source = VideoSource.parse(query);
    } catch (IllegalArgumentException error) {
     input.setError(error.getMessage());
     return;
    }
    dialog.dismiss();
    play(source);
   });
   input.setOnEditorActionListener((view, action, event) -> {
    if (action != android.view.inputmethod.EditorInfo.IME_ACTION_GO) return false;
    playButton.performClick();
    return true;
   });
  });
  input.requestFocus();
  dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  dialog.show();
 }
 private void showStatus(String message) {
  status.setText(message);
  status.setVisibility(message.equals("YouTube playing") || message.equals("YouTube paused")
    ? View.GONE : View.VISIBLE);
 }

 private void browseYouTube(String url) {
  browseQueue.clear();
  queueIndex = -1;
  fullscreen = true;
  browsing = true;
  youtube = false;
  playlist = false;
  menu.setVisibility(View.GONE);
  playbackStage.setVisibility(View.VISIBLE);
  playerView.setVisibility(View.GONE);
  status.setVisibility(View.GONE);
  web.setVisibility(View.VISIBLE);
  web.loadUrl(url);
  web.requestFocus();
 }

 private void openBrowseVideo(VideoSource source, String result) {
  resultsUrl = web.getUrl();
  browseQueue.clear();
  String selected = "https://youtu.be/" + source.youtubeId;
  try {
   JSONArray urls = new JSONObject(result).getJSONArray("urls");
   for (int i = 0; i < urls.length(); i++) {
    VideoSource item = VideoSource.parse(urls.getString(i));
    if (item.youtubeId != null) browseQueue.add("https://youtu.be/" + item.youtubeId);
   }
  } catch (JSONException | IllegalArgumentException ignored) {
   browseQueue.clear();
  }
  queueIndex = browseQueue.indexOf(selected);
  if (queueIndex < 0) {
   browseQueue.add(0, selected);
   queueIndex = 0;
  }
  play(source);
 }

 private boolean handleBrowseKey(KeyEvent event) {
  String action;
  switch (event.getKeyCode()) {
   case KeyEvent.KEYCODE_DPAD_RIGHT:
   case KeyEvent.KEYCODE_DPAD_DOWN:
    action = "next";
    break;
   case KeyEvent.KEYCODE_DPAD_LEFT:
   case KeyEvent.KEYCODE_DPAD_UP:
    action = "previous";
    break;
   case KeyEvent.KEYCODE_DPAD_CENTER:
   case KeyEvent.KEYCODE_BUTTON_A:
    action = "open";
    break;
   default:
    return false;
  }
  if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
   web.evaluateJavascript(browseControls + "(" + JSONObject.quote(action) + ")", result -> {
    if (closed || !browsing || !"open".equals(action) || "null".equals(result)) return;
    try {
     String selected = new JSONObject(result).getString("selected");
     openBrowseVideo(VideoSource.parse(selected), result);
    } catch (JSONException | IllegalArgumentException ignored) {
     // The page did not return a playable video.
    }
   });
  }
  return true;
 }

 private void play(VideoSource source) {
  browsing = false;
  getPreferences(MODE_PRIVATE).edit().putString(LAST_URL, source.url).apply();
  playLast.setVisibility(View.VISIBLE);
  fullscreen = true;
  menu.setVisibility(View.GONE);
  playbackStage.setVisibility(View.VISIBLE);
  handler.removeCallbacks(poll); handler.post(poll);
  player.stop();
  player.clearMediaItems();
  web.loadUrl("about:blank"); playlist = source.playlistId != null;
  youtube = source.youtubeId != null || playlist;
  web.setVisibility(youtube?View.VISIBLE:View.GONE); playerView.setVisibility(youtube?View.GONE:View.VISIBLE);
  showStatus("Loading…");
  if(youtube) try { String html=asset("youtube.html")
    .replace("__VIDEO_ID__", source.youtubeId == null ? "" : source.youtubeId)
    .replace("__PLAYLIST_ID__", source.playlistId == null ? "" : source.playlistId); web.loadDataWithBaseURL("https://ph.marker.glassplayer/",html,"text/html","UTF-8",null); }catch(IOException e){showStatus("YouTube player asset missing");}
  else { player.setMediaItem(MediaItem.fromUri(source.url)); player.prepare(); player.play(); status.setVisibility(View.GONE); }
 }
 private void command(String action) {
  if(closed) return;
  if(action.equals("volumeUp")||action.equals("volumeDown")){ ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,action.equals("volumeUp")?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,0);return; }
  if(youtube) { web.evaluateJavascript("window.glassCommand && window.glassCommand("+JSONObject.quote(action)+")",null);return; }
  switch(action){case "toggle":if(player.getPlayWhenReady())player.pause();else player.play();break;case "back":player.seekTo(Math.max(0,player.getCurrentPosition()-10000));break;case "forward":long next=player.getCurrentPosition()+10000;player.seekTo(player.getDuration()>0?Math.min(next,player.getDuration()):next);break;}
 }

 @Override public void onBackPressed() {
  if (!fullscreen) {
   super.onBackPressed();
   return;
  }
  if (!browsing && queueIndex >= 0 && resultsUrl != null) {
   browseYouTube(resultsUrl);
   return;
  }
  fullscreen = false;
  browsing = false;
  queueIndex = -1;
  browseQueue.clear();
  youtube = false;
  web.loadUrl("about:blank");
  player.stop();
  player.clearMediaItems();
  playbackStage.setVisibility(View.INVISIBLE);
  menu.setVisibility(View.VISIBLE);
 }

 @Override public boolean dispatchKeyEvent(KeyEvent event) {
  int key = event.getKeyCode();
  if (browsing) return handleBrowseKey(event) || super.dispatchKeyEvent(event);
  if (!fullscreen) {
   if (event.getAction() == KeyEvent.ACTION_DOWN && getCurrentFocus() instanceof Button
     && (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT)) {
    int direction = key == KeyEvent.KEYCODE_DPAD_LEFT ? View.FOCUS_UP : View.FOCUS_DOWN;
    View next = getCurrentFocus().focusSearch(direction);
    if (next != null) next.requestFocus();
    return true;
   }
   return super.dispatchKeyEvent(event);
  }
  if (queueIndex >= 0 && (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT)) {
   if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
    int next = queueIndex + (key == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
    if (next >= 0 && next < browseQueue.size()) {
     queueIndex = next;
     play(VideoSource.parse(browseQueue.get(next)));
    }
   }
   return true;
  }
  String a=null;
  switch(key){case KeyEvent.KEYCODE_DPAD_CENTER:case KeyEvent.KEYCODE_BUTTON_A:case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:case KeyEvent.KEYCODE_SPACE:a="toggle";break;case KeyEvent.KEYCODE_DPAD_LEFT:a=playlist?"previous":"back";break;case KeyEvent.KEYCODE_MEDIA_REWIND:a="back";break;case KeyEvent.KEYCODE_DPAD_RIGHT:a=playlist?"next":"forward";break;case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:a="forward";break;case KeyEvent.KEYCODE_DPAD_UP:a="volumeUp";break;case KeyEvent.KEYCODE_DPAD_DOWN:a="volumeDown";break;}
  if(a!=null){if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0)command(a);return true;}return super.dispatchKeyEvent(event);
 }
 @Override protected void onDestroy(){closed=true;handler.removeCallbacksAndMessages(null);if(player!=null)player.release();if(web!=null){web.stopLoading();web.destroy();}super.onDestroy();}
}

package ph.marker.glassplayer;
import org.junit.Test;
import static org.junit.Assert.*;
public class VideoSourceTest {
 @Test public void recognizesSupportedYoutubeForms() {
  String id="dQw4w9WgXcQ";
  for(String url:new String[]{"https://youtu.be/"+id+"?si=test","https://www.youtube.com/watch?v="+id+"&t=10","https://m.youtube.com/shorts/"+id,"https://youtube.com/live/"+id,"https://youtube.com/embed/"+id}) assertEquals(id,VideoSource.parse(url).youtubeId);
 }
 @Test public void recognizesPlaylistWithoutVideoId() {
  VideoSource source = VideoSource.parse("https://www.youtube.com/playlist?list=PLC77007E23FF423C6");
  assertEquals("PLC77007E23FF423C6", source.playlistId);
  assertNull(source.youtubeId);
 }

 @Test public void acceptsDirectMediaWithoutTreatingLookalikeAsYoutube() {
  assertNull(VideoSource.parse("https://example.com/movie.mp4?token=abc").youtubeId);
  assertNull(VideoSource.parse("https://youtube.com.example.org/movie.mp4").youtubeId);
 }
 @Test public void rejectsUnsafeAndMalformedSources() {
  for(String url:new String[]{"javascript:alert(1)","file:///etc/passwd","https://youtube.com/watch?v=oops","https://youtu.be/","https://user:password@example.com/a.mp4","not a URL","https://youtube.com/shorts/","https://youtube.com/playlist?list=123"}) {
   try{VideoSource.parse(url);fail("Accepted: "+url);}catch(IllegalArgumentException expected){}
  }
 }
}

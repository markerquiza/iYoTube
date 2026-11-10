package ph.marker.glassplayer;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Pure Java validation, shared by device and remote inputs. */
public final class VideoSource {
 public final String url, youtubeId, playlistId;
 private VideoSource(String url, String id, String playlistId) {
  this.url = url;
  this.youtubeId = id;
  this.playlistId = playlistId;
 }
 public static VideoSource parse(String input) {
  try {
   String value=input.trim();
   if (value.length()>4096) throw new IllegalArgumentException();
   URI uri=new URI(value);
   String scheme=uri.getScheme(), host=uri.getHost();
   if (!("https".equalsIgnoreCase(scheme)||"http".equalsIgnoreCase(scheme)) || host==null || uri.getUserInfo()!=null) throw new IllegalArgumentException();
   host=host.toLowerCase(Locale.ROOT);
   boolean yt=host.equals("youtu.be")||host.equals("youtube.com")||host.endsWith(".youtube.com");
   String id=null;
   if (yt) {
    String path=uri.getPath();
    if ("/playlist".equals(path)) {
     String query = uri.getRawQuery();
     if (query != null) {
      for (String pair : query.split("&")) {
       String[] parameter = pair.split("=", 2);
       if (parameter.length == 2 && parameter[0].equals("list")) {
        String list = URLDecoder.decode(parameter[1], StandardCharsets.UTF_8.name());
        if (list.matches("[A-Za-z][A-Za-z0-9_-]{1,149}")) return new VideoSource(value, null, list);
       }
      }
     }
     throw new IllegalArgumentException();
    }
    if (host.equals("youtu.be")) id=path.substring(1).split("/")[0];
    else if (path.startsWith("/shorts/")||path.startsWith("/embed/")||path.startsWith("/live/")) id=path.split("/")[2];
    else if ("/watch".equals(path) && uri.getRawQuery()!=null) {
     for (String pair:uri.getRawQuery().split("&")) {
      String[] kv=pair.split("=",2);
      if (kv.length==2 && kv[0].equals("v")) id=URLDecoder.decode(kv[1],StandardCharsets.UTF_8.name());
     }
    }
    if (id==null||!id.matches("[a-zA-Z0-9_-]{11}")) throw new IllegalArgumentException();
   }
   return new VideoSource(value,id,null);
  } catch(Exception e) { throw new IllegalArgumentException("Enter a YouTube video or playlist link, or an HTTP(S) media URL."); }
 }
}

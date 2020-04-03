package media;

import java.io.Serializable;
import java.util.Objects;

/**
 * Useful for requesting a song while also providing the associated ArtistName
 */
public final class SongInfo implements Serializable
{
    private static final long serialVersionUID = -8242968608169744520L;

    private ArtistName artistName;
    private String songName;


    private SongInfo(ArtistName artistName, String songName){
        this.artistName = artistName;
        this.songName = songName;
    }

    public static SongInfo of(ArtistName artistName, String songName)
    {
        return new SongInfo(artistName, songName);
    }

    public ArtistName getArtistName() {
        return artistName;
    }

    public String getSongName() {
        return songName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongInfo songInfo = (SongInfo) o;
        return artistName.equals(songInfo.artistName) &&
                songName.equals(songInfo.songName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistName, songName);
    }
}

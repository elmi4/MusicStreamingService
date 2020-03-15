package media;

import java.util.Arrays;
import java.util.Objects;

public final class MusicFile
{
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private byte[] musicFileExtract;

    public void setTrackName(String trackName)
    {
        this.trackName = trackName;
    }

    public void setArtistName(String artistName)
    {
        this.artistName = artistName;
    }

    public void setAlbumInfo(String albumInfo)
    {
        this.albumInfo = albumInfo;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public void setMusicFileExtract(byte[] musicFileExtract)
    {
        this.musicFileExtract = musicFileExtract;
    }

    public String getTrackName()
    {
        return trackName;
    }

    public String getArtistName()
    {
        return artistName;
    }

    public String getAlbumInfo()
    {
        return albumInfo;
    }

    public String getGenre()
    {
        return genre;
    }

    public byte[] getMusicFileExtract()
    {
        return musicFileExtract;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicFile musicFile = (MusicFile) o;
        return trackName.equals(musicFile.trackName) &&
                artistName.equals(musicFile.artistName) &&
                albumInfo.equals(musicFile.albumInfo) &&
                genre.equals(musicFile.genre) &&
                Arrays.equals(musicFileExtract, musicFile.musicFileExtract);
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hash(trackName, artistName, albumInfo, genre);
        result = 31 * result + Arrays.hashCode(musicFileExtract);
        return result;
    }
}

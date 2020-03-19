package media;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class MusicFile implements Serializable
{
    private static final long serialVersionUID = -1176320510135364380L;
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private byte[] musicFileExtract;

    public MusicFile(String trackName, String artistName, String albumInfo, String genre, byte[] musicFileExtract) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
        this.musicFileExtract = musicFileExtract;
    }

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
    public String toString()
    {
        return "MusicFile{" +
                "trackName='" + trackName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumInfo='" + albumInfo + '\'' +
                ", genre='" + genre + '\'' +
                ", number of bits: "+musicFileExtract.length +
                '}';
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

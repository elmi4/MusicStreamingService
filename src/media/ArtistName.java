package media;

import java.util.Objects;

public final class ArtistName
{
    private String artistName;

    public ArtistName(String artistName)
    {
        this.artistName = artistName;
    }

    public String getArtistName()
    {
        return artistName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistName that = (ArtistName) o;
        return artistName.equals(that.artistName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(artistName);
    }
}

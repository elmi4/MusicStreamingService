package media;

import java.util.Objects;

public final class Value
{
    private MusicFile musicFile;

    public Value(MusicFile musicFile)
    {
        this.musicFile = musicFile;
    }

    public MusicFile getMusicFile()
    {
        return musicFile;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(musicFile);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return musicFile.equals(value.musicFile);
    }
}

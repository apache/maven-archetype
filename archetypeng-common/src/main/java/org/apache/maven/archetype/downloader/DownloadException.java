package org.apache.maven.archetype.downloader;

/**
 * @author Jason van Zyl
 */
public class DownloadException
    extends Exception
{
    public DownloadException( String string )
    {
        super( string );
    }

    public DownloadException( String string,
                              Throwable throwable )
    {
        super( string, throwable );
    }

    public DownloadException( Throwable throwable )
    {
        super( throwable );
    }
}

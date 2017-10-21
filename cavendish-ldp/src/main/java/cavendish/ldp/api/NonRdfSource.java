package cavendish.ldp.api;

import java.net.URI;

public interface NonRdfSource extends Resource {
    public RdfSource getDescription();
    public boolean isRemote();
    public URI getContentLocation();
}

package ee.app.conversa.model;

/**
 * Created by edgargomez on 9/15/16.
 */
public class nHeaderTitle {

    private final String headerName;
    private final int relevance;

    public nHeaderTitle(String headerName, int relevance) {
        this.headerName = headerName;
        this.relevance = relevance;
    }

    public String getHeaderName() {
        return headerName;
    }

    public int getRelevance() {
        return relevance;
    }

}

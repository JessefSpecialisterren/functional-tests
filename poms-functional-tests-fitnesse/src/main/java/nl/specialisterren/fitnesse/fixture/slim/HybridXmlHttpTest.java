package nl.specialisterren.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.XmlHttpTest;

/**
 * Extension of XmlHttpTest that adds functionality to allow non-xml responses
 */
public class HybridXmlHttpTest extends XmlHttpTest {
    protected boolean acceptNonXmlResponses = false;

    public void acceptNonXmlResponsesInSubsequentRequests() {
        acceptNonXmlResponses = true;
    }

    public void rejectNonXmlResponsesInSubsequentRequests() {
        acceptNonXmlResponses = false;
    }

    @Override
    public boolean responseIsValid() {
        if (acceptNonXmlResponses) {
            // Return true or false as HttpTest would if throwExceptionOnHttpRequestFailure were false
            // We rarely enable those exceptions anyway, and this way we avoid relying on implementation details,
            // greatly reducing the probability that a HSAC update will break this override
            int statusCode = responseStatus();
            return !(statusCode < 100 || (statusCode >= 400 && statusCode <= 599));
        } else {
            return super.responseIsValid();
        }
    }
}

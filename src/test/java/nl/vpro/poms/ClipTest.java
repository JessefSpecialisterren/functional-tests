package nl.vpro.poms;

import com.jayway.restassured.RestAssured;
import nl.vpro.poms.helpers.ImageXmlBuilder;
import nl.vpro.poms.helpers.ProgramXmlBuilder;
import nl.vpro.poms.helpers.SegmentXmlBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@FixMethodOrder
public class ClipTest {

    private static final String BASE_URL = "https://api-dev.poms.omroep.nl";
    private static final String MEDIA_URL = BASE_URL + "/media/media";
    private static final String USERNAME = "vpro-mediatools";
    private static final String PASSWORD = "***REMOVED***";
    private static String suffix;
    private static String randomId;

    @BeforeClass
    public static void setUpShared() {
        suffix =  Long.toString(System.currentTimeMillis());
        randomId = UUID.randomUUID().toString();
    }

    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
    }

    @Test
    public void testPostClip() throws UnsupportedEncodingException, InterruptedException {
        String segmentCrid = "crid://pyapi/segment/1";
        String clipCrid = "crid://pyapi/clip/" + randomId;
        List<String> segments = Collections.singletonList(createSegment(segmentCrid, suffix, null, false));
        String clip = createClip(clipCrid, suffix, segments);

        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                body(clip).
        when().
                post(MEDIA_URL).
        then().
                log().body().
                statusCode(202).
                body(equalTo(clipCrid));
    }

    @Test
    public void testRetrieveClip() throws UnsupportedEncodingException {
        String clipCrid = "crid://pyapi/clip/" + randomId;
        String encodedClipCrid = URLEncoder.encode(clipCrid, "UTF-8");
        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                log().all().
                when().
                get(MEDIA_URL + "/" + encodedClipCrid).
                then().
                log().body().
                statusCode(200).
                body("program.title", equalTo("hoi " + suffix));
    }

    @Test
    public void testPostSegment() {
        String segmentCrid = "crid://pyapi/segment/2";
        String segment = createSegment(segmentCrid, suffix, "WO_VPRO_1425989", true);

        given().
                auth().
                basic(USERNAME, PASSWORD).
                contentType("application/xml").
                body(segment).
        when().
                post(MEDIA_URL).
        then().
                log().body().
                statusCode(202).
                body(equalTo(segmentCrid));
    }

    private String createClip(String crid, String dynamicSuffix, List<String> segments) {

        String clipImage = new ImageXmlBuilder()
                .type("PICTURE")
                .title("Plaatje van clip " + dynamicSuffix)
                .imageLocationUrl("https://placeholdit.imgix.net/~text?txtsize=15&amp;txt=image2&amp;w=120&amp;h=120")
                .build();
        return new ProgramXmlBuilder()
                .avType("MIXED")
                .type("CLIP")
                .crid(crid)
                .broadcaster("VPRO")
                .titleType("MAIN")
                .title("hoi " + dynamicSuffix)
                .images(Collections.singletonList(clipImage))
                .segments(segments)
                .build();
    }

    private String createSegment(String crid, String dynamicSuffix, String midRef, boolean addNS) {
        String segmentImage = new ImageXmlBuilder()
                .type("PICTURE")
                .title("Plaatje van segment " + dynamicSuffix)
                .imageLocationUrl("https://placeholdit.imgix.net/~text?txtsize=17&amp;txt=image2&amp;w=120&amp;h=120")
                .build();
        return new SegmentXmlBuilder()
                .avType("MIXED")
                .midRef(midRef)
                .crid(crid)
                .broadcaster("VPRO")
                .titleType("MAIN")
                .title("hoi (1) " + dynamicSuffix)
                .images(Collections.singletonList(segmentImage))
                .start("PT10S")
                .addNS(addNS)
                .build();
    }
}

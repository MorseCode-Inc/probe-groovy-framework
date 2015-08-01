package inc.morsecode.pagerduty;

import java.io.IOException;

import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;

public class PagerDutyAuthenticationHelper extends AuthenticatorHelper {

	
	public PagerDutyAuthenticationHelper() {
		// super(ChallengeScheme.CUSTOM, true, false);
		super(new ChallengeScheme("PagerDutyToken", "PagerDutyToken"), true, false);
	}
	
	
	@Override
	public void formatRequest(ChallengeWriter cw, ChallengeRequest challenge, Response response, Series<Header> httpHeaders) throws IOException {
		super.formatRequest(cw, challenge, response, httpHeaders);
		httpHeaders.add(new Header("Authorization", "Token token=123"));
	}
}

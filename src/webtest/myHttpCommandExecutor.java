package webtest;

import static org.openqa.selenium.remote.DriverCommand.GET_ALL_SESSIONS;
import static org.openqa.selenium.remote.DriverCommand.NEW_SESSION;
import static org.openqa.selenium.remote.DriverCommand.QUIT;

import java.io.IOException;
import java.net.URL;

import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandCodec;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.HttpSessionId;
import org.openqa.selenium.remote.ProtocolHandshake;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.ResponseCodec;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;
import org.openqa.selenium.remote.internal.ApacheHttpClient;

public class myHttpCommandExecutor extends HttpCommandExecutor 
{
	private CommandCodec<HttpRequest> mycommandCodec;
	private ResponseCodec<HttpResponse> myresponseCodec;
	private final HttpClient myclient;
	
	public myHttpCommandExecutor(URL addressOfRemoteServer) {
		super(addressOfRemoteServer);
		initCodec();
		this.myclient = new ApacheHttpClient.Factory().createClient(addressOfRemoteServer);
	}
	
	private void initCodec()
	{
		mycommandCodec = new W3CHttpCommandCodec();
		myresponseCodec = new W3CHttpResponseCodec();
	}
	
	public Response execute(Command command) throws IOException {
		if (command.getSessionId() == null) {
			if (QUIT.equals(command.getName())) {
				return new Response();
			}
			if (!GET_ALL_SESSIONS.equals(command.getName()) && !NEW_SESSION.equals(command.getName())) {
				throw new NoSuchSessionException("Session ID is null. Using WebDriver after calling quit()?");
			}
		}

		if (NEW_SESSION.equals(command.getName())) {
			if (mycommandCodec != null) {
				throw new SessionNotCreatedException("Session already exists");
			}
			ProtocolHandshake handshake = new ProtocolHandshake();

			ProtocolHandshake.Result result = handshake.createSession(myclient, command);
			Dialect dialect = result.getDialect();
			mycommandCodec = dialect.getCommandCodec();

			myresponseCodec = dialect.getResponseCodec();
			return result.createResponse();
		}

		if (mycommandCodec == null || myresponseCodec == null) {
			throw new WebDriverException("No command or response codec has been defined. Unable to proceed");
		}

		HttpRequest httpRequest = mycommandCodec.encode(command);
		try {

			HttpResponse httpResponse = myclient.execute(httpRequest, true);

			Response response = myresponseCodec.decode(httpResponse);
			if (response.getSessionId() == null) {
				if (httpResponse.getTargetHost() != null) {
					response.setSessionId(HttpSessionId.getSessionId(httpResponse.getTargetHost()));
				} else {
					// Spam in the session id from the request
					response.setSessionId(command.getSessionId().toString());
				}
			}
			if (QUIT.equals(command.getName())) {
				myclient.close();
			}
			return response;
		} catch (UnsupportedCommandException e) {
			if (e.getMessage() == null || "".equals(e.getMessage())) {
				throw new UnsupportedOperationException(
						"No information from server. Command name was: " + command.getName(), e.getCause());
			}
			throw e;
		}
	}

}

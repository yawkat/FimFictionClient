package at.yawk.util.httpserver;

import java.io.IOException;

public interface IHttpHandler {

	void handle(IHttpExchange arg0) throws IOException;
	
}

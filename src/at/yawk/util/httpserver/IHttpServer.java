package at.yawk.util.httpserver;

/** This is used as an interface to make using different HTTP servers easier. */
public interface IHttpServer {

	void createContext(String string, IHttpHandler httpHandler);

	void start();

	void stop(int i);
}

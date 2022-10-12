import java.net.Socket;

public class Req {
    final Socket client;
    final String method;


    final String path;
    final String version;
    final String host;

    public Req(Socket client, String requestBuilder) {
        String[] requestsLines = requestBuilder.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");

        this.client = client;
        this.method = requestLine[0];
        this.path = requestLine[1];
        this.version = requestLine[2];
        this.host = requestsLines[1].split(" ")[1];

        System.out.println("REQUEST >> " + logger());
    }

    public String logger() {
        return String.format("Client %s, method %s, path %s, version %s, host %s",
                client.toString(), method, path, version, host);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}

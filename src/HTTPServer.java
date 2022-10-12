import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.StringTokenizer;

public class HTTPServer {
    Socket s;
    BufferedReader in;
    PrintWriter out;
    ServerSocket ss;
    Req request;
    BufferedOutputStream dataOut;
    static final int PORT = 8081;
    static final String INDEX_FILE = "/index.html";
    static final String METHOD_NOT_SUPPORTED_FILE = "/support/method_not_supported.html";
    static final String NOT_FOUND_FILE = "/support/404.html";
    static final String SERVER_ERROR_FILE = "/support/500.html";

    public HTTPServer() {
        setup();
    }

    public void run() {
        while (true) {
            try {
                waitClient();
                receiveRequest();
                sendReply();
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError();
            }
        }
    }

    private void setup() {
        try {
            ss = new ServerSocket(PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitClient() {
        try {
            s = ss.accept();
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());
            dataOut = new BufferedOutputStream(s.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveRequest() {
        String line;
        StringBuilder reqBuilder = new StringBuilder();
        try {
            while ((line = in.readLine()).length() > 0) {
                reqBuilder.append(line).append("\r\n");
            }
            request = null;
            if (!reqBuilder.isEmpty()) {
                request = new Req(s, reqBuilder.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendReply() throws IOException {
        String statusCode = "200 OK";
        String contentType;
        int contentLength;
        byte[] content;
        if (request != null) {
            if (!request.getMethod().equals("GET")) {
                statusCode = "501 Not Implemented";
            }

            Path fPath = getFPath(request.getPath());
            File f = new File(fPath.toUri());

            if (!Files.exists(fPath))
                statusCode = "404 Not Found";

            StringTokenizer parse = new StringTokenizer(statusCode);
            String code = parse.nextToken();

            System.out.println(code);

            if (!code.equals("200"))
                if (code.equals("404")) {
                    fPath = getFPath(NOT_FOUND_FILE);
                    f = new File(fPath.toUri());
                } else {
                    fPath = getFPath(METHOD_NOT_SUPPORTED_FILE);
                    f = new File(fPath.toUri());
                }

            contentType = Files.probeContentType(fPath);
            contentLength = (int) f.length();
            content = Files.readAllBytes(fPath);

            sendHeaders(statusCode, contentType, contentLength);
            sendData(content, contentLength);
        }
    }

    private Path getFPath(String path) {
        if ("/".equals(path)) {
            path = INDEX_FILE;
        }

        return Paths.get("www/", path);
    }

    private void sendHeaders(String statusCode, String contentType, int contentLength) {
        out.println("HTTP/1.1 " + statusCode);
        out.println("Server: Sist.Dist. Server 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentType + ";charset=UTF-8");
        out.println("Connection: close");
        out.println("Content-Length: " + contentLength);
        out.println();
        out.flush();
    }

    private void sendData(byte[] content, int contentLength) throws IOException {
        dataOut.write(content, 0, contentLength);
        dataOut.flush();
    }

    private void sendInternalServerError() {
        try {
            String statusCode = "500 Internal Server Error";
            String contentType;
            int contentLength;
            byte[] content;

            Path fPath = getFPath(SERVER_ERROR_FILE);
            File f = new File(fPath.toUri());

            contentType = Files.probeContentType(fPath);
            contentLength = (int) f.length();
            content = Files.readAllBytes(fPath);

            sendHeaders(statusCode, contentType, contentLength);
            sendData(content, contentLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
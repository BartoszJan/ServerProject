import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerJ implements Runnable {

    static {
        try {
            System.loadLibrary("ServerJ");

        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    public static native String getUppercase(String line);

    final static int PORT = 8888;
    final static String HOSTNAME = "localhost";

    public static void main(String[] args) throws Exception {

        (new Thread(new ServerJ())).start();
    }

    @Override
    public void run() {

        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(HOSTNAME, PORT));
            serverSocketChannel.configureBlocking(false);
            System.out.println("ServerJ enabled");

            int ops = serverSocketChannel.validOps();
            SelectionKey selectionKey = serverSocketChannel.register(selector, ops, null);

            while (true) {
                selector.select();
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keySet.iterator();

                while (iterator.hasNext()) {
                    SelectionKey myKey = iterator.next();

                    if (myKey.isAcceptable()) {

                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);

                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("Connection Accepted: " + client.getLocalAddress());
                    } else if (myKey.isReadable()) {

                        SocketChannel client = (SocketChannel) myKey.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        client.read(buffer);
                        String result = new String(buffer.array());
                        System.out.println("Got: " + result);

                        if (result.contains("END\r\n")) {

                            byte[] message = "Bye!\r\n".getBytes();
                            ByteBuffer outBuffer = ByteBuffer.wrap(message);

                            while (outBuffer.hasRemaining()) {
                                client.write(outBuffer);
                            }
                            Thread.sleep(1000);

                            buffer.clear();
                            client.close();
                        } else if (result.contains("\r\n") || result.contains("\n")) {

                            String resultUppercase = getUppercase(result);
                            byte[] message = resultUppercase.getBytes();

                            ByteBuffer outBuffer = ByteBuffer.wrap(message);
                            while (outBuffer.hasRemaining()) {
                                client.write(outBuffer);
                            }
                            outBuffer.clear();
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

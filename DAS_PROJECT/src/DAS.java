import java.io.IOException;
import java.net.*;
import java.util.*;

public class DAS {
    public static void main(String[] args) {

        if (args.length != 2) {

            System.err.println("ERROR");
            return;
        }

        int port = 0;
        int number = 0;

        try {
            port = Integer.parseInt(args[0]);
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error, arguments ARE NOT numeric");
            return;
        }


        try (DatagramSocket datagramSocket = new DatagramSocket(port)) {

            master(datagramSocket, number, port);

        } catch (IOException e) {

            try {

                slave(number, port);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    public static Boolean brodcast_number = false;

    private static void master(DatagramSocket datagramSocket, int number, int port) throws IOException {

        List<Integer> numbers = new ArrayList<>();
        numbers.add(number);

        while (true) {

            byte[] numbers_recived = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(numbers_recived, numbers_recived.length);

            datagramSocket.receive(datagramPacket);

            String s = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            int numereczek = Integer.parseInt(s);

            if (numereczek == -1) {
                System.out.println(-1);
                broadcastSender(-1, port, datagramSocket);

                break;

            } else if (numereczek == 0) {

                int avg = (int) numbers.stream().mapToInt(Integer::intValue).average().orElse(0);
                System.out.println(avg);
                broadcastSender(avg, port, datagramSocket);


            } else {
                if (!brodcast_number) {
                    numbers.add(numereczek);
                    System.out.println(numbers);
                } else {
                    brodcast_number = false;
                    numbers.add(numereczek);
                    System.out.println(numbers);
                }

            }
        }
    }

    private static void slave(int number, int port_og) throws IOException {

        int random_port = new Random().nextInt(500);

        while (true) {

            try (DatagramSocket datagramSocket = new DatagramSocket(random_port)) {

                datagramSocket.close();
                break;

            } catch (SocketException e) {
                random_port = new Random().nextInt(500);
            }

        }

        DatagramSocket datagramSocket = new DatagramSocket(random_port);

        byte[] numerunio = String.valueOf(number).getBytes();

        DatagramPacket datagramPacket = new DatagramPacket(numerunio, numerunio.length,
                InetAddress.getByName("localhost"), port_og);

        datagramSocket.send(datagramPacket);


    }

    public static InetAddress getBroadcastAddress() throws IOException {

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();


            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcastAddress = interfaceAddress.getBroadcast();

                if (broadcastAddress != null) {
                    return broadcastAddress;
                }
            }
        }

        return InetAddress.getByName("255.255.255.255");
    }


    public static void broadcastSender(int value, int ogPort, DatagramSocket socket) throws IOException {

        byte[] valueInByte = String.valueOf(value).getBytes();

        socket.setBroadcast(true);
        brodcast_number = true;

        DatagramPacket datagramPacket = new DatagramPacket(valueInByte, valueInByte.length, getBroadcastAddress(), ogPort);
        socket.send(datagramPacket);

    }


}

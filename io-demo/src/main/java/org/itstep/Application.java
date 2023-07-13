package org.itstep;

import java.io.*;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Application {
    public static void main(String[] args) {
        // demo01();
        // simpleSaveObjects();
        // simpleReadObjects();

        // saveUsingDataOutputStream();
        // readUsingDataInputStream();

        //saveUsingObjectOutputStream();
        try (InputStream in = new FileInputStream("cars.ext");
             ObjectInputStream objIn = new ObjectInputStream(in)) {
            List<Car> cars = (List<Car>) objIn.readObject();
            System.out.println(cars);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveUsingObjectOutputStream() {
        List<Car> cars = getCars();
        try (OutputStream out = new FileOutputStream("cars.ext");
             ObjectOutputStream objectOut = new ObjectOutputStream(out)) {
            objectOut.writeObject(cars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readUsingDataInputStream() {
        var cars = new ArrayList<Car>();
        try (InputStream in = new FileInputStream("cars.bin");
             DataInputStream dataIn = new DataInputStream(in)) {
            while (dataIn.available() > 0) {
                cars.add(new Car(dataIn.readUTF(), dataIn.readUTF(), BigDecimal.valueOf(dataIn.readDouble())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(cars);
    }

    private static void saveUsingDataOutputStream() {
        List<Car> cars = getCars();
        try (OutputStream out = new FileOutputStream("cars.bin");
             DataOutputStream dataOut = new DataOutputStream(out)) {
            for (Car car : cars) {
                dataOut.writeUTF(car.model());
                dataOut.writeUTF(car.color());
                dataOut.writeDouble(car.price().doubleValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void simpleReadObjects() {
        var cars = new ArrayList<Car>();
        try (InputStream in = new FileInputStream("cars.dat");
             Scanner scanner = new Scanner(in)) {
            while (true) {
                try {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    cars.add(new Car(parts[0].trim(), parts[1].trim(), new BigDecimal(parts[2].trim())));
                } catch (NoSuchElementException ex) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(cars);
    }

    private static void simpleSaveObjects() {
        List<Car> cars = getCars();
        try (OutputStream out = new FileOutputStream("cars.dat")) {
            for (Car car : cars) {
                String line = car.model() + ", " + car.color() + ", " + car.price() + "\n";
                out.write(line.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Car> getCars() {
        return List.of(
                new Car("Mercedes", "White", BigDecimal.valueOf(50_000)),
                new Car("BMW", "Black", BigDecimal.valueOf(25_000)),
                new Car("Opel", "Red", BigDecimal.valueOf(10_000)),
                new Car("Tesla", "Yellow", BigDecimal.valueOf(56_000))
        );
    }

    private static void demo01() {
        try (InputStream in = new FileInputStream("readme.txt");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
             MyDecorator decorator = new MyDecorator(bufferedInputStream)) {
//            int b;
//            byte[] bytes = new byte[256];
//            int count = 0;
//            while (true) {
//                b = in.read();
//                if (b == '\n' || b < 0) {
//                    break;
//                }
//                if (count >= bytes.length) {
//                    bytes = Arrays.copyOf(bytes, bytes.length * 2);
//                }
//                bytes[count] = (byte) b;
//                count++;
//            }
//            System.out.println(new String(bytes, 0, count));
            System.out.println(decorator.readLine());
            System.out.println(decorator.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            URL url = new URL("https://itstep.dp.ua");
            try (InputStream in = url.openStream();
                 MyDecorator decorator = new MyDecorator(in)) {
                String line;
                while ((line = decorator.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

record Car(String model, String color, BigDecimal price) implements Serializable {

}

class MyDecorator implements Closeable {
    private final InputStream in;

    MyDecorator(InputStream in) {
        this.in = in;
    }

    public int linesCount() throws IOException {
        int b;
        int count = 0;
        if (in.markSupported()) {
            in.mark(in.available());
        }
        while (true) {
            b = in.read();
            if (b == '\n') {
                count++;
            }
            if (b < 0) {
                break;
            }
        }
        if (in.markSupported()) {
            in.reset();
        }
        return count;
    }

    public List<String> readAllLines() throws IOException {
        var list = new ArrayList<String>();
        String line;
        while ((line = readLine()) != null) {
            list.add(line);
        }
        return list;
    }

    public String readLine() throws IOException {
        int b;
        byte[] bytes = new byte[256];
        int count = 0;
        if (in.available() <= 0) {
            return null;
        }
        while (true) {
            b = in.read();
            if (b == '\n' || b < 0) {
                break;
            }
            if (count >= bytes.length) {
                bytes = Arrays.copyOf(bytes, bytes.length * 2);
            }
            bytes[count] = (byte) b;
            count++;
        }
        return new String(bytes, 0, count);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}

class MyFileInputStream extends FileInputStream {

    public MyFileInputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public String readLine() throws IOException {
        int b;
        byte[] bytes = new byte[256];
        int count = 0;
        while (true) {
            b = this.read();
            if (b == '\n' || b < 0) {
                break;
            }
            if (count >= bytes.length) {
                bytes = Arrays.copyOf(bytes, bytes.length * 2);
            }
            bytes[count] = (byte) b;
            count++;
        }
        return new String(bytes, 0, count);
    }
}

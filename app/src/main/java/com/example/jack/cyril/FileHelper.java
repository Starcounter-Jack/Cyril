package com.example.jack.cyril;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by jack on 09/08/2016.
 */
public class FileHelper {


    public static void writeLines(List<String> lines, File file) throws IOException {
        BufferedWriter writer = null;
        OutputStreamWriter osw = null;
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos);
            writer = new BufferedWriter(osw);
            String lineSeparator = System.getProperty("line.separator");
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                writer.write(line);
                if (i < lines.size() - 1) {
                    writer.write(lineSeparator);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            close(writer);
            close(osw);
            close(fos);
        }
    }

    public static byte[] readBytes(File file) {
        FileInputStream fis = null;
        byte[] b = null;
        try {
            fis = new FileInputStream(file);
            b = readBytesFromStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
        }
        return b;
    }

    public static void writeBytes(byte[] inBytes, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            writeBytesToStream(inBytes, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
    }

    public static void close(InputStream inStream) {
        try {
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inStream = null;
    }

    public static void close(OutputStream outStream) {
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outStream = null;
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = null;
        }
    }

    public static long copy(InputStream readStream, OutputStream writeStream) throws IOException {
        int bytesread = -1;
        byte[] b = new byte[4096]; //4096 is default cluster size in Windows for < 2TB NTFS partitions
        long count = 0;
        bytesread = readStream.read(b);
        while (bytesread != -1) {
            writeStream.write(b, 0, bytesread);
            count += bytesread;
            bytesread = readStream.read(b);
        }
        return count;
    }
    public static byte[] readBytesFromStream(InputStream readStream) throws IOException {
        ByteArrayOutputStream writeStream = null;
        byte[] byteArr = null;
        writeStream = new ByteArrayOutputStream();
        try {
            copy(readStream, writeStream);
            writeStream.flush();
            byteArr = writeStream.toByteArray();
        } finally {
            close(writeStream);
        }
        return byteArr;
    }
    public static void writeBytesToStream(byte[] inBytes, OutputStream writeStream) throws IOException {
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream(inBytes);
        try {
            copy(bis, writeStream);
        } finally {
            close(bis);
        }
    }
}

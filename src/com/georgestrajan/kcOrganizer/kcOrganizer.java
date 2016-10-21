package com.georgestrajan.kcOrganizer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class kcOrganizer {

    private static final String HIGHLIGHT_START = "- Highlight";
    private static final String DELIM = "==========";

    private static final String BOOKS_FOLDER = "Books";

    public static void main(String[] args) {

        System.out.println("1Kindle Clippings Organizer v1.0");
        if (args.length != 1) {
            System.err.println("Usage: kcOrganizer CLIPPINGSFILE");
            return;
        }

        String cFile = args[0];
        System.out.format("Processing clippings file: %1$s", cFile);

        File f = new File(cFile);
        if (!f.exists() || f.isDirectory()) {
            System.err.println("Clippings file does not exist or is a directory!");
            return;
        }

        Map<String, StringBuilder> map = new HashMap<String, StringBuilder>();
        Boolean firstLine = true;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(cFile),
                        "UTF-8"
                )
        )) {
            String line, prevLine = null;
            while ( (line = br.readLine()) != null ) {

                // for some reason the Kindle starts the file with a character we cannot read
                if (firstLine) {
                    line = line.substring(1);
                    firstLine = false;
                }

                if (prevLine != null &&
                        line != null &&
                        line.startsWith(HIGHLIGHT_START)) {

                    StringBuilder notes = new StringBuilder();

                    // Read the next lines until we get to the delimiter
                    while ((line = br.readLine()) != null && !line.equals(DELIM)) {
                        notes.append(line);
                        notes.append(System.getProperty("line.separator"));
                    }

                    // The book name is in the prevLine and the notes are in the array
                    String bookName = prevLine.trim();
                    if (!map.containsKey(bookName)) {
                        map.put(bookName, new StringBuilder());
                    }
                    map.get(bookName).append(notes);
                }

                prevLine = line;
            }
        }
        catch (FileNotFoundException fnf) {
            System.err.format("Clippings file %1$s was not found!", cFile);
        }
        catch (IOException ioexc) {
            System.err.format("IO Exception %1$s", ioexc.toString());
        }

        File booksDir = new File(BOOKS_FOLDER);
        booksDir.mkdir();

        // create a file for each book and write the notes
        map.keySet().forEach( (book) -> {
            try {
                File file = new File(BOOKS_FOLDER, getFileName(book));
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println(book);
                    pw.println(map.get(book).toString());
                }
            }
            catch (IOException ioexc) {
                System.err.format("IO Exception %1$s", ioexc.toString());
            }
        });

        /*
        // write the contents of the map to a file, organized by book
        try {
            File file = new File("Organized Clippings.txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                map.keySet().forEach( (book) -> {
                    pw.println(book);
                    pw.println(map.get(book).toString());
                } );
            }
        }
        catch (IOException ioexc) {
            System.err.format("IO Exception %1$s", ioexc.toString());
        }
        */
    }

    // convert a book name to a legal file name
    private static String getFileName(String bookName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < bookName.length() ; i++) {
            if (i > 60) {
                break;
            }
            char ch = bookName.charAt(i);
            if ((ch >= 'A' && ch <= 'z') || (ch == ' ') || (ch >= '0' && ch <= '9')) {
                sb.append(ch);
            } else {
                sb.append("_");
            }
        }
        return sb.toString();
    }

}

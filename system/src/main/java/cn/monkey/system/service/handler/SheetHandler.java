package cn.monkey.system.service.handler;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SheetHandler extends DefaultHandler {
    private final SharedStringsTable sst;
    private String lastContents;
    private boolean nextIsString;

    private SheetHandler(SharedStringsTable sst) {
        this.sst = sst;
    }

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        // c => cell
        if (name.equals("c")) {
            // Print the cell reference
            System.out.print(attributes.getValue("r") + " - ");
            // Figure out if the value is an index in the SST
            String cellType = attributes.getValue("t");
            nextIsString = cellType != null && cellType.equals("s");
        }
        // Clear contents cache
        lastContents = "";
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        // Process the last contents as required.
        // Do now, as characters() may be called more than once
        if (nextIsString) {
            int idx = Integer.parseInt(lastContents);
            lastContents = sst.getItemAt(idx).getString();
            nextIsString = false;
        }
        // v => contents of a cell
        // Output after we've seen the string contents
        if (name.equals("v")) {
            System.out.println(lastContents);
        }
    }

    public void characters(char[] ch, int start, int length) {
        lastContents += new String(ch, start, length);
    }
}

package org.homenet.raneri;

import java.io.File;
import java.io.IOException;

public class RenameAction {

    private File from;
    private File to;

    public RenameAction(File from, File to) {
        this.from = from;
        this.to = to;
    }

    public File getFrom() {
        return from;
    }

    public File getTo() {
        return to;
    }

    public void execute() throws IOException {
        from.renameTo(to);
    }

}

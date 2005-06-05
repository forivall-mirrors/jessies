package e.ptextarea;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public abstract class PAbstractTextStyler implements PTextStyler {
    protected PTextArea textArea;
    
    public PAbstractTextStyler(PTextArea textArea) {
        this.textArea = textArea;
    }
    
    public abstract List<PTextSegment> getTextSegments(int line);
}

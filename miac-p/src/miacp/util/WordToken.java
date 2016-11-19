package miacp.util;

import java.io.Serializable;

public class WordToken implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 8338067062131921969L;
	int startPos;
    int endPos;
    int index;
    public String pos;
    public String value;


    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public WordToken(int startPos, int endPos, int index, String pos,
                     String value) {
        super();
        this.startPos = startPos;
        this.endPos = endPos;
        this.index = index;
        this.pos = pos;
        this.value = value;
    }

    @Override
    public String toString() {
//		return String.format("%d \t %s/%s \t %s-%s",
//				index, value, pos, startPos, endPos );
        return String.format("%d \t %s \t %s",
                index, value, pos);
    }

}

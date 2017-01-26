package org.unfoldingword.resourcecontainer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A utility for reading maps without pulling your hair out.
 * See https://gist.github.com/neutrinog/bb274330b911801a6e210df670bf3ecf
 */
public class MapReader {
    private final Object map;

    /**
     * The map or value
     * @param obj the object to be read.
     */
    public MapReader(Object obj) {
        this.map = obj;
    }

    /**
     * Resolves a new instance of the reader with the value.
     *
     * @param key the key to look up
     * @return an instance of the reader with the value
     */
    public MapReader get(Object key) {
        if(this.map instanceof Map && ((Map)this.map).containsKey(key)) {
            return new MapReader(((Map)this.map).get(key));
        } else if (key instanceof Integer
                && this.map instanceof List
                && (Integer) key >= 0
                && ((List)this.map).size() > (Integer)key) {
            return new MapReader(((List)this.map).get((Integer)key));
        } else {
            return new MapReader(null);
        }
    }

    /**
     * Returns the value of the reader
     * @return the value
     */
    public Object value() {
        if(this.map != null) return this.map;
        return null;
    }

    /**
     * Returns the size of the object being read if it is a map or collection.
     * If the reader contains anything else this will return 0.
     *
     * @return the size of the map
     */
    public int size() {
        if(this.map instanceof Map) {
            return ((Map) this.map).size();
        } else if(this.map instanceof Collection) {
            return ((Collection)this.map).size();
        } else {
            return 0;
        }
    }

    /**
     * Convenience method for checking of the value/map is null
     * @return true if the value or map is null
     */
    public boolean isNull() {
        return this.map == null;
    }
}

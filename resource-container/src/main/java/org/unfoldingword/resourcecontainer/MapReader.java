package org.unfoldingword.resourcecontainer;

import java.util.Collection;
import java.util.Map;

/**
 * A utility for reading maps without pulling your hair out.
 */
public class MapReader {

    private final Object map;

    /**
     * The map or value
     * @param map the map that can be read or any other object as a value
     */
    public MapReader(Object map) {
        this.map = map;
    }

    /**
     * Retrieves a value from the map
     * @param key the key to look up
     * @return an instance of the reader with the value
     */
    public MapReader get(Object key) {
        if(this.map != null && this.map instanceof Map && ((Map)this.map).containsKey(key)) {
            return new MapReader(((Map)this.map).get("key"));
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
}

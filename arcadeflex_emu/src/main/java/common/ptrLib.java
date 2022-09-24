/**
 * Pointer class emulation
 *
 * @author George Moralis
 */
package common;

public class ptrLib {

    /**
     * Unsigned char * emulation class
     */
    public static class UBytePtr {

        public int bsize = 1;
        public char[] memory;
        public int offset;

        public UBytePtr() {
        }
    }
}

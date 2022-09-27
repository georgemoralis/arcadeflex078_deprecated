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

        public char read() {
            return (char) (memory[offset] & 0xFF);
        }

        public char read(int index) {
            return (char) (memory[offset + index] & 0xFF);
        }

        public void write(int value) {
            memory[offset] = (char) (value & 0xFF);
        }

        public void write(int index, int value) {
            memory[offset + index] = (char) (value & 0xFF);
        }
    }
}

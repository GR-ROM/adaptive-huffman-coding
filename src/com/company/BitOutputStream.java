package com.company;

import java.io.IOException;
import java.io.OutputStream;

class BitOutputStream {
    private final OutputStream out;
    private final boolean[] buffer = new boolean[8];
    private int count = 0;

    public BitOutputStream(OutputStream out) {
        this.out = out;
    }

    public void write(boolean x) throws IOException {
        this.buffer[7 - this.count] = x;
        this.count++;
        if (this.count == 8) {
            int num = 0;
            for (int index = 0; index != 8; index++) {
                num <<= 1;
                num |= (this.buffer[index] ? 1 : 0);
            }
            this.out.write(num);
            this.count = 0;
        }
    }

    public void close() throws IOException {
        int num = 0;
        for (int index = 0; index != 8; index++) num = num << 1 | (this.buffer[index] ? 1 : 0);
        this.out.write(num - 128);

        this.out.close();
    }

}
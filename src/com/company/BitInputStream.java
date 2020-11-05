package com.company;

import java.io.IOException;
import java.io.InputStream;

class BitInputStream {

    private final InputStream in;
    private int num = 0;
    private int count = 8;

    public BitInputStream(InputStream in) {
        this.in = in;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean read() throws IOException {
        if (this.count == 8) {
            this.num = this.in.read();
            this.count = 0;
        }
        boolean x = (num & 1) == 1;
        num = num >> 1;
        this.count++;
        return x;
    }

    public void close() throws IOException {
        this.in.close();
    }

}


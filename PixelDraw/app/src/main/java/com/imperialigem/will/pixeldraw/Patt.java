package com.imperialigem.will.pixeldraw;


import android.graphics.Color;
import android.graphics.Bitmap;


public class Patt {

    public int color;
    public int[][] data;
    //public Bitmap bitmap;
    //private int width;



    Patt(){
        //this.width = w;
        this.data = new int[10][10];
        this.color = Color.BLACK;
        //this.bitmap = Bitmap.createBitmap(width,width, Bitmap.Config.ARGB_8888);
    }

    Patt(int[][] d) {
        //this.width = w;
        this.data = d;
        this.color = Color.BLACK;
        //this.bitmap = Bitmap.createBitmap(width,width, Bitmap.Config.ARGB_8888);
    }

    Patt(int[][] d, int c) {
        //this.width = w;
        this.data = d;
        this.color = c;
        //this.bitmap = Bitmap.createBitmap(width,width, Bitmap.Config.ARGB_8888);
    }

    public int[][] getData() {
        return data;
    }

    public int getColor() {
        return color;
    }

    public void setData(int[][] d) {
        this.data = d;
    }

    public void setData(int[] idx, int d) {
        this.data[idx[0]][idx[1]] = d;
    }

    public void setColor(int c) {this.color = c; }
}

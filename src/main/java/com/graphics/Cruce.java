package com.graphics;

public class Cruce {
    public static final int ROJO = 0;
    public static final int AMARILLO = 1;
    public static final int VERDE = 2;
    final float x;
    final float z;
    VehiculoTrafico ocupante;

    public Cruce(float x, float z) {
        this.x = x;
        this.z = z;
    }

    public int color(boolean horizontal, float tiempo) {
        float fase = tiempo % 20;
        if (!horizontal) {
            fase = (fase + 10) % 20;
        }
        if (fase < 7) {
            return VERDE;
        }
        if (fase < 9) {
            return AMARILLO;
        }
        return ROJO;
    }

    boolean contiene(float px, float pz) {
        return Math.abs(px - x) < 7 && Math.abs(pz - z) < 7;
    }

    void reiniciar() {
        ocupante = null;
    }
}

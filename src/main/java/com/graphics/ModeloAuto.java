package com.graphics;

public class ModeloAuto {
    static final float RADIO_RUEDA = 0.31f;
    static final float FARO_X = 0.58f;
    static final float FARO_Y = 0.55f;
    static final float FARO_Z = 1.39f;
    private final clase2 escena;

    public ModeloAuto(clase2 escena) {
        this.escena = escena;
    }

    void dibujar() {
        escena.entero("uEmision", 0);
        piezaMalla(escena.carroceria, 0, 0.53f, 0, 1.72f, 0.43f, 2.72f, 0.82f, 0.12f, 0.10f, 0, 0);
        pieza(0, 0.37f, 0, 1.65f, 0.13f, 2.68f, 0.11f, 0.12f, 0.14f);
        pieza(0, 0.74f, -0.83f, 1.43f, 0.08f, 0.93f, 0.94f, 0.20f, 0.13f);
        pieza(0, 0.72f, 1.02f, 1.48f, 0.08f, 0.58f, 0.76f, 0.09f, 0.08f);
        piezaMalla(escena.cabina, 0, 0.90f, 0.12f, 1.31f, 0.35f, 1.42f, 0.10f, 0.23f, 0.29f, 0, 0);
        pieza(0, 1.098f, 0.12f, 1.06f, 0.045f, 0.65f, 0.86f, 0.14f, 0.10f);
        pieza(0, 0.789f, -0.63f, 1.42f, 0.015f, 0.055f, 0.08f, 0.10f, 0.12f);
        pieza(0, 0.789f, 0.86f, 1.42f, 0.015f, 0.055f, 0.08f, 0.10f, 0.12f);
        for (float lado : new float[] {-1, 1}) {
            pieza(lado * 0.66f, 0.84f, 0.13f, 0.035f, 0.18f, 0.05f, 0.83f, 0.13f, 0.10f);
            pieza(lado * 0.86f, 0.77f, -0.37f, 0.20f, 0.11f, 0.18f, 0.11f, 0.13f, 0.16f);
            pieza(lado * 0.856f, 0.64f, 0.25f, 0.02f, 0.035f, 0.20f, 0.63f, 0.66f, 0.68f);
            pieza(lado * 0.74f, 0.57f, 0.73f, 0.02f, 0.14f, 0.32f, 0.07f, 0.08f, 0.09f);
            pieza(lado * 0.55f, 0.94f, 1.09f, 0.045f, 0.25f, 0.08f, 0.09f, 0.10f, 0.12f);
            rueda(lado * 0.80f, -0.86f, true);
            rueda(lado * 0.80f, 0.87f, false);
        }
        pieza(0, 1.06f, 1.11f, 1.49f, 0.06f, 0.29f, 0.12f, 0.13f, 0.16f);
        pieza(0, 0.36f, -1.38f, 1.54f, 0.06f, 0.08f, 0.05f, 0.065f, 0.08f);
        pieza(0, 0.48f, -1.366f, 0.72f, 0.17f, 0.03f, 0.035f, 0.04f, 0.045f);
        for (int i = -2; i <= 2; i++) {
            pieza(i * 0.12f, 0.49f, -1.386f, 0.026f, 0.13f, 0.01f, 0.30f, 0.32f, 0.34f);
        }
        pieza(0, 0.50f, 1.374f, 0.34f, 0.12f, 0.025f, 0.82f, 0.84f, 0.80f);
        luces();
        escena.entero("uEmision", 0);
    }

    private void rueda(float x, float z, boolean delantera) {
        float giro = delantera ? escena.giroRuedas : 0;
        ruedaMalla(x, 0.33f, z, 0.22f, RADIO_RUEDA * 2, 0.045f, 0.052f, 0.06f, giro);
        float fuera = x + Math.signum(x) * 0.118f;
        ruedaMalla(fuera, 0.33f, z, 0.024f, 0.43f, 0.55f, 0.59f, 0.62f, giro);
        for (int i = 0; i < 5; i++) {
            float a = escena.rotacionRuedas + i * (float) Math.PI * 2 / 5;
            float localY = 0.33f + (float) Math.cos(a) * 0.11f;
            float localZ = z + (float) Math.sin(a) * 0.11f;

            piezaMalla(escena.carroceria, fuera + Math.signum(x) * 0.015f, localY, localZ,
                       0.025f, 0.23f, 0.045f, 0.11f, 0.13f, 0.15f, a, giro);
        }
        ruedaMalla(fuera + Math.signum(x) * 0.028f, 0.33f, z, 0.035f, 0.09f, 0.85f, 0.22f, 0.13f, giro);
    }

    private void ruedaMalla(float x, float y, float z, float ancho, float diametro,
                            float r, float g, float b, float giro) {
        piezaMalla(escena.cilindro, x, y, z, ancho, diametro, diametro, r, g, b, escena.rotacionRuedas, giro);
    }

    private void luces() {
        boolean faros = escena.farosEncendidos();
        for (float lado : new float[] {-1, 1}) {
            escena.entero("uEmision", faros ? 1 : 0);
            float brillo = faros ? 1 : 0.18f;
            pieza(lado * FARO_X, FARO_Y, -FARO_Z, 0.30f, 0.105f, 0.035f, brillo, brillo * 0.96f, brillo * 0.80f);
            boolean freno = escena.frenando;
            escena.entero("uEmision", freno || faros ? 1 : 0);
            pieza(lado * 0.54f, 0.62f, 1.375f, 0.40f, 0.085f, 0.028f,
                  freno ? 1 : 0.40f, freno ? 0.12f : 0.025f, 0.025f);
            boolean reversa = escena.velocidad < -0.05f;
            escena.entero("uEmision", reversa ? 1 : 0);
            pieza(lado * 0.28f, 0.62f, 1.376f, 0.10f, 0.07f, 0.03f,
                  reversa ? 1 : 0.19f, reversa ? 1 : 0.20f, reversa ? 0.85f : 0.22f);
        }
    }

    private void pieza(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        escena.pieza(x, y, z, sx, sy, sz, r, g, b);
    }

    private void piezaMalla(Malla malla, float x, float y, float z, float sx, float sy, float sz,
                            float r, float g, float b, float giroX, float giroLocalY) {
        float coseno = (float) Math.cos(escena.angulo);
        float seno = (float) Math.sin(escena.angulo);
        float mundoX = escena.autoX + coseno * x + seno * z;
        float mundoZ = escena.autoZ - seno * x + coseno * z;
        escena.malla(malla, mundoX, y + escena.alturaSuelo(escena.autoX, escena.autoZ), mundoZ, sx, sy, sz, r, g, b,
                     escena.angulo + giroLocalY, giroX, 0);
    }
}

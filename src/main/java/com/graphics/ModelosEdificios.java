package com.graphics;

public class ModelosEdificios {
    static final int CASAS = 0;
    static final int COMERCIO = 1;
    static final int DEPARTAMENTOS = 2;
    static final int TORRE = 3;
    private static final float[][] COLORES = {
        {0.82f, 0.67f, 0.45f},
        {0.70f, 0.39f, 0.29f},
        {0.48f, 0.65f, 0.59f},
        {0.86f, 0.79f, 0.63f},
        {0.68f, 0.69f, 0.76f}
    };

    static int tipo(int fila, int columna) {
        if (fila == 1 || (fila == 7 && columna <= 3)) {
            return CASAS;
        }
        if (fila == 9) {
            return COMERCIO;
        }
        if (fila <= 5 && columna >= 3 && columna <= 7) {
            return TORRE;
        }
        return DEPARTAMENTOS;
    }

    static float[][] obstaculos(int fila, int columna) {
        float x = clase1.centro(columna), z = clase1.centro(fila);
        int modelo = tipo(fila, columna);
        if (modelo == CASAS) {
            return new float[][] {{x - 1.8f, z + 0.35f, 2.85f, 4.85f},
                                  {x + 1.8f, z - 0.1f, 2.85f, 4.85f},
                                  {x, z + 4.15f, 6.7f, 0.22f}};
        }
        if (modelo == COMERCIO) {
            return new float[][] {{x, z, 7, 5.6f},
                                  {x - 3.25f, z - 3.3f, 0.11f, 0.11f},
                                  {x + 3.25f, z - 3.3f, 0.11f, 0.11f}};
        }
        if (modelo == DEPARTAMENTOS) return new float[][] {{x, z, 6.4f, 6.1f}};
        return new float[][] {{x, z, 7.3f, 7.0f}};
    }

    static void dibujar(clase1 escena, int fila, int columna) {
        float x = clase1.centro(columna);
        float z = clase1.centro(fila);
        float[] color = COLORES[(fila + columna * 2) % COLORES.length];
        int modelo = tipo(fila, columna);
        if (modelo == CASAS) {
            casas(escena, x, z, fila, columna, color);
        } else if (modelo == COMERCIO) {
            comercio(escena, x, z, fila, columna, color);
        } else if (modelo == DEPARTAMENTOS) {
            departamentos(escena, x, z, fila, columna, color);
        } else {
            torre(escena, x, z, fila, columna, color);
        }
        escena.entero("uEmision", 0);
    }

    private static void casas(clase1 e, float x, float z, int fila, int columna, float[] color) {
        for (int casa = 0; casa < 2; casa++) {
            float cx = x + (casa == 0 ? -1.8f : 1.8f);
            float cz = z + (casa == 0 ? 0.35f : -0.1f);
            float altura = casa == 0 ? 2.5f : 3.1f;
            float[] tono = casa == 0 ? color : COLORES[(fila + columna + 1) % COLORES.length];
            e.caja(cx, 0.3f + altura / 2, cz, 2.75f, altura, 4.7f, tono[0], tono[1], tono[2]);
            e.malla(e.tejado, cx, altura + 0.84f, cz, 3.1f, 1.08f, 5.1f,
                    0.52f, 0.23f, 0.15f, 0, 0, 0);
            if (e.vistaMapa) {
                continue;
            }
            e.caja(cx, altura + 1.23f, cz + 0.9f, 0.34f, 0.72f, 0.36f, 0.55f, 0.50f, 0.43f);
            e.caja(cx, 0.34f, cz - 2.9f, 1.1f, 0.09f, 0.95f, 0.69f, 0.63f, 0.54f);
            e.caja(cx, 1.11f, cz - 2.367f, 0.72f, 1.62f, 0.04f, 0.32f, 0.19f, 0.12f);
            e.caja(cx + 0.25f, 1.13f, cz - 2.4f, 0.06f, 0.05f, 0.045f, 0.75f, 0.65f, 0.39f);
            ventana(e, cx - 0.89f, 1.78f, cz - 2.38f, 0.69f, 0.78f, false, casa + columna);
            ventana(e, cx + 0.89f, 1.78f, cz - 2.38f, 0.69f, 0.78f, false, casa + fila);
            ventana(e, cx, 1.78f, cz + 2.38f, 1.18f, 0.78f, false, casa + fila + columna);
            ventana(e, cx - 1.40f, 1.72f, cz, 1.12f, 0.80f, true, casa + fila);
            ventana(e, cx + 1.40f, 1.72f, cz, 1.12f, 0.80f, true, casa + columna);
        }
        if (!e.vistaMapa) {
            e.caja(x, 0.36f, z + 3.55f, 6.8f, 0.12f, 1.12f, 0.27f, 0.46f, 0.26f);
            e.caja(x, 0.85f, z + 4.15f, 6.7f, 0.72f, 0.22f, 0.25f, 0.37f, 0.20f);
        }
    }

    private static void comercio(clase1 e, float x, float z, int fila, int columna, float[] color) {
        float altura = 3.3f + (columna % 3) * 0.35f;
        e.caja(x, altura / 2 + 0.3f, z, 7.0f, altura, 5.4f, color[0], color[1], color[2]);
        e.caja(x, altura + 0.46f, z, 7.4f, 0.3f, 5.8f, 0.67f, 0.58f, 0.45f);
        e.caja(x + 1.65f, altura + 1.04f, z + 1.15f, 2.6f, 1.15f, 2.0f, 0.73f, 0.65f, 0.53f);
        if (e.vistaMapa) {
            return;
        }
        for (float desplazamiento : new float[] {-1.8f, 1.8f}) {
            float puertaX = x + desplazamiento;
            e.caja(puertaX, 1.45f, z - 2.74f, 2.12f, 2.25f, 0.07f, 0.33f, 0.39f, 0.41f);
            for (float y = 0.6f; y < 2.6f; y += 0.22f) {
                e.caja(puertaX, y, z - 2.787f, 2.07f, 0.026f, 0.018f, 0.48f, 0.52f, 0.53f);
            }
        }
        e.caja(x, 2.89f, z - 3.05f, 7.2f, 0.13f, 0.83f, 0.60f, 0.36f, 0.24f);
        for (float lado : new float[] {-3.25f, 3.25f}) {
            e.caja(x + lado, 1.64f, z - 3.3f, 0.11f, 2.6f, 0.11f, 0.43f, 0.38f, 0.30f);
        }

        ventana(e, x + 1.65f, altura + 1.07f, z + 0.12f, 0.78f, 0.58f, false, columna);
        deposito(e, x - 1.55f, altura + 1.04f, z + 0.5f, 0.8f);
    }

    private static void departamentos(clase1 e, float x, float z, int fila, int columna, float[] color) {
        int pisos = 3 + (fila / 2 + columna / 2) % 2;
        float altura = pisos * 2.35f + 0.8f;
        e.caja(x, altura / 2 + 0.3f, z, 6.3f, altura, 5.9f, color[0], color[1], color[2]);
        e.caja(x, altura + 0.45f, z, 6.65f, 0.3f, 6.2f, 0.51f, 0.47f, 0.40f);
        if (e.vistaMapa) {
            return;
        }
        e.caja(x, 1.3f, z - 2.99f, 1.1f, 2.0f, 0.07f, 0.23f, 0.18f, 0.14f);
        e.caja(x, 2.4f, z - 3.2f, 1.7f, 0.12f, 0.65f, 0.73f, 0.65f, 0.52f);
        for (int piso = 0; piso < pisos; piso++) {
            float y = 1.75f + piso * 2.35f;
            for (float dx : new float[] {-1.9f, 1.9f}) {
                ventana(e, x + dx, y, z - 2.99f, 1.08f, 1.05f, false, piso + columna + (int) dx);
                ventana(e, x + dx, y, z + 2.99f, 1.08f, 1.05f, false, piso + fila);
                if (piso > 0) {
                    balcon(e, x + dx, y - 0.58f, z - 3.35f);
                }
            }
            for (float dz : new float[] {-1.6f, 1.6f}) {
                ventana(e, x - 3.19f, y, z + dz, 0.92f, 1.05f, true, piso + fila);
                ventana(e, x + 3.19f, y, z + dz, 0.92f, 1.05f, true, piso + columna);
            }
            e.caja(x, y + 0.72f, z, 6.4f, 0.085f, 6.0f, 0.78f, 0.72f, 0.61f);
        }
        e.caja(x + 1.6f, altura + 0.85f, z + 1.5f, 1.7f, 0.7f, 1.5f, 0.70f, 0.62f, 0.52f);
        deposito(e, x - 1.55f, altura + 1.06f, z + 1.0f, 0.9f);
    }

    private static void torre(clase1 e, float x, float z, int fila, int columna, float[] color) {
        float altura = 12.5f + (fila + columna * 2) % 4;
        e.caja(x, 1.7f, z, 7.3f, 2.8f, 6.9f, 0.74f, 0.68f, 0.56f);
        e.caja(x + 0.35f, 3.1f + altura / 2, z + 0.35f, 5.2f, altura, 5.1f, color[0], color[1], color[2]);
        e.caja(x + 0.35f, altura + 3.25f, z + 0.35f, 5.5f, 0.3f, 5.4f, 0.36f, 0.39f, 0.42f);
        e.caja(x + 0.35f, altura + 4.0f, z + 0.7f, 3.35f, 1.3f, 3.1f, 0.60f, 0.61f, 0.59f);
        if (e.vistaMapa) {
            return;
        }
        e.caja(x, 1.33f, z - 3.48f, 1.45f, 2.05f, 0.04f, 0.29f, 0.24f, 0.19f);
        e.caja(x, 2.45f, z - 3.68f, 2.2f, 0.14f, 0.67f, 0.61f, 0.48f, 0.35f);
        for (int piso = 0; piso < 5; piso++) {
            float y = 4.3f + piso * 2.35f;
            if (y + 0.55f >= altura + 3.1f) {
                break;
            }
            for (float dx : new float[] {-1.65f, 0, 1.65f}) {
                ventana(e, x + 0.35f + dx, y, z - 2.235f, 0.86f, 1.1f, false, piso + columna + (int) dx);
                ventana(e, x + 0.35f + dx, y, z + 2.935f, 0.86f, 1.1f, false, piso + fila);
            }
            for (float dz : new float[] {-1.5f, 0, 1.5f}) {
                ventana(e, x - 2.285f, y, z + 0.35f + dz, 0.8f, 1.1f, true, piso + fila);
                ventana(e, x + 2.985f, y, z + 0.35f + dz, 0.8f, 1.1f, true, piso + columna);
            }
        }
        for (float dx : new float[] {-2.16f, 2.16f}) {
            e.caja(x + 0.35f + dx, 3.1f + altura / 2, z - 2.23f,
                   0.15f, altura, 0.12f, 0.83f, 0.80f, 0.70f);
        }
    }

    private static void ventana(clase1 e, float x, float y, float z, float ancho, float alto,
                                boolean lateral, int indice) {
        float sx = lateral ? 0.065f : ancho + 0.16f;
        float sz = lateral ? ancho + 0.16f : 0.065f;
        e.caja(x, y, z, sx, alto + 0.16f, sz, 0.29f, 0.25f, 0.20f);
        boolean encendida = e.esNoche() && Math.floorMod(indice, 4) != 0;
        e.entero("uEmision", encendida ? 1 : 0);
        float r = encendida ? 0.96f : 0.20f;
        float g = encendida ? 0.75f : 0.35f;
        float b = encendida ? 0.39f : 0.40f;

        e.caja(x, y, z, lateral ? 0.085f : ancho, alto, lateral ? ancho : 0.085f, r, g, b);
        e.entero("uEmision", 0);
        e.caja(x, y, z, lateral ? 0.10f : 0.045f, alto, lateral ? 0.045f : 0.10f, 0.39f, 0.35f, 0.27f);
    }

    private static void balcon(clase1 e, float x, float y, float z) {
        e.caja(x, y, z, 1.62f, 0.12f, 0.75f, 0.66f, 0.59f, 0.48f);
        e.caja(x, y + 0.65f, z - 0.36f, 1.62f, 0.045f, 0.045f, 0.23f, 0.26f, 0.27f);
        for (float dx : new float[] {-0.76f, -0.38f, 0, 0.38f, 0.76f}) {
            e.caja(x + dx, y + 0.35f, z - 0.36f, 0.04f, 0.6f, 0.04f, 0.25f, 0.27f, 0.28f);
        }
    }

    private static void deposito(clase1 e, float x, float y, float z, float alto) {
        e.malla(e.cilindro, x, y, z, alto, 0.95f, 0.95f, 0.26f, 0.29f, 0.29f,
                0, 0, (float) Math.PI / 2);
    }
}

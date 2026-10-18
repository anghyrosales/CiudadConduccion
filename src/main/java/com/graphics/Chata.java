package com.graphics;

public class Chata {
    // Giro máximo entre el auto y la chata.
    static final float MAX_ARTICULACION = (float) Math.toRadians(60);
    static final float ESCALA = 0.85f;
    static final float ENGANCHE = 1.45f;
    static final float LANZA = 2.1f * ESCALA;
    static final float RADIO = 1.4f * ESCALA;
    float x, z, angulo, rotacionRuedas;

    Chata(float autoX, float autoZ, float giro) {
        reiniciar(autoX, autoZ, giro);
    }

    void reiniciar(float autoX, float autoZ, float giro) {
        angulo = giro;
        x = autoX + (float) Math.sin(giro) * (ENGANCHE + LANZA);
        z = autoZ + (float) Math.cos(giro) * (ENGANCHE + LANZA);
        rotacionRuedas = 0;
    }

    float[] proponer(float autoX, float autoZ, float giro) {
        float hx = autoX + (float) Math.sin(giro) * ENGANCHE;
        float hz = autoZ + (float) Math.cos(giro) * ENGANCHE;
        float orientacion = (float) Math.atan2(x - hx, z - hz);
        float diferencia = orientacion - giro;
        diferencia = (float) Math.atan2(Math.sin(diferencia), Math.cos(diferencia));
        orientacion = giro + Math.max(-MAX_ARTICULACION, Math.min(MAX_ARTICULACION, diferencia));
        return new float[] {hx + (float) Math.sin(orientacion) * LANZA,
                            hz + (float) Math.cos(orientacion) * LANZA, orientacion};
    }

    boolean puedeMover(float[] propuesta, float giroAuto, clase2 ciudad) {
        for (float[] circulo : obstaculos(propuesta[0], propuesta[1], propuesta[2])) {
            if (!ciudad.puedeCircularChata(circulo[0], circulo[1], circulo[2])) {
                return false;
            }
        }
        return true;
    }

    void aceptar(float[] propuesta) {
        float dx = propuesta[0] - x;
        float dz = propuesta[1] - z;
        float avance = -dx * (float) Math.sin(propuesta[2]) - dz * (float) Math.cos(propuesta[2]);
        rotacionRuedas = (rotacionRuedas + avance / (0.3f * ESCALA)) % ((float) Math.PI * 2);
        x = propuesta[0];
        z = propuesta[1];
        angulo = propuesta[2];
    }

    float[][] obstaculos() {
        return obstaculos(x, z, angulo);
    }

    private float[][] obstaculos(float px, float pz, float giro) {
        return new float[][] {{px, pz, RADIO},
            {px - (float) Math.sin(giro) * 1.75f * ESCALA, pz - (float) Math.cos(giro) * 1.75f * ESCALA, 0.4f * ESCALA}};
    }

    void dibujar(clase2 ciudad) {
        ciudad.entero("uEmision", 0);
        pieza(ciudad, 0, 0.4f / ESCALA, -1.05f, 0.13f, 0.13f, 2.1f, 0.28f, 0.30f, 0.32f);
        pieza(ciudad, 0, 0.48f, 0.1f, 1.6f, 0.16f, 2, 0.22f, 0.25f, 0.28f);
        for (int i = 0; i < 7; i++) {
            pieza(ciudad, 0, 0.585f, -0.75f + i * 0.28f, 1.48f, 0.05f, 0.25f, 0.56f, 0.37f, 0.19f);
        }
        for (float lado : new float[] {-1, 1}) {
            pieza(ciudad, lado * 0.78f, 0.72f, 0.1f, 0.06f, 0.1f, 2, 0.45f, 0.49f, 0.52f);
            for (float extremo : new float[] {-0.85f, 1.05f}) {
                pieza(ciudad, lado * 0.78f, 0.64f, extremo, 0.06f, 0.25f, 0.06f, 0.32f, 0.35f, 0.38f);
            }
            float ruedaX = lado * 0.88f;
            rueda(ciudad, ruedaX, 0.22f, 0.6f, 0.05f, 0.06f, 0.07f);
            rueda(ciudad, ruedaX + lado * 0.12f, 0.025f, 0.36f, 0.65f, 0.68f, 0.70f);

            float a = rotacionRuedas;
            pieza(ciudad, ruedaX + lado * 0.14f, 0.3f + (float) Math.cos(a) * 0.1f,
                  (float) Math.sin(a) * 0.1f, 0.025f, 0.055f, 0.055f, 0.12f, 0.14f, 0.16f);
            ciudad.entero("uEmision", ciudad.frenando || ciudad.farosEncendidos() ? 1 : 0);
            pieza(ciudad, lado * 0.59f, 0.48f, 1.12f, 0.24f, 0.12f, 0.04f,
                  ciudad.frenando ? 1 : 0.55f, 0.04f, 0.03f);
            ciudad.entero("uEmision", 0);
        }
    }

    private void pieza(clase2 ciudad, float px, float py, float pz, float sx, float sy, float sz,
                       float r, float g, float b) {
        ciudad.piezaVehiculo(x, z, angulo, px * ESCALA, py * ESCALA, pz * ESCALA,
                             sx * ESCALA, sy * ESCALA, sz * ESCALA, r, g, b);
    }

    private void rueda(clase2 ciudad, float localX, float ancho, float diametro, float r, float g, float b) {
        ciudad.malla(ciudad.cilindro, x + (float) Math.cos(angulo) * localX * ESCALA, 0.3f * ESCALA + ciudad.alturaSuelo(x, z),
                     z - (float) Math.sin(angulo) * localX * ESCALA, ancho * ESCALA, diametro * ESCALA, diametro * ESCALA,
                     r, g, b, angulo, rotacionRuedas, 0);
    }
}

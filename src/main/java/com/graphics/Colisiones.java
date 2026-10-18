package com.graphics;

final class Colisiones {
    static final float MITAD_ANCHO = 1.02f;
    static final float MITAD_LARGO = 1.44f;
    static final float MARGEN = 0.04f;

    private Colisiones() { }

    // Comprobar los ejes de ambos autos.
    static boolean autos(float x, float z, float giro, float otroX, float otroZ, float otroGiro) {
        float dx = otroX - x, dz = otroZ - z;
        for (float orientacion : new float[] {giro, otroGiro}) {
            float cos = (float) Math.cos(orientacion), sin = (float) Math.sin(orientacion);
            for (float[] eje : new float[][] {{cos, -sin}, {sin, cos}}) {
                float separacion = Math.abs(dx * eje[0] + dz * eje[1]);
                if (separacion >= proyeccion(eje, giro) + proyeccion(eje, otroGiro) + MARGEN) {
                    return false;
                }
            }
        }
        return true;
    }

    private static float proyeccion(float[] eje, float giro) {
        float cos = (float) Math.cos(giro), sin = (float) Math.sin(giro);
        return MITAD_ANCHO * Math.abs(eje[0] * cos - eje[1] * sin)
             + MITAD_LARGO * Math.abs(eje[0] * sin + eje[1] * cos);
    }

    static boolean circuloAuto(float x, float z, float radio, float autoX, float autoZ, float giro) {
        float dx = x - autoX, dz = z - autoZ;
        float cos = (float) Math.cos(giro), sin = (float) Math.sin(giro);
        float localX = dx * cos - dz * sin;
        float localZ = dx * sin + dz * cos;
        float fueraX = Math.max(0, Math.abs(localX) - MITAD_ANCHO);
        float fueraZ = Math.max(0, Math.abs(localZ) - MITAD_LARGO);
        return fueraX * fueraX + fueraZ * fueraZ < (radio + MARGEN) * (radio + MARGEN);
    }
}

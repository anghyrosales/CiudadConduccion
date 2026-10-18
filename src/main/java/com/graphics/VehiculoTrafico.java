package com.graphics;

import java.util.ArrayList;
import java.util.List;

public class VehiculoTrafico {
    float x;
    float z;
    float angulo;
    float velocidad;
    final float rojo;
    final float verde;
    final float azul;
    private final float[][] ruta;
    private final float[] distancias;
    private final float longitud;
    private final float inicio;
    private float recorrido;
    private Cruce cruceActual;
    private boolean entroCruce;
    private Cruce esperarAmarillo;
    private float[][] obstaculosChata = new float[0][];
    int vueltas;
    static final float FRENO = 5;
    private float giroJugador;

    public VehiculoTrafico(float avenida, float inicio, float r, float g, float b) {
        ruta = crearRuta(avenida);
        distancias = new float[ruta.length + 1];
        for (int i = 0; i < ruta.length; i++) {
            float[] siguiente = ruta[(i + 1) % ruta.length];
            distancias[i + 1] = distancias[i] + distancia(ruta[i][0], ruta[i][1], siguiente[0], siguiente[1]);
        }
        longitud = distancias[ruta.length];
        this.inicio = inicio;
        rojo = r;
        verde = g;
        azul = b;
        reiniciar();
    }

    private static float[][] crearRuta(float avenida) {
        List<float[]> puntos = new ArrayList<>();
        float centroCurva = avenida - 4;
        for (int esquina = 0; esquina < 4; esquina++) {
            float cx = centroCurva;
            float cz = -centroCurva;
            for (int paso = 0; paso <= 12; paso++) {
                double a = -Math.PI / 2 + paso * Math.PI / 24;
                float px = cx + 2 * (float) Math.cos(a);
                float pz = cz + 2 * (float) Math.sin(a);
                for (int giro = 0; giro < esquina; giro++) {
                    float anteriorX = px;
                    px = -pz;
                    pz = anteriorX;
                }
                puntos.add(new float[] {px, pz});
            }
        }
        return puntos.toArray(new float[0][]);
    }

    void reiniciar() {
        recorrido = inicio % longitud;
        velocidad = 0;
        vueltas = 0;
        cruceActual = null;
        entroCruce = false;
        esperarAmarillo = null;
        colocar();
    }

    private float[] punto(float avance) {
        float posicion = (recorrido + avance) % longitud;
        for (int i = 0; i < ruta.length; i++) {
            if (posicion < distancias[i + 1]) {
                float factor = (posicion - distancias[i]) / (distancias[i + 1] - distancias[i]);
                float[] siguiente = ruta[(i + 1) % ruta.length];
                return new float[] {ruta[i][0] + (siguiente[0] - ruta[i][0]) * factor,
                                    ruta[i][1] + (siguiente[1] - ruta[i][1]) * factor};
            }
        }
        return ruta[0].clone();
    }

    private void colocar() {
        float[] posicion = punto(0);
        float[] delante = punto(0.15f);
        x = posicion[0];
        z = posicion[1];
        angulo = (float) Math.atan2(-(delante[0] - x), -(delante[1] - z));
    }

    private Cruce proximoCruce(Cruce[] cruces) {
        for (float avance = 0; avance <= 18; avance += 0.5f) {
            float[] posicion = punto(avance);
            for (Cruce cruce : cruces) {
                if (cruce.contiene(posicion[0], posicion[1])) {
                    return cruce;
                }
            }
        }
        return null;
    }

    private float distanciaEntrada(Cruce cruce) {
        for (float avance = 0; avance <= 25; avance += 0.1f) {
            float[] posicion = punto(avance);
            if (cruce.contiene(posicion[0], posicion[1])) {
                return Math.max(0, avance - 0.3f);
            }
        }
        return 25;
    }

    private boolean salidaLibre(Cruce cruce, VehiculoTrafico[] autos, float jugadorX, float jugadorZ) {
        if (cruce.ocupante != null && cruce.ocupante != this) {
            return false;
        }
        if (cruce.contiene(jugadorX, jugadorZ)) {
            return false;
        }
        for (float[] circulo : obstaculosChata) {
            if (cruce.contiene(circulo[0], circulo[1])) return false;
        }
        for (VehiculoTrafico otro : autos) {
            if (otro != this && cruce.contiene(otro.x, otro.z)) {
                return false;
            }
        }
        boolean dentro = false;
        for (float avance = 0; avance <= 35; avance += 0.5f) {
            float[] posicion = punto(avance);
            if (cruce.contiene(posicion[0], posicion[1])) {
                dentro = true;
            } else if (dentro) {
                for (float salida = avance; salida <= avance + 5; salida += 0.5f) {
                    if (ocupado(punto(salida), autos, jugadorX, jugadorZ)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean ocupado(float[] posicion, VehiculoTrafico[] autos, float jugadorX, float jugadorZ) {
        if (Colisiones.autos(posicion[0], posicion[1], angulo, jugadorX, jugadorZ, giroJugador)) {
            return true;
        }
        for (float[] circulo : obstaculosChata) {
            if (Colisiones.circuloAuto(circulo[0], circulo[1], circulo[2], posicion[0], posicion[1], angulo)) return true;
        }
        for (VehiculoTrafico otro : autos) {
            if (otro != this && Colisiones.autos(posicion[0], posicion[1], angulo, otro.x, otro.z, otro.angulo)) {
                return true;
            }
        }
        return false;
    }

    void actualizar(float deltaTime, float tiempo, Cruce[] cruces, VehiculoTrafico[] autos,
                    float jugadorX, float jugadorZ, clase2 ciudad) {
        actualizar(deltaTime, tiempo, cruces, autos, jugadorX, jugadorZ, ciudad, new float[0][]);
    }

    void actualizar(float deltaTime, float tiempo, Cruce[] cruces, VehiculoTrafico[] autos,
                    float jugadorX, float jugadorZ, clase2 ciudad, float[][] obstaculos) {
        obstaculosChata = obstaculos;
        giroJugador = ciudad.angulo;
        if (deltaTime <= 0) {
            return;
        }
        if (cruceActual != null) {
            if (cruceActual.contiene(x, z)) {
                entroCruce = true;
            } else if (entroCruce) {
                cruceActual.ocupante = null;
                cruceActual = null;
                entroCruce = false;
            }
        }
        // Distancia disponible antes de frenar.
        float libre = 30;
        Cruce proximo = proximoCruce(cruces);
        if (cruceActual == null && proximo != null) {
            float hastaEntrada = distanciaEntrada(proximo);
            boolean horizontal = Math.abs(Math.sin(angulo)) > 0.7f;
            int color = proximo.color(horizontal, tiempo);
            float frenado = velocidad * velocidad / (2 * FRENO);
            boolean puedePasar = color == Cruce.VERDE;
            if (color == Cruce.AMARILLO) {
                if (esperarAmarillo != proximo && hastaEntrada < frenado + 0.5f && velocidad > 0.5f) {
                    puedePasar = true;
                } else {
                    esperarAmarillo = proximo;
                }
            } else {
                esperarAmarillo = null;
            }
            puedePasar = puedePasar && salidaLibre(proximo, autos, jugadorX, jugadorZ);
            if (puedePasar && hastaEntrada < 1) {
                proximo.ocupante = this;
                cruceActual = proximo;
            } else if (!puedePasar) {
                libre = hastaEntrada;
            }
        }
        for (float avance = 0; avance < libre; avance += 0.25f) {
            if (ocupado(punto(avance), autos, jugadorX, jugadorZ)) {
                libre = Math.max(0, avance - 0.25f);
                break;
            }
        }
        float objetivo = Math.min(6, (float) Math.sqrt(2 * FRENO * Math.max(0, libre - 0.25f)));
        if (velocidad < objetivo) {
            velocidad = Math.min(objetivo, velocidad + 3 * deltaTime);
        } else {
            velocidad = Math.max(objetivo, velocidad - FRENO * deltaTime);
        }
        float avance = Math.min(velocidad * deltaTime, libre);
        float[] propuesta = punto(avance);
        if (!ciudad.puedeCircularCiudad(propuesta[0], propuesta[1]) || ocupado(propuesta, autos, jugadorX, jugadorZ)) {
            velocidad = 0;
            return;
        }
        if (avance < velocidad * deltaTime) {
            velocidad = 0;
        }
        recorrido += avance;
        if (recorrido >= longitud) {
            recorrido %= longitud;
            vueltas++;
        }
        colocar();
    }

    static float distancia(float x1, float z1, float x2, float z2) {
        return (float) Math.hypot(x2 - x1, z2 - z1);
    }
}

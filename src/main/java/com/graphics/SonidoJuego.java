package com.graphics;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

final class SonidoJuego {
    private volatile boolean abierto;
    private volatile boolean silencio;
    private volatile float motor;
    private volatile boolean freno;
    private volatile float[] efecto;
    private SourceDataLine salida;
    private Thread hilo;
    private double fase;

    void iniciar() {
        abierto = true;
        hilo = new Thread(() -> {
            try {
                AudioFormat formato = new AudioFormat(22050, 16, 1, true, false);
                salida = AudioSystem.getSourceDataLine(formato);
                salida.open(formato, 4096);
                salida.start();
                byte[] bloque = new byte[1024];
                float[] actual = null;
                int cursor = 0;
                while (abierto) {
                    if (efecto != actual) { actual = efecto; cursor = 0; }
                    for (int i = 0; i < bloque.length; i += 2) {
                        fase += (45 + motor * 8) / 22050.0;
                        fase -= Math.floor(fase);
                        double muestra = motor < 0 ? 0 : (Math.sin(fase * Math.PI * 2)
                            + 0.35 * Math.sin(fase * Math.PI * 4)) * (0.035 + motor * 0.002);
                        if (freno) muestra += (Math.random() * 2 - 1) * 0.035;
                        if (actual != null && cursor < actual.length) muestra += actual[cursor++];
                        short valor = (short) (silencio ? 0 : Math.max(-1, Math.min(1, muestra)) * 24000);
                        bloque[i] = (byte) valor;
                        bloque[i + 1] = (byte) (valor >> 8);
                    }
                    salida.write(bloque, 0, bloque.length);
                }
            } catch (Exception sinDispositivo) {
                // El juego también funciona sin salida de audio.
            } finally {
                if (salida != null) salida.close();
            }
        }, "audio-ciudad");
        hilo.setDaemon(true);
        hilo.start();
    }

    void actualizar(float velocidad, boolean frenando, boolean detenido, boolean silenciado) {
        motor = detenido ? -1 : Math.abs(velocidad);
        freno = !detenido && frenando && Math.abs(velocidad) > 1;
        silencio = silenciado;
    }

    void reproducir(boolean choque, boolean victoria) {
        efecto = crearEfecto(choque, victoria);
    }

    static float[] crearEfecto(boolean choque, boolean victoria) {
        float duracion = choque ? 0.18f : victoria ? 0.9f : 0.45f;
        float[] datos = new float[Math.round(22050 * duracion)];
        for (int i = 0; i < datos.length; i++) {
            double t = i / 22050.0;
            double frecuencia = choque ? 65 : 523.25 * Math.pow(2, ((int) (t / 0.15) % 3) * 4 / 12.0);
            double tono = Math.sin(2 * Math.PI * frecuencia * t);
            if (choque) tono = (tono + Math.random() * 2 - 1) * 0.5;
            datos[i] = (float) (tono * 0.26 * Math.min(1, t * 100) * (1 - t / duracion));
        }
        return datos;
    }

    void limpiar() {
        abierto = false;
        if (salida != null) { salida.stop(); salida.flush(); salida.close(); }
        if (hilo != null) {
            try { hilo.join(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

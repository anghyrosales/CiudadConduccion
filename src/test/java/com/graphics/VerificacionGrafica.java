package com.graphics;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class VerificacionGrafica extends clase4 {
    private int cuadro;

    @Override
    protected void actualizar(float deltaTime) {
        cuadro++;
        if (cuadro == 1) {
            tecla(GLFW_KEY_ENTER);
            glfwSwapInterval(0);
        }
        if (cuadro == 3) {
            velocidad = 8;
            int[] r = PanelInfo.rectFreno(ancho, alto);
            int[] w = new int[1];
            int[] h = new int[1];
            glfwGetWindowSize(ventana, w, h);
            glfwSetCursorPos(ventana, (r[0] + r[2] / 2.0) * w[0] / ancho,
                             (alto - r[1] - r[3] / 2.0) * h[0] / alto);
            botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_PRESS);
            if (!frenoPorBoton) {
                throw new IllegalStateException("El botón visible no coincide con el área del clic");
            }
        }
        if (cuadro == 12) {
            botonRaton(GLFW_MOUSE_BUTTON_LEFT, GLFW_RELEASE);
        }
        if (cuadro == 15) { tecla(GLFW_KEY_N); tecla(GLFW_KEY_C); }
        if (cuadro == 25) { glfwSetWindowSize(ventana, 600, 900); }
        if (cuadro == 35) { tecla(GLFW_KEY_M); tecla(GLFW_KEY_F); }
        if (cuadro == 45) { tecla(GLFW_KEY_H); glfwSetWindowSize(ventana, 900, 600); }
        if (cuadro == 55) { tecla(GLFW_KEY_P); }
        if (cuadro == 65) { tecla(GLFW_KEY_P); tecla(GLFW_KEY_M); tecla(GLFW_KEY_N); }
        if (cuadro == 75) {
            tecla(GLFW_KEY_R);
            for (float[] destino : DESTINOS) {
                autoX = destino[0];
                autoZ = destino[1];
                velocidad = 0;
                actualizarEntregas(0.05f);
            }
            if (entregas != DESTINOS.length) {
                throw new IllegalStateException("No se completaron las entregas");
            }
        }
        if (cuadro == 85) {
            tecla(GLFW_KEY_R);
            if (entregas != 0 || velocidad != 0 || tiempo != 0) {
                throw new IllegalStateException("El reinicio no restauró la partida");
            }
            tecla(GLFW_KEY_C);
            tecla(GLFW_KEY_F);
            tecla(GLFW_KEY_H);
            glfwSetWindowSize(ventana, 1100, 760);
        }
        super.actualizar(0.05f);
        if (cuadro == 95) {
            glfwSetWindowShouldClose(ventana, true);
        }
    }

    protected void actualizarPartida(float deltaTime) { super.actualizar(deltaTime); }

    @Override
    protected void dibujarFrame() {
        super.dibujarFrame();
        if (cuadro == 10 || cuadro == 20 || cuadro == 30 || cuadro == 40 || cuadro == 50
            || cuadro == 60 || cuadro == 70 || cuadro == 80 || cuadro == 90) {
            capturar("cuadro-" + cuadro + ".png");
        }
    }

    protected void capturar(String nombre) {
        ByteBuffer pixeles = BufferUtils.createByteBuffer(ancho * alto * 4);
        glReadPixels(0, 0, ancho, alto, GL_RGBA, GL_UNSIGNED_BYTE, pixeles);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int indice = (y * ancho + x) * 4;
                int rojo = pixeles.get(indice) & 255;
                int verde = pixeles.get(indice + 1) & 255;
                int azul = pixeles.get(indice + 2) & 255;
                imagen.setRGB(x, alto - y - 1, rojo << 16 | verde << 8 | azul);
            }
        }
        try {
            Path carpeta = Path.of("target", "verificacion");
            Files.createDirectories(carpeta);
            ImageIO.write(imagen, "png", carpeta.resolve(nombre).toFile());
        } catch (java.io.IOException error) {
            throw new IllegalStateException("No se pudo guardar la captura", error);
        }
    }

    public static void main(String[] args) {
        System.setProperty("demo.frames", "6");
        new clase1().run();
        new clase2().run();
        new clase3().run();
        new clase4().run();
        System.setProperty("demo.frames", "0");
        System.out.println("Las cuatro etapas dibujaron seis cuadros sin errores OpenGL.");
        new VerificacionGrafica().run();
        System.out.println("Verificación gráfica terminada: controles, redimensionamiento, entregas y reinicio.");
    }
}

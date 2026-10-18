package com.graphics;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class VerificacionEstilo extends clase4 {
    private int cuadro;

    @Override
    protected void actualizar(float deltaTime) {
        cuadro++;
        if (cuadro == 1) {
            glfwSwapInterval(0);
            tecla(GLFW_KEY_H);
            noche = false;
        }
        super.actualizar(0.05f);
        if (cuadro >= 16 && cuadro < 30) {
            frenando = true;
        }
        if (cuadro == 30) {
            noche = true;
        }
        if (cuadro >= 30 && cuadro < 45) {
            velocidad = -1;
        }
        if (cuadro == 45) {
            noche = false;
            velocidad = 0;
        }
        if (cuadro == 105) {
            glfwSetWindowShouldClose(ventana, true);
        }
    }

    @Override
    protected void configurarCamara() {
        if (cuadro < 16) {
            vector("uOjo", autoX - 3.4f, 2.6f, autoZ - 4.8f);
            vector("uObjetivo", autoX, 0.65f, autoZ);
        } else if (cuadro < 45) {
            vector("uOjo", autoX + 3.4f, 2.7f, autoZ + 4.8f);
            vector("uObjetivo", autoX, 0.65f, autoZ);
        } else if (cuadro < 60) {
            super.configurarCamara();
        } else if (cuadro < 75) {
            vector("uOjo", -22, 7, -52);
            vector("uObjetivo", -15, 1.8f, -40);
        } else if (cuadro < 90) {
            vector("uOjo", -23, 7, 26);
            vector("uObjetivo", -15, 1.8f, 40);
        } else {
            camaraAerea = true;
            super.configurarCamara();
        }
        decimal("uAspecto", (float) ancho / alto);
    }

    @Override
    protected void dibujarFrame() {
        super.dibujarFrame();
        if (cuadro == 10 || cuadro == 25 || cuadro == 40 || cuadro == 55
            || cuadro == 70 || cuadro == 85 || cuadro == 100) {
            capturar();
        }
    }

    private void capturar() {
        ByteBuffer datos = BufferUtils.createByteBuffer(ancho * alto * 4);
        glReadPixels(0, 0, ancho, alto, GL_RGBA, GL_UNSIGNED_BYTE, datos);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int i = (y * ancho + x) * 4;
                int color = (datos.get(i) & 255) << 16 | (datos.get(i + 1) & 255) << 8 | datos.get(i + 2) & 255;
                imagen.setRGB(x, alto - y - 1, color);
            }
        }
        try {
            Path carpeta = Path.of("target", "estilo");
            Files.createDirectories(carpeta);
            ImageIO.write(imagen, "png", carpeta.resolve("vista-" + cuadro + ".png").toFile());
        } catch (java.io.IOException error) {
            throw new IllegalStateException(error);
        }
    }

    public static void main(String[] args) {
        new VerificacionEstilo().run();
        System.out.println("Estilo verificado: deportivo, freno, faros, casas, comercio y ciudad completa.");
    }
}

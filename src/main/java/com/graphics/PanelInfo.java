package com.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL33.*;

// Panel de texto en pantalla.
public class PanelInfo {
    private int programa;
    private int vao;
    private int vbo;
    private int textura;
    private String anterior = "";
    private final int ANCHO;
    private final int estilo;
    private int altura;
    private int rumboGrados = Integer.MIN_VALUE;

    public PanelInfo() {
        this(0);
    }

    public PanelInfo(int estilo) {
        this.estilo = estilo;
        ANCHO = estilo == 2 || estilo == 4 ? 280 : 620;
    }

    void iniciar() {
        int vertex = compilar(GL_VERTEX_SHADER, """
            #version 330 core
            layout(location=0) in vec2 posicion;
            layout(location=1) in vec2 uv;
            out vec2 coordenada;
            void main() {
                coordenada = uv;
                gl_Position = vec4(posicion, 0.0, 1.0);
            }
            """);
        int fragment = 0;
        try {
            fragment = compilar(GL_FRAGMENT_SHADER, """
                #version 330 core
                in vec2 coordenada;
                uniform sampler2D imagen;
                out vec4 color;
                void main() {
                    color = texture(imagen, coordenada);
                }
                """);
            programa = glCreateProgram();
            glAttachShader(programa, vertex);
            glAttachShader(programa, fragment);
            glLinkProgram(programa);
            if (glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) {
                throw new IllegalStateException(glGetProgramInfoLog(programa));
            }
        } finally {
            glDeleteShader(vertex);
            if (fragment != 0) {
                glDeleteShader(fragment);
            }
        }
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        glEnableVertexAttribArray(1);
        textura = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textura);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void actualizar(String texto) {
        if (texto.equals(anterior)) {
            return;
        }
        String[] filas = texto.split("\n");
        altura = estilo == 1 ? Math.max(180, 105 + (filas.length - 1) * 30) : estilo == 4 ? 210 : (estilo == 2 || estilo == 3) ? 110 : 24 + filas.length * 30;
        BufferedImage imagen = new BufferedImage(ANCHO, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dibujo = imagen.createGraphics();
        try {
            dibujo.setColor(estilo == 2 && texto.startsWith("FRENANDO") ? new Color(132, 39, 28) : new Color(13, 24, 38));
            dibujo.fillRect(0, 0, ANCHO, altura);
            dibujo.setColor(new Color(62, 214, 215));
            dibujo.fillRect(0, 0, 5, altura);
            dibujo.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            for (int i = 0; i < filas.length; i++) {
                int tamano = estilo == 4 ? 24 : estilo == 1 ? (i == 0 ? 56 : 22) : estilo == 2 ? (i == 0 ? 30 : 16) : estilo == 3 ? (i == 0 ? 42 : 24) : 21;
                dibujo.setFont(new Font(Font.MONOSPACED, Font.BOLD, tamano));
                dibujo.setColor(i == 0 ? new Color(102, 237, 231) : Color.WHITE);
                int x = estilo == 0 ? 18 : (ANCHO - dibujo.getFontMetrics().stringWidth(filas[i])) / 2;
                int y = estilo == 4 ? 170 + i * 30 : estilo == 1 ? (i == 0 ? 78 : 117 + (i - 1) * 30)
                                   : (estilo == 2 || estilo == 3) ? (i == 0 ? 43 : 73 + (i - 1) * 22) : 30 + i * 30;
                dibujo.drawString(filas[i], x, y);
            }
            if (estilo == 4) {
                dibujo.setColor(new Color(255, 204, 65));
                dibujo.rotate(-Math.toRadians(rumboGrados), 140, 77);
                dibujo.fillPolygon(new int[] {140, 108, 127, 127, 153, 153, 172},
                                   new int[] {20, 62, 62, 124, 124, 62, 62}, 7);
            }
        } finally {
            dibujo.dispose();
        }
        ByteBuffer pixeles = BufferUtils.createByteBuffer(ANCHO * altura * 4);
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < ANCHO; x++) {
                int color = imagen.getRGB(x, y);
                pixeles.put((byte) (color >> 16));
                pixeles.put((byte) (color >> 8));
                pixeles.put((byte) color);
                pixeles.put((byte) 255);
            }
        }
        pixeles.flip();
        glBindTexture(GL_TEXTURE_2D, textura);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, ANCHO, altura, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixeles);
        anterior = texto;
    }

    void dibujar(int ancho, int alto, String texto, int programaEscena, int vaoEscena) {
        dibujarConMargen(ancho, alto, texto, programaEscena, vaoEscena, 0);
    }

    void dibujarConMargen(int ancho, int alto, String texto, int programaEscena, int vaoEscena, int margenDerecho) {
        if (ancho < 32 || alto < 32) {
            return;
        }
        glActiveTexture(GL_TEXTURE0);
        actualizar(texto);
        float escala = Math.max(0.05f, Math.min(0.8f, Math.min((ancho - margenDerecho - 16f) / ANCHO, (alto - 16f) / altura)));
        dibujarRectangulo(ancho, alto, 8, 8, ANCHO * escala, altura * escala, programaEscena, vaoEscena);
    }

    void dibujarCentrado(int ancho, int alto, String texto, int programaEscena, int vaoEscena) {
        glActiveTexture(GL_TEXTURE0);
        actualizar(texto);
        float escala = Math.min(1, Math.min((ancho - 32f) / ANCHO, (alto - 32f) / altura));
        if (escala <= 0) {
            return;
        }
        dibujarRectangulo(ancho, alto, (ancho - ANCHO * escala) / 2, (alto - altura * escala) / 2,
                          ANCHO * escala, altura * escala, programaEscena, vaoEscena);
    }

    void dibujarBoton(int ancho, int alto, String texto, int programaEscena, int vaoEscena) {
        int[] r = rectFreno(ancho, alto);
        if (r[2] == 0 || r[3] == 0) {
            return;
        }
        glActiveTexture(GL_TEXTURE0);
        actualizar(texto);
        dibujarRectangulo(ancho, alto, r[0], r[1], r[2], r[3], programaEscena, vaoEscena);
    }

    void dibujarEn(int ancho, int alto, int[] r, String texto, int programaEscena, int vaoEscena) {
        if (r[2] <= 0 || r[3] <= 0) return;
        glActiveTexture(GL_TEXTURE0);
        actualizar(texto);
        dibujarRectangulo(ancho, alto, r[0], r[1], r[2], r[3], programaEscena, vaoEscena);
    }

    void dibujarRumbo(int ancho, int alto, float rumbo, String indicacion, int programaEscena, int vaoEscena) {
        int grados = Math.round((float) Math.toDegrees(rumbo));
        if (grados != rumboGrados) { rumboGrados = grados; anterior = ""; }
        int w = Math.min(150, ancho / 4), h = w * 3 / 4;
        dibujarEn(ancho, alto, new int[] {(ancho - w) / 2, alto - h - 100, w, h},
                  "DESTINO\n" + indicacion, programaEscena, vaoEscena);
    }

    static int[] rectMenu(int ancho, int alto, int indice) {
        float escala = Math.max(0.1f, Math.min(1, Math.min(ancho / 700f, alto / 600f)));
        int w = Math.round(330 * escala), h = Math.round(58 * escala);
        return new int[] {(ancho - w) / 2, alto / 2 - Math.round((35 + indice * 74) * escala), w, h};
    }

    static int[] rectFreno(int ancho, int alto) {
        int w = Math.max(0, Math.min(180, (ancho - 32) / 3));
        int h = Math.max(0, Math.min(74, alto - 32));
        return new int[] {ancho - w - 16, 16, w, h};
    }

    private void dibujarRectangulo(int ancho, int alto, float x, float y, float w, float h,
                                   int programaEscena, int vaoEscena) {
        float izquierda = -1 + 2 * x / ancho;
        float derecha = -1 + 2 * (x + w) / ancho;
        float arriba = -1 + 2 * (y + h) / alto;
        float abajo = -1 + 2 * y / alto;
        float[] vertices = {izquierda, arriba, 0, 0, izquierda, abajo, 0, 1, derecha, abajo, 1, 1,
                            izquierda, arriba, 0, 0, derecha, abajo, 1, 1, derecha, arriba, 1, 0};
        glDisable(GL_DEPTH_TEST);
        try {
            glUseProgram(programa);
            glUniform1i(glGetUniformLocation(programa, "imagen"), 0);
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
            glBindTexture(GL_TEXTURE_2D, textura);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        } finally {
            glEnable(GL_DEPTH_TEST);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glUseProgram(programaEscena);
            glBindVertexArray(vaoEscena);
        }
    }

    private int compilar(int tipo, String fuente) {
        int shader = glCreateShader(tipo);
        glShaderSource(shader, fuente);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String error = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException(error);
        }
        return shader;
    }

    void limpiar() {
        if (textura != 0) {
            glDeleteTextures(textura);
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (programa != 0) {
            glDeleteProgram(programa);
        }
    }
}
